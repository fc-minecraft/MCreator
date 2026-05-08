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

import net.mcreator.element.parts.MItemBlock;
import net.mcreator.element.types.Structure;
import net.mcreator.io.FileIO;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.minecraft.RegistryNameFixer;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.component.JMinMaxSpinner;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.component.TranslatedComboBox;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.minecraft.BiomeListField;
import net.mcreator.ui.minecraft.MCItemListField;
import net.mcreator.ui.minecraft.jigsaw.JJigsawPoolsList;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.validators.CompoundValidator;
import net.mcreator.ui.validation.ValidationGroup;
import net.mcreator.ui.validation.validators.ItemListFieldSingleTagValidator;
import net.mcreator.ui.validation.validators.ItemListFieldValidator;
import net.mcreator.util.FilenameUtilsPatched;
import net.mcreator.workspace.elements.ModElement;

import java.util.Map;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.HashMap;

public class StructureGUI extends ModElementGUI<Structure> {

	private MCItemListField ignoreBlocks;

	private final JComboBox<String> surfaceDetectionType = new TranslatedComboBox(
			Map.entry("WORLD_SURFACE_WG", "elementgui.structuregen.surface_detection_type.world_surface_wg"),
			Map.entry("WORLD_SURFACE", "elementgui.structuregen.surface_detection_type.world_surface"),
			Map.entry("OCEAN_FLOOR_WG", "elementgui.structuregen.surface_detection_type.ocean_floor_wg"),
			Map.entry("OCEAN_FLOOR", "elementgui.structuregen.surface_detection_type.ocean_floor"),
			Map.entry("MOTION_BLOCKING", "elementgui.structuregen.surface_detection_type.motion_blocking"),
			Map.entry("MOTION_BLOCKING_NO_LEAVES", "elementgui.structuregen.surface_detection_type.motion_blocking_no_leaves")
	);



	private JComboBox<String> terrainAdaptation;
	private JComboBox<String> projection;

	private BiomeListField restrictionBiomes;

	private final JMinMaxSpinner separation_spacing = new JMinMaxSpinner(2, 5, 0, 1000000, 1,
			L10N.t("elementgui.structuregen.separation"), L10N.t("elementgui.structuregen.spacing"));

