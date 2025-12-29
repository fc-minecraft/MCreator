# Биомы

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## Making a New Biome

On this page, we'll show you how to make your very own biome.

## Biome Settings

### Basic Properties

**Name:** Your biome's name

**Climate rain probability:** This controls how humid your biome is.

**Climate temperature:** Controls your actual biome's temperature. A higher value will make it hotter (e.g. desert, Nether biomes) and lower similar to icy biomes.

**Air color:** The color of the sky

**Grass color:** The color to replace put on the grass blocks

**Foliage color:** The color to replace put on the leave blocks

**Water color:** The color of the water

**Water fog color:** The color of the fog under the water

### Generation

**Surface builder ground block:** The block to replace the grass

Note: This block should have GRASS material and be tagged in **forge:dirt** Blocks tags for Forge mods for plants and trees to spawn properly in the biome.

**Surface builder underground block:** The block to replace the dirt

This block should be tagged in **forge:dirt** Blocks tags for Forge mods for plants and trees to spawn properly in the biome.

**Surface builder underwater block:** This is the block that will make the ground when the biome is similar to an ocean or a deep river (e.g. gravel in cold oceans and sand in hot oceans).

**Overworld surface coverage estimate:** This gives you a general estimate of how common your biome will be in the overworld. This value might not be 100% accurate as it says its a estimate so use it as a guide not an exact value.

**Biome generation temperature range:** The range between where the biome is allowed to generate for temperature.

**Biome generation continentalness range:** A range that controls the area where the biome should generate eg. Inland or out at sea.

**Biome generation erosion range:** A range that controls the area where the biome can generate based on how flat or bumpy the area is.

**Biome Generation Weirdness range:** A range that controls the biomes allowed area size. Currently there is not enough documentation to know truly what weirdness actually is for.

**Generate in overworld:** When enabled the biome will generate in the overworld.

**Generate biome in overworld caves:** When enabled the biome will generate as a cave biome in the overworld.

**Generate biome in nether:** When enabled the biome will generate in the nether dimension.

### Features

**Biome default features:** Pre-configured vanilla features that are often used in biomes

#### Tree type:

##### **Vanilla tree:**

If you want a vanilla tree in your biome, select one of them in the list.

##### **Custom tree:**

If you want a custom tree, select this checkbox. (Unlocks 5 options below.)

Note: The biome mod element allows you to create a custom tree type. However, since MCreator 2023.4, the feature mod element now allows you to generate custom trees with more complex generations and parameters than the following feature.

**Minimal height:** The minimum height of the stem.

**Stem:** The block for the log.

**Branch:** The block for the leaves.

**Vines:** The block for the vines. If you don't want vines, select the air block.

**Fruits:** The block for the fruits (like jungle trees). If you don't want fruits, select the air block.

Note: If you use Forge 1.16 or higher, you can select a vanilla tree type to use its shape for your custom tree.

### Structures

Structures with a checkbox are self-explanatory.

**Generate village of type:** The word in the list is the type of the village to spawn. Keep "none" to remove the village spawning.

**Generate ocean ruins in this biome?:**

* NONE: No ocean ruins
* COLD: Ocean ruins made of stone
* WARM: Ocean ruins made of sandstone

### Entity Spawning

A spawn entry is equal to an entity

**Entity:** The entity to spawn. Put only mobs, otherwise, the game will crash.

**Spawn type:** This parameter controls the spawning type for the biomes where his mob is defined to spawn in.

* A mob marked as Monster will only spawn in the dark or at night.
* A mob marked as Creature will spawn under direct sunlight on grass material blocks only. Do not use this spawn type with mob type living entities as they will not spawn
* A mob marked as Ambient will spawn under any conditions except if block type prevents it, but this category should be used for mobs with no gameplay effect such as bats
* WaterCreature will spawn in water, but with no other limitations

**Spawn weight:** This parameter controls the priority that the mob has over others when the game is choosing what mob to spawn. A higher weight means more mob spawns in the game will create this mob. Make this lower for animals, compared to monsters.

**Min and max group size:** The minimum and maximum set here set the size of groups that the mob will spawn in. Be warned that mobs that try to spawn in groups of over 20 will struggle to do so (mobs will rarely spawn).

### Effects

#### Biome music and sounds

**Biome music:** The music of the biome. This sound will be randomly played.

**Ambient sound**: A sound constantly played

**Additions sound:** This sound is played occasionally inside the biome.

**Mood sound:** The sound used in caves

**Mood sound delay (in ticks):** This value defines the time, in ticks, before the sound is played (loop).

#### Ambient particles

