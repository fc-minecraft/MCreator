# Клавиши

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a key binding?

Key binding allows the player to associate in-game client-side actions with a specified button on the keyboard or mouse. Usually, key bindings are used to separate a specific event from commonly used buttons or to create shortcuts (for example you bonded F8 to cast a fireball spell or C to open a GUI). Custom key bindings will be shown in the Minecraft settings.

## Creation and naming

* First, go to the workspace.
* Next click on **Create new element**.
* Type the name of your keybind. **Names must not be repeated!** The end-user will not see this name, so name it something that you can identify. One way to prevent your keybind name from being repeated is to put the mod type before your mod name, Example: *KeybindAbilityFireball or KeybindGUIOpen*
* Click next.

## Settings

* **Key that triggers the event:** choose what will be the default key from the keyboard or mouse. Remember that players can still change it from the Minecraft menu for personal preferences
* **Key binding name:** this will be the name shown in the Minecraft Controls menu tab for your key bind
* **Key binding category translation key:** this will be the key binding category shown in the Minecraft Controls menu tab. For better organization, all keybindings belonging to the same category should have the same category key. To actually set the category name, go to Workspace -> Localization -> Add localization entry and use key.category.misc for the entry name and then set the value to the desired category name.

## Notes

Do not set the same key for two different events, this will cause conflicts that will not allow both events to work and also could confuse players. However its possible to do this by excluding one of the two events using conditions (for example you can use WSAD keybinds to control a flying entity by allowing the procedure to work only if the player is riding that entity, without breaking default player movement controls)

A good idea could be to check if another mod already has the same key bindings as yours, but this is not necessary since players can still change keys from the settings.