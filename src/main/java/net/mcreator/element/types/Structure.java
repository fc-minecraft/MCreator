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

package net.mcreator.element.types;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.parts.BiomeEntry;
import net.mcreator.element.parts.MItemBlock;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.references.ModElementReference;
import net.mcreator.workspace.references.ResourceReference;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused") public class Structure extends GeneratableElement {

	@ResourceReference("structure") public String structure;
	public String projection;
	@ModElementReference public List<MItemBlock> ignoredBlocks;

	public int spacing;
	public int separation;
	public float frequency;
	public String frequencyReductionMethod;
	public String spreadType;
	public int salt;
	
	@JsonAdapter(LootTableEntry.Adapter.class)
	public static class LootTableEntry {
		@ModElementReference public String value;

		public LootTableEntry() {
		}

		public LootTableEntry(String value) {
			this.value = value;
		}

		public static class Adapter implements JsonDeserializer<LootTableEntry> {
			@Override
			public LootTableEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				if (json.isJsonPrimitive()) {
					return new LootTableEntry(json.getAsString());
				}
				JsonObject obj = json.getAsJsonObject();
				LootTableEntry entry = new LootTableEntry();
				if (obj.has("value")) {
					entry.value = obj.get("value").getAsString();
				}
				return entry;
			}
		}
	}

	@ModElementReference public List<LootTableEntry> chestLootTables;

	@ModElementReference public List<BiomeEntry> restrictionBiomes;
	public String terrainAdaptation;
	public String generationStep;

	public String surfaceDetectionType;
	public boolean useStartHeight;
	public String startHeightProviderType;
	public int startHeightMin;
	public int startHeightMax;

	public int size;
	public int maxDistanceFromCenter;
	@ModElementReference @ResourceReference("structure") public List<JigsawPool> jigsawPools;

	private Structure() {
		this(null);
	}

	public Structure(ModElement element) {
		super(element);

		this.size = 1;
		this.maxDistanceFromCenter = 64;
		this.jigsawPools = new ArrayList<>();
		this.spacing = 20;
		this.separation = 8;

		this.useStartHeight = false;
		this.startHeightProviderType = "UNIFORM";
		this.startHeightMin = 0;
		this.startHeightMax = 128;

		this.frequency = 1.0f;
		this.frequencyReductionMethod = "default";
		this.spreadType = "linear";
		this.salt = -1;
		this.chestLootTables = new ArrayList<>();
	}

	public List<JigsawPool.JigsawPart> getPoolParts() {
		JigsawPool.JigsawPart part = new JigsawPool.JigsawPart();
		part.weight = 1;
		part.structure = structure;
		part.projection = projection;
		part.ignoredBlocks = ignoredBlocks;
		part.chestLootTables = new ArrayList<>(chestLootTables);
		return Collections.singletonList(part);
	}

	public static class JigsawPool {

		public String poolName;
		public String fallbackPool;
		@ModElementReference @ResourceReference("structure") @Nullable public List<JigsawPart> poolParts;

		@Nullable public List<JigsawPart> getPoolParts() {
			return poolParts;
		}

		public static class JigsawPart {
			public int weight;
			@ResourceReference("structure") public String structure;
			public String projection;
			@ModElementReference public List<MItemBlock> ignoredBlocks;
			@ModElementReference public List<LootTableEntry> chestLootTables;
		}

	}

}