	private final JSpinner frequency = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.05));
	private final JComboBox<String> spreadType = new TranslatedComboBox(
			Map.entry("linear", "elementgui.structuregen.spread_type.linear"),
			Map.entry("triangular", "elementgui.structuregen.spread_type.triangular")
	);

	private final JCheckBox useStartHeight = L10N.checkbox("elementgui.common.enable");
	private final JComboBox<String> startHeightProviderType = new TranslatedComboBox(
			Map.entry("UNIFORM", "elementgui.structuregen.start_height_provider_type.uniform"),
			Map.entry("BIASED_TO_BOTTOM", "elementgui.structuregen.start_height_provider_type.biased_to_bottom"),
			Map.entry("VERY_BIASED_TO_BOTTOM", "elementgui.structuregen.start_height_provider_type.very_biased_to_bottom"),
			Map.entry("TRAPEZOID", "elementgui.structuregen.start_height_provider_type.trapezoid")
	);

	private final JMinMaxSpinner startHeightRange = new JMinMaxSpinner(0, 128, -1024, 1024, 1);

	private SearchableComboBox<String> structureSelector;

	private final JComboBox<String> generationStep = new TranslatedComboBox(
			Stream.of(ElementUtil.getDataListAsStringArray("generationsteps"))
					.map(step -> Map.entry(step, "datalist.genstep." + step.toLowerCase(Locale.ENGLISH)))
					.toArray(Map.Entry[]::new));

	private final JSpinner size = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
	private final JSpinner maxDistanceFromCenter = new JSpinner(new SpinnerNumberModel(64, 1, 128, 1));
	private JJigsawPoolsList jigsaw;

	private final ValidationGroup page1group = new ValidationGroup();

	private static final Map<String, File> externalStructures = new HashMap<>();

	public StructureGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.initGUI();
		super.finalizeGUI();
	}

	@Override protected void initGUI() {
		structureSelector = new SearchableComboBox<>(
				mcreator.getFolderManager().getStructureList().toArray(String[]::new));
		restrictionBiomes = new BiomeListField(mcreator, true);
		ignoreBlocks = new MCItemListField(mcreator, ElementUtil::loadBlocks);
		jigsaw = new JJigsawPoolsList(mcreator, this, modElement);

		projection = new TranslatedComboBox(
				Map.entry("rigid", "elementgui.structuregen.projection.rigid"),
				Map.entry("terrain_matching", "elementgui.structuregen.projection.terrain_matching")
		);

		terrainAdaptation = new TranslatedComboBox(
				Map.entry("none", "elementgui.structuregen.terrain_adaptation.none"),
				Map.entry("beard_thin", "elementgui.structuregen.terrain_adaptation.beard_thin"),
				Map.entry("beard_box", "elementgui.structuregen.terrain_adaptation.beard_box"),
				Map.entry("bury", "elementgui.structuregen.terrain_adaptation.bury"),
				Map.entry("encapsulate", "elementgui.structuregen.terrain_adaptation.encapsulate")
		);

		structureSelector.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				scanExternalStructures();
			}

			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		structureSelector.addActionListener(e -> {
			String selected = structureSelector.getSelectedItem();
			if (selected != null && externalStructures.containsKey(selected)) {
				File externalFile = externalStructures.get(selected);
				File localFile = new File(mcreator.getFolderManager().getStructuresDir(), selected + ".nbt");
				if (!localFile.exists()) {
					FileIO.copyFile(externalFile, localFile);
				}
			}
		});

		terrainAdaptation.addActionListener(e -> {
			int max = "none".equals(terrainAdaptation.getSelectedItem()) ? 128 : 116;
			SpinnerNumberModel spinnerModel = (SpinnerNumberModel) maxDistanceFromCenter.getModel();
			spinnerModel.setMaximum(max);
			spinnerModel.setValue(Math.min((int) spinnerModel.getValue(), max));
		});

		JPanel pane5 = new JPanel(new BorderLayout(3, 3));
		JPanel pane7 = new JPanel(new BorderLayout(2, 2));

		ComponentUtils.deriveFont(structureSelector, 16);

		if (!isEditingMode()) {
			generationStep.setSelectedItem("SURFACE_STRUCTURES");
			projection.setSelectedItem("rigid");
			ignoreBlocks.setListElements(List.of(new MItemBlock(modElement.getWorkspace(), "Blocks.STRUCTURE_BLOCK")));
		}

		JPanel params = new JPanel(new GridBagLayout());
		params.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(2, 5, 2, 5);
		gbc.anchor = GridBagConstraints.WEST;

		int row = 0;

		JButton importnbt = new JButton(UIRES.get("18px.add"));
		importnbt.setToolTipText(L10N.t("elementgui.structuregen.import_tooltip"));
		importnbt.setOpaque(false);
		importnbt.addActionListener(e -> {
			File sch = FileDialogs.getOpenDialog(mcreator, new String[] { ".nbt" });
			if (sch != null) {
				String strname = RegistryNameFixer.fix(sch.getName().toLowerCase(Locale.ENGLISH));
				FileIO.copyFile(sch, new File(mcreator.getFolderManager().getStructuresDir(), strname));
				structureSelector.removeAllItems();
				mcreator.getFolderManager().getStructureList().forEach(structureSelector::addItem);
				structureSelector.setSelectedItem(FilenameUtilsPatched.removeExtension(strname));
			}
		});

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/biomes_to_spawn"),
				L10N.label("elementgui.structuregen.biomes_to_spawn")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(restrictionBiomes, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/separation_spacing"),
				L10N.label("elementgui.structuregen.separation_spacing")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(separation_spacing, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/frequency"),
				L10N.label("elementgui.structuregen.frequency")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(frequency, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/spread_type"),
				L10N.label("elementgui.structuregen.spread_type")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(spreadType, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/generation_step"),
				L10N.label("elementgui.structuregen.generation_stage")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(generationStep, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/ground_detection"),
				L10N.label("elementgui.structuregen.surface_detection_type")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(surfaceDetectionType, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/start_height"),
				L10N.label("elementgui.structuregen.start_height")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(PanelUtils.westAndCenterElement(useStartHeight,
				PanelUtils.westAndCenterElement(startHeightProviderType, startHeightRange, 5, 5), 5, 5), gbc);

		useStartHeight.addActionListener(e -> updateEnabledFields());

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/terrain_adaptation"),
				L10N.label("elementgui.structuregen.terrain_adaptation")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(terrainAdaptation, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/structure"),
				L10N.label("elementgui.structuregen.select_tooltip")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		JButton refreshStructures = new JButton(UIRES.get("18px.add_new"));
		refreshStructures.setToolTipText(L10N.t("elementgui.common.refresh"));
		refreshStructures.setOpaque(false);
		refreshStructures.addActionListener(e -> scanExternalStructures());
		params.add(PanelUtils.centerAndEastElement(structureSelector, PanelUtils.join(refreshStructures, importnbt)), gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/projection"),
				L10N.label("elementgui.structuregen.projection")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(projection, gbc);

		gbc.gridy = row++;
		gbc.gridx = 0; gbc.weightx = 0;
		params.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/ignore_blocks"),
				L10N.label("elementgui.structuregen.ignore_blocks")), gbc);
		gbc.gridx = 1; gbc.weightx = 1;
		params.add(ignoreBlocks, gbc);

		pane5.setOpaque(false);

		pane5.add("Center", PanelUtils.totalCenterInPanel(params));

		JPanel jigsawSize = new JPanel(new GridBagLayout());
		jigsawSize.setOpaque(false);

		GridBagConstraints gbcJ = new GridBagConstraints();
		gbcJ.fill = GridBagConstraints.HORIZONTAL;
		gbcJ.insets = new Insets(2, 5, 2, 5);
		gbcJ.anchor = GridBagConstraints.WEST;

		gbcJ.gridy = 0; gbcJ.gridx = 0; gbcJ.weightx = 0;
		jigsawSize.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/jigsaw_size"),
				L10N.label("elementgui.structuregen.jigsaw_size")), gbcJ);
		gbcJ.gridx = 1; gbcJ.weightx = 1;
		jigsawSize.add(size, gbcJ);

		gbcJ.gridy = 1; gbcJ.gridx = 0; gbcJ.weightx = 0;
		jigsawSize.add(HelpUtils.wrapWithHelpButton(this.withEntry("structure/jigsaw_max_distance_from_center"),
				L10N.label("elementgui.structuregen.jigsaw_max_distance_from_center")), gbcJ);
		gbcJ.gridx = 1; gbcJ.weightx = 1;
		jigsawSize.add(maxDistanceFromCenter, gbcJ);

		jigsawSize.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

		pane7.setOpaque(false);

		pane7.add("North", PanelUtils.join(FlowLayout.LEFT, 0, 0, jigsawSize));

		JComponent jigsawPoolsListComp = PanelUtils.northAndCenterElement(
				HelpUtils.wrapWithHelpButton(this.withEntry("structure/jigsaw_pools"),
						L10N.label("elementgui.structuregen.jigsaw_pools")), jigsaw);
		jigsawPoolsListComp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pane7.add("Center", jigsawPoolsListComp);

		restrictionBiomes.setValidator(new CompoundValidator(
				new ItemListFieldValidator(restrictionBiomes, L10N.t("elementgui.structuregen.error_select_biomes")),
				new ItemListFieldSingleTagValidator(restrictionBiomes)));
		page1group.addValidationElement(restrictionBiomes);

		structureSelector.setValidator(() -> {
			if (structureSelector.getSelectedItem() == null || structureSelector.getSelectedItem().isEmpty())
				return new ValidationResult(ValidationResult.Type.ERROR,
						L10N.t("elementgui.structuregen.error_select_structure_spawn"));
			return ValidationResult.PASSED;
		});
		page1group.addValidationElement(structureSelector);

		addPage(L10N.t("elementgui.common.page_properties"), pane5).validate(page1group);
		addPage(L10N.t("elementgui.structuregen.page_jigsaw"), pane7, false).lazyValidate(jigsaw::getValidationResult);

		updateEnabledFields();
	}

	private void updateEnabledFields() {
		surfaceDetectionType.setEnabled(!useStartHeight.isSelected());
		startHeightProviderType.setEnabled(useStartHeight.isSelected());
		startHeightRange.setEnabled(useStartHeight.isSelected());
	}

	@Override public void reloadDataLists() {
		super.reloadDataLists();

		updateStructureSelector();

		jigsaw.reloadDataLists();
	}

	@Override public void openInEditingMode(Structure structure) {
		ignoreBlocks.setListElements(structure.ignoredBlocks);
		projection.setSelectedItem(structure.projection);
		surfaceDetectionType.setSelectedItem(structure.surfaceDetectionType);
		useStartHeight.setSelected(structure.useStartHeight);
		startHeightProviderType.setSelectedItem(structure.startHeightProviderType);
		startHeightRange.setMinValue(structure.startHeightMin);
		startHeightRange.setMaxValue(structure.startHeightMax);
		terrainAdaptation.setSelectedItem(structure.terrainAdaptation);
		structureSelector.setSelectedItem(structure.structure);
		restrictionBiomes.setListElements(structure.restrictionBiomes);
		separation_spacing.setMinValue(structure.separation);
		separation_spacing.setMaxValue(structure.spacing);
		frequency.setValue((double) structure.frequency);
		spreadType.setSelectedItem(structure.spreadType);
		generationStep.setSelectedItem(structure.generationStep);
		size.setValue(structure.size);
		maxDistanceFromCenter.setValue(structure.maxDistanceFromCenter);
		projection.setSelectedItem(structure.projection);
		jigsaw.setEntries(structure.jigsawPools);

		updateEnabledFields();
	}

	@Override public Structure getElementFromGUI() {
		Structure structure = new Structure(modElement);
		structure.ignoredBlocks = ignoreBlocks.getListElements();
		structure.projection = (String) projection.getSelectedItem();
		structure.surfaceDetectionType = (String) surfaceDetectionType.getSelectedItem();
		structure.useStartHeight = useStartHeight.isSelected();
		structure.startHeightProviderType = (String) startHeightProviderType.getSelectedItem();
		structure.startHeightMin = startHeightRange.getIntMinValue();
		structure.startHeightMax = startHeightRange.getIntMaxValue();
		structure.terrainAdaptation = (String) terrainAdaptation.getSelectedItem();
		structure.restrictionBiomes = restrictionBiomes.getListElements();
		structure.structure = structureSelector.getSelectedItem();
		structure.separation = separation_spacing.getIntMinValue();
		structure.spacing = separation_spacing.getIntMaxValue();
		structure.frequency = ((Double) frequency.getValue()).floatValue();
		structure.spreadType = (String) spreadType.getSelectedItem();
		structure.generationStep = (String) generationStep.getSelectedItem();
		structure.size = (int) size.getValue();
		structure.maxDistanceFromCenter = (int) maxDistanceFromCenter.getValue();
		structure.jigsawPools = jigsaw.getEntries();
		return structure;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return new URI(MCreatorApplication.SERVER_DOMAIN + "/wiki/how-make-structure");
	}


	private void scanExternalStructures() {
		File runDir = mcreator.getFolderManager().getClientRunDir();
		File savesDir = new File(runDir, "saves");
		if (!savesDir.exists())
			return;

		new SwingWorker<List<File>, Void>() {
			@Override protected List<File> doInBackground() {
				List<File> found = new ArrayList<>();
				File[] worlds = savesDir.listFiles(File::isDirectory);
				if (worlds != null) {
					for (File world : worlds) {
						File structDir = new File(world, "generated/minecraft/structures");
						if (structDir.exists() && structDir.isDirectory()) {
							File[] structures = structDir.listFiles(f -> f.getName().endsWith(".nbt"));
							if (structures != null) {
								Collections.addAll(found, structures);
							}
						}
					}
				}
				return found;
			}

			@Override protected void done() {
				try {
					List<File> found = get();
					boolean updated = false;
					for (File file : found) {
						String name = FilenameUtilsPatched.removeExtension(file.getName());
						if (!externalStructures.containsKey(name)) {
							externalStructures.put(name, file);
							updated = true;
						}
					}
				if (updated) {
					updateStructureSelector();
				}
				} catch (Exception ignore) {
				}
			}
		}.execute();
	}

	private void updateStructureSelector() {
		String selected = structureSelector.getSelectedItem();
		List<String> structures = new ArrayList<>(mcreator.getFolderManager().getStructureList());
		externalStructures.forEach((name, file) -> {
			if (!structures.contains(name)) {
				structures.add(name);
			}
		});
		Collections.sort(structures);
		ComboBoxUtil.updateComboBoxContents(structureSelector, structures);
		structureSelector.setSelectedItem(selected);
	}
}
