# Структуры

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

In this guide, you will learn the fundamentals for creating a basic structure using MCreator. The structure is a larger composition of blocks placed by the worldgen within the Minecraft world.

## Minecraft structure vs feature

Minecraft has two worldgen block composition types: structure and feature. The question is, when to use which?

Use structure when:

* your block composition is larger than 16x16 in width and depth, meaning it spans over more than one chunk
* you need to be able to locate the block composition using the Minecraft locate command
* you need the block composition to adapt to the terrain
* you are ok with block composition appearing at most once per chunk
* you are ok with a limited set of options the vanilla Minecraft structure configuration offers
* you want the block composition to be stored in the chunk data

Use [feature](/wiki/how-make-feature) when:

* your block composition is smaller than 16x16 in width and depth
* you want your block composition to appear multiple times in the chunk
* you need specific placement parameters that allow one to place the block composition in certain levels, over specific blocks, with specific distribution, etc.

Sometimes there will be an overlap of the needs. In this case, decide on the one offering you more of the options you need for your use case.

Examples of feature use cases: custom tree, plant, a small blob of blocks, small underground modifications, small loot dungeons, etc.

Examples of structure use cases: larger dungeons, houses, villages, big ship structures, etc.

## Building the structure

### Structure size

Structure blocks allow to create structures of 32x32x32 blocks. However, since Minecraft 1.16.x, Mojang has increased the maximal size for 48x48x48 blocks.

### Build the structure

#### Making a new build world

1. Start the test environment using the green play button at the top-right corner of your workspace.
2. Select **'Singleplayer'**.
3. Select **'Create New World'.**
4. Under the **'Game'** tab.
5. Name your world. Name it something like **"Build World"** so you can easily identify the world between testing worlds.
6. Set the **'Game Mode'** to **'Creative'.**
7. Make sure **'Allow Cheats'** is **'ON'.**
8. Select the **'World'** tab.
9. Set the **'World Type'** to **'Superflat'.** This will give you lots of building room to make your structures. You can also customize your flat world if you like, though it's not required.
10. Make sure **'Generate Structures'** is **'OFF'.**
11. Optional: Under the **'More'** tab
12. Optional: Click the **'Game Rules'** button.
13. Optional: Scroll down to **'World Updates'**
14. Optional: Turn off **'Advanced time of day'**, this will keep the time the same time, good for building structures in daylight. You can also use the /set time night or /set time day to change the time. If you change your mind the game rule can be enabled.
15. Optional: Turn off **'Update weather'**, this can help with preventing structures from being struck by lightning which can be hard to build flammable structures harder to build, this can be enabled using game rules in-game.
16. Optional: When done, make sure to click **'Done'** to save the settings.
17. The last thing to do is press the **'Create New World'** button.

#### Structure building tips

* **Structure Voids** can let the game know what parts of the structure other blocks can replace. This is handy when making things that you want to allow other blocks to have priority, some examples would be trees trees or underground structures. you can give yourself a structure void using the command '/give @p minecraft:structure\_void'
* **Blocks** you can use can be pretty much anything, you can use your modification blocks, Minecraft blocks, or even other mod blocks as long as the mod is installed for the client when they have your mod installed.
* **Cave Air** can be used in underground structures as a replacement for air blocks. In some cases, air will be seen as a replaceable block in underground structures using Cave air in your builds can help prevent this from happening.

#### Saving your structure

1. You can get the structure block by typing the following command "/give @p minecraft:structure\_block"
2. Open the F3 screen then go to the North-West-Bottom corner of your structure and place the structure block just outside the X and Z blocks as well as one block below the structure.
3. Place a structure block on the South-East-Top corner of your structure one block outside on the X and Z blocks and one block above the tallest point in the structure.
4. Set the top structure block to corner mode and set the name to something like corner. The name does not matter as you will be removing the structure block later.
5. After that go to the bottom structure block and select save mode then type the corner name you set in the top structure block. Press the DETECT button. This should auto-size the structure from the bottom structure block to the top structure block.
6. If your structure outline does not fit your structure you can adjust the two structure blocks and repeat the process until you get it to work or adjust the position manually.
7. Once happy with the outline break the top structure block as this can cause problems with other detections later on.
8. Now open the bottom structure block and give the structure a name you have not used in your mod yet. And save the structure.

## Importing structures

Keep in mind that your world still needs to exist in your workspace save folder for this to work. We suggest backing up your world so you have a copy you can work with if you accidentally delete it.

* First, go to the workspace.
* Next, click on **'Resources'**.
* Next, click on the tab called **'Structures'**.
* Next, click on **'Import structure from Minecraft'**.
* Next, select the structure that you just made.
* Click ok.

## Creating the mod element

### Creating and naming

* First, go to the workspace tab.
* Next, click on the **'Mod elements'** tab.
* Next, click the add button and select the **'Structure'** element.
* Enter the name of your element.

### Properties

