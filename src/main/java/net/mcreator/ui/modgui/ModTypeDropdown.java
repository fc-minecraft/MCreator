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

package net.mcreator.ui.modgui;

import com.formdev.flatlaf.FlatClientProperties;
import net.mcreator.element.ModElementType;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.dialogs.NewModElementDialog;
import net.mcreator.util.image.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModTypeDropdown extends JPopupMenu {

	public ModTypeDropdown(MCreator mcreator) {
		setBorder(BorderFactory.createEmptyBorder());
		putClientProperty(FlatClientProperties.POPUP_BORDER_CORNER_RADIUS, 0);

		List<ModElementType<?>> types = mcreator.getGeneratorStats().getSupportedModElementTypes();

		int columns = 1;
		if (types.size() > 30) {
			columns = 3;
		} else if (types.size() > 15) {
			columns = 2;
		}

		if (columns > 1) {
			List<ModElementType<?>> typestmp = new ArrayList<>(types);
			int rows = (int) Math.ceil((double) types.size() / columns);

			for (int c = 0; c < columns; c++) {
				for (int r = 0; r < rows; r++) {
					int index = r * columns + c;
					int originalIndex = c * rows + r;
					if (originalIndex < types.size() && index < types.size()) {
						typestmp.set(index, types.get(originalIndex));
					}
				}
			}

			types = typestmp;
		}

		types.forEach(type -> {
			JMenuItem modTypeButton = new JMenuItem(" " + type.getReadableName() + " ");

			modTypeButton.setToolTipText(type.getDescription());
			modTypeButton.addActionListener(actionEvent -> NewModElementDialog.showNameDialog(mcreator, type));
			modTypeButton.setOpaque(false);

			modTypeButton.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

			modTypeButton.setFont(modTypeButton.getFont().deriveFont(Font.BOLD, 14f));

			if (type.getShortcut() != null)
				modTypeButton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(type.getShortcut()));

			modTypeButton.setIcon(IconUtils.resize(type.getIcon(), 32, 32));

			add(modTypeButton);
		});

		setLayout(new GridLayout(0, columns));
	}

}
