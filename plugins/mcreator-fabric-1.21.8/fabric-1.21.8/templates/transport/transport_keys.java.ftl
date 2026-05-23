<#-- @formatter:off -->
package ${package}.client;

import ${package}.${JavaModName};
import ${package}.entity.${name}Entity;
import ${package}.network.${name}ControlPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side keybind registration and tick handler for ${name} transport.
 * Controls: [${data.engineToggleKey}] = toggle engine, [${data.dismountKey}] = hold to dismount.
 */
@Environment(EnvType.CLIENT)
public class ${name}TransportKeys {

	public static final KeyMapping ENGINE_TOGGLE = KeyBindingHelper.registerKeyBinding(
		new KeyMapping(
			"key.${modid}.${registryname}.engine_toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_${data.engineToggleKey},
			"key.categories.${JavaModName?lower_case}.transport"
		)
	);

	public static final KeyMapping DISMOUNT = KeyBindingHelper.registerKeyBinding(
		new KeyMapping(
			"key.${modid}.${registryname}.dismount",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_${data.dismountKey},
			"key.categories.${JavaModName?lower_case}.transport"
		)
	);

	private static boolean dismountWasDown = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(${name}TransportKeys::onClientTick);
	}

	private static void onClientTick(Minecraft mc) {
		if (mc.player == null || mc.screen != null) {
			if (dismountWasDown) {
				dismountWasDown = false;
				sendIfInTransport(${name}ControlPacket.ACTION_DISMOUNT_RELEASE);
			}
			return;
		}
		Entity vehicle = mc.player.getVehicle();
		if (!(vehicle instanceof ${name}Entity)) {
			if (dismountWasDown) {
				dismountWasDown = false;
			}
			// Consume clicks even when not in transport (prevents queued-click issues)
			ENGINE_TOGGLE.consumeClick();
			return;
		}

		// Engine toggle on key press (edge trigger)
		if (ENGINE_TOGGLE.consumeClick()) {
			ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_ENGINE_TOGGLE));
		}

		// Dismount: hold logic — send packet every tick while held
		boolean dismountDown = DISMOUNT.isDown();
		if (dismountDown) {
			ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_DISMOUNT_HOLD));
			dismountWasDown = true;
		} else if (dismountWasDown) {
			// Key released — cancel dismount countdown
			ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_DISMOUNT_RELEASE));
			dismountWasDown = false;
		}
	}

	private static void sendIfInTransport(int action) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.getVehicle() instanceof ${name}Entity) {
			ClientPlayNetworking.send(new ${name}ControlPacket(action));
		}
	}
}
<#-- @formatter:on -->
