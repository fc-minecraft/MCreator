/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.action.impl;

import net.mcreator.Launcher;
import net.mcreator.io.FileIO;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.AppIcon;
import net.mcreator.ui.laf.themes.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class AboutAction extends BasicAction {

	public AboutAction(ActionRegistry actionRegistry) {
		super(actionRegistry, "О программе", evt -> showDialog(actionRegistry.getMCreator()));
	}

	public static void showDialog(Window parent) {
		JTabbedPane tabbedPane = new JTabbedPane();

		// --- Tab 1: About ---
		JPanel logoPanel = new JPanel(new BorderLayout(24, 24));
		logoPanel.add("North", new JLabel(AppIcon.getAppIcon(128, 128)));
		logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

		String message = "MCreator FunCode Edition\n\n" +
				"Версия: " + Launcher.version.getFullString() + "\n" +
				"Разработано для обучения программированию.\n\n" +
				"Сборка и адаптация специально для FunCode.\n" +
				"Разработка FunCode версии: Nikita Gutsenkov\n\n" +
				"© FunCode. Все авторские права защищены.\n" +
				"Продукт защищен технологией DRM (Digital Rights Management).\n" +
				"Любое несанкционированное копирование, распространение\n" +
				"или модификация данного ПО строго запрещены.";

		JTextArea aboutLabel = new JTextArea(message);
		aboutLabel.setEditable(false);
		aboutLabel.setOpaque(false);
		aboutLabel.setFont(Theme.current().getFont().deriveFont(14f));

		JComponent aboutPanel = PanelUtils.westAndCenterElement(
				PanelUtils.pullElementUp(PanelUtils.centerInPanel(logoPanel)), aboutLabel, 48, 48);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 32));

		tabbedPane.addTab("О программе", aboutPanel);

		// --- Tab 2: Licenses ---
		StringBuilder licenseText = new StringBuilder();
		licenseText.append("MCreator is licensed under GPL v3.0\n\n");

		File licenseFile = new File("LICENSE.txt");
		if (licenseFile.exists()) {
			licenseText.append(FileIO.readFileToString(licenseFile));
		} else {
			licenseText.append("License file not found.");
		}

		licenseText.append("\n\n-------------------------------\n");
		licenseText.append("Third party licenses provided in 'license' folder:\n\n");

		File licenseDir = new File("license");
		if (licenseDir.isDirectory()) {
			File[] files = licenseDir.listFiles();
			if (files != null) {
				Arrays.stream(files)
						.sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
						.forEach(f -> {
							licenseText.append("=================================================================\n");
							licenseText.append("LICENSE: ").append(f.getName()).append("\n");
							licenseText.append("=================================================================\n\n");
							licenseText.append(FileIO.readFileToString(f)).append("\n\n");
						});
			}
		}

		JTextArea licenseArea = new JTextArea(licenseText.toString());
		licenseArea.setEditable(false);
		licenseArea.setFont(Theme.current().getFont().deriveFont(12f));
		// Use monospaced font for license text if possible, but theme font is cleaner.
		// licenseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		JScrollPane scrollPane = new JScrollPane(licenseArea);
		scrollPane.setPreferredSize(new Dimension(700, 500));
		// Initial scroll to top
		licenseArea.setCaretPosition(0);

		tabbedPane.addTab("Лицензии", scrollPane);

		// Show Dialog
		Object[] options = { "Закрыть" };
		JOptionPane.showOptionDialog(parent, tabbedPane, "О программе",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
}