You can find most of the information on the [Custom Structures](https://minecraft.wiki/w/Tutorials/Custom_structures) on the Minecraft Wiki.

|  |  |
| --- | --- |
| **Biomes to generate structure in** | This setting is required. You must set what biomes the structure is allowed to generate using tags or individual biomes. |
| **Separation** | The minimum distance is in chunks. Needs to be smaller than spacing. |
| **Spacing** | Roughly the average distance in chunks between two structures in this set. |
| **Generation stage** | This option determines at what stage of world generation the structure should be added.  This is the "step" setting in the structure.json file.  The list is ordered from top to bottom, bottom being late generation top being early generation. |
| **Type of reference ground detection** | This setting controls where the structure should be placed using the world [height map](https://minecraft.wiki/w/Heightmap).   * **WORLD\_SURFACE\_WG**   + Detects the highest Y-level non-air blocks.   + Only used during world generation and detected after carvers are generated. * **WORLD\_SURFACE**   + Detects the highest Y-level non-air blocks. * **OCEAN\_FLOOR\_WG**   + Detects the highest Y-level material block with a collision box.   + Only used during world generation and detected after carvers are generated. * **OCEAN\_FLOOR**   + Detects the highest Y-level material block with a collision box.   + The only exception is carpets which are ignored.   + Used only on the server side. * **MOTION\_BLOCKING**   + Detects the highest Y-level material block with a collision box.   + Supports fluid blocks such as water, lava and waterlogging blocks. * **MOTION\_BLOCKING\_NO\_LEAVES**   + Detects the highest Y-level material block with a collision box.   + Supports fluid blocks such as water, lava and waterlogging blocks.   + Ignores blocks that are leaves.   + Used only on the server side. |
| **Terrain adaption type** | This parameter controls how the terrain adapts to the structure.   * **none** - No effect on the terrain. * **beard\_thin** - Adds terrain below the structure and removes inside the structure. This is what villages use. * **beard\_box** - Stronger version of beard\_thin. This is what ancient cities use. * **bury** - Adds terrain completely around the structure. This is what strongholds use. |
| **Structure to generate** | This is the structure you want to generate using the element. |
| **Projection to the terrain** | This parameter controls how the structure adapts to the terrain.   * **rigid** - This will flatten the terrain around the structure. In most cases rigid will be the one you want to use for your structure as it keeps the structure form. This is the setting village houses use. * **terrain\_matching** - This will place the blocks while following the terrain blocks. This is the setting village paths use. |
| **Blocks to ignore when placing** | Any blocks in this list will be ignored when placed.  Select air here to not place air blocks in your structure. This will make the structure integrate better with the environment, but in the case of cave-based structures, you will likely want to place the air too. |

### Jigsaw

|  |  |
| --- | --- |
| **Jigsaw generation depth** | * This controls how many times the structure connects to other structure parts from the main structure. |
| **Maximum distance from main structure** | * This controls how far jigsaw parts can be generated from the main structure. |
| **Jigsaw pool name** | * When you add your jigsaw blocks to your structures the "Target pool" field controls what parts can generate from that jigsaw block. * This setting should be the same path as that value in the game. |
| **Fallback pool name** | * This is the pool that should be used if the first pool did not generate a structure. |
| **Entry weight** | * This controls the probability of the structure to generated. |
| **Structure** | * This selects what structure should be generated when the entry is selected. |
| **Projection to the terrain** | * This parameter controls how the structure adapts to the terrain.   + **rigid** - This will flatten the terrain around the structure. In most cases rigid will be the one you want to use for your structure as it keeps the structure form. This is the setting village houses use.   + **terrain\_matching** - This will place the blocks while following the terrain blocks. This is the setting village paths use. |
| **Blocks to not place** | * This prevents selected blocks from generating in the structure. * This is good for selecting jigsaw blocks and structure blocks. |

#### Using Jigsaw Blocks

You can find all the information about how to use jigsaw blocks on the [Jigsaw Block](https://minecraft.wiki/w/Jigsaw_Block) Minecraft Wiki page.

## Jigsaw structures size

For jigsaw blocks to attach reliably one of 2 things must occur:

* The jigsaw block must be at the edge of the structure so that the next structure generates entirely outside of the current boundaries of the current structure. This applies to both the target and the source jigsaw block.
* The attached structure must fit entirely within the boundaries of the current structure.

## Tips

### Finding your structure

You can use the locate command to find your structure.

Use the **"/locate structure namespace:structure\_name"** command to find your structure.

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

### Minecraft (Jigsaw) structures

### How to use Minecraft Jigsaw block

See also

[How to Make a Block](/wiki/how-make-block)

[How to make a Feature](/wiki/how-make-feature)

[Mod Element Types](/wiki/mod-types)

References

Minecraft Wiki - Structures
<https://minecraft.wiki/w/Structure>

Minecraft Wiki - Custom Structures
<https://minecraft.wiki/w/Tutorials/Custom_structures>

Minecraft Wiki - Structure Sets
<https://minecraft.wiki/w/Structure_set>

Minecraft Wiki - Template Pool
<https://minecraft.wiki/w/Template_pool>

Minecraft Wiki - Jigsaw Structures
<https://minecraft.wiki/w/Jigsaw_structure>