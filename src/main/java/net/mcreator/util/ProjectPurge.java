package net.mcreator.util;

import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.workspace.selector.RecentWorkspaceEntry;
import net.mcreator.preferences.PreferencesManager;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProjectPurge {

    private static final Logger LOG = LogManager.getLogger("ProjectPurge");

    public static void runPurge(MCreatorApplication app) {
        if (!PreferencesManager.PREFERENCES.projectPurge.enableProjectPurge.getValue()) {
            return;
        }

        new Thread(() -> {
            try {
                // Sleep a bit to not slow down immediate startup
                Thread.sleep(10000);

                String intervalStr = PreferencesManager.PREFERENCES.projectPurge.projectPurgeInterval.getValue();
                int months = 6;
                if (intervalStr.startsWith("3")) months = 3;
                else if (intervalStr.startsWith("12")) months = 12;

                long cutoffTime = System.currentTimeMillis() - (months * 30L * 24 * 60 * 60 * 1000);

                LOG.info("Running project purge. Cutoff time: " + new java.util.Date(cutoffTime));

                List<RecentWorkspaceEntry> recentWorkspaces = app.getRecentWorkspaces();
                List<RecentWorkspaceEntry> toRemove = new ArrayList<>();

                for (RecentWorkspaceEntry entry : recentWorkspaces) {
                    File workspaceFile = entry.getPath();
                    if (workspaceFile.exists() && workspaceFile.isFile()) {
                         long lastModified = workspaceFile.lastModified();

                         if (lastModified < cutoffTime) {
                             File projectDir = workspaceFile.getParentFile();
                             String fileName = workspaceFile.getName();
                             String dirName = projectDir.getName();
                             String expectedDirName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

                             // Safety check: The directory name must match the workspace file name (ignoring case)
                             // This prevents deleting parent folders if the workspace file was placed loosely in a common directory.
                             // e.g. "C:\Users\User\Documents\MyMod.mcreator" -> Parent "Documents" != "MyMod" -> Unsafe, Skip.
                             // e.g. "C:\Users\User\Documents\MyMod\MyMod.mcreator" -> Parent "MyMod" == "MyMod" -> Safe.
                             if (projectDir != null && projectDir.isDirectory() && dirName.equalsIgnoreCase(expectedDirName)) {
                                 LOG.info("Purging project: " + entry.getName() + " at " + projectDir + " (Last modified: " + new java.util.Date(lastModified) + ")");
                                 try {
                                     deleteDirectory(projectDir.toPath());
                                     toRemove.add(entry);
                                 } catch (IOException e) {
                                     LOG.error("Failed to delete project directory: " + projectDir, e);
                                 }
                             } else {
                                 LOG.warn("Skipping purge for project " + entry.getName() + ": Directory name mismatch or unsafe location. File: " + workspaceFile);
                             }
                         }
                    }
                }

                if (!toRemove.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        for (RecentWorkspaceEntry entry : toRemove) {
                            app.removeRecentWorkspace(entry);
                        }
                        app.updateRecentList();
                    });
                    LOG.info("Purged " + toRemove.size() + " projects.");
                }

            } catch (Exception e) {
                LOG.error("Error during project purge", e);
            }
        }).start();
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
    }
}
