package net.mcreator.util;

import net.mcreator.io.FileIO;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import net.mcreator.io.UserFolderManager;
import net.mcreator.gradle.GradleUtils;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Arrays;

public class OfflineCacheManager {

    private static final Logger LOG = LogManager.getLogger(OfflineCacheManager.class);

    private static final String MARKER_FILE_NAME = "offline_fabric_1.21.8_ready.marker";
    private static final String VERSIONS_FILE_NAME = "offline_versions.properties";

    // Fallback Versions (used if detection fails)
    private static final String FALLBACK_MC_VERSION = "1.21.8";
    private static final String FALLBACK_BUILD_FILE_VERSION = "0.133.4";

    public static File getOfflineCacheDir() {
        return UserFolderManager.getGradleHome();
    }

    public static boolean isOfflineModeReady() {
        File cache = getOfflineCacheDir();
        File marker = new File(cache, MARKER_FILE_NAME);
        return marker.exists();
    }

    public static String verifyCacheIntegrity() {
        if (!isOfflineModeReady()) return "Кэш не готов (маркер отсутствует)";

        File cache = getOfflineCacheDir();
        File versions = new File(cache, VERSIONS_FILE_NAME);
        if (!versions.exists()) return "Ошибка: файл версий отсутствует";

        File cachedProjectFiles = new File(cache, "cached_project_files");
        if (!cachedProjectFiles.exists()) return "Ошибка: кэшированные файлы проекта отсутствуют";
        if (!new File(cachedProjectFiles, ".classpath").exists()) return "Ошибка: .classpath не найден";

        return "Кэш цел (Проверено)";
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

    public static void downloadOfflineFiles(Consumer<String> statusCallback, Runnable onComplete, Runnable onError) {
        new Thread(() -> {
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

                // Detect versions from plugin
                Properties versions = detectVersions(pluginFile);
                String mcVersion = versions.getProperty("minecraft_version", FALLBACK_MC_VERSION);
                String buildFileVersion = versions.getProperty("build_file_version", FALLBACK_BUILD_FILE_VERSION);
                LOG.info("Detected versions - MC: " + mcVersion + ", Build: " + buildFileVersion);

                // Save versions to cache dir
                File versionsFile = new File(getOfflineCacheDir(), VERSIONS_FILE_NAME);
                if (!versionsFile.getParentFile().exists()) versionsFile.getParentFile().mkdirs();
                try {
                    Properties p = new Properties();
                    p.setProperty("minecraft_version", mcVersion);
                    p.setProperty("build_file_version", buildFileVersion);
                    p.store(Files.newBufferedWriter(versionsFile.toPath()), "Offline Mode Versions");
                } catch (Exception ex) {
                    LOG.warn("Failed to save versions file", ex);
                }

                statusCallback.accept("Извлечение шаблона из " + pluginId + "...");

                boolean extracted = false;

                // Extraction Logic
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
                                workspaceBasePath = entry.getName().substring(0, entry.getName().length() - "build.gradle".length());
                                break;
                            }
                        }

                        if (workspaceBasePath != null) {
                            entries = zip.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                if (entry.getName().startsWith(workspaceBasePath)) {
                                    String relativePath = entry.getName().substring(workspaceBasePath.length());
                                    if (relativePath.isEmpty()) continue;

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

                statusCallback.accept("Настройка файлов сборки...");
                captureVersionsFromTemplate(tempDir);

                // Generate dummy access widener to ensure cache compatibility with projects using it
                File metaInf = new File(tempDir, "src/main/resources/META-INF");
                if (!metaInf.exists()) metaInf.mkdirs();
                File awFile = new File(metaInf, "offline.accesswidener");
                if (!awFile.exists()) {
                     FileIO.writeStringToFile("accessWidener\tv1\tnamed\n", awFile);
                }

                preprocessBuildFiles(tempDir, mcVersion, buildFileVersion);

                statusCallback.accept("Запуск Gradle...");

                GradleConnector connector = GradleConnector.newConnector();
                connector.forProjectDirectory(tempDir);
                connector.useGradleUserHomeDir(getOfflineCacheDir());

                try (ProjectConnection connection = connector.connect()) {
                    BuildLauncher launcher = connection.newBuild();
                    // Added downloadAssets (without loom: prefix to be safe) and eclipse which transitively relies on assets
                    // Also adding genSources to ensure remapping of sources happens during cache creation
                    launcher.forTasks("dependencies", "eclipse", "downloadAssets", "genEclipseRuns", "genSources");
                    launcher.addJvmArguments("-Xmx2G");

                    // Match GradleUtils configuration to ensure cache compatibility
                    launcher.addJvmArguments("-Duser.language=en");
                    launcher.addJvmArguments("-Declipse.application=net.mcreator");

                    String java_home = GradleUtils.getJavaHome();
                    if (java_home != null) {
                        launcher.setJavaHome(new File(java_home));
                        launcher.withArguments(Arrays.asList("-Porg.gradle.java.installations.auto-detect=false",
                            "-Porg.gradle.java.installations.paths=" + java_home.replace('\\', '/')));
                    }

                    launcher.setEnvironmentVariables(GradleUtils.getEnvironment(java_home));

                    launcher.addProgressListener(event -> {
                        statusCallback.accept(event.getDescriptor().getName());
                    }, OperationType.TASK);

                    // Retry logic for the build launcher
                    int attempts = 0;
                    while (attempts < 3) {
                        try {
                            launcher.run();
                            break; // Success
                        } catch (Exception e) {
                            attempts++;
                            if (attempts >= 3) throw e;
                            LOG.warn("Gradle offline cache download attempt " + attempts + " failed, retrying...", e);
                            statusCallback.accept("Ошибка загрузки (Попытка " + attempts + "/3)...");
                            Thread.sleep(2000);
                        }
                    }
                }

                // Cache IDE files (Eclipse .project, .classpath, .settings) to speed up new project creation
                try {
                    File cachedProjectFiles = new File(getOfflineCacheDir(), "cached_project_files");
                    if (!cachedProjectFiles.exists()) cachedProjectFiles.mkdirs();

                    File dotProject = new File(tempDir, ".project");
                    File dotClasspath = new File(tempDir, ".classpath");
                    File dotSettings = new File(tempDir, ".settings");

                    if (dotProject.exists()) FileIO.copyFile(dotProject, new File(cachedProjectFiles, ".project"));
                    if (dotClasspath.exists()) FileIO.copyFile(dotClasspath, new File(cachedProjectFiles, ".classpath"));
                    if (dotSettings.exists()) FileIO.copyDirectory(dotSettings, new File(cachedProjectFiles, ".settings"));
                    LOG.info("Cached Eclipse project files for offline acceleration.");
                } catch (Exception e) {
                    LOG.warn("Failed to cache Eclipse project files", e);
                }

                File marker = new File(getOfflineCacheDir(), MARKER_FILE_NAME);
                if (!marker.getParentFile().exists()) marker.getParentFile().mkdirs();
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
            }
        }).start();
    }

