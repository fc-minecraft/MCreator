# Предметы

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## Before you start

You will need to make a texture either by an editing program such as Gimp or Paint.net, or you can simply use the embedded editor. If you need help making it, go to [Making a New Texture](/wiki/making-new-texture).

## Creation and naming

* First, go to the workspace.
* Next, click on **Create new element**.
* Type the name of your block. **Names must not be repeated!** The end-user will not see this name, so name it something that you can identify. One way you can prevent your block name from being repeated is to put the mod type before your mod name, for Example: *BlockSteel or ItemEnderStick*.
* Click next.

## Visual

This section covers the settings on the Visual page for items.

|  |  |
| --- | --- |
| **Item texture** | Click the box to select the texture you imported earlier. |
| **Item 3D model** | You can use Blockbench to create JSON models using the block/item workspace.  OBJ files are also supported. |
| **Has item glowing effect?** | Check this box if you want the item to shine like enchantment books or potions.  You can use a procedure to make a condition when the glow effect should glow by returning true. |
| **Special information** | This is the lore text for the item, similar to how potions display extra information about the item under the item display name.  You can use a procedure to make a condition to set dynamic text by returning a text value. |

## Item states

Read [this Wiki page](https://mcreator.net/wiki/item-properties) to know more about custom item states

## Properties

This section covers the settings on the Properties page for items.

|  |  |
| --- | --- |
| **In-game name** | This is the name that shows in Minecraft |
| **Rarity** | The rarity affects only the color of the item's name.   * Common: White * Uncommon: Yellow * Rare: Aqua * Epic: Light Purple |
| **Creative inventory tab** | These are the creative tabs you can find this item in creative mode. |
| **Max stack size** | This is how many of your items can fit in a stack. |
| **Enchantability** | How common rare enchantments can be enchanted on this tool. The higher the enchantability, the better enchantments you will get when enchanting the tool.  **Tools:**   * Wooden tools: 15 * Stone tools: 5 * Iron tools: 14 * Gold tools: 22 * Diamond tools: 10 * Netherite tools 15   **Armor:**   * Leather armor: 15 * Chainmail armor: 12 * Iron armor: 9 * Gold armor: 25 * Diamond armor: 10 * Netherite armor: 15   **Other:**   * Books: 1 |
| **Item destroy speed** | The destroy speed parameter controls how fast this item destroys the blocks.  **Typical values:**   * **1** - For a normal item * **1.5** - For a sword * **2>** - For a harvesting tool |
| **Attack damage** | You can activate melee damage for your item with this parameter, and set a value of the damage. |
| **Item use count/durability** | This parameter controls the durability of the item (how many times the item can be used).  Set this value to 0 to disable the use count mechanic on the given item.  Vanilla values for reference:   * Gold: 32 uses: * Wood: 59 uses. * Stone: 131 uses. * Iron: 250 uses. * Diamond: 1561 uses. * Netherite: 2031 uses. * Fishing rod: 64 uses. * Flint and steel: 64 uses. * Carrot on a stick: 25 uses. * Shears: 238 uses. * Trident 250 uses. * Crossbow: 326 uses. * Shield: 336 uses. * Bow: 384 uses. * Elytra: 432 uses. |
| **Is item immune to fire?** | This parameter controls if the item is immune to fire, like Netherite stuff.  Only available in Minecraft 1.16.x and higher |
| **Can destroy any block?** | Check this box if you want it to destroy any block even bedrock. |
| **Does the item stay in the crafting grid when crafted?** | Check this if you want the item to stay on the crafting table. |
| **Damage the item instead, when crafting** | If "stay in the grid" is enabled, you can enable this parameter to deal damage to this tool instead of keeping it with its current durability. |
| **Recipe remainder** | This is the item that will be replaced when used in crafting tables. This means the item will be part of the recipe but will be replaced with another item. |
| **Item animation** | This displays the animation when the item is used.   * eat * block * bow * crossbow * drink * none * spear |
| **Item use duration** | This value controls how long the item takes to complete the use cycle.  This value is measured in ticks (20 ticks per second).  Use non-negative numbers (1 or higher) for food items to be edible. |

## Food Properties

This section covers the settings on the Food Properties page for items.

|  |  |
| --- | --- |
| **Is this item a food?** | Check this box to enable the item to be a food item. |
| **Nutritional value** | This controls how many food bars the food fills.  Each 1 is half a food item on the HUD screen.  The default value is 4. |
| **Saturation** | This controls how long the player stays with a filled food bar before becoming hungry again.  The default value is 0.3. |
| **Eating item result** | This will act the same way as a buck of milk or mushroom soup when eaten, a item will be given back to the player. |
| **Is this food meat?** | Check this box if you want to feed wolves the food to heal them. |
| **Is always edible?** | This makes the food always edible similar to how potion items can always be used. |

## Advanced properties

This section covers the settings on the Advanced Properties page for items.

### Inventory

|  |  |
| --- | --- |
| **Bind this item to GUI** | This parameter will bind your item to the selected GUI.  When selected, it will automatically make the code open the GUI when the player right-clicks on the item.  *If you make a procedure, items won't be saved inside the item.* |
| **Size inventory** | Number of slots inside your GUI.   * *Biggest slot ID in the GUI + 1* |
| **Max stack size** | The maximal number of items in one stack. |

### Range item properties

|  |  |
| --- | --- |
| **Is this item ranged** | Check this box if the item should act like a range item.  This will allow the item to shoot projectiles. |
| **Projectile to shoot** | This is the projectile the item should shoot when used. |
| **Shoot constantly when active?** | If you want the item to have a rapid-fire when the player holds the fire button then check this box. |
| **Can use range item** | You can control when the range item can be used using procedures to make a condition and return a true value. |
| **When range item shoots projectile** | You can control when the projectile can shoot using procedures to make a condition and return a true value. |

## Triggers

On this page, you can find multiple triggers triggered with different actions and execute a procedure to do more actions.

The `itemstack` dependency will always be an instance of this tool.

It can notably be used to make a hammer using the "When block destroyed with tool" trigger.

You can also use `Player finishes using item` for food items to run an event after the food is eaten.

## Item extensions

You can also use item extensions to expand the functionality of the item for things such as making the item a fuel item, setting the dispense output and even making the item compostable. You can read more about this on this [wiki page.](https://mcreator.net/wiki/how-make-item-extension)

## Video tutorial

### Making an item mod element

If you prefer to watch the video, you can find the wiki page above summarized in a video:

### Item states / item properties

See also

[Making a new Texture](/wiki/making-new-texture)

[Minecraft Enchantability](/wiki/enchantability)

[Minecraft Particle IDs/names](/wiki/particles-ids)

[Procedure system](/wiki/procedure-system)

[How to Use Variables](/wiki/variables)

[GUI Editor](/wiki/gui-editor)

[How to Make a Food](/wiki/how-make-food)

[How to make a Projectile](/wiki/how-make-projectile)

References

Minecraft Wiki - Items Index
<https://minecraft.wiki/w/Item>