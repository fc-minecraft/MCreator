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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OfflineCacheManager {

    private static final Logger LOG = LogManager.getLogger(OfflineCacheManager.class);

    // Use MCreator's default Gradle User Home location
    public static File getOfflineCacheDir() {
        return new File(System.getProperty("user.home"), ".mcreator/gradle");
    }

    public static boolean isOfflineModeReady() {
        // Quick check without calculating full size
        File cache = getOfflineCacheDir();
        File cachesDir = new File(cache, "caches");
        return cachesDir.exists() && cachesDir.isDirectory() && cachesDir.list().length > 0;
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
                // We look for any plugin containing "fabric" in ID
                Optional<Plugin> pluginOpt = PluginLoader.INSTANCE.getPlugins().stream()
                        .filter(p -> p.getID().contains("fabric"))
                        .max(Plugin::compareTo); // Get the latest one

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

                if (pluginFile.isDirectory()) {
                    // Exploded plugin
                    // Search for any "workspacebase" folder inside
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
                     if (!extracted) {
                         // Check direct child
                         if (new File(pluginFile, "workspacebase").exists()) {
                             FileIO.copyDirectory(new File(pluginFile, "workspacebase"), tempDir);
                             extracted = true;
                         }
                     }

                     // Also check for nested structure like "fabric-1.21.8/workspacebase" even if parent is not matching exact name
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
                    // ZIP/JAR plugin
                    try (ZipFile zip = ZipIO.openZipFile(pluginFile)) {
                        Enumeration<? extends ZipEntry> entries = zip.entries();
                        String workspaceBasePath = null;

                        // Find the path to workspacebase
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            // Check for workspacebase/build.gradle anywhere in zip
                            if (entry.getName().endsWith("workspacebase/build.gradle")) {
                                String parent = entry.getName().substring(0, entry.getName().length() - "build.gradle".length());
                                workspaceBasePath = parent;
                                break;
                            }
                        }

                        if (workspaceBasePath != null) {
                            LOG.info("Found workspacebase at: " + workspaceBasePath);
                            // Extract everything under this path
                            entries = zip.entries(); // reset or re-iterate
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

                if (!extracted) {
                     // Last resort: maybe it's the target user mentioned
                     if (pluginId.equals("generator-fabric-1.21.8") && pluginFile.isDirectory()) {
                          File t = new File(pluginFile, "fabric-1.21.8/workspacebase");
                          if (t.exists()) {
                              FileIO.copyDirectory(t, tempDir);
                              extracted = true;
                          }
                     }
                }

                if (!extracted) {
                     // Check if simple zip extraction worked or if we missed it
                     // Verify if build.gradle exists in tempDir
                     if (!new File(tempDir, "build.gradle").exists()) {
                         LOG.error("Failed to extract template from " + pluginFile);
                         throw new RuntimeException("Failed to extract template from plugin " + pluginId);
                     }
                }

                // 2. Run Gradle
                GradleConnector connector = GradleConnector.newConnector();
                connector.forProjectDirectory(tempDir);

                // Explicitly use MCreator's Gradle User Home
                connector.useGradleUserHomeDir(getOfflineCacheDir());

                try (ProjectConnection connection = connector.connect()) {
                    BuildLauncher launcher = connection.newBuild();

                    // Tasks: dependencies (downloads jars) and eclipse (downloads sources/javadoc/mappings/assets)
                    launcher.forTasks("dependencies", "eclipse");

                    launcher.addJvmArguments("-Xmx2G");

                    launcher.run();
                }

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
}
