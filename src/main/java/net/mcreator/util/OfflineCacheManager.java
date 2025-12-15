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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OfflineCacheManager {

    private static final Logger LOG = LogManager.getLogger(OfflineCacheManager.class);

    private static final String MARKER_FILE_NAME = "offline_fabric_1.21.8_ready.marker";

    public static File getOfflineCacheDir() {
        return new File(System.getProperty("user.home"), ".mcreator/gradle");
    }

    public static boolean isOfflineModeReady() {
        File cache = getOfflineCacheDir();
        File marker = new File(cache, MARKER_FILE_NAME);
        return marker.exists();
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
                preprocessBuildFiles(tempDir);

                statusCallback.accept("Запуск Gradle...");

                GradleConnector connector = GradleConnector.newConnector();
                connector.forProjectDirectory(tempDir);
                connector.useGradleUserHomeDir(getOfflineCacheDir());

                try (ProjectConnection connection = connector.connect()) {
                    BuildLauncher launcher = connection.newBuild();
                    launcher.forTasks("dependencies", "eclipse");
                    launcher.addJvmArguments("-Xmx2G");

                    launcher.addProgressListener(event -> {
                        statusCallback.accept(event.getDescriptor().getName());
                    }, OperationType.TASK);

                    launcher.run();
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

    private static void preprocessBuildFiles(File workspaceDir) {
        File buildGradle = new File(workspaceDir, "build.gradle");
        File mcreatorGradle = new File(workspaceDir, "mcreator.gradle");

        // Inject repositories into mcreator.gradle
        String repos = "repositories {\n" +
                       "    maven { url 'https://maven.fabricmc.net/' }\n" +
                       "    mavenCentral()\n" +
                       "    maven { url 'https://libraries.minecraft.net/' }\n" +
                       "}\n";
        FileIO.writeStringToFile(repos, mcreatorGradle);

        // Versions for 1.21.4
        String mcVersion = "1.21.4";
        String buildFileVersion = "0.115.0"; // Known stable for 1.21.4

        if (buildGradle.exists()) {
            String content = FileIO.readFileToString(buildGradle);

            content = content.replace("${modid}", "offline");

            // Replace generator calls with hardcoded known working versions
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
}
