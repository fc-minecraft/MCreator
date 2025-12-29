# Наложения

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is an overlay?

Overlays are special GUIs that appear client-side in the player or block interface. Examples of overlay uses are placing textures over the player view (like the carved pumpkins) or creating additional bars (like the default hunger and health bar).

## Creation and naming

* First, go to the workspace.
* Next click on **Create new element**.
* Type the name of your overlay. **Names must not be repeated!** The end-user will not see this name, so name it something that you can identify. One way to prevent your keybind name from being repeated is to put the mod type before your mod name, Example: *CustomPumpkinOverlay or CustomBar*
* Click next.

## Overlay Properties

* **Overlay target:** where the overlay will appear (can be set to appear in the main menu)
* **Render priority:** this will set the priority for your overlay. Overlays with higher priorities will override the others
* **Base texture:** the texture on the overlay background

## Editor options

* **Snap components on the grid:** Show a grid to easily place components
* **Grid x/y**: The size in x and y of each case of the grid
* **Offset x/y**: Move the grid from the selected x and y

## Overlay Components

Each component has a render priority in Minecraft. This priority is also shown in MCreator with their order on the menu. The higher a component is, the higher its priority is.

### Label

* **Text:** You can either choose *Fixed* to write a specific text or select a procedure returning a String value. Using a procedure can allow you to use normal text, but also custom variables or get values from an entity, the world or the value of another GUI component. If *Fixed* is used, the text will still be translatable.
* **Text color:** The color that the text will use, no matter if it uses a *Fixed* value or a procedure.
* **Label display condition:** If the label should only be displayed under one or multiple specific conditions, you can use a procedure returning that/those condition(s).

### Image

* **Texture:** The image you want to display on your GUI. Note that the texture needs to be saved inside the GUI/overlay category
* **Image display condition:** If the image should only be displayed under one or multiple specific conditions, you can use a procedure returning that/those condition(s).

### Rendered entity

* **Display model entity provider:** Use the return value of the selected procedure to determine the model that will be displayed. The return value must be an entity.
* **Model display condition:** If the model should only be displayed under one or multiple specific conditions, you can use a procedure returning that/those condition(s)
* **Model scale:** Determine the size of the displayed entity model.
* **Model yaw rotation in degree:** This defines the initial rotation of the rendered entity model in degrees.
* **Follow mouse movement with rotation**: If checked, the model will rotate depending on the cursor's position.

## Notes

Too much overlays could crash the game or confuse players

Overlays with the same priority and position will cause visual glitch