    private static Properties detectVersions(File pluginFile) {
        Properties props = new Properties();
        try {
            if (pluginFile.isDirectory()) {
                // Try to find generator.yaml recursively (depth 2)
                File yaml = findFile(pluginFile, "generator.yaml", 2);
                if (yaml != null) parseYamlVersions(new FileInputStream(yaml), props);
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
        if (depth < 0) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.getName().equals(name)) return f;
            if (f.isDirectory()) {
                File res = findFile(f, name, depth - 1);
                if (res != null) return res;
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
            if (v.startsWith("\"") && v.endsWith("\"")) return v.substring(1, v.length() - 1);
            if (v.startsWith("'") && v.endsWith("'")) return v.substring(1, v.length() - 1);
            return v;
        }
        return "";
    }

    private static void captureVersionsFromTemplate(File tempDir) {
        try {
            File gradleProps = new File(tempDir, "gradle.properties");
            if (!gradleProps.exists()) return;

            Properties templateProps = new Properties();
            try (InputStream is = new FileInputStream(gradleProps)) {
                templateProps.load(is);
            }

            File versionsFile = new File(getOfflineCacheDir(), VERSIONS_FILE_NAME);
            Properties cacheProps = new Properties();
            if (versionsFile.exists()) {
                try (InputStream is = new FileInputStream(versionsFile)) {
                    cacheProps.load(is);
                }
            }

            // Copy relevant keys
            String[] keys = {"loader_version", "yarn_mappings", "fabric_version", "minecraft_version"};
            for (String key : keys) {
                if (templateProps.containsKey(key)) {
                    cacheProps.setProperty(key, templateProps.getProperty(key));
                }
            }

            try (java.io.OutputStream os = Files.newOutputStream(versionsFile.toPath())) {
                cacheProps.store(os, "Offline Mode Versions");
            }

            LOG.info("Captured template versions: " + cacheProps);

        } catch (Exception e) {
            LOG.warn("Failed to capture versions from template", e);
        }
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
            content = content.replaceAll("generator\\.getGeneratorBuildFileVersion\\(\\)", "'" + buildFileVersion + "'");

            FileIO.writeStringToFile(content, buildGradle);
        }

        File gradleProps = new File(workspaceDir, "gradle.properties");
        if (gradleProps.exists()) {
            String content = FileIO.readFileToString(gradleProps);
            if (!content.contains("modid=")) {
                content += "\nmodid=offline";
            }
            FileIO.writeStringToFile(content, gradleProps);
        }
    }

