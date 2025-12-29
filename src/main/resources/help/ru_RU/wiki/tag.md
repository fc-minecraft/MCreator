# Связки (Tags)

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What are Minecraft tags?

Tag elements give mod creators a way to group vanilla, mod or other creator mods to a group. Tags can then be used to share recipes or groups or items in procedures. There are a bunch of settings and a few ways to configure your tag so let's get started.

## MCreator 2024.1 and newer

In MCreator 2024.1, the tags are no longer a mod element, but rather a panel on the workspace tab. See the tutorial video below to learn more about how the tags are managed in 2024.1 and newer MCreator versions.

## Naming the tags

If your mod is made for Minecraft versions higher than 1.13.2 you will want to make sure Tag Namespaces are set up properly. Tags can be used for blocks and items like Ore Dictionary, but aside from that tags can also be used in procedures or in other MCreator elements.

### Valid names

* acacia\_log
* lava
* oakboat

## Tag Namespace

Tag Namespaces have three option, this setting defines the way the tag will function. Read the settings below to further understand what each setting offers and can be used for.

* **Forge:** is a replacement for Ore Dictionary for tags. They can be used to give other mod creators access to their mod with your mod if you provide them with the tag name.
* **Minecraft:** is used to add custom blocks or items to vanilla tag groups. For example, adding your mod logs to Minecraft's log group.
* **Mod:** is used for grouping things in your mod only.

## Tag Type

Tag Namespaces have three option, this setting defines the way the tag will function. Read the settings below to further understand what each setting offers and can be used for.

* **Items:** both Tags and Ore Dictionary can use the item type.
* **Blocks:** only use the block type if you are targeting a block. Ore Dictionary does not have support for this type.
* **Functions:** only use this for Tags. This is used for tagging functions into groups. One such group is called "tick" from the "Minecraft" namespace listed above. Functions tagged under the "tick" namespace will be executed each game tick.

## Procedures Use

When you are working with procedures you can also test for tags for both blocks and items, using the namespace followed by a colon then tag name.

* Use the namespace "**forge:**" with a colon then the tag name to test for tags from **other mods.**
  *(To use this one your item or block must be under the "forge" namespace)*
* Use the namespace "**minecraft:**" with a colon then the tag name to test for tags from **Minecraft.**
  *(To use this one your item or block must be under the "minecraft" namespace)*
* Use the namespace "**mod\_id:**" with a colon then the tag name to test for tags from **your mod.**
  *(To use this one your item or block must be under the "mod" namespace)*

### Examples

* **c:copper\_ingot** - This will test for a tag used by another mod called "copper\_ingot"
* **minecraft:logs** - This will test for a tag that Minecraft uses called "logs"
* **ruby\_craft:ruby** - This will test for a tag in your mod that is part of the tag "ruby"

![Tag procedure example](//cdn.mcreator.net/sites/default/files/wiki/tags_1.png)

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

See also

[How to Make an Item](/wiki/how-make-item)

[How to Make a Block](/wiki/how-make-block)

[How to Make a Tool](/wiki/how-make-tool)

[How to Make a Recipe](/wiki/how-make-recipe)

[Commonly used tags by Minecraft mods](/wiki/commonly-used-tags-minecraft-mods)

References

Minecraft Wiki - Tags
<https://minecraft.wiki/w/Tag>

Minecraft Forge - Ore Dictionary tag information
<https://mcforge.readthedocs.io/en/latest/utilities/tags/>