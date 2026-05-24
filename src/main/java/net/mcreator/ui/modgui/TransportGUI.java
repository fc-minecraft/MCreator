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

	public TransportGUI(MCreator mcreator, @Nonnull ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.previewPanel = new ModelPreviewPanel();
		this.fuelItemsEditor = new FuelItemsEditorView();
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
		JPanel visualsPanel = new JPanel(new GridLayout(8, 2, 5, 5));
		visualsPanel.setOpaque(false);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/name"), L10N.label("elementgui.transport.name")));
		visualsPanel.add(mobName);

		visualsPanel.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/model"), L10N.label("elementgui.transport.model")));
		visualsPanel.add(transportModel);

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

		JPanel seatControls = new JPanel(new GridLayout(3, 2, 5, 5));
		seatControls.setOpaque(false);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_x"), L10N.label("elementgui.transport.seat_offset_x")));
		seatControls.add(seatOffsetX);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_y"), L10N.label("elementgui.transport.seat_offset_y")));
		seatControls.add(seatOffsetY);
		seatControls.add(HelpUtils.wrapWithHelpButton(this.withEntry("transport/seat_offset_z"), L10N.label("elementgui.transport.seat_offset_z")));
		seatControls.add(seatOffsetZ);

		seatPanel.add(seatControls, BorderLayout.NORTH);
		seatPanel.add(previewPanel, BorderLayout.CENTER);
		seatPanel.add(L10N.label("elementgui.transport.preview_help"), BorderLayout.SOUTH);

		// Add change listeners to spinners to update preview seat offset
		seatOffsetX.addChangeListener(e -> updatePreviewOffset());
		seatOffsetY.addChangeListener(e -> updatePreviewOffset());
		seatOffsetZ.addChangeListener(e -> updatePreviewOffset());

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

		keybindGrid.add(showEngineHUD);   keybindGrid.add(new JLabel());
		keybindGrid.add(showFuelHUD);     keybindGrid.add(new JLabel());
		keybindGrid.add(showThrottleHUD); keybindGrid.add(new JLabel());
		keybindGrid.add(showHints);       keybindGrid.add(new JLabel());

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
		previewPanel.setSeatOffset(x, y, z);
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
		engineToggleKey.setSelectedItem(
			transport.engineToggleKey != null && !transport.engineToggleKey.isEmpty() ? transport.engineToggleKey : "F");
		dismountKey.setSelectedItem(
			transport.dismountKey != null && !transport.dismountKey.isEmpty() ? transport.dismountKey : "Q");
		showEngineHUD.setSelected(transport.showEngineHUD);
		showFuelHUD.setSelected(transport.showFuelHUD);
		showThrottleHUD.setSelected(transport.showThrottleHUD);
		showHints.setSelected(transport.showHints);

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
		transport.engineToggleKey = (String) engineToggleKey.getSelectedItem();
		transport.dismountKey     = (String) dismountKey.getSelectedItem();
		transport.showEngineHUD   = showEngineHUD.isSelected();
		transport.showFuelHUD     = showFuelHUD.isSelected();
		transport.showThrottleHUD = showThrottleHUD.isSelected();
		transport.showHints       = showHints.isSelected();

		// Phase 2: Physics
		transport.accelerationRate = ((Number) accelerationRate.getValue()).doubleValue();
		transport.brakeFactor      = ((Number) brakeFactor.getValue()).doubleValue();
		transport.stallSpeed       = ((Number) stallSpeed.getValue()).doubleValue();
		transport.inertiaFactor    = ((Number) inertiaFactor.getValue()).doubleValue();

		// Phase 2: Crash
		transport.explosionRadius      = ((Number) explosionRadius.getValue()).floatValue();
		transport.crashDamageToPlayer  = crashDamageToPlayer.isSelected();
		transport.crashDropItems       = crashDropItems.isSelected();

		return transport;
	}

	@Nullable @Override public URI contextURL() throws URISyntaxException {
		return new URI("https://mcreator.net/wiki");
	}

	/**
	 * Beautiful interactive 3D model preview panel for seat offset placement.
	 */
	private class ModelPreviewPanel extends JPanel {

		private class Box3D {
			float[][] corners = new float[8][3];

			Box3D(float[][] worldCorners) {
				this.corners = worldCorners;
			}

			Box3D(float x, float y, float z, float w, float h, float d) {
				corners[0] = new float[]{x, y, z};
				corners[1] = new float[]{x + w, y, z};
				corners[2] = new float[]{x + w, y + h, z};
				corners[3] = new float[]{x, y + h, z};
				corners[4] = new float[]{x, y, z + d};
				corners[5] = new float[]{x + w, y, z + d};
				corners[6] = new float[]{x + w, y + h, z + d};
				corners[7] = new float[]{x, y + h, z + d};
			}
		}

		private final List<Box3D> boxes = new ArrayList<>();
		private float seatX = 0, seatY = 0, seatZ = 0;

		private float yaw = -0.8f;
		private float pitch = 0.4f;
		private float zoom = 5.0f;

		private Point lastMousePoint;

		ModelPreviewPanel() {
			setBackground(new Color(30, 30, 30));
			setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
			setPreferredSize(new Dimension(400, 300));
			setFocusable(true);
			setRequestFocusEnabled(true);

			MouseAdapter mouseAdapter = new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					requestFocusInWindow();
					lastMousePoint = e.getPoint();
				}

				@Override public void mouseDragged(MouseEvent e) {
					if (lastMousePoint != null) {
						int dx = e.getX() - lastMousePoint.x;
						int dy = e.getY() - lastMousePoint.y;
						yaw -= dx * 0.01f;
						pitch += dy * 0.01f;
						pitch = Math.max(-1.4f, Math.min(1.4f, pitch));
						lastMousePoint = e.getPoint();
						repaint();
					}
				}

				@Override public void mouseWheelMoved(MouseWheelEvent e) {
					zoom *= (e.getWheelRotation() < 0) ? 1.1f : 0.9f;
					zoom = Math.max(1.0f, Math.min(50.0f, zoom));
					repaint();
				}
			};

			addMouseListener(mouseAdapter);
			addMouseMotionListener(mouseAdapter);
			addMouseWheelListener(mouseAdapter);

			// Setup key bindings for keyboard control of seat offset
			InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			ActionMap am = getActionMap();

			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "move_forward");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "move_backward");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "move_left");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "move_right");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "move_up");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, KeyEvent.SHIFT_DOWN_MASK), "move_down");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "move_down");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "move_down");

			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "move_forward");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move_backward");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "move_left");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "move_right");

			am.put("move_forward", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(0, 0, -0.05); }
			});
			am.put("move_backward", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(0, 0, 0.05); }
			});
			am.put("move_left", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(0.05, 0, 0); }
			});
			am.put("move_right", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(-0.05, 0, 0); }
			});
			am.put("move_up", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(0, 0.05, 0); }
			});
			am.put("move_down", new AbstractAction() {
				@Override public void actionPerformed(ActionEvent e) { nudgeSeat(0, -0.05, 0); }
			});
		}

		private void nudgeSeat(double dx, double dy, double dz) {
			double newX = ((Number) seatOffsetX.getValue()).doubleValue() + dx;
			double newY = ((Number) seatOffsetY.getValue()).doubleValue() + dy;
			double newZ = ((Number) seatOffsetZ.getValue()).doubleValue() + dz;
			seatOffsetX.setValue(Math.round(newX * 100.0) / 100.0);
			seatOffsetY.setValue(Math.round(newY * 100.0) / 100.0);
			seatOffsetZ.setValue(Math.round(newZ * 100.0) / 100.0);
		}

		void setSeatOffset(double x, double y, double z) {
			this.seatX = (float) x;
			this.seatY = (float) y;
			this.seatZ = (float) z;
			repaint();
		}

		void loadModel(@Nullable Model model) {
			boxes.clear();
			if (model == null || model.getReadableName().equals("Biped") || model.getReadableName().equals("Zombie")) {
				// Load default Biped wireframe
				boxes.add(new Box3D(-4, -8, -4, 8, 8, 8)); // Head
				boxes.add(new Box3D(-4, 0, -2, 8, 12, 4)); // Torso
				boxes.add(new Box3D(-8, 0, -2, 4, 12, 4)); // Right Arm
				boxes.add(new Box3D(4, 0, -2, 4, 12, 4));  // Left Arm
				boxes.add(new Box3D(-4, 12, -2, 4, 12, 4)); // Right Leg
				boxes.add(new Box3D(0, 12, -2, 4, 12, 4));  // Left Leg
			} else {
				try {
					File file = model.getFile();
					if (file != null && file.isFile()) {
						if (file.getName().endsWith(".java")) {
							String content = new String(Files.readAllBytes(file.toPath()));
							parseJavaModel(content);
						} else if (file.getName().endsWith(".json")) {
							String content = new String(Files.readAllBytes(file.toPath()));
							parseJsonModel(content);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (boxes.isEmpty()) {
					// Fallback box
					boxes.add(new Box3D(-8, -8, -8, 16, 16, 16));
				}
			}
			repaint();
		}

		private final Map<String, float[]> boneOffsets = new HashMap<>();
		private final Map<String, float[]> boneRotations = new HashMap<>();
		private final Map<String, String> boneParents = new HashMap<>();

		private void parseJavaModel(String content) {
			boneOffsets.clear();
			boneRotations.clear();
			boneParents.clear();

			// ----------------------------------------------------------------
			// Pass 1: collect all bone (PartDefinition) declarations.
			// Handles addOrReplaceChild(...) with PartPose.ZERO / offset / rotation / offsetAndRotation.
			// We walk char-by-char to find "addOrReplaceChild(", then grab the PartPose args robustly.
			// ----------------------------------------------------------------
			Pattern boneDeclPat = Pattern.compile(
				"(?:PartDefinition\\s+)?([a-zA-Z0-9_]+)\\s*=\\s*(?:[a-zA-Z0-9_]+)\\.addOrReplaceChild\\s*\\(");
			Matcher boneDecl = boneDeclPat.matcher(content);
			while (boneDecl.find()) {
				String varName = boneDecl.group(1);
				if (varName.equals("meshdefinition") || varName.equals("partdefinition")) continue;

				// Find parent: the identifier before .addOrReplaceChild
				int eqIdx = content.lastIndexOf('=', boneDecl.start() + varName.length() + 10);
				String beforeEq = content.substring(Math.max(0, boneDecl.start()), boneDecl.end());
				Pattern parentPat = Pattern.compile("=\\s*([a-zA-Z0-9_]+)\\.addOrReplaceChild");
				Matcher parentMatch = parentPat.matcher(beforeEq);
				String parentVar = "partdefinition";
				if (parentMatch.find()) parentVar = parentMatch.group(1);

				// Grab full argument list of addOrReplaceChild(...)
				int argsStart = boneDecl.end();
				int depth = 1;
				int pos = argsStart;
				while (pos < content.length() && depth > 0) {
					char c = content.charAt(pos);
					if (c == '(') depth++;
					else if (c == ')') depth--;
					pos++;
				}
				String fullArgs = content.substring(argsStart, pos - 1);

				// Find PartPose.ZERO / .offset(...) / .rotation(...) / .offsetAndRotation(...)
				// IMPORTANT: offsetAndRotation must be FIRST since "offset" is a prefix of it
				Pattern posePat = Pattern.compile(
					"PartPose\\s*\\.\\s*(ZERO|offsetAndRotation|rotation|offset)\\s*(?:\\(([^)]+)\\))?");
				Matcher poseMatcher = posePat.matcher(fullArgs);
				float ox = 0, oy = 0, oz = 0, rx = 0, ry = 0, rz = 0;
				if (poseMatcher.find()) {
					String method = poseMatcher.group(1);
					String args = poseMatcher.group(2);
					if (args != null) {
						String[] a = args.split(",");
						try {
							if ("offset".equals(method) && a.length >= 3) {
								ox = parseF(a[0]); oy = parseF(a[1]); oz = parseF(a[2]);
							} else if ("rotation".equals(method) && a.length >= 3) {
								rx = parseF(a[0]); ry = parseF(a[1]); rz = parseF(a[2]);
							} else if ("offsetAndRotation".equals(method) && a.length >= 6) {
								ox = parseF(a[0]); oy = parseF(a[1]); oz = parseF(a[2]);
								rx = parseF(a[3]); ry = parseF(a[4]); rz = parseF(a[5]);
							}
						} catch (NumberFormatException ignored) {}
					}
				}
				boneOffsets.put(varName, new float[]{ox, oy, oz});
				boneParents.put(varName, parentVar);
				if (rx != 0 || ry != 0 || rz != 0) {
					boneRotations.put(varName, new float[]{rx, ry, rz});
				}
			}

			// ----------------------------------------------------------------
			// Pass 2: collect all addBox calls.
			// Strategy: for each addBox, find the nearest preceding bone variable assignment.
			// We scan through the "createBodyLayer" method linearly, tracking current bone.
			// ----------------------------------------------------------------
			// Find the body of createBodyLayer
			int methodStart = content.indexOf("createBodyLayer");
			if (methodStart < 0) methodStart = 0;
			// Limit to method body
			int braceStart = content.indexOf('{', methodStart);
			int methodEnd = content.length();
			if (braceStart >= 0) {
				int d = 1;
				int p = braceStart + 1;
				while (p < content.length() && d > 0) {
					char c = content.charAt(p);
					if (c == '{') d++;
					else if (c == '}') d--;
					p++;
				}
				methodEnd = p;
			}
			String methodBody = content.substring(Math.max(0, braceStart), Math.min(content.length(), methodEnd));

			// Walk line by line, track current bone var, emit boxes
			String currentBoneVar = null;
			// Regex: any known bone variable = something (either addOrReplaceChild or addChild etc.)
			Pattern assignPat = Pattern.compile("\\b([a-zA-Z0-9_]+)\\s*=\\s*[a-zA-Z0-9_]+\\.(?:addOrReplaceChild|addChild)");
			Pattern addBoxPat = Pattern.compile(
				"addBox\\(\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*," +
				"\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?");

			String[] statements = methodBody.split(";");
			for (String stmt : statements) {
				stmt = stmt.trim();
				// Update current bone if this statement contains a bone assignment
				Matcher assignMatcher = assignPat.matcher(stmt);
				if (assignMatcher.find()) {
					String v = assignMatcher.group(1);
					if (boneOffsets.containsKey(v)) {
						currentBoneVar = v;
					}
				}

				// Collect all addBox calls on this statement
				if (stmt.contains("addBox")) {
					Matcher boxMatcher = addBoxPat.matcher(stmt);
					while (boxMatcher.find()) {
						try {
							float x = parseF(boxMatcher.group(1));
							float y = parseF(boxMatcher.group(2));
							float z = parseF(boxMatcher.group(3));
							float w = parseF(boxMatcher.group(4));
							float h = parseF(boxMatcher.group(5));
							float d = parseF(boxMatcher.group(6));

							String bone = currentBoneVar;
							float[][] localCorners = {
								{x, y, z}, {x+w, y, z}, {x+w, y+h, z}, {x, y+h, z},
								{x, y, z+d}, {x+w, y, z+d}, {x+w, y+h, z+d}, {x, y+h, z+d}
							};
							float[][] worldCorners = new float[8][3];
							for (int i = 0; i < 8; i++) {
								worldCorners[i] = transformToWorld(localCorners[i], bone);
							}
							boxes.add(new Box3D(worldCorners));
						} catch (NumberFormatException ignored) {}
					}
				}
			}
		}

		private static float parseF(String s) {
			return Float.parseFloat(s.trim().replaceAll("(?i)f$", ""));
		}

		private float[] transformToWorld(float[] pos, String bone) {
			if (bone == null || "partdefinition".equals(bone) || "meshdefinition".equals(bone)) {
				return pos;
			}
			// 1. Rotate pos around (0,0,0) by this bone's rotation (pitch, yaw, roll)
			float[] rot = pos;
			if (boneRotations.containsKey(bone)) {
				float[] r = boneRotations.get(bone); // pitch, yaw, roll
				rot = rotate(pos, r[0], r[1], r[2]);
			}
			// 2. Translate by this bone's offset
			float[] offset = boneOffsets.getOrDefault(bone, new float[]{0, 0, 0});
			float[] translated = new float[] { rot[0] + offset[0], rot[1] + offset[1], rot[2] + offset[2] };

			// 3. Transform by parent bone
			String parent = boneParents.get(bone);
			return transformToWorld(translated, parent);
		}

		private float[] rotate(float[] pos, float pitch, float yaw, float roll) {
			float x = pos[0];
			float y = pos[1];
			float z = pos[2];

			// 1. Roll (Z)
			float cosZ = (float) Math.cos(roll);
			float sinZ = (float) Math.sin(roll);
			float x1 = x * cosZ - y * sinZ;
			float y1 = x * sinZ + y * cosZ;
			float z1 = z;

			// 2. Yaw (Y)
			float cosY = (float) Math.cos(yaw);
			float sinY = (float) Math.sin(yaw);
			float x2 = x1 * cosY + z1 * sinY;
			float y2 = y1;
			float z2 = -x1 * sinY + z1 * cosY;

			// 3. Pitch (X)
			float cosX = (float) Math.cos(pitch);
			float sinX = (float) Math.sin(pitch);
			float x3 = x2;
			float y3 = y2 * cosX - z2 * sinX;
			float z3 = y2 * sinX + z2 * cosX;

			return new float[]{x3, y3, z3};
		}

		private void parseJsonModel(String content) {
			try {
				JsonObject json = JsonParser.parseString(content).getAsJsonObject();
				if (json.has("elements")) {
					JsonArray elements = json.getAsJsonArray("elements");
					for (JsonElement el : elements) {
						JsonObject obj = el.getAsJsonObject();
						JsonArray from = obj.getAsJsonArray("from");
						JsonArray to = obj.getAsJsonArray("to");

						float x1 = from.get(0).getAsFloat();
						float y1 = from.get(1).getAsFloat();
						float z1 = from.get(2).getAsFloat();

						float x2 = to.get(0).getAsFloat();
						float y2 = to.get(1).getAsFloat();
						float z2 = to.get(2).getAsFloat();

						// Translate coordinates to match Java model space (Ground Y=24, centered at X=0, Z=0)
						// JSON blocks typically go from 0 to 16, centered at 8.
						boxes.add(new Box3D(x1 - 8.0f, 24.0f - y2, z1 - 8.0f, x2 - x1, y2 - y1, z2 - z1));
					}
				}
			} catch (Exception ignored) {}
		}

		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			// Draw floor grid at Y=24
			g2.setColor(new Color(60, 60, 60));
			for (int i = -4; i <= 4; i++) {
				draw3DLine(g2, i * 16.0f, 24.0f, -64.0f, i * 16.0f, 24.0f, 64.0f, w, h);
				draw3DLine(g2, -64.0f, 24.0f, i * 16.0f, 64.0f, 24.0f, i * 16.0f, w, h);
			}

			// Draw model boxes
			g2.setColor(Theme.current().getForegroundColor());
			for (Box3D box : boxes) {
				draw3DBox(g2, box, w, h);
			}

			// Draw Seat (Seat is in blocks, model is in 1/16 blocks. Y in Minecraft goes UP, in Java model Y goes DOWN from 24)
			float sx = -seatX * 16.0f;
			float sy = 24.0f - seatY * 16.0f;
			float sz = -seatZ * 16.0f;

			// Draw seat indicator (Red cross)
			g2.setColor(Color.RED);
			g2.setStroke(new BasicStroke(2));
			draw3DLine(g2, sx - 4, sy, sz, sx + 4, sy, sz, w, h);
			draw3DLine(g2, sx, sy - 4, sz, sx, sy + 4, sz, w, h);
			draw3DLine(g2, sx, sy, sz - 4, sx, sy, sz + 4, w, h);

			// Draw wireframe sitting player (Cyan)
			g2.setColor(new Color(0, 180, 220));
			g2.setStroke(new BasicStroke(1));
			// Head
			draw3DBox(g2, new Box3D(sx - 2, sy - 14, sz - 2, 4, 4, 4), w, h);
			// Torso
			draw3DBox(g2, new Box3D(sx - 2.5f, sy - 10, sz - 1.5f, 5, 6, 3), w, h);
			// Thighs (extending forward, along Z)
			draw3DBox(g2, new Box3D(sx - 2.5f, sy - 4, sz - 6, 5, 2, 6), w, h);
			// Shins (extending down)
			draw3DBox(g2, new Box3D(sx - 2.5f, sy - 2, sz - 6, 5, 6, 2), w, h);
		}

		private void draw3DBox(Graphics2D g2, Box3D box, int w, int h) {
			Point p000 = project(box.corners[0][0], box.corners[0][1], box.corners[0][2], w, h);
			Point p100 = project(box.corners[1][0], box.corners[1][1], box.corners[1][2], w, h);
			Point p110 = project(box.corners[2][0], box.corners[2][1], box.corners[2][2], w, h);
			Point p010 = project(box.corners[3][0], box.corners[3][1], box.corners[3][2], w, h);

			Point p001 = project(box.corners[4][0], box.corners[4][1], box.corners[4][2], w, h);
			Point p101 = project(box.corners[5][0], box.corners[5][1], box.corners[5][2], w, h);
			Point p111 = project(box.corners[6][0], box.corners[6][1], box.corners[6][2], w, h);
			Point p011 = project(box.corners[7][0], box.corners[7][1], box.corners[7][2], w, h);

			// Translucent face fill to solve depth visual issues
			g2.setColor(new Color(150, 150, 150, 20));
			fillFace(g2, p000, p100, p110, p010);
			fillFace(g2, p001, p101, p111, p011);
			fillFace(g2, p000, p001, p011, p010);
			fillFace(g2, p100, p101, p111, p110);
			fillFace(g2, p000, p100, p101, p001);
			fillFace(g2, p010, p110, p111, p011);

			// Edge outlines
			g2.setColor(Theme.current().getForegroundColor());
			drawEdge(g2, p000, p100);
			drawEdge(g2, p100, p110);
			drawEdge(g2, p110, p010);
			drawEdge(g2, p010, p000);

			drawEdge(g2, p001, p101);
			drawEdge(g2, p101, p111);
			drawEdge(g2, p111, p011);
			drawEdge(g2, p011, p001);

			drawEdge(g2, p000, p001);
			drawEdge(g2, p100, p101);
			drawEdge(g2, p110, p111);
			drawEdge(g2, p010, p011);
		}

		private void fillFace(Graphics2D g2, Point a, Point b, Point c, Point d) {
			if (a != null && b != null && c != null && d != null) {
				int[] xs = { a.x, b.x, c.x, d.x };
				int[] ys = { a.y, b.y, c.y, d.y };
				g2.fillPolygon(xs, ys, 4);
			}
		}

		private void drawEdge(Graphics2D g2, Point a, Point b) {
			if (a != null && b != null) {
				g2.drawLine(a.x, a.y, b.x, b.y);
			}
		}

		private void draw3DLine(Graphics2D g2, float x1, float y1, float z1, float x2, float y2, float z2, int w, int h) {
			Point p1 = project(x1, y1, z1, w, h);
			Point p2 = project(x2, y2, z2, w, h);
			if (p1 != null && p2 != null) {
				g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}

		private Point project(float x, float y, float z, int w, int h) {
			// Center around (0, 12, 0)
			float cx = x;
			float cy = y - 12.0f;
			float cz = z;

			// Rotate around Y axis (Yaw)
			float rotX = (float) (cx * Math.cos(yaw) - cz * Math.sin(yaw));
			float rotZ = (float) (cx * Math.sin(yaw) + cz * Math.cos(yaw));

			// Rotate around X axis (Pitch)
			float rotY = (float) (cy * Math.cos(pitch) - rotZ * Math.sin(pitch));
			float projZ = (float) (cy * Math.sin(pitch) + rotZ * Math.cos(pitch));

			// Projection factors
			float distance = 120.0f;
			float factor = distance / (distance + projZ);

			int screenX = (int) (w / 2 + rotX * zoom * factor);
			int screenY = (int) (h / 2 + rotY * zoom * factor);

			return new Point(screenX, screenY);
		}
	}

	private class FuelItemsEditorView extends JPanel implements net.mcreator.ui.validation.IValidable {
		private final JPanel listPanel = new JPanel();
		private final JButton addRowButton = new JButton(L10N.t("elementgui.transport.add_fuel_item"));
		private final List<FuelRow> rows = new ArrayList<>();
		private net.mcreator.ui.validation.Validator validator;
		private ValidationResult currentValidationResult;

		public FuelItemsEditorView() {
			setLayout(new BorderLayout(5, 5));
			setOpaque(false);

			listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
			listPanel.setOpaque(false);

			JScrollPane scrollPane = new JScrollPane(listPanel);
			scrollPane.setPreferredSize(new Dimension(380, 200));
			scrollPane.setOpaque(false);
			scrollPane.getViewport().setOpaque(false);

			add(scrollPane, BorderLayout.CENTER);

			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			bottomPanel.setOpaque(false);
			bottomPanel.add(addRowButton);
			add(bottomPanel, BorderLayout.SOUTH);

			addRowButton.addActionListener(e -> {
				addFuelRow(null, 250.0);
				revalidate();
				repaint();
			});
		}

		public void setFuelEntries(List<Transport.FuelEntry> entries) {
			listPanel.removeAll();
			rows.clear();
			if (entries != null) {
				for (Transport.FuelEntry entry : entries) {
					addFuelRow(entry.item, entry.fuelAmount);
				}
			}
			revalidate();
			repaint();
		}

		public List<Transport.FuelEntry> getFuelEntries() {
			List<Transport.FuelEntry> entries = new ArrayList<>();
			for (FuelRow row : rows) {
				MItemBlock item = row.itemHolder.getBlock();
				if (item != null && !item.isAir()) {
					entries.add(new Transport.FuelEntry(item, ((Number) row.valueSpinner.getValue()).doubleValue()));
				}
			}
			return entries;
		}

		private void addFuelRow(MItemBlock item, double val) {
			FuelRow row = new FuelRow(item, val);
			rows.add(row);
			listPanel.add(row);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			addRowButton.setEnabled(enabled);
			for (FuelRow row : rows) {
				row.itemHolder.setEnabled(enabled);
				row.valueSpinner.setEnabled(enabled);
				row.deleteButton.setEnabled(enabled);
			}
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (currentValidationResult != null && currentValidationResult.type() != ValidationResult.Type.PASSED) {
				g.setColor(currentValidationResult.type().getColor());
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
		}

		@Override
		public ValidationResult getValidationStatus() {
			ValidationResult res = validator == null ? null : validator.validateIfEnabled(this);
			this.currentValidationResult = res;
			repaint();
			return res;
		}

		@Override
		public void setValidator(net.mcreator.ui.validation.Validator validator) {
			this.validator = validator;
		}

		@Override
		public net.mcreator.ui.validation.Validator getValidator() {
			return validator;
		}

		private class FuelRow extends JPanel {
			final MCItemHolder itemHolder;
			final JSpinner valueSpinner;
			final JButton deleteButton;

			FuelRow(MItemBlock item, double val) {
				setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
				setOpaque(false);

				itemHolder = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
				itemHolder.setPreferredSize(new Dimension(150, 30));
				if (item != null) {
					itemHolder.setBlock(item);
				}

				valueSpinner = new JSpinner(new SpinnerNumberModel(val, 1.0, 100000.0, 50.0));
				valueSpinner.setPreferredSize(new Dimension(80, 28));

				deleteButton = new JButton("X");
				deleteButton.setMargin(new Insets(2, 6, 2, 6));

				add(itemHolder);
				add(new JLabel("->"));
				add(valueSpinner);
				add(new JLabel(L10N.t("elementgui.transport.units_short", "ед.")));
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
}
