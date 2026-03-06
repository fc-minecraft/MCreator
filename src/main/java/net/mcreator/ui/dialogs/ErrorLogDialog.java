package net.mcreator.ui.dialogs;

import net.mcreator.io.UserFolderManager;
import net.mcreator.ui.init.L10N;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.RandomAccessFile;

public class ErrorLogDialog extends JDialog {
    public static void showDialog(Window parent) {
        JDialog dialog = new JDialog(parent, L10N.t("dialog.error_log.title"), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        File logFile = new File(UserFolderManager.getFileFromUserFolder("logs"), "mcreator.log");
        StringBuilder logContent = new StringBuilder();
        if (logFile.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
                long length = raf.length();
                long MAX_READ = 256 * 1024; // 256 KB limit max
                if (length > MAX_READ) {
                    raf.seek(length - MAX_READ);
                    logContent.append("[" + L10N.t("dialog.error_log.info") + "]\n\n");
                }
                byte[] buffer = new byte[(int) Math.min(length, MAX_READ)];
                raf.readFully(buffer);

                String rawLog = new String(buffer, "UTF-8");
                java.util.regex.Pattern tsPattern = java.util.regex.Pattern
                        .compile("^\\d{4}-\\d{2}-\\d{2}-\\d{2}:\\d{2}:\\d{2} ");
                String[] lines = rawLog.split("\\r?\\n");
                String lastLineKey = null;
                String lastLineFull = null;
                int repeatCount = 0;

                for (String line : lines) {
                    String key = tsPattern.matcher(line).replaceFirst("");
                    if (key.equals(lastLineKey)) {
                        repeatCount++;
                    } else {
                        if (lastLineFull != null) {
                            logContent.append(lastLineFull);
                            if (repeatCount > 1) {
                                logContent.append(" [x").append(repeatCount).append("]");
                            }
                            logContent.append("\n");
                        }
                        lastLineKey = key;
                        lastLineFull = line;
                        repeatCount = 1;
                    }
                }
                if (lastLineFull != null) {
                    logContent.append(lastLineFull);
                    if (repeatCount > 1) {
                        logContent.append(" [x").append(repeatCount).append("]");
                    }
                    logContent.append("\n");
                }
            } catch (Exception e) {
                logContent.append(L10N.t("dialog.error_log.error_read")).append(" ").append(e.getMessage());
            }
        } else {
            logContent.append(L10N.t("dialog.error_log.not_found"));
        }

        textArea.setText(logContent.toString());
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> textArea.setCaretPosition(textArea.getDocument().getLength()));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton copyButton = new JButton(L10N.t("dialog.error_log.copy"));
        copyButton.addActionListener(e -> {
            String sysInfo = "";
            try {
                sysInfo = net.mcreator.ui.action.impl.AboutAction.getSystemInfo() +
                        "=================================================================\n\n";
            } catch (Exception ignored) {
            }

            String fullLogContent = sysInfo + textArea.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(fullLogContent), null);
            JOptionPane.showMessageDialog(dialog, L10N.t("dialog.error_log.copied"));
        });

        JButton closeButton = new JButton(L10N.t("dialog.error_log.close"));
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(copyButton);
        buttonPanel.add(closeButton);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
