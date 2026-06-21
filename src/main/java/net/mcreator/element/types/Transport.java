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

package net.mcreator.element.types;

import net.mcreator.element.BaseType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.parts.TabEntry;
import net.mcreator.element.types.interfaces.ICommonType;
import net.mcreator.element.types.interfaces.IEntityWithModel;
import net.mcreator.element.types.interfaces.IMCItemProvider;
import net.mcreator.element.types.interfaces.ITabContainedElement;
import net.mcreator.minecraft.MCItem;
import net.mcreator.minecraft.MinecraftImageGenerator;
import net.mcreator.ui.workspace.resources.TextureType;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.references.ModElementReference;
import net.mcreator.workspace.references.TextureReference;
import net.mcreator.workspace.resources.Model;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.io.FileIO;
import net.mcreator.io.ResourcePointer;
import net.mcreator.ui.init.ImageMakerTexturesCache;
import net.mcreator.util.image.ImageUtils;
import java.lang.module.ModuleDescriptor;

import net.mcreator.element.parts.IWorkspaceDependent;
import net.mcreator.element.parts.MItemBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

@SuppressWarnings({ "unused", "NotNullFieldNotInitialized" })
public class Transport extends GeneratableElement
		implements IEntityWithModel, ITabContainedElement, ICommonType, IMCItemProvider, IWorkspaceDependent {

	@Nonnull public String modelName;
	@Nonnull public String mobName;
	@TextureReference(TextureType.ENTITY) public String texture;
	@Nonnull public String itemTexture;

	@Nonnull public String transportType; // LAND, AIR, WATER

	public double speed;
	public double strafeSpeed;
	public double steeringSpeed;
	public double jumpForce;

	public double seatOffsetX;
	public double seatOffsetY;
	public double seatOffsetZ;
	public double seatYaw;
	public double modelYawOffset;

	public boolean hasSpawnEgg;
	public Color spawnEggBaseColor;
	public Color spawnEggDotColor;
	@ModElementReference public List<TabEntry> creativeTabs;

	public double maxHealth;
	public boolean planeMechanics;
	public boolean helicopterMechanics;
	public double maxAltitude;
	public boolean enableCrash;
	public double crashSpeed;
	public boolean enableFuel;
	public MItemBlock fuelItem;
	public List<FuelEntry> fuelItems;
	public double fuelConsumption;
	public double fuelCapacity;
	@Nonnull public String spinParts;
	public double spinSpeed;

	// Phase 2: Keybinds
	@Nonnull public String engineToggleKey;  // GLFW key name, e.g. "F"
	@Nonnull public String dismountKey;       // e.g. "Q"

	// Phase 2: HUD visibility
	public boolean showEngineHUD;
	public boolean showFuelHUD;
	public boolean showThrottleHUD;
	public boolean showHints;
	@ModElementReference @Nullable public String overlayBoundTo;
	public List<HudElement> hudElements;
	@Nonnull public String hudType;

	// Phase 2: Realistic physics
	public double accelerationRate;  // throttle gain per tick when key held
	public double brakeFactor;       // throttle decay per tick
	public double stallSpeed;        // plane stall threshold
	public double inertiaFactor;     // how much inertia is kept on land/water

	// Phase 2: Crash settings
	public float explosionRadius;
	public boolean crashDamageToPlayer;
	public boolean crashDropItems;

	public static class FuelEntry {
		public MItemBlock item;
		public double fuelAmount;

		public FuelEntry() {}

		public FuelEntry(MItemBlock item, double fuelAmount) {
			this.item = item;
			this.fuelAmount = fuelAmount;
		}
	}

	public static class HudElement {
		/** Unique internal id (UUID). */
		public String id;
		/** User-visible label shown in editor and optionally rendered as prefix in-game. */
		public String label;
		/**
		 * Element type:
		 *  TEXT         – static text content
		 *  VEHICLE_VALUE – renders a named vehicle data value (speed, fuel, throttle, engine_status, altitude)
		 *  PROGRESS_BAR  – renders a colored progress bar for a named vehicle value
		 */
		public String type;
		/** For VEHICLE_VALUE / PROGRESS_BAR: which value to show (SPEED, FUEL, THROTTLE, ENGINE_STATUS, ALTITUDE). */
		public String valueExpression;
		/** For TEXT type: the static string to display. Also used as label prefix for VEHICLE_VALUE. */
		public String textContent;
		/** Anchor corner/edge of the screen. */
		public String anchor;
		/** Pixel offset from the anchor X. */
		public int xOffset;
		/** Pixel offset from the anchor Y. */
		public int yOffset;
		/** Rendering color (ARGB). */
		public Color color;
		/**
		 * Display condition:
		 *  ALWAYS, ENGINE_ON, ENGINE_OFF, MOVING
		 */
		public String displayCondition;
		/** Width of the bar in pixels (only for PROGRESS_BAR). */
		public int barWidth;
		/** Height of the bar in pixels (only for PROGRESS_BAR). */
		public int barHeight;

		public HudElement() {}

		/** Convenience constructor for simple value indicators. */
		public HudElement(String id, String label, String type, String valueExpression,
				String textContent, String anchor, int xOffset, int yOffset,
				Color color, String displayCondition, int barWidth, int barHeight) {
			this.id = id;
			this.label = label;
			this.type = type;
			this.valueExpression = valueExpression;
			this.textContent = textContent;
			this.anchor = anchor;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.color = color;
			this.displayCondition = displayCondition;
			this.barWidth = barWidth;
			this.barHeight = barHeight;
		}

		/** Returns a human-readable description for the editor list. */
		public String getDisplayName() {
			if (label != null && !label.isEmpty()) return label;
			if (type != null) {
				switch (type) {
					case "VEHICLE_VALUE": return (valueExpression != null ? valueExpression : "Value") + " indicator";
					case "PROGRESS_BAR": return (valueExpression != null ? valueExpression : "Value") + " bar";
					case "TEXT": return "Text: " + (textContent != null && textContent.length() > 16 ? textContent.substring(0, 16) + "…" : textContent);
				}
			}
			return "HUD element";
		}
	}

	private Transport() {
		this(null);
	}

	public Transport(ModElement element) {
		super(element);

		this.modelName = "Biped";
		this.mobName = element != null ? element.getName() : "";
		this.texture = "";
		this.itemTexture = "";
		this.transportType = "LAND";

		this.speed = 0.8;
		this.strafeSpeed = 0.15;
		this.steeringSpeed = 0.05;
		this.jumpForce = 0.5;

		this.seatOffsetX = 0.0;
		this.seatOffsetY = 0.0;
		this.seatOffsetZ = 0.0;
		this.seatYaw = 0.0;
		this.modelYawOffset = 0.0;

		this.hasSpawnEgg = true;
		this.spawnEggBaseColor = new Color(255, 255, 255);
		this.spawnEggDotColor = new Color(255, 0, 0);
		this.creativeTabs = new ArrayList<>();

		this.maxHealth = 20.0;
		this.planeMechanics = false;
		this.helicopterMechanics = false;
		this.maxAltitude = 320.0;
		this.enableCrash = false;
		this.crashSpeed = 0.4;
		this.enableFuel = false;
		this.fuelItem = element != null ? new MItemBlock(element.getWorkspace(), "Items.COAL") : null;
		this.fuelItems = new ArrayList<>();
		if (element != null) {
			this.fuelItems.add(new FuelEntry(new MItemBlock(element.getWorkspace(), "Items.COAL"), 250.0));
			this.fuelItems.add(new FuelEntry(new MItemBlock(element.getWorkspace(), "Items.CHARCOAL"), 250.0));
			this.fuelItems.add(new FuelEntry(new MItemBlock(element.getWorkspace(), "Items.COAL_BLOCK"), 2500.0));
			this.fuelItems.add(new FuelEntry(new MItemBlock(element.getWorkspace(), "Items.LAVA_BUCKET"), 5000.0));
		}
		this.fuelConsumption = 0.1;
		this.fuelCapacity = 1000.0;
		this.spinParts = "propeller,rotor";
		this.spinSpeed = 0.5;

		// Phase 2: keybinds
		this.engineToggleKey = "F";
		this.dismountKey = "Q";

		// Phase 2: HUD – default elements
		this.hudType = "ACTIONBAR";
		this.showEngineHUD = true;
		this.showFuelHUD = true;
		this.showThrottleHUD = true;
		this.showHints = true;
		this.overlayBoundTo = "";

		this.hudElements = new ArrayList<>();
		this.hudElements.add(new HudElement("el_engine", "Engine Status", "VEHICLE_VALUE", "ENGINE_STATUS",
				net.mcreator.ui.init.L10N.t("elementgui.transport.hud.default_prefix.engine"), "TOP_LEFT", 10, 10, new Color(255, 255, 255), "ALWAYS", 80, 6));
		this.hudElements.add(new HudElement("el_speed",  "Speedometer",   "VEHICLE_VALUE", "SPEED",
				net.mcreator.ui.init.L10N.t("elementgui.transport.hud.default_prefix.speed"),  "TOP_LEFT", 10, 22, new Color(100, 220, 255), "ENGINE_ON", 80, 6));
		this.hudElements.add(new HudElement("el_throttle","Throttle",      "PROGRESS_BAR",  "THROTTLE",
				net.mcreator.ui.init.L10N.t("elementgui.transport.hud.default_prefix.throttle"), "TOP_LEFT", 10, 34, new Color(80, 200, 120), "ENGINE_ON", 80, 6));
		this.hudElements.add(new HudElement("el_fuel",   "Fuel Level",    "PROGRESS_BAR",  "FUEL",
				net.mcreator.ui.init.L10N.t("elementgui.transport.hud.default_prefix.fuel"),     "TOP_LEFT", 10, 46, new Color(255, 200, 60), "ENGINE_ON", 80, 6));
		this.hudElements.add(new HudElement("el_hints",  "Control Hints", "TEXT",           "",
				String.format(net.mcreator.ui.init.L10N.t("elementgui.transport.hud.default_prefix.hints"), this.engineToggleKey, this.dismountKey), "BOTTOM_LEFT", 10, -20, new Color(200, 200, 200), "ENGINE_OFF", 80, 6));

		// Phase 2: Physics
		this.accelerationRate = 0.015;
		this.brakeFactor = 0.02;
		this.stallSpeed = 0.35;
		this.inertiaFactor = 0.98;

		// Phase 2: Crash
		this.explosionRadius = 2.0f;
		this.crashDamageToPlayer = true;
		this.crashDropItems = false;
	}

	@Override @Nullable public Model getEntityModel() {
		Model.Type modelType = Model.Type.BUILTIN;
		if (!modelName.equals("Biped") && !modelName.equals("Zombie"))
			modelType = Model.Type.JAVA;
		return Model.getModelByParams(getModElement().getWorkspace(), modelName, modelType);
	}

	@Override public Collection<BaseType> getBaseTypesProvided() {
		return List.of(BaseType.ITEM, BaseType.ENTITY);
	}

	@Override public List<TabEntry> getCreativeTabs() {
		return creativeTabs;
	}

	@Override public List<MCItem> getCreativeTabItems() {
		return providedMCItems();
	}

	@Override public List<MCItem> providedMCItems() {
		return List.of(new MCItem.Custom(this.getModElement(), "spawn_egg", "item", "Spawn egg"));
	}

	@Override public ImageIcon getIconForMCItem(Workspace workspace, String suffix) {
		if ("spawn_egg".equals(suffix)) {
			if (hasSpawnEgg) {
				return MinecraftImageGenerator.generateSpawnEggIcon(spawnEggBaseColor, spawnEggDotColor);
			} else if (itemTexture != null && !itemTexture.isEmpty()) {
				try {
					File f = workspace.getFolderManager().getTextureFile(net.mcreator.util.FilenameUtilsPatched.removeExtension(itemTexture), TextureType.ITEM);
					if (f.isFile()) {
						return new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
					}
				} catch (Exception ignored) {}
			}
		}
		return null;
	}

	@Override public BufferedImage generateModElementPicture() {
		Image image = null;
		try {
			if (texture != null && !texture.isEmpty()) {
				File f = getModElement().getWorkspace().getFolderManager()
						.getTextureFile(net.mcreator.util.FilenameUtilsPatched.removeExtension(texture), TextureType.ENTITY);
				if (f.isFile()) {
					image = new ImageIcon(f.getAbsolutePath()).getImage();
				}
			}
		} catch (Exception ignored) {}
		return MinecraftImageGenerator.Preview.generateMobPreviewPicture(image, spawnEggBaseColor, spawnEggDotColor, hasSpawnEgg);
	}

	@Override public void finalizeModElementGeneration() {
		if (hasSpawnEgg && ModuleDescriptor.Version.parse(
						getModElement().getGeneratorConfiguration().getGeneratorMinecraftVersion())
				.compareTo(ModuleDescriptor.Version.parse("1.21.5")) >= 0 && (
				getModElement().getGeneratorConfiguration().getGeneratorFlavor().getGamePlatform()
						== GeneratorFlavor.GamePlatform.JAVAEDITION)) {
			File spawnEggTextureFile = getModElement().getFolderManager()
					.getTextureFile(getModElement().getRegistryName() + "_spawn_egg_generated", TextureType.ITEM);
			ImageIcon spawnEgg = ImageUtils.drawOver(ImageUtils.colorize(ImageMakerTexturesCache.CACHE.get(
							new ResourcePointer("templates/textures/texturemaker/egg_base.png")), spawnEggBaseColor, true),
					ImageUtils.colorize(ImageMakerTexturesCache.CACHE.get(
									new ResourcePointer("templates/textures/texturemaker/egg_accent.png")), spawnEggDotColor,
							true));
			FileIO.writeImageToPNGFile(ImageUtils.toBufferedImage(spawnEgg.getImage()), spawnEggTextureFile);
		}
	}

	@Override
	public void setWorkspace(@Nullable Workspace workspace) {
		if (this.fuelItems == null || this.fuelItems.isEmpty()) {
			this.fuelItems = new ArrayList<>();
			if (this.fuelItem != null && !this.fuelItem.isEmpty()) {
				this.fuelItems.add(new FuelEntry(this.fuelItem, 250.0));
			}
		}
		if (this.hudElements == null || this.hudElements.isEmpty()) {
			this.hudElements = new ArrayList<>();
			this.hudElements.add(new HudElement("el_engine", "Engine Status", "VEHICLE_VALUE", "ENGINE_STATUS", "Engine: ", "TOP_LEFT", 10, 10, new Color(255, 255, 255), "ALWAYS", 80, 6));
			this.hudElements.add(new HudElement("el_speed",  "Speedometer",   "VEHICLE_VALUE", "SPEED",         "Speed: ",  "TOP_LEFT", 10, 22, new Color(100, 220, 255), "ENGINE_ON", 80, 6));
			this.hudElements.add(new HudElement("el_throttle","Throttle",      "PROGRESS_BAR",  "THROTTLE",      "Throttle", "TOP_LEFT", 10, 34, new Color(80, 200, 120), "ENGINE_ON", 80, 6));
			this.hudElements.add(new HudElement("el_fuel",   "Fuel Level",    "PROGRESS_BAR",  "FUEL",          "Fuel",     "TOP_LEFT", 10, 46, new Color(255, 200, 60), "ENGINE_ON", 80, 6));
		}
		if (this.hudType == null || this.hudType.isEmpty()) {
			if (this.overlayBoundTo != null && !this.overlayBoundTo.isEmpty()) {
				this.hudType = "OVERLAY";
			} else if (this.hudElements != null && !this.hudElements.isEmpty()) {
				this.hudType = "CUSTOM";
			} else {
				this.hudType = "ACTIONBAR";
			}
		}
	}

	@Nullable
	@Override
	public Workspace getWorkspace() {
		return getModElement() != null ? getModElement().getWorkspace() : null;
	}
}
