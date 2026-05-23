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
 *
 * NOTE: KeyMapping.isDown() / consumeClick() are suppressed while riding a vehicle.
 * We use InputConstants.isKeyDown() (raw GLFW) for reliable polling.
 */
@Environment(EnvType.CLIENT)
public class ${name}TransportKeys {

	/** Registered for Minecraft key settings screen only. Actual detection uses raw GLFW below. */
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

	// Raw GLFW key codes for in-vehicle polling
	private static final int ENGINE_KEY_CODE  = GLFW.GLFW_KEY_${data.engineToggleKey};
	private static final int DISMOUNT_KEY_CODE = GLFW.GLFW_KEY_${data.dismountKey};

	private static boolean engineWasDown  = false;
	private static boolean dismountWasDown = false;
	private static int clientDismountTicks = 0;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(${name}TransportKeys::onClientTick);
	}

	private static void onClientTick(Minecraft mc) {
		if (mc.player == null || mc.screen != null) {
			// Reset states when no player or screen is open
			if (dismountWasDown) {
				dismountWasDown = false;
				if (mc.player != null && mc.player.getVehicle() instanceof ${name}Entity) {
					ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_DISMOUNT_RELEASE));
				}
			}
			engineWasDown = false;
			clientDismountTicks = 0;
			return;
		}

		Entity vehicleEntity = mc.player.getVehicle();
		if (!(vehicleEntity instanceof ${name}Entity vehicle)) {
			dismountWasDown = false;
			engineWasDown   = false;
			clientDismountTicks = 0;
			return;
		}

		// --- Raw GLFW polling (works while riding) ---
		long window = mc.getWindow().getWindow();

		// Engine toggle: edge-triggered (press, not hold)
		boolean engineDown = InputConstants.isKeyDown(window, ENGINE_KEY_CODE);
		if (engineDown && !engineWasDown) {
			ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_ENGINE_TOGGLE));
		}
		engineWasDown = engineDown;

		// Dismount: level-triggered (hold)
		boolean dismountDown = InputConstants.isKeyDown(window, DISMOUNT_KEY_CODE);
		if (dismountDown) {
			ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_DISMOUNT_HOLD));
			dismountWasDown = true;
			clientDismountTicks = Math.min(20, clientDismountTicks + 1);
		} else {
			if (dismountWasDown) {
				ClientPlayNetworking.send(new ${name}ControlPacket(${name}ControlPacket.ACTION_DISMOUNT_RELEASE));
				dismountWasDown = false;
			}
			clientDismountTicks = 0;
		}

		// --- Client-side HUD rendering ---
		StringBuilder hud = new StringBuilder();

		// Dismount progress bar
		if (clientDismountTicks > 0) {
			int progress = (clientDismountTicks * 10) / 20;
			hud.append("§eВыход: §c");
			for (int i = 0; i < 10; i++) {
				hud.append(i < progress ? "█" : "░");
			}
			hud.append("  ");
		}

		// Engine status
		<#if data.showEngineHUD>
		hud.append(vehicle.isEngineOn() ? "§aДвигатель: ВКЛ" : "§cДвигатель: ВЫКЛ");
		</#if>

		// Throttle status (direct reading of client-side physics value)
		<#if data.showThrottleHUD>
		int throttlePct = (int) (Math.abs(vehicle.getThrottle()) * 100);
		hud.append(" §7|§b Тяга: ").append(throttlePct).append("%");
		</#if>

		// Fuel status
		<#if data.showFuelHUD && data.enableFuel>
		float fuel = vehicle.getFuel();
		float cap  = ${(data.fuelCapacity)?c}f;
		int   pct  = (int) ((fuel / cap) * 100);
		hud.append(" §7|§e Топливо: ").append(pct).append("%");
		</#if>

		// Control hints (only show when engine is OFF)
		<#if data.showHints>
		if (!vehicle.isEngineOn()) {
			hud.append("  §8[").append("${data.engineToggleKey}").append(" - завести]");
			hud.append("  §8[").append("${data.dismountKey}").append(" - выйти (держать)]");
		}
		</#if>

		if (hud.length() > 0) {
			mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(hud.toString()), true);
		}
	}
}
<#-- @formatter:on -->
