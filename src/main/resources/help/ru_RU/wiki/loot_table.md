# Таблицы лута

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

*Loot tables are technical JSON files that represent what items should be in naturally generated containers, what items should drop when breaking a block or killing a mob, or what items can be fished. It does not affect dropped experience, or dropped non-item entities such as slimes from larger slimes or silverfish from infested blocks.* [Official Minecraft Wiki](https://minecraft.gamepedia.com/Loot_table)

## Creating a Loot table

* Click on "Add New Mod Element"
* Choose "Loot table" and name it.
* Set the properties of the loot table
* Save your mod element

### Loot table properties

**Registry name:** Click on the dropdown list and choose the "category" of the loot table. It doesn't define the loot table type. It's only to standardize loot table names. After, you have to change the last part of the name (after the "/") for your name. For example, I'm making a new loot table for the cow. I will select "entity/chicken" and replace the "chicken" by "cow".

**Namespace:** If you want to change a vanilla loot table, you have to choose "minecraft", but if you want to create a new loot table for your mod, choose "mod".

**Type:** It will define which type of loot table you make.

### Loot table pools

**Loot table pool:** A loot table pool is a group of blocks/items or a single block/item. When the game has to use the loot table, it will take a random number of blocks/items of each pool (between the minimum and maximum you have set). You can put as many pools as you want in one loot table.

**Add loot table pool:** Add a new pool in the loot table.

**Remove this pool:** Remove this whole pool

**Min rolls:** It's the minimum number of blocks/items the game will take. (You can put it to 0.)

**Max rolls:** It's the maximal number of blocks/items the game will take.

**Bonus roll(s):** The bonus rolls are an optional option used to add a possibility to give more rolls to a specific pool. (The min and max work the same way than before).

**Add pool's table entry:** Add a new entry in the pool

**Remove this entry:** Remove this specific entr

**Entry item**: The block/item

**Entry weight:** The possibility of the block/item to be used.

**Min count:** The minimal number of the block/item that will be dropped.

**Max count:** The maximal number of the block/item that will be dropped.

**Affected by fortune:** This setting defines if the entry is affected by the fortune enchantment.

***Enable explosion decay:** This setting defines if the entry is affected by the explosions.*

**Silk touch mode:** This setting defines how the entry reacts with or without silk touch.

**Entry enchantment min and max:** The minimal and maximal level of enchantments on the entry. Enchantments are random.

## Use a chest loot table

To use a loot table for a chest (or another block with storage like the Barrel) you need to execute the following command.

*/data merge block ~ ~ ~ {LootTable:"minecraft\_or\_your\_mod\_id:the/loot\_table/registry/Name"}*

*The three ~ are how many blocks in X, Y, Z of your position the block is.*

*The content inside the {} is the namespace you put (so minecraft: or yourModID:), and the registry name of your loot table.*

## Calculating the weight chance

Weight is division between the total weight for all items in that pool, this can make it harder to calculate the chance of an specific item for dropping. However it is possible to calculate though some basic math.

* **SUM** = the total weight in the pool
* **WEIGHT** = the weight of the value you want to know about.
* **VALUE** = the value from the first part of the calculation.
* **CHANCE** = the chance of the item to drop.
* **/** = division

```
SUM / WEIGHT = VALUE
100 / VALUE = CHANCE
```

EXAMPLE
Calculate sticks chances we can use the math above.

**Apple** has a weight of **100**
**Sticks** has a weight of **40**
Total weight is **140** ( SUM )

140 / 40 = 3.5 
100 / 3.5 = 28.57%

The total odds of the stick dropping out of 100 is 28.57% of the time.
Apples have a 71.43% chance each time the loot table is run.

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

A tutorial showing how to use loot tables in procedures:

See also

[Minecraft vanilla loot tables list](/wiki/minecraft-vanilla-loot-tables-list)

[Minecraft Entity list](/wiki/entity-ids)

[All of Minecraft blocks and all of Minecraft items](/wiki/minecraft-block-and-item-list-registry-and-code-names)

References

Minecraft Wiki - Loot Table
<https://minecraft.wiki/w/Loot_table>