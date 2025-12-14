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

package net.mcreator.ui.minecraft;

import net.mcreator.generator.mapping.MappableElement;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.MCItem;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.init.BlockItemIcons;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.util.image.EmptyIcon;
import net.mcreator.util.image.ImageUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DataListComboBox extends SearchableComboBox<DataListEntry> {

	private final MCreator mcreator;

	public DataListComboBox(MCreator mcreator, List<DataListEntry> list) {
		super(list.stream().filter(e -> e.isSupportedInWorkspace(mcreator.getWorkspace()))
				.toArray(DataListEntry[]::new));
		this.mcreator = mcreator;
		init(mcreator);
	}

	public DataListComboBox(MCreator mcreator) {
		this.mcreator = mcreator;
		init(mcreator);
	}

	private void init(MCreator mcreator) {
		setRenderer(new CustomRenderer(mcreator));
	}

	@Override public void addItem(DataListEntry item) {
		if (item.isSupportedInWorkspace(mcreator.getWorkspace()))
			super.addItem(item);
	}

	public void setSelectedItem(String string) {
		this.setSelectedItem(new DataListEntry.Dummy(string));
	}

	public void setSelectedItem(MappableElement mappableElement) {
		if (mappableElement == null)
			setSelectedIndex(getItemCount() > 0 ? 0 : -1);
		else
			this.setSelectedItem(new DataListEntry.Dummy(mappableElement.getUnmappedValue()));
	}

	public void setSelectedItem(DataListEntry dataListEntry) {
		super.setSelectedItem(dataListEntry);
	}

	@Override @Nonnull public DataListEntry getSelectedItem() {
		Object superretval = super.getSelectedItem();
		if (superretval == null)
			return new DataListEntry.Null();

		return super.getSelectedItem();
	}

	public static class CustomRenderer extends JLabel implements ListCellRenderer<DataListEntry> {

		private final MCreator mcreator;

		public CustomRenderer(MCreator mcreator) {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);

			this.mcreator = mcreator;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DataListEntry> list, DataListEntry value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setText(value.getReadableName());

			if (value instanceof DataListEntry.Custom custom) {
				setIcon(MCItem.getBlockIconBasedOnName(custom.getModElement().getWorkspace(), value.getName()));
			} else if (value.getTexture() == null) {
				setIcon(new EmptyIcon(32, 32));
			} else {
				setIcon(BlockItemIcons.getIconForItem(value.getTexture()));
			}

			if (!value.isSupportedInWorkspace(mcreator.getWorkspace())) {
				Icon imageIcon = getIcon();
				if (imageIcon instanceof ImageIcon)
					setIcon(ImageUtils.changeSaturation((ImageIcon) imageIcon, 0.1f));
				setForeground(Theme.current().getAltForegroundColor());
			}

			setHorizontalTextPosition(SwingConstants.RIGHT);
			setHorizontalAlignment(SwingConstants.LEFT);

			return this;
		}

	}

}
