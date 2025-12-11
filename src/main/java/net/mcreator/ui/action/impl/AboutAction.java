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
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.AppIcon;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.themes.Theme;

import javax.swing.*;
import java.awt.*;

public class AboutAction extends BasicAction {

	public AboutAction(ActionRegistry actionRegistry) {
		super(actionRegistry, "О программе", evt -> showDialog(actionRegistry.getMCreator()));
	}

	public static void showDialog(Window parent) {
		// Only "Close" button
		Object[] options = { "Закрыть" };

		JPanel logoPanel = new JPanel(new BorderLayout(24, 24));
		logoPanel.add("North", new JLabel(AppIcon.getAppIcon(128, 128)));
		// Removed text logo, keeping just the icon for minimalism or custom branding if available
		// logoPanel.add("Center", new JLabel(UIRES.SVG.getBuiltIn("logo", 250, (int) (250 * (63 / 350.0)))));
		logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        // Simplified Russian text
        String message = "MCreator FunCode Edition\n\n" +
                "Версия: " + Launcher.version.getFullString() + "\n" +
                "Разработано для обучения программированию.\n\n" +
                "Сборка и адаптация специально для FunCode.";

		JTextArea aboutLabel = new JTextArea(message);
        aboutLabel.setEditable(false);
        aboutLabel.setOpaque(false);
        aboutLabel.setFont(Theme.current().getFont().deriveFont(14f));

		JComponent dialogPanel = PanelUtils.westAndCenterElement(
				PanelUtils.pullElementUp(PanelUtils.centerInPanel(logoPanel)), aboutLabel, 48, 48);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 32));

		JOptionPane.showOptionDialog(parent, dialogPanel, "О программе",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
}
