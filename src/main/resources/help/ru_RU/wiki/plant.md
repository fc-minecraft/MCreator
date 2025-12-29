# Растения

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What Is a plant?

A plant is a specific kind of block with some feature that allows it to be placed only on specific blocks, grow, tick randomly without being forced, and generated in patches on the ground. Remember that plants don't have many properties even if they are blocks, so If you're planning to make a block, check [How to make a Block](/wiki/how-make-block) instead.

## Creating the texture

Before creating your plant, you'll need a texture for it. You can use external programs such as Paint.net or Gimp or the built-in texture editor. The texture has to be saved as a block texture. For more info, you can check the page [Making a new Texture](/wiki/making-new-texture).

## Creating the plant

* Open your workspace
* Click on **Add new mod element**
* Select **Plant** from the list, or press n for a shortcut
* Enter the name of your mod element

### Texture and type

* Choose a texture
* Choose a 3D model for your plant: the default options are the vanilla cross and crop models, but you can also use models you imported for blocks
* Choose a hitbox for your plant (optional)
* Choose the type of your plant:
  + **Static plant:** you can choose between flower and grass type **only for a different generation.**Flower type will make your plant generate like vanilla flowers in groups randomly, grass type instead will be generated massively to cover the entire biome area
  + **Double plant:** two blocks tall, behave like static plants but you will need two different textures for the top and the bottom
  + **Growable plants:** behave like sugar canes and they will be generated everywhere in different heights.
    - **Max. Height:** the plant will naturally (even during generation) grow until it reaches this height randomly

### Plant properties

* **In-game name:** This is the name of your plant when you hover over it in-game
* **Creative inventory tab:** This is where your plant will be in the creative inventory. Usually, plants are found in *Decorations*.
* **Hardness:** This is how long it takes to mine the plant. A higher value means it takes longer to mine. Usually, vanilla plants have 0.
* **Resistance:** This is how the plant reacts to explosions. A higher value means the plant is more resistant to explosions. Usually, vanilla plants have 0.
* **Jump factor:**player jump height modifier like slime blocks
* **Speed factor:**player speed modifier like soul sand
* **Sound on step:** This is the type of sound your plant will make when placed, destroyed, or walked on. It's usually set to *Plant*
* **Luminance:** This is how much light the plant gives off, as a value between 0 and 15. If it's set to 0, the plant won't emit any light. If it's set to 15, the plant will have a light value of 15 (same as Glowstone).
* **Is unbreakable:** If checked, the plant can't be destroyed, behaving like Bedrock.
* **Is replaceable:** If checked, the plant can be replaced by other blocks. If you're making custom grass, this option should be enabled.
* **Creative pick item:** This is the item that will appear in your inventory when you "pick" (middle-click with the mouse) the plant. If empty, the item will be the plant itself.
* **Custom drop:** This option makes it so the plant drops something else. You'll need at least one item or block before setting this option. If you want to create a plant with custom loot check the box "Use loot table for drops" and [create a loot table.](https://mcreator.net/wiki/how-make-loot-table)
* **Drop amount:** How many items are dropped when the plant is broken. It shouldn't be higher than 1 if the plant drops itself.

### Advanced Properties

* **Has block entity:**check this parameter if you want to make your plant have NBT. Warning: block entities generate more data and therefore lag than normal blocks so it is recommended not to create a plant with a block entity and let it generate massively
* **Force Plant Ticking:** force your plant to tick with the world. This could be good if your plant is generated with structures.
* **Plant color on the map:** the color of the plant on the map. It is set to default the color will be the same as the plant material
* **Is flammable:** If checked fire can spread to and stay on this plant.
* **Plant type**: This setting determines where the plant can stay. Check [this page](/wiki/plant-types-list) for more info.

### Triggers

If you want your plants to have custom events

### Generation and events

#### Plant spawning settings

* **Spawn frequency on chunks:** This option determines how often chunks try to generate this plant. Plants aren't generated individually but in patches. For example, setting this value to **1** does NOT mean there's only one plant in each chunk; instead, each chunk will have (at most) one cluster of these plants. Because of this, setting this value to a low amount (4 or less) is enough for most purposes.
* **Patch size**: This parameter determines how many plants at most can generate in a single patch. Higher values mean more plants in a single patch.
* **Generate plant at any height (1.18.x and above only):** check if your plant can spawn only on the surface or in the caves too. This option is necessary for 1.18.x for Nether/Nether-like dimensions plants because they are considered caves.
* **Spawn dimensions:** Plants will try to spawn in all the dimensions that appear in the list. Keep in mind plants won't generate if no block can hold them. For example, static plants with plains won't appear in the End because there is no default grass/dirt-like a block.
* **Restriction biomes:** If empty, your plant can spawn anywhere in the specified dimensions; otherwise, it'll generate only in the biomes you picked here.
* **Addition conditions:** if you want your plant generate with more custom settings

You can now click on the **Save mod element**. Your plant is now done!

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

### Video tutorial - single crops

### Video tutorial - double crops

See also

[Making a new Texture](/wiki/making-new-texture)

[How to Make a Block](/wiki/how-make-block)

[Plant Types List](/wiki/plant-types-list)

[Burn Time of Fuels](/wiki/burn-time-fuels)

[List of Hardness Values of Blocks](/wiki/list-hardness-values-blocks)

References

Minecraft Wiki - Plants
<https://minecraft.wiki/w/Plant>