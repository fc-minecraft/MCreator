# Частицы

*Примечание: Эта страница была автоматически загружена из официальной вики MCreator.*

Section

[Mod elements](/wiki/section/mod-elements)

## What is a particle?

Particles are visual effects that some blocks, entities and some other entities use for visual display. One example of particles in the vanilla game is composting particles. When composting items, green particles are generated inside the compost bin. Another example is when entities have potion effects. Potion particles are displayed around the entity to indicate the potion the entity has for its status effect.

## Creation and naming

* First, go to the workspace.
* Next, click on **Create New Element**.
* Select the particle element.
* Type the name of your particle. **Names must not be repeated!** The end-user will not see this name, so name it something you can identify. One way to prevent your particle name from being repeated is to put the mod type before your mod name, for example, ParticleFlame*or ParticleWind*.
* Click next.

## Properties

Here you can find a full list of particle properties and information about each setting.

| Setting: | Description: |
| --- | --- |
| Texture | This setting sets the image display for the particle.   * Learn more about making texture on this wiki page - [Making a New Texture](/wiki/making-new-texture). |
| Animate this particle? | Check this box if the texture of your particle should be animated.   * Only works with tiled textures. |
| Animated texture frame duration in ticks | This parameter controls how many ticks should pass before switching to the next animation frame.   * There are 20 ticks per 1 second. |
| Particle render type | This parameter defines how this particle should be rendered   * **Opaque:** A transparent without mipmapping (similar to death particles) * **Translucent:** A partially transparent and the most resource heavy option (similar to potion effect particles) * **Lit:** A Glowing particle that emits light during its lifetime (similar to explosion particles) |
| Particle visual scale | This parameter defines how much the particle texture should be scaled. |
| Particle bounding box width and height | This parameter controls how wide this particle is (in block units). |
| Speed factor for input speed | This parameter controls how much external speed parameter is taken into account when spawning the particle. When set to 0, particle will not move and only fall, in case the gravity of the particle is larger than 0.   * Use 0 for particles that only move due to gravity (e.g. drips) |
| Particle angular velocity | This parameter controls the initial spinning velocity of the particle.   * Negative values mean a counterclockwise rotation. * A value of 0.314 is roughly the same as 1 rotation per second. * Clockwise, measured in rad/tick. |
| Particle angular acceleration | This parameter controls the spinning acceleration of the particle.   * Clockwise, measured in rad/tick^2 |
| Particle gravity | This parameter specifies particle falling speed.   * Negative values will make this particle fly up to the sky. |
| Particle maximal age (in ticks) and variation (max. +/- per particle) | The particle will expire as soon as its age is equal to this number of ticks.   * This can happen sooner or later if the maximal age diff is greater than 0. * Set the variation to 0 to make particles age at a static rate. |
| Always show the particle? | This parameter controls if the particle is shown in all cases, even if the particle display is set to minimal in the video settings.   * Normally only used when particles are required to be seen for mechanics e.g. barrier blocks. * Leave this setting disabled if working with just aesthetic particles like potion effects or block particles to help with performance. |
| Does particle collide? | Check to make this particle able to collide with blocks (like fluid drips or block-breaking particles). |
| Additional particle expiry condition | The particle will expire before its maximal age if it passes the selected condition.  If no condition is defined, a particle can only expire when it reaches the maximal age in ticks.  **Custom dependencies:**  The following dependency blocks can be used in this condition.   * **age**: A number dependency that gets the current particle age. * **onGround**: A logic dependency that returns true when the particle is on the ground. |

## Test and editing

If you want to test the particle you will need to create a procedure that will spawn a particle, This can be done in almost any way, eg. through player update ticks, block update ticks, or when the block is right-clicked on. You will need to use one of the many Spawn particle procedure blocks found under "World management" when you are editing procedures.

## Video tutorial

If you prefer to watch the video, you can find the wiki page above summarized in a video:

See also

[How to Make a Block](/wiki/how-make-block)

[Minecraft Particle IDs/names](/wiki/particles-ids)

References

Minecraft Wiki - Particles
<https://minecraft.wiki/w/Particles>