# Измерения

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a dimension?

Dimensions are accessible realms within a Minecraft world characterized by a way of generation, biomes, structures, and other things unique for each dimension. The daylight cycle and weather may work differently or not exist in different dimensions and respawn points. Minecraft vanilla has actually 3 dimensions:

- The Overworld, also called the Surface, is an Earth-like realm. Is the first dimension where the player spawns and the first explorable. Has the most biomes collection in the game and has a bedrock layer that covers the world limit in the depths.

- The Nether is the second dimension in the game. To enter the player needs to build an obsidian portal in The Overworld and light it with fire. The Nether has a generation like a huge cave and like The Overworld has a bedrock layer on the bottom but has also a second bedrock layer on the top.

- The End is, as the name says, the final Minecraft dimension. To enter the player needs to find an End portal in a Strongholds and activate it with Eyes of Ender. Here is the home of the Ender Dragon, the final boss. The player can't exit from The End without beating the Dragon. The End has a generation of floating islands similar to an asteroid belt. The main island is where the player spawns and the Other Island "orbits" around it. There are no Bedrock layers in The End so there is easy access to the void.

## Creating a custom dimension

First, go in the workspace a click on the Dimension element. Give to your dimension the name you want without spaces.

|  |  |
| --- | --- |
| **Properties** |  |
| Main filler block and fluids | The block that forms the dimension (like a stone for The Overworld or Endstone for The End) and used by algorithms to create the terrain shape for the dimension. For fluid will create oceans in the biomes under the water level in Normal dimensions and the lower part in Nether-like dimensions. |
| World gen type: | How the dimension has to generate:  - Normal gen will make your dimension like The Overworld  - Nether gen will make your dimension as a huge cave-like The Nether  - End gen will make your dimensions a shattered floating island like the End. Warning: the dimension will be exactly like the End so with a central island and other islands orbit around with a huge void between.  These are only pre-made templates for your dimension. You can change the generation using .json files (1.16.x and above only) or coding. |
| Biomes in the dimensions: | Biome that will be in the dimension and generate the cover layers. There always is at least one biomes in the dimension to work. You can also add vanilla biomes and, modifying files, other mods biomes. |
| Sky / fog color override: | The fog/sky color of your dimension overrides the biome's color. Leave this empty to keep the biomes colors |
| Behavior: | How your dimension will work and if the bed explodes or not or are disabled for a respawning anchor |

## Portals (optional)

If you want that your dimension has a portal like the Nether one. Select the frame-block for the portal, the igniter, the portal sound, and the portal particles. The portal igniter doesn't work as a normal item and will call in the recipe as the dimension name.

## Triggers

If you want your dimension to have a trigger for procedures. Remember: some triggers may not work if the player enters the dimension with other ways different from the default portal.

## Notes

* Adding 30 > biomes can lead to biomes not generating normally. This can lead to biomes being large in width and generating in strips rather than round like biomes. For this reason, it's advised to keep the biomes under 30 for your dimension unless you are going for a more large biome generation.
* For larger biomes a nice sweet spot is 33 biomes, the more biomes you add the larger they get in size, 33 will be on average about 1k to 2k width and an unknown depth in size.

See also

[How to Make a Biome](/wiki/how-make-biome)

[How to Make a Block](/wiki/how-make-block)

[How to Make a Fluid](/wiki/how-make-fluid)

[Procedure system](/wiki/procedure-system)

References

Minecraft Wiki - Dimension
<https://minecraft.wiki/w/Dimension>