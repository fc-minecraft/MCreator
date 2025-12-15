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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OfflineCacheManager {

    private static final Logger LOG = LogManager.getLogger(OfflineCacheManager.class);

    // Marker file to indicate successful offline setup for the specific template
    private static final String MARKER_FILE_NAME = "offline_fabric_1.21.8_ready.marker";

    // Use MCreator's default Gradle User Home location
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

    /**
     * Downloads offline files by running a dummy Gradle build using the template.
     */
    public static void downloadOfflineFiles(Runnable onComplete, Runnable onError) {
        new Thread(() -> {
            File tempDir = null;
            try {
                LOG.info("Starting offline cache download...");

                // 1. Setup a temporary workspace
                tempDir = Files.createTempDirectory("offline_setup").toFile();

                // Find a suitable plugin (Fabric generator)
                Optional<Plugin> pluginOpt = PluginLoader.INSTANCE.getPlugins().stream()
                        .filter(p -> p.getID().contains("fabric"))
                        .max(Plugin::compareTo);

                if (pluginOpt.isEmpty()) {
                     String availablePlugins = PluginLoader.INSTANCE.getPlugins().stream()
                         .map(Plugin::getID)
                         .collect(Collectors.joining(", "));
                     LOG.error("No Fabric generator plugin found. Available: " + availablePlugins);
                     throw new RuntimeException("No Fabric generator plugin found.");
                }

                Plugin plugin = pluginOpt.get();
                File pluginFile = plugin.getFile();
                String pluginId = plugin.getID();
                LOG.info("Using plugin for offline template: " + pluginId);

                boolean extracted = false;

                // Extraction Logic (Directory or ZIP)
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
                     if (!extracted && files != null) {
                        for (File f : files) {
                             if (f.isDirectory()) {
                                 File subWorkspace = new File(f, "workspacebase");
                                 if (subWorkspace.exists()) {
                                     FileIO.copyDirectory(subWorkspace, tempDir);
                                     extracted = true;
                                     break;
                                 }
                             }
                        }
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
                            LOG.info("Found workspacebase at: " + workspaceBasePath);
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

                // Preprocess build files to fix templates variables
                preprocessBuildFiles(tempDir, pluginFile);

                // 2. Run Gradle
                GradleConnector connector = GradleConnector.newConnector();
                connector.forProjectDirectory(tempDir);
                connector.useGradleUserHomeDir(getOfflineCacheDir());

                try (ProjectConnection connection = connector.connect()) {
                    BuildLauncher launcher = connection.newBuild();
                    launcher.forTasks("dependencies", "eclipse");
                    launcher.addJvmArguments("-Xmx2G");
                    launcher.run();
                }

                // Create Marker File
                File marker = new File(getOfflineCacheDir(), MARKER_FILE_NAME);
                if (!marker.getParentFile().exists()) marker.getParentFile().mkdirs();
                marker.createNewFile();

                LOG.info("Offline cache download complete.");
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

    private static void preprocessBuildFiles(File workspaceDir, File pluginFile) {
        File buildGradle = new File(workspaceDir, "build.gradle");
        File mcreatorGradle = new File(workspaceDir, "mcreator.gradle");

        // Ensure mcreator.gradle exists
        if (!mcreatorGradle.exists()) {
            try {
                mcreatorGradle.createNewFile();
            } catch (IOException e) {
                LOG.error("Failed to create mcreator.gradle", e);
            }
        }

        // Try to read generator.yaml to get versions
        String mcVersion = "1.21.4"; // Default fallback
        String buildFileVersion = "0.133.4"; // Default fallback

        // Attempt to parse generator.yaml from plugin (if possible)
        // Simplified: we stick to defaults for 1.21.8 which is the target.

        if (buildGradle.exists()) {
            String content = FileIO.readFileToString(buildGradle);

            // Replace modid
            content = content.replace("${modid}", "offline");

            // Replace generator calls
            content = content.replace("${generator.getGeneratorMinecraftVersion()}", mcVersion);
            content = content.replace("${generator.getGeneratorBuildFileVersion()}", buildFileVersion);

            // Replace direct property access if any (e.g. generator.get...) without ${}
            content = content.replace("generator.getGeneratorMinecraftVersion()", "'" + mcVersion + "'");
            content = content.replace("generator.getGeneratorBuildFileVersion()", "'" + buildFileVersion + "'");

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
