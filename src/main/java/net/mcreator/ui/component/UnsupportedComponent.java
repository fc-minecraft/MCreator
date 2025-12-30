/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2021, Pylo, opensource contributors
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

package net.mcreator.ui.component;

import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.util.ColorUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class UnsupportedComponent extends JPanel {

	/**
	 * Helper util method that marks provided component as not supported.
	 *
	 * @param comp The component to be marked.
	 */
	public static void markUnsupported(Component comp) {
		Container parent = comp.getParent();
		if (parent != null && !(parent instanceof UnsupportedComponent)) {
			LayoutManager parentLayout = parent.getLayout();
			int index = Arrays.asList(parent.getComponents()).indexOf(comp);
			if (parentLayout instanceof BorderLayout borderLayout) {
				Object constraints = borderLayout.getConstraints(comp);
				parent.remove(index);
				parent.add(new UnsupportedComponent(comp), constraints, index);
			} else if (parentLayout instanceof GridBagLayout gridBagLayout) {
				Object constraints = gridBagLayout.getConstraints(comp);
				parent.remove(index);
				parent.add(new UnsupportedComponent(comp), constraints, index);
			} else {
				parent.remove(index);
				parent.add(new UnsupportedComponent(comp), index);
			}
		}
	}

	private final Image warning = UIRES.get("18px.warning").getImage();

	/**
	 * Constructs a panel that displays an overlay with "unsupported" warning over the component marked as such.
	 *
	 * @param origin The component to be marked.
	 */
	UnsupportedComponent(Component origin) {
		setLayout(new GridLayout());
		setOpaque(false);

		setBounds(origin.getBounds());

		// disable origin component and prevent any mouse clicks/key presses from being handled by it
		origin.setEnabled(false);

		Arrays.stream(origin.getMouseListeners()).forEach(origin::removeMouseListener);
		Arrays.stream(origin.getMouseMotionListeners()).forEach(origin::removeMouseMotionListener);
		Arrays.stream(origin.getMouseWheelListeners()).forEach(origin::removeMouseWheelListener);
		Arrays.stream(origin.getKeyListeners()).forEach(origin::removeKeyListener);

		add(origin);
		setVisible(false);
	}

	@Override public void paint(Graphics g) {
		super.paint(g);
	}

}
