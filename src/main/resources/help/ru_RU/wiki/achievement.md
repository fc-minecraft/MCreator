# Достижения

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## Creating an advancement

* Start MCreator
* Click on the "Workspace" tab.
* Click on the "Mod Elements" tab.
* Now click on the "Plus" button.
* Select "Advancement" for the list.
* Name it and click "Advancement"
* Just edit the settings on this page to your liking.
* Click next then finish and you're done!

## Settings and Configuration

### Advancement display parameters

| Settings | Information |
| --- | --- |
| Advancement GUI name | This is the name given to be displayed for the task. If the task is a parent then it will also be the advancement tab name. |
| Advancement description | This is where you put information about how to achieve the task. |
| Advancement icon | This is the tasks display icon. If this is a root parent then it will be the advancement icon too. |
| Advancement background | If your advancement is the first of a new path, you can choose the background of the path with this option. |
| Advancement type | This is the type of achievement   * **Task** is a basic achievement type and is most common. * **Goal** is a long term goal witch you strive to achieve. * **Challenge** is to test a player or challenge them to something. |
| Advancement parent | This is the path of witch your achievement will be listed under. You can use "No parent: root" will make a new path. Minecraft main paths are story, nether, end, adventure, and husbandry. |
| Show toast when completed | Show the advancement top right of the screen when the player completes the advancement. |
| Announce to chat when completed | This sends a message to the chat for all players to see when the advancement has been completed. |
| Hide if not completed yet | The advancement is hidden until it has been completed by the player. This is useful for parent tasks to hide the hole advancement tab. |
| Hide advancement display | This hides the advancement completely from the advancement tab. Even when the task has been completed the task will still not show. |

### Advancement logic

|  |  |
| --- | --- |
| **Settings** | **Information** |
| Reward XP | This is how many experience the player will receive after completed the advancement. |
| Reward function | You can choose a function to execute when the player completes the advancement. |
| Reward loot tables | The player will receive some items from the selected loot table. |
| Reward recipes | This can be used to unlock recipes that the player has not learned yet. |

## Making your achievement obtainable

### Advancement trigger builder

If your advancement has a little trigger, you can use this method. If you want to make a more complex trigger, or a trigger with procedure block, you can always use a procedure.

* Select a procedure block, and add it before the "Grant this advancement to the player" block.
* Select the block/item/biome/dimension/numbers if needed.
* Save your advancement.

### Via a procedure

How do you make it so you gain your achievement? You will need to make a procedure on the element that you wish to trigger the achievement to happen. When you are making the procedure, you will want to follow the steps below.

* Go to the procedure events in your mod element
* Choose the event trigger and create a procedure if you didn't have one.
* If you want a second condition, create it with the "If Do" procedure block.
* Then, add the element that says "Add achievement to provided player - Achievement" (In the "Player Procedures" section)
* Select the achievement you just made in the dropdown box.
* Save your procedure and your mod element.

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in the videos below:

See also

[Usage of Events](/wiki/usage-events)

[Procedure system](/wiki/procedure-system)

[How to Make a Loot Table](/wiki/how-make-loot-table)

[Minecraft advancements list](/wiki/minecraft-advancements-list)

References

Minecraft Wiki - Advancements
<https://minecraft.wiki/w/Advancement>