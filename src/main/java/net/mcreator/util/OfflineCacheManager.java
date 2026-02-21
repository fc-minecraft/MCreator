package net.mcreator.util;

import io.sentry.Sentry;
import net.mcreator.io.FileIO;
import net.mcreator.io.UserFolderManager;
import net.mcreator.io.zip.ZipIO;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.plugin.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.OperationType;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OfflineCacheManager {

    private static final Logger LOG = LogManager.getLogger(OfflineCacheManager.class);

    private static final String MARKER_FILE_NAME = "offline_fabric_1.21.8_ready.marker";

    private static final String COMMON_AW_CONTENT = "accessWidener v2 named\n" +
            "\n" +
            "accessible field net/minecraft/world/item/BucketItem content Lnet/minecraft/world/level/material/Fluid;\n"
            +
            "accessible field net/minecraft/world/level/block/LiquidBlock fluid Lnet/minecraft/world/level/material/FlowingFluid;\n"
            +
            "\n" +
            "# Start of user code block custom AWs\n" +
            "# End of user code block custom AWs";
    private static final String VERSIONS_FILE_NAME = "offline_versions.properties";

    // Fallback Versions (used if detection fails)
    private static final String FALLBACK_MC_VERSION = "1.21.8";
    private static final String FALLBACK_LOADER_VERSION = "0.17.2";
    private static final String FALLBACK_BUILD_FILE_VERSION = "0.133.4";

    public static File getOfflineCacheDir() {
        return new File(System.getProperty("user.home"), ".mcreator/gradle");
    }

    public static boolean isOfflineModeReady() {
        File cache = getOfflineCacheDir();
        File marker = new File(cache, MARKER_FILE_NAME);
        return marker.exists();
    }

    public static String verifyCacheIntegrity() {
        if (!isOfflineModeReady())
            return "Кэш не готов (маркер отсутствует)";

        File cache = getOfflineCacheDir();
        File versions = new File(cache, VERSIONS_FILE_NAME);
        if (!versions.exists())
            return "Ошибка: файл версий отсутствует";

        try {
            Properties p = new Properties();
            p.load(Files.newBufferedReader(versions.toPath()));
            String loaderVersion = p.getProperty("loader_version", "0.0.0");

            // Strict check: mismatched version is invalid, even if newer (to ensure
            // consistency)
            if (!loaderVersion.equals(FALLBACK_LOADER_VERSION)) {
                return "Версия кэша не совпадает (" + loaderVersion + " != " + FALLBACK_LOADER_VERSION + ")";
            }
        } catch (Exception e) {
            return "Ошибка чтения версий кэша: " + e.getMessage();
        }

        File cachedProjectFiles = new File(cache, "cached_project_files");
        if (!cachedProjectFiles.exists())
            return "Ошибка: кэшированные файлы проекта отсутствуют";
        if (!new File(cachedProjectFiles, ".classpath").exists())
            return "Ошибка: .classpath не найден";

        File cachedBuildDir = new File(cache, "cached_build");
        if (!cachedBuildDir.exists())
            return "Ошибка: кэшированная папка build отсутствует (нужен перезапуск настройки)";
        if (!cachedBuildDir.isDirectory() || cachedBuildDir.list().length == 0)
            return "Ошибка: кэшированная папка build пуста";

        // Check for Fabric Loom plugin in global cache
        File loomCache = new File(UserFolderManager.getGradleHome(),
                "caches/modules-2/files-2.1/net.fabricmc/fabric-loom");
        if (!loomCache.exists() || !loomCache.isDirectory() || loomCache.list().length == 0)
            return "Ошибка: Плагин Fabric Loom не найден в кэше Gradle. Офлайн режим может не работать.";

        return "Кэш цел (Версия " + FALLBACK_LOADER_VERSION + ")";
    }

    public static long getCacheSize() {
        File cache = getOfflineCacheDir();
        if (cache.exists() && cache.isDirectory()) {
            return org.apache.commons.io.FileUtils.sizeOfDirectory(cache);
        }
        return 0;
    }

    public static void deleteOfflineCache() {
        File cache = getOfflineCacheDir();
        if (cache.exists()) {
            FileIO.deleteDir(cache);
        }
    }

    private static final java.util.concurrent.atomic.AtomicBoolean isDownloading = new java.util.concurrent.atomic.AtomicBoolean(
            false);

    public static void downloadOfflineFiles(Consumer<String> statusCallback, Runnable onComplete, Runnable onError) {
        if (isDownloading.getAndSet(true)) {
            LOG.warn("Offline download is already running. Ignoring duplicate request.");
            return;
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            // Delete marker immediately to mark cache as invalid during update
            File marker = new File(getOfflineCacheDir(), MARKER_FILE_NAME);
            if (marker.exists())
                marker.delete();

            File tempDir = null;
            try {
                LOG.info("Starting offline cache download...");
                statusCallback.accept("Подготовка рабочего пространства...");

                tempDir = Files.createTempDirectory("offline_setup").toFile();

                Optional<Plugin> pluginOpt = PluginLoader.INSTANCE.getPlugins().stream()
                        .filter(p -> p.getID().contains("fabric"))
                        .max(Plugin::compareTo);

                if (pluginOpt.isEmpty()) {
                    LOG.error("No Fabric generator plugin found.");
                    throw new RuntimeException("No Fabric generator plugin found.");
                }

                Plugin plugin = pluginOpt.get();
                File pluginFile = plugin.getFile();
                String pluginId = plugin.getID();
                LOG.info("Using plugin for offline template: " + pluginId);

                Properties versions = detectVersions(pluginFile);
                String mcVersion = versions.getProperty("minecraft_version", FALLBACK_MC_VERSION);
                String buildFileVersion = versions.getProperty("build_file_version", FALLBACK_BUILD_FILE_VERSION);
                LOG.info("Detected versions - MC: " + mcVersion + ", Build: " + buildFileVersion);

                File versionsFile = new File(getOfflineCacheDir(), VERSIONS_FILE_NAME);
                if (!versionsFile.getParentFile().exists())
                    versionsFile.getParentFile().mkdirs();
                try {
                    Properties p = new Properties();
                    p.setProperty("minecraft_version", mcVersion);
                    p.setProperty("loader_version", FALLBACK_LOADER_VERSION); // Default to current fallback
                    p.setProperty("build_file_version", buildFileVersion);
                    p.store(Files.newBufferedWriter(versionsFile.toPath()), "Offline Mode Versions");
                } catch (Exception ex) {
                    LOG.warn("Failed to save versions file", ex);
                }

                statusCallback.accept("Извлечение шаблона из " + pluginId + "...");

                boolean extracted = false;

                if (pluginFile.isDirectory()) {
                    File[] files = pluginFile.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isDirectory() && new File(f, "workspacebase").exists()) {
                                FileIO.copyDirectory(new File(f, "workspacebase"), tempDir);
                                extracted = true;
                                break;
                            }
                        }
                    }
                    if (!extracted && new File(pluginFile, "workspacebase").exists()) {
                        FileIO.copyDirectory(new File(pluginFile, "workspacebase"), tempDir);
                        extracted = true;
                    }
                }

                if (!extracted && ZipIO.checkIfZip(pluginFile)) {
                    try (ZipFile zip = ZipIO.openZipFile(pluginFile)) {
                        Enumeration<? extends ZipEntry> entries = zip.entries();
                        String workspaceBasePath = null;
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            if (entry.getName().endsWith("workspacebase/build.gradle")) {
                                workspaceBasePath = entry.getName().substring(0,
                                        entry.getName().length() - "build.gradle".length());
                                break;
                            }
                        }

                        if (workspaceBasePath != null) {
                            entries = zip.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                if (entry.getName().startsWith(workspaceBasePath)) {
                                    String relativePath = entry.getName().substring(workspaceBasePath.length());
                                    if (relativePath.isEmpty())
                                        continue;

                                    File dest = new File(tempDir, relativePath);
                                    if (entry.isDirectory()) {
                                        dest.mkdirs();
                                    } else {
                                        dest.getParentFile().mkdirs();
                                        try (InputStream is = zip.getInputStream(entry)) {
                                            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        }
                                    }
                                    extracted = true;
                                }
                            }
                        }
                    }
                }

                if (!extracted && pluginId.equals("generator-fabric-1.21.8") && pluginFile.isDirectory()) {
                    File t = new File(pluginFile, "fabric-1.21.8/workspacebase");
                    if (t.exists()) {
                        FileIO.copyDirectory(t, tempDir);
                        extracted = true;
                    }
                }

                if (!extracted) {
                    if (!new File(tempDir, "build.gradle").exists()) {
                        LOG.error("Failed to extract template from " + pluginFile);
                        throw new RuntimeException("Failed to extract template from plugin " + pluginId);
                    }
                }

                File metaInf = new File(tempDir, "src/main/resources/META-INF");
                metaInf.mkdirs();

                File commonAw = new File(metaInf, "common.accesswidener");
                try {
                    FileIO.writeStringToFile(COMMON_AW_CONTENT, commonAw);
                } catch (Exception e) {
                    LOG.warn("Failed to create common.accesswidener", e);
                }

                statusCallback.accept("Настройка файлов сборки...");
                preprocessBuildFiles(tempDir, mcVersion, buildFileVersion);

                statusCallback.accept("Запуск Gradle...");

                GradleConnector connector = GradleConnector.newConnector();
                connector.forProjectDirectory(tempDir);
                connector.useGradleUserHomeDir(getOfflineCacheDir());

                try (ProjectConnection connection = connector.connect()) {
                    BuildLauncher launcher = connection.newBuild();

                    // Changed tasks:
                    // 1. resolveDependencies (forces download of hidden artifacts like
                    // mixin-extensions)
                    // 2. build (forces full compilation and remapping to populate cache)
                    // launch)
                    launcher.forTasks("dependencies", "resolveDependencies", "eclipse", "downloadAssets",
                            "genEclipseRuns", "genSourcesWithCfr", "validateAccessWidener", "build");

                    // Memory optimization
                    long totalMem = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
                            .getOperatingSystemMXBean()).getTotalMemorySize();
                    if (totalMem > 10737418240L) { // > 10GB
                        launcher.addJvmArguments("-Xmx4096m");
                    } else {
                        launcher.addJvmArguments("-Xmx1024m");
                    }
                    launcher.addArguments("--build-cache", "--parallel");

                    launcher.addProgressListener(event -> {
                        statusCallback.accept(event.getDescriptor().getName());
                    }, OperationType.TASK);

                    int attempts = 0;
                    while (attempts < 3) {
                        try {
                            launcher.run();
                            break;
                        } catch (Exception e) {
                            attempts++;
                            if (attempts >= 3)
                                throw e;
                            LOG.warn("Gradle offline cache download attempt " + attempts + " failed, retrying...", e);
                            statusCallback.accept("Ошибка загрузки (Попытка " + attempts + "/3)...");
                            Thread.sleep(2000);
                        }
                    }
                }

                try {
                    File cachedProjectFiles = new File(getOfflineCacheDir(), "cached_project_files");
                    if (!cachedProjectFiles.exists())
                        cachedProjectFiles.mkdirs();

                    File dotProject = new File(tempDir, ".project");
                    File dotClasspath = new File(tempDir, ".classpath");
                    File dotSettings = new File(tempDir, ".settings");

                    if (dotProject.exists())
                        FileIO.copyFile(dotProject, new File(cachedProjectFiles, ".project"));
                    if (dotClasspath.exists())
                        FileIO.copyFile(dotClasspath, new File(cachedProjectFiles, ".classpath"));
                    if (dotSettings.exists())
                        FileIO.copyDirectory(dotSettings, new File(cachedProjectFiles, ".settings"));
                    LOG.info("Cached Eclipse project files for offline acceleration.");
                } catch (Exception e) {
                    LOG.warn("Failed to cache Eclipse project files", e);
                }

                try {
                    File cachedBuildDir = new File(getOfflineCacheDir(), "cached_build");
                    if (cachedBuildDir.exists())
                        FileIO.deleteDir(cachedBuildDir);
                    cachedBuildDir.mkdirs();

                    File buildDir = new File(tempDir, "build");
                    if (buildDir.exists()) {
                        FileIO.copyDirectory(buildDir, cachedBuildDir);
                        LOG.info("Cached Gradle 'build' directory for offline acceleration.");
                    } else {
                        LOG.warn("Build directory not found after setup, cannot cache it.");
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to cache build directory", e);
                }

                // REMOVED: .gradle directory caching to prevent absolute path issues

                marker = new File(getOfflineCacheDir(), MARKER_FILE_NAME);
                if (!marker.getParentFile().exists())
                    marker.getParentFile().mkdirs();
                marker.createNewFile();

                LOG.info("Offline cache download complete.");
                statusCallback.accept("Готово!");
                SwingUtilities.invokeLater(onComplete);

            } catch (Exception e) {
                LOG.error("Failed to download offline files", e);
                SwingUtilities.invokeLater(onError);
            } finally {
                if (tempDir != null) {
                    FileIO.deleteDir(tempDir);
                }
                long duration = System.currentTimeMillis() - startTime;
                Sentry.metrics().distribution("offline_cache_init_time", (double) duration);
                isDownloading.set(false);
            }
        }).start();
    }

    private static Properties detectVersions(File pluginFile) {
        Properties props = new Properties();
        try {
            if (pluginFile.isDirectory()) {
                // Try to find generator.yaml recursively (depth 2)
                File yaml = findFile(pluginFile, "generator.yaml", 2);
                if (yaml != null)
                    parseYamlVersions(new FileInputStream(yaml), props);
            } else if (ZipIO.checkIfZip(pluginFile)) {
                try (ZipFile zip = ZipIO.openZipFile(pluginFile)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry e = entries.nextElement();
                        if (e.getName().endsWith("generator.yaml")) {
                            parseYamlVersions(zip.getInputStream(e), props);
                            break; // Assume first generator.yaml is correct
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to detect versions from plugin", e);
        }
        return props;
    }

    private static File findFile(File dir, String name, int depth) {
        if (depth < 0)
            return null;
        File[] files = dir.listFiles();
        if (files == null)
            return null;
        for (File f : files) {
            if (f.getName().equals(name))
                return f;
            if (f.isDirectory()) {
                File res = findFile(f, name, depth - 1);
                if (res != null)
                    return res;
            }
        }
        return null;
    }

    private static void parseYamlVersions(InputStream is, Properties props) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("minecraft_version:")) {
                    props.setProperty("minecraft_version", parseValue(line));
                } else if (line.startsWith("build_file_version:")) {
                    props.setProperty("build_file_version", parseValue(line));
                }
            }
        } catch (Exception e) {
            LOG.warn("Error parsing yaml", e);
        }
    }

    private static String parseValue(String line) {
        String[] parts = line.split(":", 2);
        if (parts.length > 1) {
            String v = parts[1].trim();
            if (v.startsWith("\"") && v.endsWith("\""))
                return v.substring(1, v.length() - 1);
            if (v.startsWith("'") && v.endsWith("'"))
                return v.substring(1, v.length() - 1);
            return v;
        }
        return "";
    }

    private static void preprocessBuildFiles(File workspaceDir, String mcVersion, String buildFileVersion) {
        File buildGradle = new File(workspaceDir, "build.gradle");
        File mcreatorGradle = new File(workspaceDir, "mcreator.gradle");

        // Inject repositories into mcreator.gradle
        String repos = "\nrepositories {\n" +
                "    maven { url 'https://maven.fabricmc.net/' }\n" +
                "    mavenCentral()\n" +
                "    maven { url 'https://libraries.minecraft.net/' }\n" +
                "}\n";

        if (mcreatorGradle.exists()) {
            String content = FileIO.readFileToString(mcreatorGradle);
            content += repos;
            FileIO.writeStringToFile(content, mcreatorGradle);
        } else {
            FileIO.writeStringToFile(repos, mcreatorGradle);
        }

        if (buildGradle.exists()) {
            String content = FileIO.readFileToString(buildGradle);

            content = content.replace("${modid}", "offline");

            // Replace generator calls with detected versions
            content = content.replace("${generator.getGeneratorMinecraftVersion()}", mcVersion);
            content = content.replace("${generator.getGeneratorBuildFileVersion()}", buildFileVersion);

            // Regex for cases without ${} if any
            content = content.replaceAll("generator\\.getGeneratorMinecraftVersion\\(\\)", "'" + mcVersion + "'");
            content = content.replaceAll("generator\\.getGeneratorBuildFileVersion\\(\\)",
                    "'" + buildFileVersion + "'");

            // Force build.gradle to use common.accesswidener
            if (content.contains("accessWidenerPath")) {
                content = content.replaceAll("file\\(\"src/main/resources/META-INF/[^\"]+\\.accesswidener\"\\)",
                        "file(\"src/main/resources/META-INF/common.accesswidener\")");
            } else {
                content = content.replaceAll("src/main/resources/META-INF/.+\\.accesswidener",
                        "src/main/resources/META-INF/common.accesswidener");
            }

            // Stabilize Refmap Name
            if (content.contains("defaultRefmapName")) {
                content = content.replaceAll("defaultRefmapName\\s*=.*", "defaultRefmapName = \"mixins.refmap.json\"");
            }

            // Stabilize Output Jar Name to prevent remapping on project name change
            if (!content.contains("base.archivesName =")) {
                content += "\nbase.archivesName = 'offline-mod'\n";
            } else {
                content = content.replaceAll("base.archivesName\\s*=.*", "base.archivesName = 'offline-mod'");
            }

            // Add task to force download of all dependencies including hidden compiler
            // extensions
            String resolveTask = "\n" +
                    "task resolveDependencies {\n" +
                    "    doLast {\n" +
                    "        configurations.findAll { it.canBeResolved }.each { it.resolve() }\n" +
                    "    }\n" +
                    "}\n";
            content += resolveTask;

            FileIO.writeStringToFile(content, buildGradle);
        }

        File gradleProps = new File(workspaceDir, "gradle.properties");
        String content = "";
        if (gradleProps.exists()) {
            content = FileIO.readFileToString(gradleProps);
        }

        if (!content.contains("modid=")) {
            content += "\nmodid=offline";
        }

        if (!content.contains("org.gradle.caching=")) {
            content += "\norg.gradle.caching=true";
            content += "\norg.gradle.parallel=true";
            content += "\norg.gradle.vfs.watch=true";
            long totalMem = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
                    .getOperatingSystemMXBean()).getTotalMemorySize();
            if (totalMem > 10737418240L) { // > 10GB
                content += "\norg.gradle.jvmargs=-Xmx4096m";
            } else {
                content += "\norg.gradle.jvmargs=-Xmx1024m";
            }
        }
        FileIO.writeStringToFile(content, gradleProps);
    }

    /**
     * Applies offline mode fixes to an existing workspace.
     * Use this when creating a new project in offline mode to ensure it matches the
     * cached versions.
     */
    public static void applyOfflineFixes(File workspaceDir) {
        LOG.info("Applying offline mode fixes to workspace: " + workspaceDir);

        String mcVersion = FALLBACK_MC_VERSION;
        String loaderVersion = FALLBACK_LOADER_VERSION;
        String buildFileVersion = FALLBACK_BUILD_FILE_VERSION;
        try {
            File versionsFile = new File(getOfflineCacheDir(), VERSIONS_FILE_NAME);
            if (versionsFile.exists()) {
                Properties p = new Properties();
                p.load(Files.newBufferedReader(versionsFile.toPath()));
                mcVersion = p.getProperty("minecraft_version", FALLBACK_MC_VERSION);
                loaderVersion = p.getProperty("loader_version", FALLBACK_LOADER_VERSION);
                buildFileVersion = p.getProperty("build_file_version", FALLBACK_BUILD_FILE_VERSION);
            }
        } catch (Exception e) {
            LOG.warn("Failed to load offline versions properties", e);
        }

        File mcreatorGradle = new File(workspaceDir, "mcreator.gradle");
        if (mcreatorGradle.exists()) {
            String content = FileIO.readFileToString(mcreatorGradle);
            if (!content.contains("maven { url 'https://maven.fabricmc.net/' }")) {
                String repos = "\nrepositories {\n" +
                        "    maven { url 'https://maven.fabricmc.net/' }\n" +
                        "    mavenCentral()\n" +
                        "    maven { url 'https://libraries.minecraft.net/' }\n" +
                        "}\n";
                content += repos;
                FileIO.writeStringToFile(content, mcreatorGradle);
            }
        }

        File buildGradle = new File(workspaceDir, "build.gradle");
        if (buildGradle.exists()) {
            String content = FileIO.readFileToString(buildGradle);

            File gradleProps = new File(workspaceDir, "gradle.properties");
            if (gradleProps.exists()) {
                String props = FileIO.readFileToString(gradleProps);
                props = props.replaceAll("(?m)^minecraft_version=.*", "minecraft_version=" + mcVersion);
                props = props.replaceAll("(?m)^yarn_mappings=.*", "yarn_mappings=" + mcVersion + "+build.1");
                props = props.replaceAll("(?m)^fabric_loader_version=.*", "fabric_loader_version=" + loaderVersion);
                props = props.replaceAll("(?m)^loader_version=.*", "loader_version=" + loaderVersion);
                props = props.replaceAll("(?m)^fabric_version=.*", "fabric_version=" + buildFileVersion);

                if (!props.contains("org.gradle.caching=")) {
                    props += "\norg.gradle.caching=true";
                    props += "\norg.gradle.parallel=true";
                    long totalMem = ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
                            .getOperatingSystemMXBean()).getTotalMemorySize();
                    if (totalMem > 10737418240L) { // > 10GB
                        props += "\norg.gradle.jvmargs=-Xmx4096m";
                    } else {
                        props += "\norg.gradle.jvmargs=-Xmx1024m";
                    }
                }

                FileIO.writeStringToFile(props, gradleProps);
            }

            content = content.replaceAll("com\\.mojang:minecraft:[0-9\\.]+", "com.mojang:minecraft:" + mcVersion);
            content = content.replaceAll("net\\.fabricmc:fabric-loader:[0-9\\.]+",
                    "net.fabricmc:fabric-loader:" + loaderVersion);
            content = content.replaceAll("net\\.fabricmc:yarn:[0-9\\.+]+:v2",
                    "net.fabricmc:yarn:" + mcVersion + "+build.1:v2");

            if (content.contains("accessWidenerPath")) {
                content = content.replaceAll("file\\(\"src/main/resources/META-INF/[^\"]+\\.accesswidener\"\\)",
                        "file(\"src/main/resources/META-INF/common.accesswidener\")");
            }

            if (content.contains("defaultRefmapName")) {
                content = content.replaceAll("defaultRefmapName\\s*=.*", "defaultRefmapName = \"mixins.refmap.json\"");
            }

            // CRITICAL: Force output jar name to match cache, enabling cache hits for
            // remapping
            if (!content.contains("base.archivesName =")) {
                content += "\nbase.archivesName = 'offline-mod'\n";
            } else {
                content = content.replaceAll("base.archivesName\\s*=.*", "base.archivesName = 'offline-mod'");
            }

            FileIO.writeStringToFile(content, buildGradle);
            LOG.info("Offline fixes applied to build.gradle for workspace: " + workspaceDir);
        }

        File metaInf = new File(workspaceDir, "src/main/resources/META-INF");
        if (metaInf.exists() && metaInf.isDirectory()) {
            File[] awFiles = metaInf
                    .listFiles((dir, name) -> name.endsWith(".accesswidener") && !name.equals("common.accesswidener"));
            if (awFiles != null) {
                for (File aw : awFiles) {
                    try {
                        if (aw.exists()) {
                            aw.delete();
                        }
                        File commonAw = new File(metaInf, "common.accesswidener");
                        FileIO.writeStringToFile(COMMON_AW_CONTENT, commonAw);
                        LOG.info("Overwrote " + aw.getName()
                                + " and created common.accesswidener to match offline cache input.");
                    } catch (Exception e) {
                        LOG.warn("Failed to stabilize access widener " + aw.getName(), e);
                    }
                }
            }
        } else {
            try {
                File commonAw = new File(metaInf, "common.accesswidener");
                if (!commonAw.exists()) {
                    FileIO.writeStringToFile(COMMON_AW_CONTENT, commonAw);
                }
            } catch (Exception e) {
                LOG.warn("Failed to create fallback common.accesswidener", e);
            }
        }

        File cachedProjectFiles = new File(getOfflineCacheDir(), "cached_project_files");
        if (cachedProjectFiles.exists()) {
            try {
                File dotProject = new File(cachedProjectFiles, ".project");
                File dotClasspath = new File(cachedProjectFiles, ".classpath");
                File dotSettings = new File(cachedProjectFiles, ".settings");

                boolean restored = false;

                if (dotProject.exists() && !new File(workspaceDir, ".project").exists()) {
                    FileIO.copyFile(dotProject, new File(workspaceDir, ".project"));
                    restored = true;
                }
                if (dotClasspath.exists() && !new File(workspaceDir, ".classpath").exists()) {
                    FileIO.copyFile(dotClasspath, new File(workspaceDir, ".classpath"));
                    restored = true;
                }
                if (dotSettings.exists() && !new File(workspaceDir, ".settings").exists()) {
                    FileIO.copyDirectory(dotSettings, new File(workspaceDir, ".settings"));
                    restored = true;
                }

                if (restored)
                    LOG.info("Restored cached Eclipse files to accelerate workspace setup.");
            } catch (Exception e) {
                LOG.warn("Failed to restore cached Eclipse files", e);
            }
        }

        File cachedBuildDir = new File(getOfflineCacheDir(), "cached_build");
        if (cachedBuildDir.exists() && cachedBuildDir.isDirectory()) {
            try {
                File targetBuildDir = new File(workspaceDir, "build");
                if (!targetBuildDir.exists() || (targetBuildDir.isDirectory() && targetBuildDir.list() != null
                        && targetBuildDir.list().length == 0)) {
                    LOG.info("Restoring cached Gradle 'build' directory (pre-remapped sources)...");
                    if (!targetBuildDir.exists())
                        targetBuildDir.mkdirs();
                    FileIO.copyDirectory(cachedBuildDir, targetBuildDir);
                    LOG.info("Restored 'build' directory.");
                }
            } catch (Exception e) {
                LOG.warn("Failed to restore cached build directory", e);
            }
        }
    }

    public static void exportOfflineCache(java.io.File destination, java.util.function.Consumer<String> statusCallback,
            Runnable onComplete, Runnable onError) {
        if (isDownloading.getAndSet(true)) {
            LOG.warn("Offline cache operation is already running. Ignoring export request.");
            return;
        }
        new Thread(() -> {
            try {
                statusCallback.accept("Экспорт кэша...");
                net.mcreator.io.zip.ZipIO.zipDir(getOfflineCacheDir().getAbsolutePath(), destination.getAbsolutePath());
                statusCallback.accept("Экспорт завершен!");
                javax.swing.SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                LOG.error("Failed to export offline cache", e);
                javax.swing.SwingUtilities.invokeLater(onError);
            } finally {
                isDownloading.set(false);
            }
        }).start();
    }

    public static void importOfflineCache(java.io.File source, java.util.function.Consumer<String> statusCallback,
            Runnable onComplete, Runnable onError) {
        if (isDownloading.getAndSet(true)) {
            LOG.warn("Offline cache operation is already running. Ignoring import request.");
            return;
        }
        new Thread(() -> {
            try {
                statusCallback.accept("Очистка старого кэша...");
                deleteOfflineCache();

                statusCallback.accept("Импорт кэша...");
                net.mcreator.io.zip.ZipIO.unzip(source.getAbsolutePath(), getOfflineCacheDir().getAbsolutePath());

                statusCallback.accept("Импорт завершен!");
                javax.swing.SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                LOG.error("Failed to import offline cache", e);
                javax.swing.SwingUtilities.invokeLater(onError);
            } finally {
                isDownloading.set(false);
            }
        }).start();
    }

}
