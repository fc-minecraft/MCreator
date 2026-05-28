<#-- @formatter:off -->
<#include "../mcitems.ftl">
package ${package}.network;

import ${package}.${JavaModName};
import ${package}.entity.${name}Entity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * C2S packet for transport controls: engine toggle, dismount hold/release.
 * Sent by the client each tick (DISMOUNT_HOLD) or on key press (ENGINE_TOGGLE, DISMOUNT_RELEASE).
 */
public record ${name}ControlPacket(int action) implements CustomPacketPayload {

	public static final int ACTION_ENGINE_TOGGLE  = 0;
	public static final int ACTION_DISMOUNT_HOLD  = 1;
	public static final int ACTION_DISMOUNT_RELEASE = 2;

	public static final Type<${name}ControlPacket> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(${JavaModName}.MODID, "transport_ctrl_${registryname}")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ${name}ControlPacket> STREAM_CODEC = StreamCodec.of(
		(buf, msg) -> buf.writeVarInt(msg.action()),
		buf  -> new ${name}ControlPacket(buf.readVarInt())
	);

	@Override public Type<${name}ControlPacket> type() { return TYPE; }

	public static void handleData(${name}ControlPacket message, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ServerPlayer player = context.player();
			Entity vehicle = player.getVehicle();

			if (vehicle instanceof ${name}Entity transport) {
				switch (message.action()) {
					case ACTION_ENGINE_TOGGLE -> {
						<#if data.enableFuel>
						boolean engineState = transport.isEngineOn();
						if (!engineState && transport.getFuel() <= 0f) {
							player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cНет топлива!"), true);
							vehicle.level().playSound(null, vehicle.getX(), vehicle.getY(), vehicle.getZ(),
								net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_OFF,
								net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 0.5f);
						} else {
							transport.toggleEngine();
						}
						<#else>
						transport.toggleEngine();
						</#if>
					}
					case ACTION_DISMOUNT_HOLD -> transport.onDismountHeld(player);
					case ACTION_DISMOUNT_RELEASE -> transport.resetDismountCounter();
				}
			}
		});
	}
}
<#-- @formatter:on -->
