package net.mcreator.ui.dialogs.preferences;

import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.util.OfflineCacheManager;
import net.mcreator.util.DesktopUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.AbstractMap;
import java.util.Map;

public class OfflineModePanel extends JPanel {

    private final PreferencesDialog dialog;
    private final JLabel statusLabel;
    private final JLabel cacheSizeLabel;
    private final JButton downloadButton;
    private final JButton deleteButton;
    private final JButton verifyButton;
    private final JButton openFolderButton;
    private final JButton exportButton;
    private final JButton importButton;
    private final JCheckBox offlineModeCheckbox;
    private final JProgressBar progressBar;
    private final JTextArea logArea;

    public OfflineModePanel(PreferencesDialog dialog) {
        this.dialog = dialog;
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        statusLabel = new JLabel("Статус: Проверка...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        cacheSizeLabel = new JLabel("Размер кэша: Вычисление...");
        cacheSizeLabel.setForeground(Color.WHITE);

        downloadButton = new JButton("Загрузить офлайн копию");
        downloadButton.setFont(downloadButton.getFont().deriveFont(14f).deriveFont(Font.BOLD));
        downloadButton.setPreferredSize(new Dimension(250, 40));

        deleteButton = new JButton("Очистить офлайн кэш");
        verifyButton = new JButton("Проверить целостность");
        openFolderButton = new JButton("Открыть папку кэша");

        exportButton = new JButton("Экспорт кэша");
        importButton = new JButton("Импорт кэша");

        offlineModeCheckbox = new JCheckBox("Всегда запускать Gradle в офлайн режиме");
        offlineModeCheckbox.setOpaque(false);
        offlineModeCheckbox.setForeground(Color.WHITE);
        offlineModeCheckbox.setSelected(PreferencesManager.PREFERENCES.gradle.offline.get());
        offlineModeCheckbox.addActionListener(
                e -> PreferencesManager.PREFERENCES.gradle.offline.set(offlineModeCheckbox.isSelected()));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Лог выполнения"));

        // Actions
        downloadButton.addActionListener(this::downloadAction);
        deleteButton.addActionListener(this::deleteAction);
        verifyButton.addActionListener(e -> {
            String result = OfflineCacheManager.verifyCacheIntegrity();
            JOptionPane.showMessageDialog(dialog, result, "Проверка целостности",
                    result.startsWith("Ошибка") ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        });
        openFolderButton.addActionListener(e -> DesktopUtils.openSafe(OfflineCacheManager.getOfflineCacheDir()));

        exportButton.addActionListener(this::exportAction);
        importButton.addActionListener(this::importAction);

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        add(statusLabel, gbc);

        gbc.gridy++;
        add(cacheSizeLabel, gbc);

        gbc.gridy++;
        add(offlineModeCheckbox, gbc);

        gbc.gridy++;
        add(progressBar, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(logScrollPane, gbc);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(downloadButton);
        add(buttonPanel, gbc);

        gbc.gridy++;
        JPanel subButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subButtons.setOpaque(false);
        subButtons.add(deleteButton);
        subButtons.add(verifyButton);
        subButtons.add(openFolderButton);
        add(subButtons, gbc);

        gbc.gridy++;
        JPanel ioButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ioButtons.setOpaque(false);
        ioButtons.add(exportButton);
        ioButtons.add(importButton);
        add(ioButtons, gbc);

        // Add to dialog
        String name = "Офлайн режим";
        JScrollPane scrollPane = new JScrollPane(PanelUtils.pullElementUp(this));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        dialog.model.addElement(name);
        dialog.preferences
                .add(PanelUtils.northAndCenterElement(new JLabel("Настройки офлайн режима"), scrollPane, 0, 0), name);

        dialog.registerOfflineModePanel(this);

        updateStatus();
    }

    private void setButtonsEnabled(boolean enabled) {
        downloadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        offlineModeCheckbox.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        importButton.setEnabled(enabled);
    }

    private void exportAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт кэша офлайн режима");
        fileChooser.setSelectedFile(new java.io.File("mcreator_offline_cache.zip"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ZIP Archive", "zip"));

        if (fileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            java.io.File dest = fileChooser.getSelectedFile();
            if (!dest.getName().toLowerCase().endsWith(".zip"))
                dest = new java.io.File(dest.getParentFile(), dest.getName() + ".zip");

            setButtonsEnabled(false);
            progressBar.setVisible(true);
            statusLabel.setText("Статус: Экспорт...");
            statusLabel.setForeground(Color.WHITE);
            logArea.setText("");

            OfflineCacheManager.exportOfflineCache(dest,
                    (status) -> SwingUtilities.invokeLater(() -> {
                        logArea.append(status + "\n");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                        statusLabel.setText("Статус: " + status);
                    }),
                    () -> { // Success
                        updateStatus();
                        setButtonsEnabled(true);
                        progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(dialog, "Кэш успешно экспортирован!");
                    },
                    () -> { // Error
                        updateStatus();
                        setButtonsEnabled(true);
                        progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(dialog, "Ошибка экспорта. Проверьте лог.", "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
        }
    }

    private void importAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Вы уверены, что хотите импортировать кэш?\n" +
                        "ВНИМАНИЕ: Текущий кэш будет полностью удален и заменен импортированным.",
                "Подтверждение импорта", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Импорт кэша офлайн режима");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ZIP Archive", "zip"));

            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                java.io.File src = fileChooser.getSelectedFile();

                setButtonsEnabled(false);
                progressBar.setVisible(true);
                statusLabel.setText("Статус: Импорт...");
                statusLabel.setForeground(Color.WHITE);
                logArea.setText("");

                OfflineCacheManager.importOfflineCache(src,
                        (status) -> SwingUtilities.invokeLater(() -> {
                            logArea.append(status + "\n");
                            logArea.setCaretPosition(logArea.getDocument().getLength());
                            statusLabel.setText("Статус: " + status);
                        }),
                        () -> { // Success
                            updateStatus();
                            setButtonsEnabled(true);
                            progressBar.setVisible(false);
                            JOptionPane.showMessageDialog(dialog, "Кэш успешно импортирован!");
                        },
                        () -> { // Error
                            updateStatus();
                            setButtonsEnabled(true);
                            progressBar.setVisible(false);
                            JOptionPane.showMessageDialog(dialog, "Ошибка импорта. Проверьте лог.", "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        });
            }
        }
    }

    public void highlightDownload() {
        downloadButton.requestFocusInWindow();

        // Timer to flash the button border
        Timer flashTimer = new Timer(500, new java.awt.event.ActionListener() {
            boolean bright = true;
            int count = 0;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (count > 10 || !downloadButton.isDisplayable()) { // Stop after 5 seconds or if closed
                    ((Timer) e.getSource()).stop();
                    downloadButton.setBorder(UIManager.getBorder("Button.border")); // Reset border
                    return;
                }
                if (bright) {
                    // Red border
                    downloadButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.RED, 2),
                            BorderFactory.createEmptyBorder(3, 8, 3, 8) // Internal padding to keep text spaced
                    ));
                } else {
                    // Transparent/White border (or lighter red)
                    downloadButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 200, 200), 2),
                            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                }
                bright = !bright;
                count++;
            }
        });
        flashTimer.start();
    }

    private void updateStatus() {
        if (!progressBar.isVisible()) {
            statusLabel.setText("Статус: Вычисление...");
            statusLabel.setForeground(Color.WHITE);
        }

        new SwingWorker<Map.Entry<Long, Boolean>, Void>() {
            @Override
            protected Map.Entry<Long, Boolean> doInBackground() {
                long size = OfflineCacheManager.getCacheSize();
                boolean ready = OfflineCacheManager.isOfflineModeReady();
                return new AbstractMap.SimpleEntry<>(size, ready);
            }

            @Override
            protected void done() {
                try {
                    Map.Entry<Long, Boolean> result = get();
                    long size = result.getKey();
                    boolean ready = result.getValue();

                    String sizeStr = formatSize(size);
                    cacheSizeLabel.setText("Размер кэша: " + sizeStr);

                    if (ready) {
                        statusLabel.setText("Статус: Готово (Файлы загружены)");
                        statusLabel.setForeground(Color.GREEN);
                        downloadButton.setText("Обновить офлайн копию");
                    } else {
                        statusLabel.setText("Статус: Не загружено");
                        statusLabel.setForeground(Color.RED);
                        downloadButton.setText("Загрузить офлайн копию");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Статус: Ошибка проверки");
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private String formatSize(long v) {
        if (v < 1024)
            return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.2f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    private void downloadAction(ActionEvent e) {
        downloadButton.setEnabled(false);
        deleteButton.setEnabled(false);
        offlineModeCheckbox.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Статус: Инициализация...");
        statusLabel.setForeground(Color.WHITE);
        logArea.setText(""); // Clear log

        OfflineCacheManager.downloadOfflineFiles(
                (status) -> SwingUtilities.invokeLater(() -> {
                    logArea.append(status + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                    statusLabel.setText("Статус: " + status);
                }),
                () -> { // Success
                    updateStatus();
                    downloadButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    offlineModeCheckbox.setEnabled(true);
                    progressBar.setVisible(false);
                    JOptionPane.showMessageDialog(dialog, "Файлы для офлайн режима успешно загружены!");
                },
                () -> { // Error
                    updateStatus();
                    downloadButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    offlineModeCheckbox.setEnabled(true);
                    progressBar.setVisible(false);
                    statusLabel.setText("Статус: Ошибка загрузки");
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Не удалось загрузить файлы. Проверьте лог.");
                });
    }

    private void deleteAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Вы уверены, что хотите очистить кэш Gradle?\n" +
                        "ВНИМАНИЕ: Это удалит зависимости для ВСЕХ проектов и генераторов.\n" +
                        "Вам придется загружать их заново для других проектов.",
                "Подтверждение очистки", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            OfflineCacheManager.deleteOfflineCache();
            updateStatus();
            logArea.setText("Кэш очищен.\n");
            JOptionPane.showMessageDialog(dialog, "Кэш очищен.");
        }
    }
}
