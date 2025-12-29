# Переменные

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Procedure system](/wiki/section/procedure-system)

This page will teach you some things about variables.

## What is a variable

A variable is a value that holds data like states or numbers or even words for later use. They are useful when needing to test if something is happening or if a word is the same as something else, they can also be used in math to test for number values.

## Variable types

There are four kinds of variables, Logic, String, Numbers, and Itemstack. Each type holds specific types of data.

* **Logic:** Stores true or false data to blocks, worlds or maps. The real term for this kind of a variable is a boolean.
* **String:** Stores text data to blocks, worlds or maps. The real term for this kind of a variable is actually a string.
* **Number:** Stores solid or decimal numbers data to blocks, world or maps. The real term for this kind of a variable is an integer.
* **Itemstack:** Stores Minecraft item or blockitem data to blocks, world or maps. The real term for this kind of variable is an itemstack.
* **Block state**: Stores a block with its current values (including NBT).

## (Global) variables

Global variables save variable data to dimensions or entire maps or only run during a session or to a player instance. Below lists each global variable type and some of the key features each one has to offer.

* **Global session:** Sets a variable to all worlds, main screen, and menus, but does not save them.
* **Global world:** Sets and saves a variable to a dimension.
* **Global map:** Sets and saves a variable to the entire map/save
* **Player lifetime:** A variable specific to each player, it holds its value for the time of player living and is reset when a player dies. This variable is synced to the clients, *unlike entity NBT tags*.
* **Player persistent:**A variable specific to each player. This variable is synced to the clients, *unlike entity NBT tags*.

## Local variables

Local variables can be used in mod elements are store the variable locally within that element. Each procedure execution will reset these variables, thus local. They use all four variable types listed above.

Local variables are meant to be used to store value for the time of procedure execution, for example, a counter inside a while block. They can not be used for tick counters, for example, as they will reset each tick trigger.

## NBT variables/tags

NBT are variable types like local entities however rather than having a worldwide variable set per element you can target a specific block, item or entity to have a variable stored in them.

Block and item NBT variables are synced to the clients, but entity NBT variables hold own instance for server and client-side and are not synced.

If you want to display, for example, player stats in an overlay which is client-side, use player variables instead as they are synced for you to the client for display.

## Syncing variables

Global variables are automatically synced from the server to all the clients, but not in the opposite direction. This means variables can only be effectively updated from the server side, in order to be synced to all the clients. This is to prevent the possibility that a client could sync arbitrary variable values to the server, allowing for hacks and exploits to be made.

## Variables explained in videos

If you prefer to watch the video, you can find the wiki page above summarized in videos below:

See also

[Developing multiplayer compatible mods](/wiki/developing-multiplayer-compatible-mods)

[Procedure system](/wiki/procedure-system)

[How to Make a Block](/wiki/how-make-block)

[How to Make a Living Entity](/wiki/how-make-mob)

[How to Make an Item](/wiki/how-make-item)