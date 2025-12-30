package net.mcreator.util;

import net.mcreator.ui.MCreator;
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
        if (!PreferencesManager.PREFERENCES.projectPurge.enableProjectPurge.get()) {
            return;
        }

        // Capture state on the current thread (expected to be EDT based on call site in MCreatorApplication)
        List<RecentWorkspaceEntry> recentWorkspacesSnapshot = new ArrayList<>(app.getRecentWorkspaces());

        // We can't rely on getOpenMCreators() right now because the purge runs delayed and the user might open projects in the meantime.
        // Instead, we will fetch the list of open projects inside the delayed thread, but we need to do it on EDT.

        new Thread(() -> {
            try {
                // Sleep to not slow down immediate startup and allow user to settle in
                Thread.sleep(10000);

                String intervalStr = PreferencesManager.PREFERENCES.projectPurge.projectPurgeInterval.get();
                int months = 6;
                if (intervalStr.startsWith("3")) months = 3;
                else if (intervalStr.startsWith("12")) months = 12;

                long cutoffTime = System.currentTimeMillis() - (months * 30L * 24 * 60 * 60 * 1000);

                LOG.info("Running project purge. Cutoff time: " + new java.util.Date(cutoffTime));

                // Fetch currently open workspaces safely on EDT
                List<File> openWorkspacePaths = new ArrayList<>();
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        for (MCreator mcreator : app.getOpenMCreators()) {
                            if (mcreator.getWorkspace() != null && mcreator.getWorkspace().getFileManager() != null && mcreator.getWorkspace().getFileManager().getWorkspaceFile() != null) {
                                openWorkspacePaths.add(mcreator.getWorkspace().getFileManager().getWorkspaceFile().getAbsoluteFile());
                            }
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Failed to fetch open workspaces", e);
                    return; // Abort purge if we can't determine what's open
                }

                List<RecentWorkspaceEntry> toRemove = new ArrayList<>();

                for (RecentWorkspaceEntry entry : recentWorkspacesSnapshot) {
                    File workspaceFile = entry.getPath().getAbsoluteFile();

                    // Critical Safety: Do not purge if currently open
                    if (openWorkspacePaths.contains(workspaceFile)) {
                        LOG.info("Skipping purge for project " + entry.getName() + ": Project is currently open.");
                        continue;
                    }

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
