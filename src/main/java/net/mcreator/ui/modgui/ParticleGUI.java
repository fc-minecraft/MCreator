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

import net.mcreator.blockly.data.Dependency;
import net.mcreator.element.types.Particle;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.JMinMaxSpinner;
import net.mcreator.ui.component.TranslatedComboBox;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.TypedTextureSelectorDialog;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.minecraft.TextureSelectionButton;
import net.mcreator.ui.procedure.ProcedureSelector;
import net.mcreator.ui.validation.ValidationGroup;
import net.mcreator.ui.workspace.resources.TextureType;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.elements.VariableTypeLoader;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ParticleGUI extends ModElementGUI<Particle> {

	private TextureSelectionButton texture;
	private final JMinMaxSpinner scale = new JMinMaxSpinner(0, 5, 0, 128, 0.1);
	private final JMinMaxSpinner lifetime = new JMinMaxSpinner(10, 20, 1, 12000, 1);
	private final JMinMaxSpinner speed = new JMinMaxSpinner(0.8, 1.2, 0, 128, 0.01);
	private final JSpinner gravity = new JSpinner(new SpinnerNumberModel(0, -100, 100, 0.01));

	private final JColor color = new JColor(mcreator, false, true);

	private final JCheckBox hasPhysics = L10N.checkbox("elementgui.common.enable");
	private final JCheckBox canCollide = L10N.checkbox("elementgui.common.enable");
	private final JCheckBox alwaysShow = L10N.checkbox("elementgui.common.enable");
	private final JCheckBox animate = L10N.checkbox("elementgui.common.enable");

	private final JComboBox<String> renderType = new TranslatedComboBox(
			Map.entry("OPAQUE", "elementgui.particle.render_type.opaque"),
			Map.entry("TRANSLUCENT", "elementgui.particle.render_type.translucent")
	);

	private ProcedureSelector additionalExpiryCondition;

	private final ValidationGroup page1group = new ValidationGroup();

	public ParticleGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.initGUI();
		super.finalizeGUI();
	}

	@Override protected void initGUI() {
		texture = new TextureSelectionButton(new TypedTextureSelectorDialog(mcreator, TextureType.PARTICLE)).requireValue(
				"elementgui.particle.error_texture");

		color.setPreferredSize(new Dimension(250, 42));

		JPanel spo2 = new JPanel(new GridLayout(10, 2, 0, 2));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/texture"),
				L10N.label("elementgui.particle.texture")));
		spo2.add(PanelUtils.join(texture));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/color"), L10N.label("elementgui.particle.color")));
		spo2.add(PanelUtils.join(color));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/scale"), L10N.label("elementgui.particle.scale")));
		spo2.add(scale);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/lifetime"),
				L10N.label("elementgui.particle.lifetime")));
		spo2.add(lifetime);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/speed"), L10N.label("elementgui.particle.speed")));
		spo2.add(speed);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gravity"),
				L10N.label("elementgui.particle.gravity")));
		spo2.add(gravity);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/has_physics"),
				L10N.label("elementgui.particle.has_physics")));
		spo2.add(hasPhysics);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/has_collision"),
				L10N.label("elementgui.particle.has_collision")));
		spo2.add(canCollide);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/render_type"),
				L10N.label("elementgui.particle.render_type")));
		spo2.add(renderType);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/emissive_rendering"),
				L10N.label("elementgui.common.emissive_rendering")));
		spo2.add(alwaysShow);

		spo2.setOpaque(false);

		additionalExpiryCondition = new ProcedureSelector(this.withEntry("particle/expiry_condition"), mcreator,
				L10N.t("elementgui.particle.additional_expiry_condition"), VariableTypeLoader.BuiltInTypes.LOGIC,
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity")).makeInline();

		JPanel page1 = new JPanel(new BorderLayout(10, 10));
		page1.add("Center", PanelUtils.totalCenterInPanel(
				PanelUtils.northAndCenterElement(spo2, PanelUtils.gridElements(2, 1, 0, 2,
						HelpUtils.wrapWithHelpButton(this.withEntry("particle/animate_texture"),
								L10N.label("elementgui.particle.animate_texture")), animate, additionalExpiryCondition))));
		page1.setOpaque(false);

		page1group.addValidationElement(texture);

		addPage(L10N.t("elementgui.common.page_properties"), page1).validate(page1group);

		hasPhysics.setOpaque(false);
		hasPhysics.addActionListener(e -> {
			canCollide.setEnabled(hasPhysics.isSelected());
			if (!hasPhysics.isSelected())
				canCollide.setSelected(false);
		});
		canCollide.setOpaque(false);
		alwaysShow.setOpaque(false);
		animate.setOpaque(false);

		if (!isEditingMode()) {
			hasPhysics.setSelected(true);
		}
	}

	@Override public void openInEditingMode(Particle particle) {
		texture.setTexture(particle.texture);
		/*
		scale.setMinValue(particle.scale.min);
		scale.setMaxValue(particle.scale.max);
		lifetime.setMinValue(particle.lifetime.min);
		lifetime.setMaxValue(particle.lifetime.max);
		speed.setMinValue(particle.speed.min);
		speed.setMaxValue(particle.speed.max);
		*/
		gravity.setValue(particle.gravity);
		/*
		color.setColor(particle.color);
		hasPhysics.setSelected(particle.hasPhysics);
		*/
		canCollide.setSelected(particle.canCollide);
		alwaysShow.setSelected(particle.alwaysShow);
		animate.setSelected(particle.animate);
		renderType.setSelectedItem(particle.renderType);
		additionalExpiryCondition.setSelectedProcedure(particle.additionalExpiryCondition);
	}

	@Override public Particle getElementFromGUI() {
		Particle particle = new Particle(modElement);
		particle.texture = texture.getTextureHolder();
		/*
		particle.scale = new Particle.ParticleProperty(scale.getMinValue(), scale.getMaxValue());
		particle.lifetime = new Particle.ParticleProperty((int) lifetime.getMinValue(), (int) lifetime.getMaxValue());
		particle.speed = new Particle.ParticleProperty(speed.getMinValue(), speed.getMaxValue());
		*/
		particle.gravity = (double) gravity.getValue();
		/*
		particle.color = color.getColor();
		particle.hasPhysics = hasPhysics.isSelected();
		*/
		particle.canCollide = canCollide.isSelected();
		particle.animate = animate.isSelected();
		particle.alwaysShow = alwaysShow.isSelected();
		particle.renderType = (String) renderType.getSelectedItem();
		particle.additionalExpiryCondition = additionalExpiryCondition.getSelectedProcedure();
		return particle;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return new URI(MCreatorApplication.SERVER_DOMAIN + "/wiki/how-make-particle");
	}

}
