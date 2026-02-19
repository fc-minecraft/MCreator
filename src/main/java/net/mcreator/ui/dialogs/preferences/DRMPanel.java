package net.mcreator.ui.dialogs.preferences;

import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.DRMAuthManager;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.themes.Theme;

import javax.swing.*;
import java.awt.*;

public class DRMPanel {

    public DRMPanel(PreferencesDialog preferencesDialog) {
        String name = "Лицензия"; // "License"
        preferencesDialog.model.addElement(name);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Header
        JLabel header = new JLabel("Информация о лицензии");
        ComponentUtils.deriveFont(header, 16);
        header.setForeground(Theme.current().getForegroundColor());
        panel.add(header, gbc);

        gbc.gridy = 1;
        panel.add(Box.createVerticalStrut(10), gbc);

        // Status
        gbc.gridy = 2;
        long days = DRMAuthManager.getDaysRemaining();
        JLabel statusLabel = new JLabel("<html><b>Статус:</b> Активна (" + days + " дн.)</html>");
        ComponentUtils.deriveFont(statusLabel, 14);
        panel.add(statusLabel, gbc);

        gbc.gridy = 3;
        JLabel userLabel = new JLabel("<html><b>Пользователь:</b> " + DRMAuthManager.getCurrentLogin() + "</html>");
        ComponentUtils.deriveFont(userLabel, 14);
        panel.add(userLabel, gbc);

        gbc.gridy = 4;
        panel.add(Box.createVerticalStrut(20), gbc);

        // Actions
        gbc.gridy = 5;
        JButton logoutBtn = new JButton("Выйти из аккаунта");
        ComponentUtils.deriveFont(logoutBtn, 14);
        logoutBtn.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(preferencesDialog,
                    "Вы уверены, что хотите выйти? Приложение будет закрыто.",
                    "Выход",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                DRMAuthManager.logout();
                System.exit(0);
            }
        });
        panel.add(PanelUtils.pullElementUp(logoutBtn), gbc);

        // Add to main dialog
        JScrollPane scrollPane = new JScrollPane(PanelUtils.pullElementUp(panel));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(null);

        // Header for the right side
        JComponent titlebar = L10N.label("dialog.preferences.description", name,
                "Управление учебной лицензией MCreator");
        titlebar.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        preferencesDialog.preferences.add(PanelUtils.northAndCenterElement(titlebar, scrollPane, 0, 0), name);
        preferencesDialog.sectionPanels.put(name, panel);
    }
}
