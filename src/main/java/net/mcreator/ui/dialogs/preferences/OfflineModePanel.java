package net.mcreator.ui.dialogs.preferences;

import net.mcreator.ui.dialogs.preferences.PreferencesDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.util.OfflineCacheManager;
import net.mcreator.io.FileIO;
import net.mcreator.util.DesktopUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.AbstractMap;
import java.util.Map;

public class OfflineModePanel extends JPanel {

    private final PreferencesDialog dialog;
    private final JLabel statusLabel;
    private final JLabel progressLabel;
    private final JButton downloadButton;
    private final JButton deleteButton;
    private final JButton openFolderButton;
    private final JProgressBar progressBar;

    public OfflineModePanel(PreferencesDialog dialog) {
        this.dialog = dialog;
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        statusLabel = new JLabel("Статус: Проверка...");
        progressLabel = new JLabel(" ");
        downloadButton = new JButton("Загрузить офлайн копию");
        deleteButton = new JButton("Очистить офлайн кэш");
        openFolderButton = new JButton("Открыть папку кэша");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Actions
        downloadButton.addActionListener(this::downloadAction);
        deleteButton.addActionListener(this::deleteAction);
        openFolderButton.addActionListener(e -> DesktopUtils.openSafe(OfflineCacheManager.getOfflineCacheDir()));

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        add(statusLabel, gbc);

        gbc.gridy++;
        add(progressLabel, gbc);

        gbc.gridy++;
        add(progressBar, gbc);

        gbc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(downloadButton);
        buttons.add(deleteButton);
        buttons.add(openFolderButton);
        add(buttons, gbc);

        // Add to dialog
        String name = "Офлайн режим";
        JScrollPane scrollPane = new JScrollPane(PanelUtils.pullElementUp(this));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        dialog.model.addElement(name);
        dialog.preferences.add(PanelUtils.northAndCenterElement(new JLabel("Настройки офлайн режима"), scrollPane, 0, 0), name);

        updateStatus();
    }

    private void updateStatus() {
        statusLabel.setText("Статус: Вычисление...");
        statusLabel.setForeground(Color.BLACK);

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

                    String sizeStr = org.apache.commons.io.FileUtils.byteCountToDisplaySize(size);

                    if (ready) {
                        statusLabel.setText("Статус: Готово (Общий размер кэша: " + sizeStr + ")");
                        statusLabel.setForeground(new Color(0, 150, 0));
                    } else {
                        statusLabel.setText("Статус: Не загружено (Общий размер кэша: " + sizeStr + ")");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Статус: Ошибка проверки");
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void downloadAction(ActionEvent e) {
        downloadButton.setEnabled(false);
        deleteButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Статус: Инициализация...");
        statusLabel.setForeground(Color.BLACK); // Standard color

        OfflineCacheManager.downloadOfflineFiles(
            (status) -> SwingUtilities.invokeLater(() -> {
                progressLabel.setText(status);
                // Simple indeterminate unless we parse status
                progressBar.setIndeterminate(true);
            }),
            () -> { // Success
                updateStatus();
                downloadButton.setEnabled(true);
                deleteButton.setEnabled(true);
                progressBar.setVisible(false);
                progressLabel.setText("Загрузка завершена.");
                JOptionPane.showMessageDialog(dialog, "Файлы для офлайн режима успешно загружены!");
            },
            () -> { // Error
                updateStatus();
                downloadButton.setEnabled(true);
                deleteButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Статус: Ошибка загрузки");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(dialog, "Не удалось загрузить файлы. Проверьте логи.");
            }
        );
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
            JOptionPane.showMessageDialog(dialog, "Кэш очищен.");
        }
    }
}