    /**
     * Applies offline mode fixes to an existing workspace.
     * Use this when creating a new project in offline mode to ensure it matches the cached versions.
     */
    public static void applyOfflineFixes(File workspaceDir) {
        LOG.info("Applying offline mode fixes to workspace: " + workspaceDir);

        // Load versions from cache
        Properties p = new Properties();
        String mcVersion = FALLBACK_MC_VERSION;
        String buildFileVersion = FALLBACK_BUILD_FILE_VERSION;
        try {
             File versionsFile = new File(getOfflineCacheDir(), VERSIONS_FILE_NAME);
             if (versionsFile.exists()) {
                 p.load(Files.newBufferedReader(versionsFile.toPath()));
                 mcVersion = p.getProperty("minecraft_version", FALLBACK_MC_VERSION);
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

            // NOTE: MCreator Fabric generator often puts version in gradle.properties or directly in build.gradle.

            // Let's handle gradle.properties first
            File gradleProps = new File(workspaceDir, "gradle.properties");
            if (gradleProps.exists()) {
                String props = FileIO.readFileToString(gradleProps);
                props = props.replaceAll("minecraft_version=.*", "minecraft_version=" + mcVersion);

                String yarn = p.getProperty("yarn_mappings", mcVersion + "+build.1");
                props = props.replaceAll("yarn_mappings=.*", "yarn_mappings=" + yarn);

                String loader = p.getProperty("loader_version", "0.15.11");
                props = props.replaceAll("loader_version=.*", "loader_version=" + loader);

                String fabric = p.getProperty("fabric_version", buildFileVersion);
                props = props.replaceAll("fabric_version=.*", "fabric_version=" + fabric);

                FileIO.writeStringToFile(props, gradleProps);
            }

            // If the version is directly in build.gradle
            content = content.replaceAll("com\\.mojang:minecraft:[0-9\\.]+", "com.mojang:minecraft:" + mcVersion);

            String loader = p.getProperty("loader_version", "0.15.11");
            content = content.replaceAll("net\\.fabricmc:fabric-loader:[0-9\\.]+", "net.fabricmc:fabric-loader:" + loader);

            // mappings "net.fabricmc:yarn:..."
            String yarn = p.getProperty("yarn_mappings", mcVersion + "+build.1");
            content = content.replaceAll("net\\.fabricmc:yarn:[0-9\\.+]+:v2", "net.fabricmc:yarn:" + yarn + ":v2");

            FileIO.writeStringToFile(content, buildGradle);
        }

        // Restore cached Eclipse files if available to skip initial sync
        File cachedProjectFiles = new File(getOfflineCacheDir(), "cached_project_files");
        if (cachedProjectFiles.exists()) {
            LOG.info("Restoring cached Eclipse files to accelerate workspace setup...");
            try {
                File dotProject = new File(cachedProjectFiles, ".project");
                File dotClasspath = new File(cachedProjectFiles, ".classpath");
                File dotSettings = new File(cachedProjectFiles, ".settings");

                if (dotProject.exists()) FileIO.copyFile(dotProject, new File(workspaceDir, ".project"));
                if (dotClasspath.exists()) FileIO.copyFile(dotClasspath, new File(workspaceDir, ".classpath"));
                if (dotSettings.exists()) FileIO.copyDirectory(dotSettings, new File(workspaceDir, ".settings"));
            } catch (Exception e) {
                LOG.warn("Failed to restore cached Eclipse files", e);
            }
        }
    }
}
