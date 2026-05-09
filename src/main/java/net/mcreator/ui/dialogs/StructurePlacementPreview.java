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

package net.mcreator.ui.dialogs;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.L10N;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * A dialog that visualizes structure placement based on spacing, separation, frequency, and spread type.
 */
public class StructurePlacementPreview extends JDialog {

	private final int spacing;
	private final int separation;
	private final float frequency;
	private final String spreadType;

	public StructurePlacementPreview(MCreator mcreator, int spacing, int separation, float frequency, String spreadType) {
		super(mcreator, L10N.t("elementgui.structuregen.visualize.title"), true);
		this.spacing = spacing;
		this.separation = separation;
		this.frequency = frequency;
		this.spreadType = spreadType;

		this.initGUI();
	}

	private void initGUI() {
		this.setSize(600, 600);
		this.setLocationRelativeTo(getOwner());

		JPanel previewPanel = new JPanel() {
			@Override protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawPreview(g, getWidth(), getHeight());
			}
		};
		previewPanel.setBackground(new Color(40, 40, 40));

		this.add(previewPanel);
	}

	private void drawPreview(Graphics g, int width, int height) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int padding = 40;
		int drawAreaSize = Math.min(width, height) - padding * 2;
		
		// Area to show in chunks
		int chunksToShow = 64; 
		if (spacing > 16) {
			chunksToShow = spacing * 4;
		}

		float scale = (float) drawAreaSize / chunksToShow;

		// Draw chunk grid (thin lines)
		g2.setColor(new Color(60, 60, 60));
		g2.setStroke(new BasicStroke(1));
		for (int i = 0; i <= chunksToShow; i++) {
			int pos = padding + (int) (i * scale);
			g2.drawLine(pos, padding, pos, padding + (int)(chunksToShow * scale));
			g2.drawLine(padding, pos, padding + (int)(chunksToShow * scale), pos);
		}

		// Draw spacing grid (structure cells - thicker lines)
		g2.setColor(new Color(100, 100, 100, 150));
		g2.setStroke(new BasicStroke(2));
		for (int i = 0; i <= chunksToShow; i += spacing) {
			int pos = padding + (int) (i * scale);
			g2.drawLine(pos, padding, pos, padding + (int)(chunksToShow * scale));
			g2.drawLine(padding, pos, padding + (int)(chunksToShow * scale), pos);
		}

		// Simulation logic
		g2.setColor(new Color(255, 200, 0));
		
		int gridCells = (chunksToShow / spacing) + 1;
		for (int gx = 0; gx < gridCells; gx++) {
			for (int gz = 0; gz < gridCells; gz++) {
				// Semi-deterministic seed based on cell coordinates to simulate Minecraft behavior
				long cellSeed = (long) gx * 341873128712L + (long) gz * 132897987541L;
				Random cellRandom = new Random(cellSeed);

				int range = spacing - separation;
				if (range < 1) range = 1;

				int xOffset;
				int zOffset;

				if ("triangular".equals(spreadType)) {
					xOffset = (cellRandom.nextInt(range) + cellRandom.nextInt(range)) / 2;
					zOffset = (cellRandom.nextInt(range) + cellRandom.nextInt(range)) / 2;
				} else {
					xOffset = cellRandom.nextInt(range);
					zOffset = cellRandom.nextInt(range);
				}

				// Frequency check
				if (frequency >= 1.0f || cellRandom.nextFloat() < frequency) {
					int cx = gx * spacing + xOffset;
					int cz = gz * spacing + zOffset;

					if (cx < chunksToShow && cz < chunksToShow) {
						int px = padding + (int) (cx * scale);
						int py = padding + (int) (cz * scale);
						int dotSize = Math.max(4, (int) scale - 2);
						g2.fillOval(px + ((int)scale - dotSize)/2, py + ((int)scale - dotSize)/2, dotSize, dotSize);
					}
				}
			}
		}
		
		// Legend
		g2.setColor(Color.WHITE);
		g2.drawString(L10N.t("elementgui.structuregen.spacing") + ": " + spacing, padding, padding - 20);
		g2.drawString(L10N.t("elementgui.structuregen.separation") + ": " + separation, padding + 100, padding - 20);
		g2.drawString(L10N.t("elementgui.structuregen.frequency") + ": " + frequency, padding + 200, padding - 20);
	}
}
