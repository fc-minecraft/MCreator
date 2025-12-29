# Жидкости

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a fluid?

Fluids are special types of blocks that have the ability to flow over the terrain and fall according to the game's gravity. Items and entities are pushed in the direction of the flow (for lava-like fluids only after 1.16.x and up) and can swim through it. In water and lava (BE only) entities that can't breathe underwater and don't have water water-breathing effect will slowly lose oxygen and die. Based on their behaviour fluids can flow at different speeds, have different densities and, if enabled, deal damage to entities and items.

## Getting started

* Open MCreator and your workspace
* Create two textures, possibly animated for a better effect, one for the still part of the fluid and one for the flowing part
* Create a new Fluid element and name it as you want
* Select the right texture for each field
* Click on ”Next page”

## Fluid and properties

### Textures

* **Still texture:** The still texture is for source blocks not flowing blocks.
* **Flowing:** This controls the flowing texture for the liquid, not the source block.

### Fluid properties

* **In-game name:** The name of the fluid in the game.
* **Fluid type:**
  + **Water:** Inherits some water properties from the base game.
  + **Lava:** Inherits some lava properties from the base game.
* **Flow strength:** This parameter determines how much the fluid pushes entities when it's flowing. With negative values, it will pull entities instead of pushing them away.
* **Level decrease per block:** This parameter determines how much the level of this fluid decreases for every block it spreads to. With higher values, the fluid will cover a smaller area. This value is 1 for water and lava in the Nether and 2 for lava in the Overworld.
* **Slope find distance:** This parameter determines how far away a slope can be to affect the spreading of this fluid. Instead of spreading evenly, the fluid will try to reach the slope. With lower values, the fluid will usually cover larger areas if the ground is uneven.
* **Can source multiply:** If true it acts similar to water, the liquid can make more water sources of itself.
* **Spawn drip particles:** If true the block will spawn drop particles below the block that the liquid is placed on.
* **Drip particle:** The drip particle that should show if the block can spawn particles.
* **Tint type:** This can control the colour of the block to allow support for the tint index, similar to how water uses the water tint colour for biomes.
* **Additional flow condition:** A condition that controls when the liquid can flow, the condition must return true in order to flow.

### Fluid bucket properties

* **Autogenerate fluid bucket:** If checked a vanilla bucket will be made for this liquid.
* **In-game name of bucket:** The name that the liquid bucket should be called.
* **Bucket texture:** The item texture that is used for the bucket.
* **Creative inventory tab:** The creative tab in which the liquid bucket resides.
* **Bucket empty sound:** The sound that plays when the bucket is emptied.
* **Rarity:** The item's display name color.
  + **Common:** White
  + **Uncommon:** Yellow
  + **Rare:** Aqua
  + **Epic:** Light Purple
* **Special information about the bucket:** Information about the item. Also known as "lore text". This text will be displayed when the player hovers over the item.

## Advanced properties

### Fluid block properties

* **Resistance:** This controls how likely the liquid will break if an explosion happens.
* **Luminance:** This controls how much light the liquid should give off. Lava has a light level of 15.
* **Light opacity:** This controls how much light gets passed through the liquid 15 will block all light 0 will let all light through.
* **Tick rate:** If set to 1 or above the block will tick. This can be used for triggers. Setting the value to 0 will disable ticking.
* **Block flammability:** This parameter determines how quickly the block is consumed by fire.
* **Fire spreading speed:** This parameter controls how quickly the fire spreads to other blocks.
* **Block color on the map:** The color which the block is shown on the map. Air will make the color transparent.
* **Enable emissive rendering:** Check this property to have a glowing effect (similar to the magma block) applied to your block.

### Modded fluid properties

* **Luminosity:** Set the light value of your fluid. Setting the value to 0 will disable the light.
* **Density:** Set a density value in kg/m3 units.
* **Viscosity:** The viscosity is how difficult it is to walk in the fluid.
* **Temperature:** This is the temperature of your fluid, measured in Kelvin. The higher the value, the hotter your fluid will be. The default value is 300K, which is around room temperature.

## Triggers

| Trigger name | Description |
| --- | --- |
| **When block added** | When the block is added to the world this trigger will be called. |
| **When neighbour block changes** | When the block next to this block on any side updates this trigger is called. |
| **Update tick** | If the tick rate is 1 or above this procedure will be called each time the tick rate reaches its time. |
| **When mob/player collides block** | When the entity is in the liquids tile this trigger is called. |
| **Client display random tick** | A tick update on the client side is mainly used for things like particles and sounds. |
| **When block is destroyed by explosion** | When the block is destroyed by an explosion this trigger will be called. |
| **Before replacing a block** | This procedure is called before a block is replaced by this liquid. |

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

See also

[How to Make a Block](/wiki/how-make-block)

[How to Make a Dimension](/wiki/how-make-dimension)

[Procedure system](/wiki/procedure-system)

References

Minecraft Wiki - Fluid
<https://minecraft.wiki/w/Fluid>