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

import net.mcreator.element.types.Transport;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.init.L10N;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Flexible HUD editor tab for the Transport mod element.
 *
 * <pre>
 * ┌────────────────┬─────────────────────────┬──────────────────────┐
 * │  Element list  │   Minecraft HUD preview  │  Properties panel    │
 * │  [＋][⧉][✕]  │   (drag-and-drop)        │  (context-sensitive) │
 * │  [▲][▼]       │                           │                      │
 * └────────────────┴─────────────────────────┴──────────────────────┘
 * </pre>
 *
 * <p>Users can add any number of HUD elements, each with an independent
 * type, label, anchor, offset, color and display condition.
 *
 * <p>The {@code overlaySupplier} returns the current value of the
 * "overlayBoundTo" selector so the canvas can show a warning when
 * a custom overlay is bound.
 */
public class HudEditorPanel extends JPanel {

	// ── Anchor constants ──────────────────────────────────────────────────────
	private static final String[] ANCHORS = {
			"TOP_LEFT", "TOP_CENTER", "TOP_RIGHT",
			"LEFT",     "CENTER",     "RIGHT",
			"BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT"
	};

	// ── Value expression choices ──────────────────────────────────────────────
	private static final String[] VALUE_EXPRESSIONS = {
			"SPEED", "FUEL", "THROTTLE", "ENGINE_STATUS", "ALTITUDE", "HEALTH"
	};

	// ── Display conditions ────────────────────────────────────────────────────
	private static final String[] DISPLAY_CONDITIONS = {
			"ALWAYS", "ENGINE_ON", "ENGINE_OFF", "MOVING"
	};

	// ── Element types ─────────────────────────────────────────────────────────
	private static final String[] ELEMENT_TYPES = {
			"VEHICLE_VALUE", "PROGRESS_BAR", "TEXT"
	};

	// ─────────────────────────────────────────────────────────────────────────
	// Data
	// ─────────────────────────────────────────────────────────────────────────

	private final DefaultListModel<Transport.HudElement> listModel = new DefaultListModel<>();
	private final JList<Transport.HudElement>            elementList;
	private @Nullable Transport.HudElement               selected  = null;
	private boolean                                      updating  = false;
	private int                                          idCounter = 0;

	// ─────────────────────────────────────────────────────────────────────────
	// Property controls (always visible)
	// ─────────────────────────────────────────────────────────────────────────

	private final JTextField     labelField    = new JTextField(12);
	private final JComboBox<String> typeCombo  = new JComboBox<>(ELEMENT_TYPES);
	private final JComboBox<String> anchorCombo= new JComboBox<>(ANCHORS);
	private final JSpinner       xSpin         = new JSpinner(new SpinnerNumberModel(0, -2000, 2000, 1));
	private final JSpinner       ySpin         = new JSpinner(new SpinnerNumberModel(0, -2000, 2000, 1));
	private final JColor         colorPicker;

	// ─────────────────────────────────────────────────────────────────────────
	// Type-specific controls
	// ─────────────────────────────────────────────────────────────────────────

	private final JComboBox<String> valueExprCombo  = new JComboBox<>(VALUE_EXPRESSIONS);
	private final JTextField        textContentField = new JTextField(12);
	private final JComboBox<String> displayCondCombo = new JComboBox<>(DISPLAY_CONDITIONS);
	private final JSpinner          barWidthSpin     = new JSpinner(new SpinnerNumberModel(80, 8, 400, 4));
	private final JSpinner          barHeightSpin    = new JSpinner(new SpinnerNumberModel(6,  2, 40,  1));

	// rows that show / hide depending on element type
	private final JPanel valueExprRow;
	private final JPanel textRow;
	private final JPanel barSizeRow;

	// ─────────────────────────────────────────────────────────────────────────
	// Preview
	// ─────────────────────────────────────────────────────────────────────────

	private final HudPreviewPanel preview;

	// supplier checked when redrawing to know if a custom overlay or actionbar is active and gets disabled message
	private final Supplier<String> disabledMessageSupplier;

	// ─────────────────────────────────────────────────────────────────────────
	// Constructor
	// ─────────────────────────────────────────────────────────────────────────