**Enable particles:** To have ambient particles, you have to enable this checkbox.

**Particle type:** The particle to use

**Particle spawn probability (in %):** The probability to spawn particles.

Note: This value is divided by 100 in the code.

## 1.18+ biomes

Biomes in Minecraft versions above (including) 1.18 act quite differently than before. The generation of these biomes is highly dependent on the following parameters:

* Weight
* Temperature
* Raining possibility
* Base height
* Height variation

You may need to tinker with these parameters to achieve the desired generation. If you set similar parameters to multiple biomes, they will compete for the same spot in the world and thus be smaller and more scattered. Optimally, make biome parameters diverse enough between your and also vanilla biomes to make biomes appear in the world consistently.

If your biome weight is small and it has parameters similar to some other biome, it may not generate at all. Keep in mind setting biome weight too big can cause other biomes from your mod, other mod, or even vanilla biomes to not generate at all.

World generator also tries to find the best match, but if you for example only have two biomes in dimension, it can happen that your biome that is set to spawn on flat regions (low base height) may generate in other regions too, as no better suitable biome will be found by the world generator.

#### 2023.1 And Up

Biomes seem to be now calculated based on humidity and temperature for placement. You can find a chart on [Minecraft wiki's biome page.](https://minecraft.fandom.com/wiki/Biome#Generation)

### A quick reference

#### 2023.1 And Up

You can now use the ranges to set the values for biome generation from this MCreator [wiki page](https://mcreator.net/wiki/vanilla-biome-settings-data-list). Keep in mind you may need to still tinker with the settings to get something that works but it should be a bit easier to use compared to prior versions we had to work with.

## Entity Spawning Tags

Some passive entities require tags to be able to spawn on custom grass blocks. Keep in mind that your grass block should be part of the grass material, and the grass block should have the tag for minecraft:dirt and forge:dirt still.

Below is a list of available tags that can be used to allow passive entities to generate and some other uses.
The tags should be under the MINECRAFT namespace and for the BLOCK tag type.

* animals\_spawnable\_on
  + This allows for animals (chickens, cows, pigs, sheep etc.) to spawn on blocks.
  + Default blocks
    - minecraft:grass\_block
* axolotls\_spawnable\_on
  + This allows for axolotls to generate on blocks.
  + Default blocks
    - minecraft:clay
* foxes\_spawnable\_on
  + This allows for foxes to spawn on blocks.
  + Default blocks
    - minecraft:grass\_block
    - minecraft:snow
    - minecraft:snow\_block
    - minecraft:podzol
    - minecraft:coarse\_dirt
* goats\_spawnable\_on
  + This allows for goats to spawn on blocks.
  + Default blocks
    - minecraft:stone
    - minecraft:snow
    - minecraft:snow\_block
    - minecraft:packed\_ice
    - minecraft:gravel
* mooshrooms\_spawnable\_on
  + This allows for mooshrooms to spawn on blocks.
  + Default blocks
    - minecraft:mycelium
* parrots\_spawnable\_on
  + This allows for parrots to spawn on blocks.
  + Default blocks
    - minecraft:grass\_block
    - minecraft:air
    - minecraft:leaves
    - minecraft:logs
* polar\_bears\_spawnable\_on
  + This allows for polar bears to spawn on blocks.
  + Default blocks
    - minecraft:ice
* prevent\_mob\_spawning\_inside
  + This tag prevents entities spawn spawning inside specific blocks like rails.
  + Default blocks
    - minecraft:rails
* rabbits\_spawnable\_on
  + This allows for rabbits to spawn on blocks.
  + Default blocks
    - minecraft:grass\_block
    - minecraft:snow
    - minecraft:snow\_block
    - minecraft:sand
* valid\_spawn
  + This controls valid spawn locations for players when they first spawn in the world.
  + Default blocks
    - minecraft:grass\_block
    - minecraft:podzol
* wolves\_spawnable\_on
  + This allows for wolves to spawn on blocks.
  + Default blocks
    - minecraft:grass\_block
    - minecraft:snow
    - minecraft:snow\_block

## Video Tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

See also

[How to make a Feature](/wiki/how-make-feature)

[How to Make a Dimension](/wiki/how-make-dimension)

[How to make a Particle](/wiki/how-make-particle)

[Minecraft Entity list](/wiki/entity-ids)

[Minecraft Biome List](/wiki/minecraft-biome-list)

[Vanilla Biome Settings Data List](/wiki/vanilla-biome-settings-data-list)

References

Minecraft Wiki - Biomes
<https://minecraft.wiki/w/Biome>