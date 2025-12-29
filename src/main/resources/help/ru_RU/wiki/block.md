# Блоки

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a Minecraft block?

Blocks are the basic units of the game that make up the game's world and can be collected and placed anywhere. Blocks can be harvested with the bare player's hands or tools if needed in survival, unbreakable blocks apart, or all instant destroyed in creative. Most blocks, for gameplay choice, need a correct tool to be collected, and sometimes the tool itself needs a correct harvest level (see [How to make a tool](https://mcreator.net/wiki/how-make-tool)).

## Visual

|  |  |
| --- | --- |
| **Properties** | **Values** |
| Block textures: | The texture of your block. You can choose different textures for each side of the block or a single texture for the entire block. This option will not work on custom models since the textures must be settled on the model file itself. |
| Tint type: | To use tin you need to make sure the cube in Blockbench has support for a tint index in order to use this feature.   * **No tint:** This will not use any tint. * **Grass:** This will use the tint map for grass blocks. * **Foliage:** This will use the biome's foliage tint map. * **Birch foliage:** This will use the birch biome foliage map. * **Spruce foliage:** This will use the spruce biome foliage map. * **Defualt foliage:** This will use the default map for foliage. * **Water:** This will use the biome's water tint map. * **Sky:** This will use the biome's sky tint map. * **Fog:** This will use the biome's fog color for the block tint. * **Water fog:** This will use the biome's water fog color for the block tint. |
| Block base: | Select the base to be used with this block. This option replaces the old texture blocks option. It's used to create blocks with complex logic. Because of how they work, you won't be able to edit all the parameters of your block. You can choose between:   * **Stairs:** Your block will behave like stairs and use the vanilla stairs models. * **Slabs:** Your block will behave like slabs. * **Fence:** Your block will behave like fences and use the vanilla fence models. * **Wall:** Your block will behave like walls and use the vanilla wall models. * **Leaves:** Your block will behave like leaves, and will use some vanilla leave settings. * **Trapdoor**: Your block will behave like a trapdoor and use the vanilla trapdoor model. * **Pane:** Your block will behave like glass panes or iron bars. * **Door**: This will make behave like regular doors, and create both the top and lower parts. * **Fence gate:** Your block will behave like fence gates. * **End rod**: Your block will behave like an end rod and use the vanilla end rod model. * **Pressure plate**: Your block will behave like a pressure plate (also with a Redstone connection) and use the vanilla pressure plate model. * **Button**: Your block will behave like a button (also with a Redstone connection) and use the vanilla button model. |
| Block item texture: | This replaces the block item texture with a texture similar to an item like an apple or other 2D shape. |
| Block particle texture: | This replaces the block particle texture for breaking or running on when you select a texture for this. Otherwise, it will be taken from the "Block textures" section. |
| Block model: | This is a feature to fine-tune the rendering of your block and is optional to use.   * **Built-in models:** Choose *Custom* for a custom model.   + **Normal:** A full six-sided cube.   + **Single texture:** A full six-sided cube with only one texture. It uses the bottom block for the texture.   + **Cross model:** A model that crosses over like flowers. It uses the bottom block for the texture.   + **Grass block:** This will use a grass block model base with support for top, bottom, sides, and overlay textures. * **Custom 3D model type:** If you are importing a 3D model that you have created in BCraft Cubik or Blender, choose *Obj model file*; you can also write a JSON file (see [this page](http://minecraft.gamepedia.com/Model) for more information) in which case, select the option for *JSON model definition*. * **Import 3D model:** Select this button to open up the .obj or .json file for your custom render. You may also select the buttons for BCraft Cubik or Blender (respectively, as shown) to open an editor. |
| Block rotation: | This is a feature that you can make your block rotate based on the block face or player rotation and other ways.   * **No rotation:** Fixed block orientation. * **Y-axis rotation (S/W/N/E):** Rotates only the sides based on the way the player is facing. * **D/U/N/S/W/E rotation:** Rotates all sides based on the way the player is facing. * **Y-axis rotation (S/W/N/E):** Rotates only the sides based on the block face the block is clicked on. * **D/U/N/S/W/E rotation:** Rotates all sides based on the block face the block is clicked on. * **Log rotation (X/Y/Z):** Rotates the block like vanilla logs. |
| Has pitch rotation: | If your block rotates horizontally, check this box to allow it to face the floor, walls or ceiling in any direction. This option is used by lever, grindstone, etc. |
| Waterloggable: | Check if you want your block to be waterloggable. Note: this option could be useless if your block has a normal block model |
| Check this box if your block has transparency: | Check this feature to make the block support transparency. Leave unchecked for a solid block, check if your block is similar to leaves, glass, iron bars, etc. |
| Transparency type: | * **Solid:** No transparency (similar to dirt, stone, etc.) * **Cutout:** Transparent without mipmapping (similar to glass) * **Cutout mipped:** Like Cutout, but with mipmapping (for a good explanation of what mipmapping is, see [this page](https://textureingraphics.wordpress.com/what-is-texture-mapping/anti-aliasing-problem-and-mipmapping/)) * **Translucent:** Partially transparent and the most resource-heavy option (similar to ice) |
| Check this box to enable connected sides. | This parameter should only be used in combination with transparent blocks.  This will make the internal sides of the block connect similar to how glass, ice and other similar blocks do this. |
| Check this to hide the fluid texture when submerged: | This parameter should only be used in combination with transparent blocks.  If this is checked, the block won't display the fluid texture when submerged, similar to glass blocks. |
| Special information: | This will add text like how enchantments show. |

## Bounding boxes

### Current versions

Edits the physical bounds of the block with ranges between 0 and 16, and describes the shape of all faces

* **Disable applying of block model offset to bounding box:** If the block has a random model offset, its bounding box will also be moved, unless this option is selected. If the bounding box is submerging into neighboring blocks because of the offset, check this box to prevent that. For example, this option would be false for bamboo, and true for tall grass.
* **Add bounding box:** This will allow you to add a new shape to input the bounding box manually.
* **Generate from block model:** This option is only available when a custom model is used. It will allow you to generate the hit blox based on the cubes in the model.
* **Entry coordinates:**
  + **Min. (X, Y, and Z) coord:** default values are 0, and each position is relative to the North, West, and Down part of your block's position. Rotation is taken into account but should be made based on the direction of the model.
  + **Max. (X, Y, and Z) coord:** default values are 0, and each position is relative to the South, East, and Up part of your block's position. Rotation is taken into account but should be made based on the direction of the model.

### Older versions

Edits the physical bounds of the block with ranges between 0 and 1, and describes the shape of all faces

* **Block face shape:** describes the shape of all faces. It's used for example by fences and walls to determine whether they should connect to the block, or by torches to tell if you can place them on the block. You should choose *Solid* for full cubes and *Undefined* for other block models.
* **X/Y/Z coord:** these values determine the bounding box of your block (the black outline of your block, and the collision box if your block is colliding properties)
  + The 3 min coordinates reside in the bottom corner of the block, while the 3 max coordinates reside in the top corner
  + Lower half slabs would have the settings: 0, 0, 0, 1, 0.5, 1 (min x, y, z, max x, y, z)
  + Upper half slabs would have the settings: 0, 0.5, 0, 1, 1, 1

## Properties

### General properties

* **In-game name:** This is the shown name of the block or item when you hover over it in any GUI in Minecraft.
* **Material:** Select the material of the block. It isn't vital what to choose in most cases, but if you select water material, farmland will become wet if it's near it, if you select wood, you can smelt your block in the furnace, selecting lava means that you can get empty buckets full of lava by clicking on your block. Here is some more info about [Minecraft materials](/wiki/materials).
* **Creative inventory tab:** This is where your block will be in creative mode.
* **Hardness:** This is how long it takes to mine the block.
* **Resistance:** How resistant the block is to an explosion, higher numbers have more resistance.
* **Slipperiness:** A higher can slide when they walk on the block. Positive numbers make the block slippery. Values range from 0.1 to 2.0
  + Default value: 0.6 (This is what most regular blocks use like dirt, wood, stone, etc.)
  + Slime blocks: 0.8
  + Ice: 0.89
  + Packed Ice: 0.98
  + Blue Ice: 0.989
* **Jump Factor:** this will control the jump height of entities. The normal jump factor is 1.0
* **Speed factor:** This parameter controls the speed of entities on this block. The normal speed factor is 1.0
* **Luminance:** This is how much light the block gives off, as a value between 0 and 1. If set to 0, the block won't emit light. If set to 1, the block will have a light value of 15 (same as Glowstone)
* **Light opacity:** This property makes the block allow (1) or not (0) the light to pass through, 126 makes it semi-transparent and 255 will allow all light through.
* **Has gravity:** Check this if you want the block to act like sand or gravel, it will fall if not on the ground.
* **Can walk through the block:**Whether entities are able to walk through this block or not.
* **Enable emissive rendering:**This property makes your block glow like a magma block.
* **Is replaceable:** This allows the block to act like tall grass, placing a block in the same location will allow the block to be replaced without a drop.

### Dropping properties

* **Custom drop:**This is the block/item it drops when mined.
* **Drop amount:** This is how much it drops when mined.
* **Use loot table for drops:** When enabled, the block will not define drops in code (drops defined in block mod element), instead, block drops need to be defined with a loot table.
  1. Create loot table mod element with registry name "blocks/(your blocks registry).
  2. The loot table namespace must be "mod"
  3. The loot table type must be "block"
* **Creative pick item:** This determines what item is selected when you "pick" (middle-click with the mouse) the block. If empty, the item is the block itself.
* **Tool able to destroy it:** This is what tool you want to mine the block, *Example:* set to an axe to mine wood.
* **Tool harvest level to break with drop:** This is the tier of tool required to break the block. 0 is hand/wood, 1 is stone, 2 is iron and 3 is diamond. Only the tier of tool you specify will be able to break your block.
* **Requires the correct tool for drops:** If this option is enabled, the block will drop only when broken with the specified harvest tool.
* **Is unbreakable:** Check this if you want it to act like bedrock.

### Sound properties

* **Vanilla sound set:** This allows you to use the built-in game sound sets for breaking, falling, hitting, placing, and stepping based on the type of block.
* **Custom sound set:** This allows you to mix and match sounds for the breaking, falling, hitting, placing, and stepping or use your own custom sound.

## Advanced properties

### Advanced block properties

* **Tick rate:** Determines how often the block should call the "Tick Update" event. Remember: if the tick rate is set to 0, the block will not tick at all, and the "Tick Update" event will not be called.
* **Tick randomly:** Read its comment under it. Generally used for plants.
* **Block color on the map:** This is the color your block appears as on maps. If set to *Default*, the color is based on the material of the block.
* **Can plants sustain on this block:** Any plant can be placed on the block if checked.
* **Beacon color modifier:**Select the color your block will apply to the beacon beam like glasses. Keep default for vanilla handling.
* **Can this block be beacon base:** If checked, the block can be used to build the pyramid of beacons.
* **Does block act like ladder:** If checked, entities will be able to climb on the block.
* **Enchantments power bonus:** How much the block should affect nearby enchantment tables. Bookshelves have a value of 1.
* **Block flammability**: Check [this page](https://minecraft.gamepedia.com/Fire#Flammable_blocks) for reference and have Vanilla values.
* **Fire spreading speed:**How quickly the fire spreads. Keep 0 for vanilla handling.
* **Reaction to being pushed:**How your block reacts to being pushed by a piston.
  + **Normal:** Normal reaction (stone etc.)
  + **Destroy:** Block will be destroyed (plants etc.)
  + **Block:** Block doesn't react to piston (obsidian etc.)
  + **Push Only:** The block can only be pushed by a piston
  + **Ignore:** Block will ignore piston
* **AI path node type:** This parameter controls how the block will be "seen" by the AI path navigators of the mobs.
* **Random model offset:**This parameter controls if block placement should be randomly offset and by which axis.
* **Block valid placement condition:** This makes the block require a specific way for the block to be placed, similar to torches requiring a condition to continue to be placed, in the case of torches, wall torches require a block on the side or ground torches require a block below.

### Redstone properties

* **Does redstone connect to this block:** If checked, Redstone dust will always connect to this block (similar to Redstone Blocks)
* **Does emit redstone:** If you check this parameter, this block will be able to emit redstone power.
* **Emit redstone power:** Only used when the block can emit redstone. Power is between 0 and 15, with 15 being the highest amount of power level.
  + **Fixed:** This will be a fixed amount of Redstone power.
  + **Condition:** A condition that uses a number return block to return a dynamic redstone power level.

### Bone meal properties

* **Can this block be fertilized with bonemeal:** If this option is enabled, the block can be fertilized with bone meal.
  + **Can bone meal be used on this block:**
    - **Fixed:** This will always allow bone meal to be used on the block.
    - **Condition:** A condition that uses a logic return block to enable or disable bone meal usage.
  + **Bone meal success condition:**
    - **Fixed:** This will always return a success if no condition is specified.
    - **Condition:** A condition that uses a logic return block to determine the block's success.
  + **On bone meal success:** The procedure to call when the bone meal success happens.

## Block's tile entity and inventory

If your block has a GUI, NBT variable, or uses forge fluid or energy you will need to make the block a tile entity.

* **Enable tile entity and inventory on this block:**check this box if you want to add an inventory or use NBT variables.
* **Bind this block to GUI:** If your block has a GUI,  select the GUI for the block here.
* **Open bound GUI on right-click:** Open the bound GUI when the player right-clicks in the block.
* **Number of Inventory (slot count):**This is how many slots your GUI will use for the inventory.
* **Max size of stack:**This is how many items in the slots can stack too.
* **Drop items from inventory when destroyed:**Check this if you want the items to drop when the block is broken.
* **Enable block output comparator data:**Check this if you want the block to work with comparators.
* **Disable taking from the following slots:** Input slots will not be used as output/source for hopper item transmission. Use commas to separate the slot numbers.
* **Disable inserting into the following slots:** Output slots will not be used as input for hopper item transmission. Use commas to separate the slot numbers.

### Outdated

* **Output slots:**Select what slots are output slots, and use commas to separate the slot numbers.
* Click next

## Energy & fluid storage

**Your mod must enable tile entity for it to work!**

### Energy storage

* **Enable energy storage:**Whether to enable energy storage or not.
* **Initial energy:**The energy amount the block will have when placed or spawned.
* **Maximal energy capacity:**Maximum energy amount that the block can store.
* **Max energy recieve:**Maximum amount of energy that block can receive per insertion.
* **Max energy extract:**Maximum amount of energy that the block can extract per extraction.

### Fluid tank

* **Enable fluid storage:** Whether to enable fluid storage or not.
* **Maximal fluid storage:**Maximum fluid amount that the block can store.
* **Restrict accepted fluids:** Only selected fluids will be accepted.

## Procedures

* See this post to introduce you to [procedures](/wiki/usage-events).
* Procedure triggers
  + **On block right clicked:**Trigger a procedure when the block is right-clicked. It won't be called if the player is sneaking.
  + **When block added:**Trigger a procedure when the block is added.
  + **When neighboring block changes:** Trigger a procedure when a block nearby updates.
  + **Update tick:** Trigger a procedure when the tick rate updates.
  + **When block destroyed by player:**Trigger a procedure when the player breaks the block.
  + **When block destroyed by explosion:** Trigger a procedure when the block is broken by an explosion.
  + **When player starts to destroy:** Trigger a procedure when a player starts to destroy the block.
  + **When entity collides in the block:** Trigger a procedure when an entity is in the block.
  + **When entity walks on the block:**Trigger a procedure when an entity walks on the block. It won't be called if the entity is sneaking.
  + **On block hit by projectile:** A trigger happens when the block is hit by a projectile.
  + **When block is placed by:** Trigger a procedure when something places the block (entity/block)
  + **Redstone on:** Trigger a procedure when the block is powered by Redstone.
  + **Redstone off:** Trigger a procedure when the block is not powered by Redstone.
  + **Random tick update event:**Trigger a procedure randomly (player side only)

## Generation

### Block spawning/generation properties

* **Dimensions to generate in:**Select what dimensions your block can generate in.
* **Blocks this ore can replace:** Select what blocks your block will only spawn in if it's in that world(s)
* **Restrict ore spawning per biome:** Check this box if you want to restrict the ore to spawn only in specific biomes.
  + **Restriction biomes:** Select what biomes you want the ore to generate in.
* **Average amount of ore groups per chunk:** Set the average amount of ore veins to spawn per chunk.
  + **Vanilla ore groups:**
    - Coal Ore - 20
    - Iron Ore - 20
    - Gold Ore - 2
    - Redstone Ore - 8
    - Diamond Ore - 1
    - Lapis Lazuli Ore - 1
* **Average amount of ores in a group:**Set the average amount of block per ore veins.
  + **Vanilla ore amount:**
    - Coal Ore - 17
    - Iron Ore - 9
    - Gold Ore - 9
    - Redstone Ore - 8
    - Diamond Ore - 8
    - Lapis Lazuli Ore - 7
* **Generation height:**
  + **Minimal height:**The lowest point that the block can spawn at.
  + **Maximal height:** The highest point that the block can spawn at.
* **Additional generation condition:**Specifies a condition to be met, blocks will only spawn when the condition is met. Check out [this](https://mcreator.net/wiki/how-use-conditions) page for more info about conditions.

## Test and editing

* If you want to test your Block, Click Start Client towards the top middle of MCreator.
* **Your mod becomes visible in "My workspace" when you open My workspace.**
  + If you need to edit your element in "My workspace" click on "Edit element".

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

Another tutorial that shows how to make a custom ore based on the block:

See also

[Block dimensions and bounding box](/wiki/block-dimensions-and-bonding-box)

[Making GUIs with slots](/wiki/making-guis-slots)

[Minecraft materials list](/wiki/materials)

[Minecraft Particle IDs/names](/wiki/particles-ids)

[Procedure system](/wiki/procedure-system)

[Making a new Texture](/wiki/making-new-texture)

[How to Use Variables](/wiki/variables)

[List of Hardness Values of Blocks](/wiki/list-hardness-values-blocks)

[List of block resistance levels](/wiki/list-block-resistance-levels)

[How to Make an Item](/wiki/how-make-item)

[How to Make a Loot Table](/wiki/how-make-loot-table)

[How to Use Conditions](/wiki/how-use-conditions)