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

package net.mcreator.ui.modgui.transport;

import net.mcreator.element.parts.MItemBlock;
import net.mcreator.element.types.Transport;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.minecraft.MCItemHolder;
import net.mcreator.ui.validation.IValidable;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable editor for the list of fuel items accepted by a transport entity.
 * Each row shows an item picker, an amount spinner, and a delete button.
 */
public class FuelItemsEditorView extends JPanel implements IValidable {

	private final JPanel    listPanel    = new JPanel();
	private final JButton   addRowButton;
	private final List<FuelRow> rows     = new ArrayList<>();
	private Validator       validator;
	private ValidationResult lastResult;

	private final MCreator mcreator;

	public FuelItemsEditorView(MCreator mcreator) {
		this.mcreator = mcreator;
		addRowButton  = new JButton(L10N.t("elementgui.transport.add_fuel_item"));

		setLayout(new BorderLayout(5, 5));
		setOpaque(false);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setOpaque(false);

		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setPreferredSize(new Dimension(380, 200));
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);

		add(scroll,    BorderLayout.CENTER);

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		bottom.setOpaque(false);
		bottom.add(addRowButton);
		add(bottom, BorderLayout.SOUTH);

		addRowButton.addActionListener(e -> { addFuelRow(null, 250.0); revalidate(); repaint(); });
	}

	// ── Public API ────────────────────────────────────────────────────────────

	public void setFuelEntries(List<Transport.FuelEntry> entries) {
		listPanel.removeAll();
		rows.clear();
		if (entries != null) {
			for (Transport.FuelEntry entry : entries) addFuelRow(entry.item, entry.fuelAmount);
		}
		revalidate();
		repaint();
	}

	public List<Transport.FuelEntry> getFuelEntries() {
		List<Transport.FuelEntry> result = new ArrayList<>();
		for (FuelRow row : rows) {
			MItemBlock item = row.itemHolder.getBlock();
			if (item != null && !item.isAir())
				result.add(new Transport.FuelEntry(item, ((Number) row.valueSpinner.getValue()).doubleValue()));
		}
		return result;
	}

	// ── IValidable ───────────────────────────────────────────────────────────

	@Override public void setValidator(Validator v)       { this.validator = v; }
	@Override public Validator getValidator()             { return validator; }
	@Override public ValidationResult getValidationStatus() {
		lastResult = validator == null ? null : validator.validateIfEnabled(this);
		repaint();
		return lastResult;
	}

	@Override public void paint(Graphics g) {
		super.paint(g);
		if (lastResult != null && lastResult.type() != ValidationResult.Type.PASSED) {
			g.setColor(lastResult.type().getColor());
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}

	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		addRowButton.setEnabled(enabled);
		for (FuelRow row : rows) {
			row.itemHolder.setEnabled(enabled);
			row.valueSpinner.setEnabled(enabled);
			row.deleteButton.setEnabled(enabled);
		}
	}

	// ── Private helpers ──────────────────────────────────────────────────────

	private void addFuelRow(MItemBlock item, double val) {
		FuelRow row = new FuelRow(item, val);
		rows.add(row);
		listPanel.add(row);
	}

	// ── Inner row component ──────────────────────────────────────────────────

	private class FuelRow extends JPanel {
		final MCItemHolder itemHolder;
		final JSpinner     valueSpinner;
		final JButton      deleteButton;

		FuelRow(MItemBlock item, double val) {
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
			setOpaque(false);

			itemHolder = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
			itemHolder.setPreferredSize(new Dimension(150, 30));
			if (item != null) itemHolder.setBlock(item);

			valueSpinner = new JSpinner(new SpinnerNumberModel(val, 1.0, 100_000.0, 50.0));
			valueSpinner.setPreferredSize(new Dimension(80, 28));

			deleteButton = new JButton("✕");
			deleteButton.setMargin(new Insets(2, 6, 2, 6));

			add(itemHolder);
			add(new JLabel("→"));
			add(valueSpinner);
			add(new JLabel(L10N.t("elementgui.transport.units_short", "units")));
			add(deleteButton);

			deleteButton.addActionListener(e -> {
				listPanel.remove(FuelRow.this);
				rows.remove(FuelRow.this);
				listPanel.revalidate();
				listPanel.repaint();
			});
		}
	}
}
