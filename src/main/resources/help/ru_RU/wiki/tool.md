# Инструменты

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a tool?

A tool is an object used to extract certain blocks, deal more damage, and do special things like change certain blocks that the player's hand alone cannot do. Normally tools aren't stackable and have a durability bar (unless the tool is unbreakable) that indicates the remaining durability when the tool is used and warns the player when the tool is about to break. Tools can be repaired using one or multiple items in anvils or combining them in a crafting table or in a grindstone.

### Visual

#### Tool texture

The texture of your tool

#### Tool 3D model

Select the model to be used with this item. The model only defines the visual look.

* **Normal:** The default model used by the Vanilla tool selected
* **Custom:** A custom JSON or OBJ model you made and imported into MCreator.

When making custom models, JSON is recommended due to vanilla support for this model type.

#### Has item glowing effect

If a condition is specified, the item will only glow in case this condition is passed. This will be ignored if the glow effect is disabled.

#### Enable glowing effect

Checking this check box will always enable the glowing effect on your tool, the same way enchantments work.

#### Special information

This will add text like how enchantments show.

### Properties

#### In-game name

The display name for the tool in the GUI.

#### Type

The type of tool you wish to create.

| Tool type | Description |
| --- | --- |
| Pickaxe | Mines better rocky materials. |
| Axe | Mines better wood and can strip logs. |
| Sword | Deal more damage to entities and can do a sweep attack. |
| Spade | Mines better dirt, grass, gravel, sand and can create dirt paths. |
| Hoe | Mines better organics blocks and can turn dirt into farmland. |
| Shield | Protects players from incoming attacks. |
| Shears | Mines better plant and allow to get some blocks as drops like grass. |
| Fishing rod | Allows you to fish from water sources. |
| Special | An advanced custom tool type, basically you choose what it will do. |
| MultiTool | An advanced tool type, it's quite self-explanatory. |

#### Creative inventory tab

The tab in which the item is under is in creative mode.

#### Block drop tier

Tool tiers control whether a tool can break a block with a drop. Better tools can also break blocks that require lower-tried tools, e.g. stone can break blocks that require stone as well as wood.

| Tier name | Description |
| --- | --- |
| Wood | * Sets the tool tier to wood. * Blocks mined will only drop if they can be mined with Wood or better. |
| Stone | * Sets the tool tier to stone. * Blocks mined will only drop if they can be mined with Stone or better. |
| Iron | * Sets the tool tier to iron. * Blocks mined will only drop if they can be mined with Iron or better. |
| Diamond | * Sets the tool tier to diamond. * Blocks mined will only drop if they can be mined with Diamond or better. |
| Gold | * Sets the tool tier to gold. * Blocks mined will only drop if they can be mined with Gold or better. |
| Netherite | * Sets the tool tier to netherite. * Blocks mined will only drop if they can be mined with Netherite or better. |

#### Efficiency

How fast the tool can mine. You can add more levels. Example: Stone tools have lower efficiency than diamond tools.

| Tool | Efficiency |
| --- | --- |
| Wood | 2 |
| Stone | 4 |
| Iron | 6 |
| Diamond | 8 |
| Netherite | 9 |
| Gold | 10 |

#### Enchantability

How common rare enchantments can be enchanted on this tool. The higher the enchantability, the better enchantments you will get when enchanting the tool.

| Tools | Armor | Other |
| --- | --- | --- |
| * **Wooden**: 15 * **Stone**: 5 * **Iron**: 14 * **Gold**: 22 * **Diamond**: 10 * **Netherite**: 15 | * **Leather**: 15 * **Chainmail**: 12 * **Iron**: 9 * **Gold**: 25 * **Diamond**: 10 * **Netherite**: 15 | * **Books**: 1 |

#### Attack damage

The level of damage dealt when attacking with this weapon. This is **not**the amount of health the weapon takes. Instead, 1 gives the tool the same attack strength as a wooden tool, 2 is stone, etc. In other terms. (For some reason, axes use the 1.8 attack damage here)

Shovels do exactly the amount of damage you put here. Pickaxes do one damage more than you put here. Axes do two damage more than you put here. Swords do three damage more than you put here.

| Tool | Wood | Stone | Iron | Gold | Diamond | Netherite | N/A |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Axe | 7 | 9 | 9 | 7 | 9 | 10 | -- |
| Hoe | 1 | 1 | 1 | 1 | 1 | 1 | -- |
| Pickaxe | 2 | 3 | 4 | 2 | 5 | 6 | -- |
| Shovel | 1.25 | 1.75 | 2.25 | 1.25 | 2.75 | 3.25 | -- |
| Sword | 4 | 5 | 6 | 4 | 7 | 8 | -- |
| Trident | -- | -- | -- | -- | -- | -- | 9 |
| Other | -- | -- | -- | -- | -- | -- | 1 |

#### Attack speed

This is the speed at which the item has when using the attack mouse button. Some tools use different speeds such as axes vs. pickaxes.

| Tool | Wood | Stone | Iron | Gold | Diamond | Netherite | N/A |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Axe | 0.8 | 0.8 | 0.9 | 1.0 | 1.0 | 1.0 | -- |
| Hoe | 1.0 | 2.0 | 3.0 | 1.0 | 4.0 | 4.0 | -- |
| Pickaxe | 1.2 | 1.2 | 1.2 | 1.2 | 1.2 | 1.2 | -- |
| Shovel | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 | -- |
| Sword | 1.6 | 1.6 | 1.6 | 1.6 | 1.6 | 1.6 | -- |
| Trident | -- | -- | -- | -- | -- | -- | 1.1 |
| Other | -- | -- | -- | -- | -- | -- | 4.0 |

#### Number of uses

The amount of time the tool can be used before it breaks.

| Tool name | Maximum uses |
| --- | --- |
| Gold | 32 |
| Wood | 59 |
| Stone | 131 |
| Iron | 250 |
| Diamond | 1561 |
| Netherite | 2032 |
| Fishing rod | 64 |
| Flint and steel | 64 |
| Carrot on a stick | 25 |
| Shears | 238 |
| Trident | 250 |
| Crossbow | 326 |
| Shield | 336 |
| Bow | 384 |
| Elytra | 432 |

#### Repair items

The item(s) with which the tool can be repaired in an anvil.

#### Shield blocking model

The model that should be used when the shield tool type is in the blocking state.

#### Blocks affected

What blocks the tool can be used on. This is for the advanced tool types.

#### Is the tool immune to fire?

* This parameter controls if the item is immune to fire, like Netherite stuff.
* NOTE: Only available in Minecraft 1.16.x and higher

#### Does the item stay in the crafting grid when crafted?

Check this if you want the item to stay on the crafting table.

#### Damage the item instead, when crafting

If "stay in the grid" is enabled, you can enable this parameter to deal damage to this tool instead of keeping it with its current durability.

### Triggers

On this page, you can find multiple triggers triggered by different actions and execute a procedure to perform more actions.

The `itemstack` dependency will always be an instance of this tool.

It can notably be used to make a hammer using the "When block destroyed with tool" trigger.

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

See also

[Making a new Texture](/wiki/making-new-texture)

[Minecraft Enchantability](/wiki/enchantability)

[How to Make an Item](/wiki/how-make-item)

[Procedure system](/wiki/procedure-system)

[How to Use Conditions](/wiki/how-use-conditions)

References

Minecraft Wiki - Damage
<https://minecraft.wiki/w/Damage>

Minecraft Wiki - Tools
<https://minecraft.wiki/w/Tool>

Minecraft Wiki - Weapons
<https://minecraft.wiki/w/Weapon>