	public HudEditorPanel(MCreator mcreator, Supplier<String> disabledMessageSupplier) {
		this.disabledMessageSupplier = disabledMessageSupplier;
		this.colorPicker           = new JColor(mcreator, false, false).withColorTextColumns(5);

		setLayout(new BorderLayout(8, 8));
		setOpaque(false);

		// ── LEFT: list + buttons ──────────────────────────────────────────────
		elementList = buildElementList();
		JPanel leftPanel = buildLeftPanel();
		add(leftPanel, BorderLayout.WEST);

		// ── CENTER: preview ───────────────────────────────────────────────────
		preview = new HudPreviewPanel(listModel, () -> selected, disabledMessageSupplier);
		preview.setSpinnerSyncCallback((newX, newY) -> {
			updating = true;
			xSpin.setValue(newX);
			ySpin.setValue(newY);
			updating = false;
		});
		preview.setElementClickCallback(idx -> elementList.setSelectedIndex(idx));
		add(preview, BorderLayout.CENTER);

		// ── RIGHT: properties ─────────────────────────────────────────────────
		valueExprRow = labelledRow(L10N.t("elementgui.transport.hud.prop_value"),       valueExprCombo);
		textRow      = labelledRow(L10N.t("elementgui.transport.hud.prop_text"), textContentField);
		barSizeRow   = buildBarSizeRow();
		add(buildPropertiesPanel(), BorderLayout.EAST);

		typeCombo.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof String s) {
					setText(switch (s) {
						case "VEHICLE_VALUE" -> L10N.t("elementgui.transport.hud.type_vehicle_value");
						case "PROGRESS_BAR" -> L10N.t("elementgui.transport.hud.type_progress_bar");
						case "TEXT" -> L10N.t("elementgui.transport.hud.type_text");
						default -> s;
					});
				}
				return this;
			}
		});

		displayCondCombo.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof String s) {
					setText(switch (s) {
						case "ALWAYS" -> L10N.t("elementgui.transport.hud.cond_always");
						case "ENGINE_ON" -> L10N.t("elementgui.transport.hud.cond_engine_on");
						case "ENGINE_OFF" -> L10N.t("elementgui.transport.hud.cond_engine_off");
						case "MOVING" -> L10N.t("elementgui.transport.hud.cond_moving");
						default -> s;
					});
				}
				return this;
			}
		});

		valueExprCombo.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof String s) {
					setText(switch (s) {
						case "SPEED" -> L10N.t("elementgui.transport.hud.val_speed");
						case "FUEL" -> L10N.t("elementgui.transport.hud.val_fuel");
						case "THROTTLE" -> L10N.t("elementgui.transport.hud.val_throttle");
						case "ENGINE_STATUS" -> L10N.t("elementgui.transport.hud.val_engine");
						case "ALTITUDE" -> L10N.t("elementgui.transport.hud.val_altitude");
						case "HEALTH" -> L10N.t("elementgui.transport.hud.val_health");
						default -> s;
					});
				}
				return this;
			}
		});

		// ── Wire listeners ────────────────────────────────────────────────────
		wireListeners();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Public API
	// ─────────────────────────────────────────────────────────────────────────

	public void loadElements(List<Transport.HudElement> elements) {
		listModel.clear();
		if (elements != null) elements.forEach(listModel::addElement);
		if (!listModel.isEmpty()) elementList.setSelectedIndex(0);
		preview.repaint();
	}

	public List<Transport.HudElement> getElements() {
		List<Transport.HudElement> result = new ArrayList<>();
		for (int i = 0; i < listModel.size(); i++) result.add(listModel.get(i));
		return result;
	}

	/** Called externally when the overlayBoundTo value changes. */
	public void refreshOverlayState() {
		preview.repaint();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Building sub-panels
	// ─────────────────────────────────────────────────────────────────────────

	private JList<Transport.HudElement> buildElementList() {
		JList<Transport.HudElement> list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new ElementListCellRenderer());
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				Transport.HudElement sel = list.getSelectedValue();
				if (sel != null) loadPropertiesFrom(sel);
			}
		});
		return list;
	}

	private JPanel buildLeftPanel() {
		JScrollPane scroll = new JScrollPane(elementList);
		scroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

		JButton addBtn  = iconBtn("＋");
		JButton dupBtn  = iconBtn("⧉");
		JButton delBtn  = iconBtn("✕");
		JButton upBtn   = iconBtn("▲");
		JButton downBtn = iconBtn("▼");

		addBtn .addActionListener(e -> showAddDialog());
		dupBtn .addActionListener(e -> duplicate());
		delBtn .addActionListener(e -> delete());
		upBtn  .addActionListener(e -> move(-1));
		downBtn.addActionListener(e -> move(+1));

		JPanel buttons = new JPanel(new GridLayout(1, 5, 3, 0));
		buttons.setOpaque(false);
		for (JButton b : new JButton[]{ addBtn, dupBtn, delBtn, upBtn, downBtn }) buttons.add(b);

		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.setOpaque(false);
		panel.setPreferredSize(new Dimension(195, 0));
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildPropertiesPanel() {
		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints g = new GridBagConstraints();
		g.insets   = new Insets(3, 6, 3, 6);
		g.fill     = GridBagConstraints.HORIZONTAL;
		g.weightx  = 1.0;

		int[] row = { 0 };
		// label + type + layout
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_label"),  labelField);
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_type"),   typeCombo);
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_anchor"), anchorCombo);
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_x"),      xSpin);
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_y"),      ySpin);
		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_color"),  colorPicker);

		// separator
		g.gridx = 0; g.gridy = row[0]++; g.gridwidth = 2;
		grid.add(new JSeparator(), g);
		g.gridwidth = 1;

		addRow2(grid, g, row, L10N.t("elementgui.transport.hud.prop_show_when"), displayCondCombo);

		// dynamic rows
		g.gridx = 0; g.gridy = row[0]++; g.gridwidth = 2; grid.add(valueExprRow, g); g.gridwidth = 1;
		g.gridx = 0; g.gridy = row[0]++; g.gridwidth = 2; grid.add(textRow,      g); g.gridwidth = 1;
		g.gridx = 0; g.gridy = row[0]++; g.gridwidth = 2; grid.add(barSizeRow,   g); g.gridwidth = 1;

		// spacer
		g.gridx = 0; g.gridy = row[0]; g.gridwidth = 2; g.weighty = 1.0;
		grid.add(Box.createGlue(), g);

		JScrollPane scroll = new JScrollPane(grid,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(null);

		JPanel outer = new JPanel(new BorderLayout());
		outer.setOpaque(false);
		outer.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(90, 90, 90)), L10N.t("elementgui.transport.hud.panel_properties")));
		outer.setPreferredSize(new Dimension(265, 0));
		outer.add(scroll, BorderLayout.CENTER);
		return outer;
	}

	private JPanel buildBarSizeRow() {
		barWidthSpin .setPreferredSize(new Dimension(58, 24));
		barHeightSpin.setPreferredSize(new Dimension(48, 24));
		JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		inner.setOpaque(false);
		inner.add(new JLabel("W:")); inner.add(barWidthSpin);
		inner.add(new JLabel("H:")); inner.add(barHeightSpin);
		return labelledRow(L10N.t("elementgui.transport.hud.prop_bar_size"), inner);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Listener wiring
	// ─────────────────────────────────────────────────────────────────────────

	private void wireListeners() {
		typeCombo.addActionListener(e -> {
			if (selected != null && !updating) {
				selected.type = (String) typeCombo.getSelectedItem();
				refreshTypeRows();
				refreshList();
				preview.repaint();
			}
		});
		onChange(labelField, () -> {
			if (selected != null) { selected.label = labelField.getText(); refreshList(); }
		});
		anchorCombo.addActionListener(e -> {
			if (selected != null && !updating) { selected.anchor = (String) anchorCombo.getSelectedItem(); preview.repaint(); }
		});
		xSpin.addChangeListener(e -> {
			if (selected != null && !updating) { selected.xOffset = num(xSpin); preview.repaint(); }
		});
		ySpin.addChangeListener(e -> {
			if (selected != null && !updating) { selected.yOffset = num(ySpin); preview.repaint(); }
		});
		colorPicker.addColorSelectedListener(e -> {
			if (selected != null && !updating) { selected.color = colorPicker.getColor(); preview.repaint(); }
		});
		valueExprCombo.addActionListener(e -> {
			if (selected != null && !updating) { selected.valueExpression = (String) valueExprCombo.getSelectedItem(); preview.repaint(); }
		});
		onChange(textContentField, () -> {
			if (selected != null) { selected.textContent = textContentField.getText(); preview.repaint(); }
		});
		displayCondCombo.addActionListener(e -> {
			if (selected != null && !updating) selected.displayCondition = (String) displayCondCombo.getSelectedItem();
		});
		barWidthSpin.addChangeListener(e -> {
			if (selected != null && !updating) { selected.barWidth = num(barWidthSpin); preview.repaint(); }
		});
		barHeightSpin.addChangeListener(e -> {
			if (selected != null && !updating) { selected.barHeight = num(barHeightSpin); preview.repaint(); }
		});
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Element actions
	// ─────────────────────────────────────────────────────────────────────────

	private void showAddDialog() {
		String[] options = {
				L10N.t("elementgui.transport.hud.type_vehicle_value"),
				L10N.t("elementgui.transport.hud.type_progress_bar"),
				L10N.t("elementgui.transport.hud.type_text")
		};
		int choice = JOptionPane.showOptionDialog(
				SwingUtilities.getWindowAncestor(this),
				L10N.t("elementgui.transport.hud.add_dialog_message"),
				L10N.t("elementgui.transport.hud.add_dialog_title"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
		if (choice < 0) return;
		String type  = choice == 1 ? "PROGRESS_BAR" : choice == 2 ? "TEXT" : "VEHICLE_VALUE";
		String label = choice == 1 ? L10N.t("elementgui.transport.hud.default_label_bar")
				: choice == 2 ? L10N.t("elementgui.transport.hud.default_label_text")
				: L10N.t("elementgui.transport.hud.default_label_value");
		idCounter++;
		Transport.HudElement el = new Transport.HudElement(
				"custom_" + idCounter, label, type, "SPEED", label,
				"TOP_LEFT", 10, 10 + listModel.size() * 14,
				Color.WHITE, "ALWAYS", 80, 6);
		listModel.addElement(el);
		elementList.setSelectedIndex(listModel.size() - 1);
		preview.repaint();
	}

	private void duplicate() {
		if (selected == null) return;
		idCounter++;
		Transport.HudElement c = new Transport.HudElement(
				"custom_" + idCounter, selected.label + " (copy)",
				selected.type, selected.valueExpression, selected.textContent,
				selected.anchor, selected.xOffset + 10, selected.yOffset + 10,
				selected.color != null ? new Color(selected.color.getRGB()) : Color.WHITE,
				selected.displayCondition, selected.barWidth, selected.barHeight);
		listModel.addElement(c);
		elementList.setSelectedIndex(listModel.size() - 1);
		preview.repaint();
	}

	private void delete() {
		int idx = elementList.getSelectedIndex();
		if (idx < 0) return;
		listModel.remove(idx);
		if (listModel.isEmpty()) { selected = null; }
		else elementList.setSelectedIndex(Math.min(idx, listModel.size() - 1));
		preview.repaint();
	}

	private void move(int delta) {
		int idx = elementList.getSelectedIndex();
		int newIdx = idx + delta;
		if (idx < 0 || newIdx < 0 || newIdx >= listModel.size()) return;
		Transport.HudElement el = listModel.remove(idx);
		listModel.add(newIdx, el);
		elementList.setSelectedIndex(newIdx);
		preview.repaint();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Properties panel population
	// ─────────────────────────────────────────────────────────────────────────

	private void loadPropertiesFrom(Transport.HudElement el) {
		updating = true;
		selected = el;
		labelField     .setText(str(el.label));
		typeCombo      .setSelectedItem(str(el.type, "VEHICLE_VALUE"));
		anchorCombo    .setSelectedItem(str(el.anchor, "TOP_LEFT"));
		xSpin          .setValue(el.xOffset);
		ySpin          .setValue(el.yOffset);
		colorPicker    .setColor(el.color != null ? el.color : Color.WHITE);
		valueExprCombo .setSelectedItem(str(el.valueExpression, "SPEED"));
		textContentField.setText(str(el.textContent));
		displayCondCombo.setSelectedItem(str(el.displayCondition, "ALWAYS"));
		barWidthSpin   .setValue(el.barWidth  > 0 ? el.barWidth  : 80);
		barHeightSpin  .setValue(el.barHeight > 0 ? el.barHeight : 6);
		updating = false;
		refreshTypeRows();
		preview.repaint();
	}

	private void refreshTypeRows() {
		String t = selected != null ? selected.type : "";
		boolean isText = "TEXT".equals(t);
		boolean isBar  = "PROGRESS_BAR".equals(t);
		valueExprRow.setVisible(!isText);
		textRow.setVisible(true);
		((JLabel) textRow.getComponent(0)).setText(isText ? L10N.t("elementgui.transport.hud.prop_text") + ": " : L10N.t("elementgui.transport.hud.prop_prefix") + ": ");
		barSizeRow.setVisible(isBar);
		revalidate();
	}

	private void refreshList() {
		int idx = elementList.getSelectedIndex();
		if (idx >= 0 && idx < listModel.size())
			listModel.set(idx, listModel.get(idx));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helpers
	// ─────────────────────────────────────────────────────────────────────────

	private static JButton iconBtn(String txt) {
		JButton b = new JButton(txt);
		b.setFocusPainted(false);
		b.setMargin(new Insets(2, 4, 2, 4));
		return b;
	}

	private static JPanel labelledRow(String label, JComponent ctrl) {
		JPanel p = new JPanel(new BorderLayout(5, 0));
		p.setOpaque(false);
		p.add(new JLabel(label + ": "), BorderLayout.WEST);
		p.add(ctrl, BorderLayout.CENTER);
		return p;
	}

	private static void addRow2(JPanel grid, GridBagConstraints g, int[] rowCounter,
			String label, JComponent ctrl) {
		g.gridx = 0; g.gridy = rowCounter[0]; g.weightx = 0.32;
		grid.add(new JLabel(label), g);
		g.gridx = 1; g.weightx = 0.68;
		grid.add(ctrl, g);
		rowCounter[0]++;
	}

	private static void onChange(JTextField field, Runnable action) {
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e)  { action.run(); }
			@Override public void removeUpdate(DocumentEvent e)  { action.run(); }
			@Override public void changedUpdate(DocumentEvent e) { action.run(); }
		});
	}

	private static int    num(JSpinner s) { return ((Number) s.getValue()).intValue(); }
	private static String str(String s)              { return s != null ? s : ""; }
	private static String str(String s, String def)  { return s != null && !s.isEmpty() ? s : def; }

	// ─────────────────────────────────────────────────────────────────────────
	// List cell renderer
	// ─────────────────────────────────────────────────────────────────────────

	private static class ElementListCellRenderer extends DefaultListCellRenderer {
		@Override public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean selected, boolean focused) {
			super.getListCellRendererComponent(list, value, index, selected, focused);
			if (value instanceof Transport.HudElement el) {
				String tag = switch (el.type != null ? el.type : "") {
					case "VEHICLE_VALUE" -> "[V] ";
					case "PROGRESS_BAR"  -> "[▬] ";
					case "TEXT"          -> "[T] ";
					default              -> "[?] ";
				};
				String name = el.label;
				if (name == null || name.isEmpty()) {
					name = switch (el.type != null ? el.type : "") {
						case "VEHICLE_VALUE" -> L10N.t("elementgui.transport.hud.type_vehicle_value");
						case "PROGRESS_BAR" -> L10N.t("elementgui.transport.hud.type_progress_bar");
						case "TEXT" -> L10N.t("elementgui.transport.hud.type_text");
						default -> L10N.t("elementgui.transport.hud.default_label_value");
					};
				}
				if ("Engine Status".equals(name)) name = L10N.t("elementgui.transport.hud.val_engine");
				else if ("Speedometer".equals(name)) name = L10N.t("elementgui.transport.hud.val_speed");
				else if ("Throttle".equals(name)) name = L10N.t("elementgui.transport.hud.val_throttle");
				else if ("Fuel Level".equals(name)) name = L10N.t("elementgui.transport.hud.val_fuel");
				else if ("Control Hints".equals(name)) name = L10N.t("elementgui.transport.show_hints");

				setText(tag + name);
			}
			return this;
		}
	}
}
