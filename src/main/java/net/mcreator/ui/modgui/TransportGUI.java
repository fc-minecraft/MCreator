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

package net.mcreator.ui.modgui;

import net.mcreator.ui.modgui.transport.*;

import com.google.gson.*;
import net.mcreator.element.types.Transport;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.TranslatedComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.renderer.ModelComboBoxRenderer;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.minecraft.TabListField;
import net.mcreator.ui.minecraft.TextureComboBox;
import net.mcreator.ui.minecraft.MCItemHolder;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.ui.validation.ValidationGroup;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.workspace.resources.TextureType;
import net.mcreator.util.ListUtils;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.resources.Model;
import net.mcreator.element.parts.MItemBlock;
import net.mcreator.element.ModElementType;
import net.mcreator.ui.minecraft.SingleModElementSelector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransportGUI extends ModElementGUI<Transport> {

	private final Model biped = new Model.BuiltInModel("Biped");
	private final SearchableComboBox<Model> transportModel = new SearchableComboBox<>(new Model[] { biped });
	private TextureComboBox transportTexture;
	private TextureComboBox itemTexture;
	private final VTextField mobName = new VTextField().requireValue("elementgui.transport.error_entity_needs_name");

	private final JComboBox<String> transportType = new TranslatedComboBox(
			Map.entry("LAND", "elementgui.transport.type.land"),
			Map.entry("AIR", "elementgui.transport.type.air"),
			Map.entry("WATER", "elementgui.transport.type.water")
	);

	private final JSpinner speed = new JSpinner(new SpinnerNumberModel(0.6, 0, 50, 0.05));
	private final JSpinner strafeSpeed = new JSpinner(new SpinnerNumberModel(0.15, 0, 50, 0.05));
	private final JSpinner steeringSpeed = new JSpinner(new SpinnerNumberModel(0.05, 0, 1, 0.01));
	private final JSpinner jumpForce = new JSpinner(new SpinnerNumberModel(0.5, 0, 10, 0.05));

	private final JSpinner seatOffsetX = new JSpinner(new SpinnerNumberModel(0.0, -100, 100, 0.05));
	private final JSpinner seatOffsetY = new JSpinner(new SpinnerNumberModel(0.0, -100, 100, 0.05));
	private final JSpinner seatOffsetZ = new JSpinner(new SpinnerNumberModel(0.0, -100, 100, 0.05));
	private final JSpinner seatYaw = new JSpinner(new SpinnerNumberModel(0.0, -360.0, 360.0, 1.0));
	private final JSpinner modelYawOffset = new JSpinner(new SpinnerNumberModel(0.0, -360.0, 360.0, 1.0));

	private final JCheckBox hasSpawnEgg = L10N.checkbox("elementgui.transport.has_spawn_egg");
	private final JColor spawnEggBaseColor = new JColor(mcreator, false, false).withColorTextColumns(5);
	private final JColor spawnEggDotColor = new JColor(mcreator, false, false).withColorTextColumns(5);

	private final TabListField creativeTabs = new TabListField(mcreator);
	private final ModelPreviewPanel previewPanel;

	private final JSpinner maxHealth = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 1000.0, 1.0));
	private final JComboBox<String> flightMode = new TranslatedComboBox(
			Map.entry("NONE", "elementgui.transport.flight_none"),
			Map.entry("PLANE", "elementgui.transport.plane_mechanics"),
			Map.entry("HELICOPTER", "elementgui.transport.helicopter_mechanics")
	);
	private final JSpinner maxAltitude = new JSpinner(new SpinnerNumberModel(320.0, 0.0, 1000.0, 10.0));
	private final JCheckBox enableCrash = new JCheckBox();
	private final JSpinner crashSpeed = new JSpinner(new SpinnerNumberModel(0.4, 0.1, 10.0, 0.1));
	private final JCheckBox enableFuel = new JCheckBox();
	private final FuelItemsEditorView fuelItemsEditor;
	private final JSpinner fuelConsumption = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 100.0, 0.05));
	private final JSpinner fuelCapacity = new JSpinner(new SpinnerNumberModel(1000.0, 10.0, 100000.0, 100.0));
	private final JTextField spinParts = new JTextField();
	private final JSpinner spinSpeed = new JSpinner(new SpinnerNumberModel(0.5, 0.0, 10.0, 0.05));

	// Phase 2: Keybinds
	private static final String[] KEY_OPTIONS = {
		"A","B","C","D","E","F","G","H","I","J","K","L","M",
		"N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
		"0","1","2","3","4","5","6","7","8","9",
		"F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12"
	};
	private final JComboBox<String> engineToggleKey = new JComboBox<>(KEY_OPTIONS);
	private final JComboBox<String> dismountKey     = new JComboBox<>(KEY_OPTIONS);

	// Phase 2: HUD visibility
	private final JCheckBox showEngineHUD   = new JCheckBox();
	private final JCheckBox showFuelHUD     = new JCheckBox();
	private final JCheckBox showThrottleHUD = new JCheckBox();
	private final JCheckBox showHints       = new JCheckBox();
	private final JComboBox<String> hudType = new TranslatedComboBox(
			Map.entry("ACTIONBAR", "elementgui.transport.hud_type.actionbar"),
			Map.entry("CUSTOM", "elementgui.transport.hud_type.custom"),
			Map.entry("OVERLAY", "elementgui.transport.hud_type.overlay")
	);
	private SingleModElementSelector overlayBoundTo;

	// Phase 2: Physics
	private final JSpinner accelerationRate = new JSpinner(new SpinnerNumberModel(0.04, 0.001, 1.0, 0.005));
	private final JSpinner brakeFactor      = new JSpinner(new SpinnerNumberModel(0.06, 0.001, 1.0, 0.005));
	private final JSpinner stallSpeed       = new JSpinner(new SpinnerNumberModel(0.15, 0.0, 5.0, 0.01));
	private final JSpinner inertiaFactor    = new JSpinner(new SpinnerNumberModel(0.98, 0.0, 1.0, 0.01));

	// Phase 2: Crash expanded
	private final JSpinner explosionRadius      = new JSpinner(new SpinnerNumberModel(2.0, 0.5, 20.0, 0.5));
	private final JCheckBox crashDamageToPlayer = new JCheckBox();
	private final JCheckBox crashDropItems      = new JCheckBox();

	private final ValidationGroup page1group = new ValidationGroup();
	private HudEditorPanel hudEditorPanel;

	public TransportGUI(MCreator mcreator, @Nonnull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.previewPanel = new ModelPreviewPanel();
		this.previewPanel.setNudgeCallback((dx, dy, dz) -> {
			double newX = ((Number) seatOffsetX.getValue()).doubleValue() + dx;
			double newY = ((Number) seatOffsetY.getValue()).doubleValue() + dy;
			double newZ = ((Number) seatOffsetZ.getValue()).doubleValue() + dz;
			seatOffsetX.setValue(Math.round(newX * 100.0) / 100.0);
			seatOffsetY.setValue(Math.round(newY * 100.0) / 100.0);
			seatOffsetZ.setValue(Math.round(newZ * 100.0) / 100.0);
		});
		this.fuelItemsEditor = new FuelItemsEditorView(mcreator);
		this.initGUI();
		super.finalizeGUI();
	}

	@Override protected void initGUI() {
		transportTexture = new TextureComboBox(mcreator, TextureType.ENTITY);
		itemTexture = new TextureComboBox(mcreator, TextureType.ITEM);

		transportModel.setPreferredSize(new Dimension(400, 42));
		transportModel.setRenderer(new ModelComboBoxRenderer());
		transportModel.addActionListener(e -> updatePreviewModel());

		hasSpawnEgg.setOpaque(false);
		hasSpawnEgg.setSelected(true);
		spawnEggBaseColor.setOpaque(false);
		spawnEggDotColor.setOpaque(false);

		hasSpawnEgg.addActionListener(e -> {
			spawnEggBaseColor.setEnabled(hasSpawnEgg.isSelected());
			spawnEggDotColor.setEnabled(hasSpawnEgg.isSelected());
			itemTexture.setEnabled(!hasSpawnEgg.isSelected());
		});

		// Page 1: Visuals
		JPanel visualsPanel = new JPanel(new GridLayout(9, 2, 5, 5));
		visualsPanel.setOpaque(false);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/name"), L10N.label("elementgui.transport.name")));
		visualsPanel.add(mobName);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/model"), L10N.label("elementgui.transport.model")));
		visualsPanel.add(transportModel);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/model_yaw"), L10N.label("elementgui.transport.model_yaw")));
		visualsPanel.add(modelYawOffset);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/texture"), L10N.label("elementgui.transport.texture")));
		visualsPanel.add(transportTexture);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/has_spawn_egg"), hasSpawnEgg));
		visualsPanel.add(new JLabel());

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/spawn_egg_base"), L10N.label("elementgui.transport.spawn_egg_base")));
		visualsPanel.add(spawnEggBaseColor);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/spawn_egg_dot"), L10N.label("elementgui.transport.spawn_egg_dot")));
		visualsPanel.add(spawnEggDotColor);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/item_texture"), L10N.label("elementgui.transport.item_texture")));
		visualsPanel.add(itemTexture);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/creative_tab"), L10N.label("elementgui.transport.creative_tab")));
		visualsPanel.add(creativeTabs);

		// Page 2: Movement
		JPanel movementPanel = new JPanel(new GridLayout(5, 2, 5, 5));
		movementPanel.setOpaque(false);

		movementPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/type"), L10N.label("elementgui.transport.type")));
		movementPanel.add(transportType);

		movementPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/speed"), L10N.label("elementgui.transport.speed")));
		movementPanel.add(speed);

		movementPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/strafe_speed"), L10N.label("elementgui.transport.strafe_speed")));
		movementPanel.add(strafeSpeed);

		movementPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/steering"), L10N.label("elementgui.transport.steering")));
		movementPanel.add(steeringSpeed);

		movementPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/jump_force"), L10N.label("elementgui.transport.jump_force")));
		movementPanel.add(jumpForce);

		// Page 3: Seat position & 3D Preview
		JPanel seatPanel = new JPanel(new BorderLayout(10, 10));
		seatPanel.setOpaque(false);

		JPanel seatControls = new JPanel(new GridLayout(4, 2, 5, 5));
		seatControls.setOpaque(false);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_x"), L10N.label("elementgui.transport.seat_offset_x")));
		seatControls.add(seatOffsetX);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_y"), L10N.label("elementgui.transport.seat_offset_y")));
		seatControls.add(seatOffsetY);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_z"), L10N.label("elementgui.transport.seat_offset_z")));
		seatControls.add(seatOffsetZ);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_yaw"), L10N.label("elementgui.transport.seat_yaw")));
		seatControls.add(seatYaw);

		seatPanel.add(seatControls, BorderLayout.NORTH);
		seatPanel.add(previewPanel, BorderLayout.CENTER);
		seatPanel.add(L10N.label("elementgui.transport.preview_help"), BorderLayout.SOUTH);

		// Add change listeners to spinners to update preview seat offset
		seatOffsetX.addChangeListener(e -> updatePreviewOffset());
		seatOffsetY.addChangeListener(e -> updatePreviewOffset());
		seatOffsetZ.addChangeListener(e -> updatePreviewOffset());
		seatYaw.addChangeListener(e -> updatePreviewOffset());
		modelYawOffset.addChangeListener(e -> updatePreviewOffset());

		transportTexture.setValidator(() -> {
			if (!biped.equals(transportModel.getSelectedItem())) {
				if (!transportTexture.hasTexture()) {
					return new ValidationResult(ValidationResult.Type.ERROR,
							L10N.t("elementgui.transport.error_custom_model_needs_texture"));
				}
			}
			return ValidationResult.PASSED;
		});

		itemTexture.setValidator(() -> {
			if (!hasSpawnEgg.isSelected()) {
				if (!itemTexture.hasTexture()) {
					return new ValidationResult(ValidationResult.Type.ERROR,
							L10N.t("elementgui.transport.error_item_needs_texture"));
				}
			}
			return ValidationResult.PASSED;
		});

		fuelItemsEditor.setValidator(() -> {
			if (enableFuel.isSelected()) {
				if (fuelItemsEditor.getFuelEntries().isEmpty()) {
					return new ValidationResult(ValidationResult.Type.ERROR,
							L10N.t("elementgui.transport.error_fuel_needs_item"));
				}
			}
			return ValidationResult.PASSED;
		});
		enableCrash.setText(L10N.t("elementgui.transport.enable_crash"));
		enableCrash.setOpaque(false);
		enableFuel.setText(L10N.t("elementgui.transport.enable_fuel"));
		enableFuel.setOpaque(false);

		transportType.addActionListener(e -> updateFlightControlsState());
		flightMode.addActionListener(e -> updateFlightControlsState());

		enableCrash.addActionListener(e -> crashSpeed.setEnabled(enableCrash.isSelected()));
		enableFuel.addActionListener(e -> {
			fuelItemsEditor.setEnabled(enableFuel.isSelected());
			fuelConsumption.setEnabled(enableFuel.isSelected());
			fuelCapacity.setEnabled(enableFuel.isSelected());
		});
		page1group.addValidationElement(mobName);
		page1group.addValidationElement(transportTexture);
		page1group.addValidationElement(itemTexture);
		page1group.addValidationElement(fuelItemsEditor);

		addPage(L10N.t("elementgui.transport.page_visual"), PanelUtils.totalCenterInPanel(visualsPanel)).validate(page1group);
		addPage(L10N.t("elementgui.transport.page_movement"), PanelUtils.totalCenterInPanel(movementPanel));
		addPage(L10N.t("elementgui.transport.page_seat"), seatPanel);

		// Page 4: Advanced Flight & Fuel (Two Column Layout)
		JPanel mainAdvancedPanel = new JPanel(new GridLayout(1, 2, 15, 10));
		mainAdvancedPanel.setOpaque(false);

		// Left Column: Parameters
		JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
		leftPanel.setOpaque(false);

		JPanel leftGrid = new JPanel(new GridLayout(8, 2, 5, 5));
		leftGrid.setOpaque(false);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/max_health"), L10N.label("elementgui.transport.max_health")));
		leftGrid.add(maxHealth);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/flight_mode"), L10N.label("elementgui.transport.flight_mode")));
		leftGrid.add(flightMode);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/max_altitude"), L10N.label("elementgui.transport.max_altitude")));
		leftGrid.add(maxAltitude);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/enable_crash"), enableCrash));
		leftGrid.add(new JLabel());

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/crash_speed"), L10N.label("elementgui.transport.crash_speed")));
		leftGrid.add(crashSpeed);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/spin_parts"), L10N.label("elementgui.transport.spin_parts")));
		leftGrid.add(spinParts);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/spin_speed"), L10N.label("elementgui.transport.spin_speed")));
		leftGrid.add(spinSpeed);

		leftGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/enable_fuel"), enableFuel));
		leftGrid.add(new JLabel());

		JPanel fuelSettingsGrid = new JPanel(new GridLayout(2, 2, 5, 5));
		fuelSettingsGrid.setOpaque(false);

		fuelSettingsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/fuel_consumption"), L10N.label("elementgui.transport.fuel_consumption")));
		fuelSettingsGrid.add(fuelConsumption);

		fuelSettingsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/fuel_capacity"), L10N.label("elementgui.transport.fuel_capacity")));
		fuelSettingsGrid.add(fuelCapacity);

		leftPanel.add(leftGrid, BorderLayout.NORTH);
		leftPanel.add(fuelSettingsGrid, BorderLayout.CENTER);

		// Right Column: Fuel Items List
		JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
		rightPanel.setOpaque(false);
		rightPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/fuel_items"), L10N.label("elementgui.transport.fuel_items")), BorderLayout.NORTH);
		rightPanel.add(fuelItemsEditor, BorderLayout.CENTER);

		mainAdvancedPanel.add(leftPanel);
		mainAdvancedPanel.add(rightPanel);

		addPage(L10N.t("elementgui.transport.page_advanced"), PanelUtils.totalCenterInPanel(mainAdvancedPanel));

		// Page 5: Controls & HUD
		JPanel controlsPanel = new JPanel(new GridLayout(1, 2, 15, 10));
		controlsPanel.setOpaque(false);

		// Left: Keybinds + HUD toggles
		JPanel keybindGrid = new JPanel(new GridLayout(8, 2, 5, 5));
		keybindGrid.setOpaque(false);

		keybindGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/engine_toggle_key"), L10N.label("elementgui.transport.engine_key")));
		keybindGrid.add(engineToggleKey);

		keybindGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/dismount_key"), L10N.label("elementgui.transport.dismount_key")));
		keybindGrid.add(dismountKey);

		showEngineHUD.setText(L10N.t("elementgui.transport.show_engine_hud"));
		showEngineHUD.setOpaque(false);
		showFuelHUD.setText(L10N.t("elementgui.transport.show_fuel_hud"));
		showFuelHUD.setOpaque(false);
		showThrottleHUD.setText(L10N.t("elementgui.transport.show_throttle_hud"));
		showThrottleHUD.setOpaque(false);
		showHints.setText(L10N.t("elementgui.transport.show_hints"));
		showHints.setOpaque(false);

		hudType.addActionListener(e -> updateHUDCheckboxesState());
		keybindGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/hud_type"), L10N.label("elementgui.transport.hud_type")));
		keybindGrid.add(hudType);

		keybindGrid.add(showEngineHUD);   keybindGrid.add(new JLabel());
		keybindGrid.add(showFuelHUD);     keybindGrid.add(new JLabel());
		keybindGrid.add(showThrottleHUD); keybindGrid.add(new JLabel());
		keybindGrid.add(showHints);       keybindGrid.add(new JLabel());

		overlayBoundTo = new SingleModElementSelector(mcreator, ModElementType.OVERLAY);
		overlayBoundTo.setDefaultText(L10N.t("elementgui.common.no_overlay", "No overlay"));
		overlayBoundTo.addEntrySelectedListener(e -> updateHUDCheckboxesState());

		keybindGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/overlay_bound_to"), L10N.label("elementgui.transport.overlay_bound_to")));
		keybindGrid.add(overlayBoundTo);

		// Right: Physics + crash settings
		JPanel physicsGrid = new JPanel(new GridLayout(8, 2, 5, 5));
		physicsGrid.setOpaque(false);

		physicsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/acceleration_rate"), L10N.label("elementgui.transport.acceleration_rate")));
		physicsGrid.add(accelerationRate);

		physicsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/brake_factor"), L10N.label("elementgui.transport.brake_factor")));
		physicsGrid.add(brakeFactor);

		physicsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/stall_speed"), L10N.label("elementgui.transport.stall_speed")));
		physicsGrid.add(stallSpeed);

		physicsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/inertia_factor"), L10N.label("elementgui.transport.inertia_factor")));
		physicsGrid.add(inertiaFactor);

		physicsGrid.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/explosion_radius"), L10N.label("elementgui.transport.explosion_radius")));
		physicsGrid.add(explosionRadius);

		crashDamageToPlayer.setText(L10N.t("elementgui.transport.crash_damage_to_player"));
		crashDamageToPlayer.setOpaque(false);
		physicsGrid.add(crashDamageToPlayer); physicsGrid.add(new JLabel());

		crashDropItems.setText(L10N.t("elementgui.transport.crash_drop_items"));
		crashDropItems.setOpaque(false);
		physicsGrid.add(crashDropItems); physicsGrid.add(new JLabel());

		enableCrash.addActionListener(e2 -> {
			explosionRadius.setEnabled(enableCrash.isSelected());
			crashDamageToPlayer.setEnabled(enableCrash.isSelected());
			crashDropItems.setEnabled(enableCrash.isSelected());
		});

		controlsPanel.add(keybindGrid);
		controlsPanel.add(physicsGrid);

		addPage(L10N.t("elementgui.transport.page_controls"), PanelUtils.totalCenterInPanel(controlsPanel));

		hudEditorPanel = new HudEditorPanel(mcreator, () -> {
			String selectedType = (String) hudType.getSelectedItem();
			if ("ACTIONBAR".equals(selectedType)) {
				return L10N.t("elementgui.transport.hud.disabled_actionbar");
			} else if ("OVERLAY".equals(selectedType)) {
				return L10N.t("elementgui.transport.hud.disabled_overlay");
			}
			return null;
		});
		addPage(L10N.t("elementgui.transport.page_hud_editor"), hudEditorPanel);

		updateFlightControlsState();
	}

	private void updatePreviewModel() {
		Model selected = (Model) transportModel.getSelectedItem();
		previewPanel.loadModel(selected);
	}

	private void updatePreviewOffset() {
		double x = ((Number) seatOffsetX.getValue()).doubleValue();
		double y = ((Number) seatOffsetY.getValue()).doubleValue();
		double z = ((Number) seatOffsetZ.getValue()).doubleValue();
		double yaw = ((Number) seatYaw.getValue()).doubleValue();
		double modYaw = ((Number) modelYawOffset.getValue()).doubleValue();
		previewPanel.setSeatOffset(x, y, z, yaw);
		previewPanel.setModelYaw(modYaw);
	}

	@Override public void reloadDataLists() {
		super.reloadDataLists();
		transportTexture.reload();
		itemTexture.reload();

		ComboBoxUtil.updateComboBoxContents(transportModel, ListUtils.merge(Collections.singletonList(biped),
				Model.getModels(mcreator.getWorkspace()).stream()
						.filter(el -> el.getType() == Model.Type.JAVA || el.getType() == Model.Type.MCREATOR)
						.collect(Collectors.toList())));

		updatePreviewModel();
		updatePreviewOffset();
	}

	@Override protected void openInEditingMode(Transport transport) {
		mobName.setText(transport.mobName);
		Model entityModel = transport.getEntityModel();
		if (entityModel != null)
			transportModel.setSelectedItem(entityModel);

		transportTexture.setTextureFromTextureName(transport.texture);
		itemTexture.setTextureFromTextureName(transport.itemTexture);
		transportType.setSelectedItem(transport.transportType);

		speed.setValue(transport.speed);
		strafeSpeed.setValue(transport.strafeSpeed);
		steeringSpeed.setValue(transport.steeringSpeed);
		jumpForce.setValue(transport.jumpForce);

		seatOffsetX.setValue(transport.seatOffsetX);
		seatOffsetY.setValue(transport.seatOffsetY);
		seatOffsetZ.setValue(transport.seatOffsetZ);
		seatYaw.setValue(transport.seatYaw);
		modelYawOffset.setValue(transport.modelYawOffset);

		hasSpawnEgg.setSelected(transport.hasSpawnEgg);
		spawnEggBaseColor.setColor(transport.spawnEggBaseColor);
		spawnEggDotColor.setColor(transport.spawnEggDotColor);
		creativeTabs.setListElements(transport.creativeTabs);

		spawnEggBaseColor.setEnabled(transport.hasSpawnEgg);
		spawnEggDotColor.setEnabled(transport.hasSpawnEgg);
		itemTexture.setEnabled(!transport.hasSpawnEgg);

		maxHealth.setValue(transport.maxHealth);
		if (transport.planeMechanics) {
			flightMode.setSelectedItem("PLANE");
		} else if (transport.helicopterMechanics) {
			flightMode.setSelectedItem("HELICOPTER");
		} else {
			flightMode.setSelectedItem("NONE");
		}
		maxAltitude.setValue(transport.maxAltitude);
		enableCrash.setSelected(transport.enableCrash);
		crashSpeed.setValue(transport.crashSpeed);
		enableFuel.setSelected(transport.enableFuel);
		if (transport.fuelItems == null || transport.fuelItems.isEmpty()) {
			transport.fuelItems = new ArrayList<>();
			if (transport.fuelItem != null && !transport.fuelItem.isEmpty()) {
				transport.fuelItems.add(new Transport.FuelEntry(transport.fuelItem, 250.0));
			}
		}
		fuelItemsEditor.setFuelEntries(transport.fuelItems);
		fuelConsumption.setValue(transport.fuelConsumption);
		fuelCapacity.setValue(transport.fuelCapacity);
		spinParts.setText(transport.spinParts);
		spinSpeed.setValue(transport.spinSpeed);

		// Phase 2: Controls & HUD
		hudType.setSelectedItem(transport.hudType != null && !transport.hudType.isEmpty() ? transport.hudType : "ACTIONBAR");
		engineToggleKey.setSelectedItem(
			transport.engineToggleKey != null && !transport.engineToggleKey.isEmpty() ? transport.engineToggleKey : "F");
		dismountKey.setSelectedItem(
			transport.dismountKey != null && !transport.dismountKey.isEmpty() ? transport.dismountKey : "Q");
		showEngineHUD.setSelected(transport.showEngineHUD);
		showFuelHUD.setSelected(transport.showFuelHUD);
		showThrottleHUD.setSelected(transport.showThrottleHUD);
		showHints.setSelected(transport.showHints);
		overlayBoundTo.setEntry(transport.overlayBoundTo != null ? transport.overlayBoundTo : "");
		updateHUDCheckboxesState();

		// Phase 2: Physics
		accelerationRate.setValue(transport.accelerationRate > 0 ? transport.accelerationRate : 0.04);
		brakeFactor.setValue(transport.brakeFactor > 0 ? transport.brakeFactor : 0.06);
		stallSpeed.setValue(transport.stallSpeed > 0 ? transport.stallSpeed : 0.15);
		inertiaFactor.setValue(transport.inertiaFactor > 0 ? transport.inertiaFactor : 0.98);

		// Phase 2: Crash
		explosionRadius.setValue(transport.explosionRadius > 0 ? (double) transport.explosionRadius : 2.0);
		crashDamageToPlayer.setSelected(transport.crashDamageToPlayer);
		crashDropItems.setSelected(transport.crashDropItems);
		explosionRadius.setEnabled(transport.enableCrash);
		crashDamageToPlayer.setEnabled(transport.enableCrash);
		crashDropItems.setEnabled(transport.enableCrash);

		updatePreviewModel();
		updatePreviewOffset();
		updateFlightControlsState();

		if (transport.hudElements == null || transport.hudElements.isEmpty()) {
			transport.setWorkspace(mcreator.getWorkspace());
		}
		hudEditorPanel.loadElements(transport.hudElements);
	}

	private void updateHUDCheckboxesState() {
		String selectedType = (String) hudType.getSelectedItem();
		boolean isActionbar = "ACTIONBAR".equals(selectedType);
		boolean isOverlay = "OVERLAY".equals(selectedType);

		showEngineHUD.setEnabled(isActionbar);
		showFuelHUD.setEnabled(isActionbar);
		showThrottleHUD.setEnabled(isActionbar);
		showHints.setEnabled(isActionbar);

		overlayBoundTo.setEnabled(isOverlay);

		if (hudEditorPanel != null) {
			hudEditorPanel.refreshOverlayState();
		}
	}

	private void updateFlightControlsState() {
		boolean isAir = "AIR".equals(transportType.getSelectedItem());
		flightMode.setEnabled(isAir);
		if (!isAir) {
			flightMode.setSelectedItem("NONE");
		}
		maxAltitude.setEnabled(isAir);

		boolean isPlane = "PLANE".equals(flightMode.getSelectedItem());
		stallSpeed.setEnabled(isPlane);
	}

	@Override public Transport getElementFromGUI() {
		Transport transport = new Transport(modElement);
		transport.mobName = mobName.getText();
		transport.modelName = ((Model) Objects.requireNonNull(transportModel.getSelectedItem())).getReadableName();
		transport.texture = transportTexture.getTextureName();
		transport.itemTexture = itemTexture.getTextureName();
		transport.transportType = (String) transportType.getSelectedItem();

		transport.speed = ((Number) speed.getValue()).doubleValue();
		transport.strafeSpeed = ((Number) strafeSpeed.getValue()).doubleValue();
		transport.steeringSpeed = ((Number) steeringSpeed.getValue()).doubleValue();
		transport.jumpForce = ((Number) jumpForce.getValue()).doubleValue();

		transport.seatOffsetX = ((Number) seatOffsetX.getValue()).doubleValue();
		transport.seatOffsetY = ((Number) seatOffsetY.getValue()).doubleValue();
		transport.seatOffsetZ = ((Number) seatOffsetZ.getValue()).doubleValue();
		transport.seatYaw = ((Number) seatYaw.getValue()).doubleValue();
		transport.modelYawOffset = ((Number) modelYawOffset.getValue()).doubleValue();

		transport.hasSpawnEgg = hasSpawnEgg.isSelected();
		transport.spawnEggBaseColor = spawnEggBaseColor.getColor();
		transport.spawnEggDotColor = spawnEggDotColor.getColor();
		transport.creativeTabs = creativeTabs.getListElements();

		transport.maxHealth = ((Number) maxHealth.getValue()).doubleValue();
		transport.planeMechanics = "PLANE".equals(flightMode.getSelectedItem());
		transport.helicopterMechanics = "HELICOPTER".equals(flightMode.getSelectedItem());
		transport.maxAltitude = ((Number) maxAltitude.getValue()).doubleValue();
		transport.enableCrash = enableCrash.isSelected();
		transport.crashSpeed = ((Number) crashSpeed.getValue()).doubleValue();
		transport.enableFuel = enableFuel.isSelected();
		transport.fuelItems = fuelItemsEditor.getFuelEntries();
		transport.fuelItem = !transport.fuelItems.isEmpty() ? transport.fuelItems.get(0).item : null;
		transport.fuelConsumption = ((Number) fuelConsumption.getValue()).doubleValue();
		transport.fuelCapacity = ((Number) fuelCapacity.getValue()).doubleValue();
		transport.spinParts = spinParts.getText();
		transport.spinSpeed = ((Number) spinSpeed.getValue()).doubleValue();

		// Phase 2: Controls & HUD
		transport.hudType         = (String) hudType.getSelectedItem();
		transport.engineToggleKey = (String) engineToggleKey.getSelectedItem();
		transport.dismountKey     = (String) dismountKey.getSelectedItem();
		transport.showEngineHUD   = showEngineHUD.isSelected();
		transport.showFuelHUD     = showFuelHUD.isSelected();
		transport.showThrottleHUD = showThrottleHUD.isSelected();
		transport.showHints       = showHints.isSelected();
		transport.overlayBoundTo  = overlayBoundTo.getEntry();

		// Phase 2: Physics
		transport.accelerationRate = ((Number) accelerationRate.getValue()).doubleValue();
		transport.brakeFactor      = ((Number) brakeFactor.getValue()).doubleValue();
		transport.stallSpeed       = ((Number) stallSpeed.getValue()).doubleValue();
		transport.inertiaFactor    = ((Number) inertiaFactor.getValue()).doubleValue();

		// Phase 2: Crash
		transport.explosionRadius      = ((Number) explosionRadius.getValue()).floatValue();
		transport.crashDamageToPlayer  = crashDamageToPlayer.isSelected();
		transport.crashDropItems       = crashDropItems.isSelected();

		if (hudEditorPanel != null) {
			transport.hudElements = hudEditorPanel.getElements();
			if ("CUSTOM".equals(transport.hudType)) {
				transport.showEngineHUD = false;
				transport.showFuelHUD = false;
				transport.showThrottleHUD = false;
				transport.showHints = false;
				for (Transport.HudElement el : transport.hudElements) {
					if ("el_engine".equals(el.id)) transport.showEngineHUD = true;
					if ("el_fuel".equals(el.id)) transport.showFuelHUD = true;
					if ("el_throttle".equals(el.id)) transport.showThrottleHUD = true;
					if ("el_hints".equals(el.id)) transport.showHints = true;
				}
			}
		}

		return transport;
	}

	@Nullable @Override public URI contextURL() throws URISyntaxException {
		return new URI("https://mcreator.net/wiki");
	}
}