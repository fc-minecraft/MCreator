<#-- @formatter:off -->
<#include "../mcitems.ftl">
package ${package}.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ${name}Entity extends PathfinderMob {

	// --- Synced data ---
	private static final EntityDataAccessor<Float>   DATA_FUEL    = SynchedEntityData.defineId(${name}Entity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_ENGINE  = SynchedEntityData.defineId(${name}Entity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float>   DATA_THROTTLE = SynchedEntityData.defineId(${name}Entity.class, EntityDataSerializers.FLOAT);

	// --- Server-side state ---
	private Vec3 lastVelocity = Vec3.ZERO;
	private int  soundTicks   = 0;
	private double currentFlightSpeed = 0.0;

	/** Ticks passenger has been holding the dismount key. Resets on key release. */
	private int  dismountHoldTicks = 0;
	/** Set to true when dismountHoldTicks reaches threshold — allows removePassenger to proceed. */
	private boolean canDismount = false;
	/** Tick of last received DISMOUNT_HOLD packet. Used to auto-reset if packets stop arriving. */
	private long lastDismountPacketTick = -1L;

	// --- Constants ---
	private static final int DISMOUNT_TICKS_REQUIRED = 20; // 1 second

	public ${name}Entity(EntityType<${name}Entity> type, Level world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FUEL,    ${(data.fuelCapacity)?c}f);
		builder.define(DATA_ENGINE,  false);
		builder.define(DATA_THROTTLE, 0.0f);
	}

	// --- Public accessors (used by renderer) ---
	public boolean isEngineOn()  { return this.entityData.get(DATA_ENGINE); }
	public float   getFuel()     { return this.entityData.get(DATA_FUEL); }
	public float   getThrottle() { return this.entityData.get(DATA_THROTTLE); }
	public float   getFuelCapacity() { return ${(data.fuelCapacity)?c}f; }

	// =========================================================================
	// Packet handlers (called from ${name}ControlPacket)
	// =========================================================================

	/** Toggle engine on/off. Includes fuel check. */
	public void toggleEngine() {
		boolean engineState = this.entityData.get(DATA_ENGINE);
		this.entityData.set(DATA_ENGINE, !engineState);
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
			!engineState ? net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON
			             : net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_OFF,
			net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);

		Entity passenger = this.getFirstPassenger();
		if (passenger instanceof Player player) {
			player.displayClientMessage(
				net.minecraft.network.chat.Component.translatable(!engineState ? "hud.${modid}.${registryname}.engine_started" : "hud.${modid}.${registryname}.engine_stopped"),
				true
			);
		}
	}

	public void onDismountHeld(Player player) {
		lastDismountPacketTick = this.level().getGameTime();
		dismountHoldTicks++;

		if (dismountHoldTicks >= DISMOUNT_TICKS_REQUIRED) {
			canDismount = true;
			this.ejectPassengers();
		}
	}

	/** Called when the player releases the dismount key. */
	public void resetDismountCounter() {
		dismountHoldTicks = 0;
		canDismount = false;
		lastDismountPacketTick = -1L;
	}

	// =========================================================================
	// Save / Load
	// =========================================================================

	@Override
	public void readAdditionalSaveData(ValueInput tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(DATA_FUEL,    tag.getFloatOr("Fuel",     ${(data.fuelCapacity)?c}f));
		this.entityData.set(DATA_ENGINE,  tag.getBooleanOr("EngineOn", false));
		this.entityData.set(DATA_THROTTLE, tag.getFloatOr("Throttle", 0.0f));
	}

	@Override
	public void addAdditionalSaveData(ValueOutput tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat("Fuel",     this.entityData.get(DATA_FUEL));
		tag.putBoolean("EngineOn", this.entityData.get(DATA_ENGINE));
		tag.putFloat("Throttle", this.entityData.get(DATA_THROTTLE));
	}

	// =========================================================================
	// Interaction (right-click)
	// =========================================================================

	@Override
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		return entity instanceof LivingEntity ? (LivingEntity) entity : null;
	}

	@Override
	public InteractionResult mobInteract(Player sourceentity, InteractionHand hand) {
		super.mobInteract(sourceentity, hand);
		if (sourceentity.getVehicle() == this) return InteractionResult.PASS;

		net.minecraft.world.item.ItemStack itemstack = sourceentity.getItemInHand(hand);

		<#if data.enableFuel && data.fuelItems?has_content>
		// --- Refueling ---
		float fuelToAdd = 0f;
		<#list data.fuelItems as fuelEntry>
		<#if fuelEntry.item??>
		<#if fuelEntry?index == 0>
		if (itemstack.is(${mappedMCItemToItem(fuelEntry.item)})) { fuelToAdd = ${fuelEntry.fuelAmount?c}f; }
		<#else>
		else if (itemstack.is(${mappedMCItemToItem(fuelEntry.item)})) { fuelToAdd = ${fuelEntry.fuelAmount?c}f; }
		</#if>
		</#if>
		</#list>

		if (fuelToAdd > 0f) {
			float currentFuel = this.entityData.get(DATA_FUEL);
			float capacity    = ${(data.fuelCapacity)?c}f;
			if (currentFuel < capacity) {
				if (!this.level().isClientSide()) {
					this.entityData.set(DATA_FUEL, Math.min(capacity, currentFuel + fuelToAdd));
					if (!sourceentity.getAbilities().instabuild) itemstack.shrink(1);
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
						net.minecraft.sounds.SoundEvents.BUNDLE_DROP_CONTENTS,
						net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
					float newFuel = this.entityData.get(DATA_FUEL);
					sourceentity.displayClientMessage(
						net.minecraft.network.chat.Component.translatable("hud.${modid}.${registryname}.fuel_refueled")
							.append(String.valueOf(Math.round(newFuel)))
							.append(" / ")
							.append(String.valueOf((int) capacity)),
						true
					);
				}
				return InteractionResult.SUCCESS;
			} else {
				sourceentity.displayClientMessage(
					net.minecraft.network.chat.Component.translatable("hud.${modid}.${registryname}.fuel_full"), true);
				return InteractionResult.SUCCESS;
			}
		}
		</#if>

		// --- Mount ---
		if (!this.level().isClientSide()) {
			sourceentity.startRiding(this);
		}
		return InteractionResult.SUCCESS;
	}

	// =========================================================================
	// Entity properties
	// =========================================================================

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float f) {
		return new Vec3(${data.seatOffsetX}f, ${data.seatOffsetY}f + 0.6f, ${data.seatOffsetZ}f);
	}

	@Override public boolean dismountsUnderwater() { return false; }
	@Override public boolean isPushable()          { return false; }

	@Override
	public boolean isNoGravity() {
		<#if data.transportType == "AIR">return true;
		<#else>return super.isNoGravity();
		</#if>
	}

	// =========================================================================
	// removePassenger — guarded by canDismount flag
	// =========================================================================

	@Override
	public void removePassenger(Entity passenger) {
		// Always allow removal if entity is dead/dying, being removed, or passenger is not a player
		boolean forceAllow = !this.isAlive() || this.isRemoved();

		if (!forceAllow && passenger instanceof Player && !this.level().isClientSide()) {
			if (!canDismount) {
				// Block accidental Shift-dismount — player must hold the dedicated key
				// Re-attach the passenger silently
				passenger.startRiding(this, true);
				return;
			}
		}
		super.removePassenger(passenger);
		if (passenger instanceof Player) {
			resetDismountCounter();
		}
	}

	/** Override die() to ensure passengers are always ejected on death. */
	@Override
	public void die(net.minecraft.world.damagesource.DamageSource source) {
		canDismount = true; // allow ejection
		this.ejectPassengers();
		super.die(source);
	}

	// =========================================================================
	// Tick — physics, fuel, HUD, crash, dismount watchdog
	// =========================================================================

	@Override
	public void tick() {
		Vec3 currentMotion = this.getDeltaMovement();

		if (!this.level().isClientSide()) {
			// --- Crash check ---
			<#if data.enableCrash>
			if (this.horizontalCollision || this.verticalCollision) {
				double speedSq = this.lastVelocity.x * this.lastVelocity.x
					+ this.lastVelocity.y * this.lastVelocity.y
					+ this.lastVelocity.z * this.lastVelocity.z;
				if (speedSq > ${(data.crashSpeed)?c} * ${(data.crashSpeed)?c}) {
					Entity passenger = this.getFirstPassenger();
					if (passenger instanceof Player player && ${data.crashDamageToPlayer?string("true","false")}) {
						player.hurt(player.damageSources().explosion(null, null), (float)(speedSq * 5.0));
					}
					<#if data.crashDropItems>
					this.spawnAtLocation(this.level() instanceof net.minecraft.server.level.ServerLevel sl ? sl : null,
						new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 1));
					</#if>
					this.level().explode(this, this.getX(), this.getY(), this.getZ(),
						${data.explosionRadius}f, net.minecraft.world.level.Level.ExplosionInteraction.TNT);
					this.discard();
					return;
				}
			}
			</#if>
			this.lastVelocity = currentMotion;

			// --- Fuel drain ---
			boolean engineOn = this.entityData.get(DATA_ENGINE);
			if (engineOn) {
				<#if data.enableFuel>
				float fuel     = this.entityData.get(DATA_FUEL);
				float throttle = this.entityData.get(DATA_THROTTLE);
				float consumption = (float) ${(data.fuelConsumption)?c} * (1.0f + Math.abs(throttle) * 2.0f);
				fuel -= consumption;
				if (fuel <= 0f) {
					fuel = 0f;
					this.entityData.set(DATA_ENGINE, false);
					this.entityData.set(DATA_THROTTLE, 0.0f);
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
						net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_OFF,
						net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 0.5f);
					Entity passenger = this.getFirstPassenger();
					if (passenger instanceof Player player) {
						player.displayClientMessage(
							net.minecraft.network.chat.Component.translatable("hud.${modid}.${registryname}.fuel_empty"), true);
					}
				}
				this.entityData.set(DATA_FUEL, fuel);
				</#if>

				// Engine loop sound
				soundTicks++;
				if (soundTicks >= 20) {
					soundTicks = 0;
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
						net.minecraft.sounds.SoundEvents.MINECART_RIDING,
						net.minecraft.sounds.SoundSource.NEUTRAL,
						0.5f, 0.6f + Math.abs(this.entityData.get(DATA_THROTTLE)) * 0.4f);
				}
			}

			// --- Dismount watchdog: if we stop receiving packets, reset counter ---
			if (dismountHoldTicks > 0 && lastDismountPacketTick >= 0) {
				if (this.level().getGameTime() - lastDismountPacketTick > 3) {
					resetDismountCounter();
				}
			}


		}

		super.tick();
	}

	// =========================================================================
	// travel() — movement physics per transport type
	// =========================================================================

	@Override
	public void travel(Vec3 dir) {
		if (!this.isAlive()) { super.travel(dir); return; }

		LivingEntity passenger = this.getControllingPassenger();
		if (!this.isVehicle() || passenger == null) { super.travel(dir); return; }

		float strafeInput  = passenger.xxa;
		float forwardInput = passenger.zza;
		boolean engineOn   = this.entityData.get(DATA_ENGINE);
		float throttle     = this.entityData.get(DATA_THROTTLE);

		<#if data.transportType == "AIR">
		// ---- AIR ----
		<#if data.planeMechanics>
		// === PLANE ===
		long time = this.level().getGameTime();

		// 1. Calculate target pitch based on keyboard input (Space = pitch up, Shift = pitch down)
		float targetPitch = 0.0f;
		if (getJumping(passenger)) {
			targetPitch = -35.0f; // climb / nose up (negative pitch is up in MC)
		} else if (passenger.isShiftKeyDown()) {
			targetPitch = 35.0f;  // dive / nose down (positive pitch is down in MC)
		}

		// If stalling, force the nose down and disable user pitch control!
		boolean isStalling = this.currentFlightSpeed < ${data.stallSpeed} && !this.onGround();
		if (isStalling) {
			targetPitch = 30.0f; // Force nose down to gain speed
		}

		// Add slight turbulence to pitch & yaw if flying in the air (wind wobble)
		double pitchTurbulence = 0.0;
		double yawTurbulence = 0.0;
		if (!this.onGround()) {
			pitchTurbulence = Math.sin(time * 0.04) * 0.35 + Math.cos(time * 0.09) * 0.15;
			yawTurbulence = Math.cos(time * 0.03) * 0.25;
		}

		float pitchDiff = targetPitch - this.getXRot();
		float pitchSpeed = isStalling ? 0.15f : 0.07f;
		this.setXRot((float)(this.getXRot() + pitchDiff * pitchSpeed + pitchTurbulence));

		// Steering yaw following passenger look angle (mouse direction)
		float targetYaw = passenger.getYRot();
		float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
		float maxSteer = (float) ${data.steeringSpeed} * 45.0f;
		float yawStep = Mth.clamp(yawDiff * 0.05f, -maxSteer, maxSteer); // 5% interpolation per tick for smooth yaw turns
		this.setYRot((float)(this.getYRot() + yawStep + yawTurbulence));
		this.setRot(this.getYRot(), this.getXRot());
		this.yBodyRot = this.getYRot();
		this.yHeadRot = this.getYRot();

		// 2. Calculate throttle
		if (engineOn) {
			if (forwardInput > 0.0f) {
				throttle = Math.min(1.0f, throttle + (float)${data.accelerationRate});
			} else if (forwardInput < 0.0f) {
				throttle = Math.max(-0.3f, throttle - (float)${data.brakeFactor});
			} else {
				// Cruise control: slow throttle decay when no keys are pressed
				throttle += (0.0f - throttle) * 0.005f;
			}
		} else {
			throttle += (0.0f - throttle) * 0.05f;
		}
		this.entityData.set(DATA_THROTTLE, throttle);

		// 3. Speed calculation with momentum and gravity drag
		double baseTargetSpeed = throttle * ${data.speed};
		double yawRad = -this.getYRot() * (Math.PI / 180.0);
		double pitchRad = -this.getXRot() * (Math.PI / 180.0);

		// Gravity affects speed: climbing slows down, diving increases speed
		double gravitySpeedModifier = Math.sin(pitchRad) * 0.5; // climbing (pitchRad > 0) reduces speed, diving increases
		double targetSpeed = baseTargetSpeed - gravitySpeedModifier;
		if (targetSpeed < 0.0) {
			targetSpeed = 0.0;
		}

		double speedInterpolation;
		if (engineOn) {
			speedInterpolation = 0.008; // Slower speed changes due to aircraft mass/inertia (heavy momentum)
		} else {
			speedInterpolation = this.onGround() ? 0.1 : 0.004; // slow speed decay in air (glide), faster on ground
		}
		this.currentFlightSpeed = Mth.lerp(speedInterpolation, this.currentFlightSpeed, targetSpeed);
		double speed = this.currentFlightSpeed;

		// 4. Direction vectors based on Pitch & Yaw (realistic 3D flight trajectory)
		double cosPitch = Math.cos(pitchRad);
		double vx = Math.sin(yawRad) * cosPitch * speed;
		double vz = Math.cos(yawRad) * cosPitch * speed;
		double vy = Math.sin(pitchRad) * speed * 0.8; // scaled for climbs matching speed/pitch

		// 5. Lift and Gravity
		double gravity = 0.08; // Standard MC gravity
		double maxSpeedVal = ${data.speed} > 0.01 ? ${data.speed} : 0.5;
		double liftFactor = (speed * speed) / (maxSpeedVal * maxSpeedVal);
		liftFactor = Mth.clamp(liftFactor, 0.0, 1.5);
		double lift = gravity * liftFactor * cosPitch;

		double thermalLift = !this.onGround() ? (Math.sin(time * 0.02) * 0.012) : 0.0;
		double verticalInput = vy - gravity + lift + thermalLift;

		// Glide mechanics when engine is off but we have speed
		if (!engineOn && speed >= ${data.stallSpeed}) {
			verticalInput = Math.max(verticalInput, -0.05); // smooth glide
		}

		// Stall physics
		if (isStalling) {
			double stallFactor = 1.0 - (speed / ${data.stallSpeed});
			verticalInput = -0.3 * stallFactor; // Fall down fast!
		}

		if (this.getY() >= ${data.maxAltitude}) {
			verticalInput = Math.min(verticalInput, -0.1);
		}

		Vec3 motion = new Vec3(vx, verticalInput, vz);
		this.setDeltaMovement(motion);
		super.travel(Vec3.ZERO);
		// Retain motion vector in next tick (bypassing vanilla 0.8 air friction)
		this.setDeltaMovement(motion.scale(0.99));

		<#elseif data.helicopterMechanics>
		// === HELICOPTER ===
		float targetYaw = passenger.getYRot();
		float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
		float maxSteer = (float) ${data.steeringSpeed} * 45.0f;
		float yawStep = Mth.clamp(yawDiff * 0.05f, -maxSteer, maxSteer); // 5% interpolation per tick for smooth yaw turns
		this.setYRot(this.getYRot() + yawStep);
		
		// Tilt forward/backward based on input
		float targetPitch = forwardInput * 15.0f;
		float pitchDiff = targetPitch - this.getXRot();
		this.setXRot(this.getXRot() + pitchDiff * 0.1f);
		this.setRot(this.getYRot(), this.getXRot());
		this.yBodyRot = this.getYRot();
		this.yHeadRot = this.getYRot();

		if (engineOn) {
			if (forwardInput > 0.0f) {
				throttle = Math.min(1.0f, throttle + (float)${data.accelerationRate} * 2);
			} else if (forwardInput < 0.0f) {
				throttle = Math.max(-0.5f, throttle - (float)${data.brakeFactor} * 2);
			} else {
				throttle += (0.0f - throttle) * 0.1f;
			}
		} else {
			throttle += (0.0f - throttle) * 0.2f;
		}
		this.entityData.set(DATA_THROTTLE, throttle);

		double speed = throttle * ${data.speed};
		double strafeSpeed = strafeInput * ${data.strafeSpeed};

		double verticalInput = 0.0;
		if (engineOn) {
			if (getJumping(passenger)) {
				verticalInput = ${data.jumpForce};
			} else if (passenger.isShiftKeyDown()) {
				verticalInput = -${data.jumpForce};
			} else {
				// Hover stabilization
				Vec3 current = this.getDeltaMovement();
				verticalInput = -current.y * 0.5;
			}
		} else {
			// Autorotation fall
			verticalInput = -0.08;
		}

		if (this.getY() >= ${data.maxAltitude}) {
			verticalInput = Math.min(verticalInput, -0.1);
		}

		double yawRad = -this.getYRot() * (Math.PI / 180.0);
		double vx = Math.sin(yawRad) * speed + Math.cos(yawRad) * strafeSpeed;
		double vz = Math.cos(yawRad) * speed - Math.sin(yawRad) * strafeSpeed;

		Vec3 motion = new Vec3(vx, verticalInput, vz);
		this.setDeltaMovement(motion);
		super.travel(Vec3.ZERO);
		this.setDeltaMovement(new Vec3(motion.x * 0.95, motion.y, motion.z * 0.95));

		<#else>
		// === GENERIC AIR ===
		float targetYaw = passenger.getYRot();
		float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
		float maxSteer = (float) ${data.steeringSpeed} * 45.0f;
		float yawStep = Mth.clamp(yawDiff * 0.05f, -maxSteer, maxSteer); // 5% interpolation per tick for smooth yaw turns
		this.setYRot(this.getYRot() + yawStep);
		
		float targetPitch = passenger.getXRot() * 0.5f;
		float pitchDiff = targetPitch - this.getXRot();
		this.setXRot(this.getXRot() + pitchDiff * 0.05f); // 5% interpolation per tick for smooth pitch shifts
		this.setRot(this.getYRot(), this.getXRot());

		if (engineOn) {
			if (forwardInput > 0.0f) {
				throttle = Math.min(1.0f, throttle + (float)${data.accelerationRate} * 3);
			} else if (forwardInput < 0.0f) {
				throttle = Math.max(-0.5f, throttle - (float)${data.brakeFactor} * 3);
			} else {
				throttle += (0.0f - throttle) * 0.2f;
			}
		} else {
			throttle += (0.0f - throttle) * 0.2f;
		}
		this.entityData.set(DATA_THROTTLE, throttle);

		double speed = throttle * ${data.speed};
		double strafeSpeed = strafeInput * ${data.strafeSpeed};

		double verticalInput = 0.0;
		if (engineOn) {
			if (getJumping(passenger)) {
				verticalInput = ${data.jumpForce};
			} else if (passenger.isShiftKeyDown()) {
				verticalInput = -${data.jumpForce};
			}
		} else {
			verticalInput = -0.25;
		}

		if (this.getY() >= ${data.maxAltitude}) {
			verticalInput = Math.min(verticalInput, -0.1);
		}

		double yawRad = -this.getYRot() * (Math.PI / 180.0);
		double vx = Math.sin(yawRad) * speed + Math.cos(yawRad) * strafeSpeed;
		double vz = Math.cos(yawRad) * speed - Math.sin(yawRad) * strafeSpeed;

		Vec3 motion = new Vec3(vx, verticalInput, vz);
		this.setDeltaMovement(motion);
		super.travel(Vec3.ZERO);
		this.setDeltaMovement(motion.scale(0.95));
		</#if>

		<#elseif data.transportType == "WATER">
		// ---- WATER ----
		// Steering
		float targetYaw    = passenger.getYRot();
		float yawDiff      = Mth.wrapDegrees(targetYaw - this.getYRot());
		float steerDegrees = (float) ${data.steeringSpeed} * 45.0f;
		this.setYRot(this.getYRot() + Mth.clamp(yawDiff, -steerDegrees, steerDegrees));
		this.setXRot(passenger.getXRot() * 0.5f);
		this.setRot(this.getYRot(), this.getXRot());
		this.yBodyRot  = this.getYRot();
		this.yHeadRot  = this.getYRot();

		<#if data.enableFuel>
		if (engineOn) {
			if (forwardInput > 0.0f)      throttle = Math.min(1.0f,  throttle + (float)${data.accelerationRate} * 2);
			else if (forwardInput < 0.0f) throttle = Math.max(-0.5f, throttle - (float)${data.brakeFactor} * 2);
			else                          throttle += (0.0f - throttle) * 0.05f;
		} else {
			throttle += (0.0f - throttle) * 0.1f;
		}
		<#else>
		if (engineOn) {
			throttle = forwardInput;
		} else {
			throttle = 0.0f;
		}
		</#if>
		this.entityData.set(DATA_THROTTLE, throttle);
		double speed = throttle * ${data.speed};

		Vec3 cur = this.getDeltaMovement();
		double verticalInput = cur.y;
		if (this.isInWater()) {
			verticalInput = 0.02; // floating buoyancy
			if (getJumping(passenger))           verticalInput =  ${data.jumpForce};
			else if (passenger.isShiftKeyDown()) verticalInput = -${data.jumpForce};
		} else {
			verticalInput = cur.y - 0.08; // normal gravity out of water
		}

		// Vector-based velocity with water drift (grip coefficient)
		double yawRad = -this.getYRot() * (Math.PI / 180.0);
		double targetVx = Math.sin(yawRad) * speed + Math.cos(yawRad) * (strafeInput * ${data.strafeSpeed});
		double targetVz = Math.cos(yawRad) * speed - Math.sin(yawRad) * (strafeInput * ${data.strafeSpeed});

		double grip = this.isInWater() ? 0.08 : 0.02; // slides a lot more on land, drifts in water
		double vx = Mth.lerp(grip, cur.x, targetVx);
		double vz = Mth.lerp(grip, cur.z, targetVz);

		Vec3 motion = new Vec3(vx, verticalInput, vz);
		this.setDeltaMovement(motion);
		super.travel(Vec3.ZERO);

		<#else>
		// ---- LAND ----
		// Steering
		float targetYaw    = passenger.getYRot();
		float yawDiff      = Mth.wrapDegrees(targetYaw - this.getYRot());
		float steerDegrees = (float) ${data.steeringSpeed} * 45.0f;
		this.setYRot(this.getYRot() + Mth.clamp(yawDiff, -steerDegrees, steerDegrees));
		this.setXRot(passenger.getXRot() * 0.5f);
		this.setRot(this.getYRot(), this.getXRot());
		this.yBodyRot  = this.getYRot();
		this.yHeadRot  = this.getYRot();

		<#if data.enableFuel>
		if (engineOn) {
			if (forwardInput > 0.0f)      throttle = Math.min(1.0f,  throttle + (float)${data.accelerationRate} * 2);
			else if (forwardInput < 0.0f) throttle = Math.max(-0.5f, throttle - (float)${data.brakeFactor} * 2);
			else                          throttle += (0.0f - throttle) * (float)${data.brakeFactor} * 0.5f;
		} else {
			// Coast-to-stop with inertia
			throttle += (0.0f - throttle) * 0.05f;
		}
		<#else>
		if (engineOn) {
			throttle = forwardInput;
		} else {
			throttle = 0.0f;
		}
		</#if>
		this.entityData.set(DATA_THROTTLE, throttle);
		double speed = throttle * ${data.speed};

		Vec3 cur = this.getDeltaMovement();
		double vy = cur.y;
		if (getJumping(passenger) && this.onGround() && (!${data.enableFuel?string("true","false")} || engineOn)) {
			vy = ${data.jumpForce};
		}

		// Vector-based velocity with tire grip (using inertiaFactor from UI)
		double yawRad = -this.getYRot() * (Math.PI / 180.0);
		double targetVx = Math.sin(yawRad) * speed + Math.cos(yawRad) * (strafeInput * ${data.strafeSpeed});
		double targetVz = Math.cos(yawRad) * speed - Math.sin(yawRad) * (strafeInput * ${data.strafeSpeed});

		// Calculate traction based on the inertiaFactor setting (default 0.98 -> 0.22 traction)
		double traction = this.onGround() ? (1.0 - ${data.inertiaFactor} * 0.8) : 0.05;
		double vx = Mth.lerp(traction, cur.x, targetVx);
		double vz = Mth.lerp(traction, cur.z, targetVz);

		Vec3 motion = new Vec3(vx, vy, vz);
		this.setDeltaMovement(motion);
		super.travel(Vec3.ZERO);
		</#if>

		// Animation
		double d1 = this.getX() - this.xo;
		double d0 = this.getZ() - this.zo;
		float  f1 = (float) Math.sqrt(d1 * d1 + d0 * d0) * 4;
		if (f1 > 1.0F) f1 = 1.0F;
		this.walkAnimation.setSpeed(this.walkAnimation.speed() + (f1 - this.walkAnimation.speed()) * 0.4F);
		this.walkAnimation.position(this.walkAnimation.position() + this.walkAnimation.speed());
		this.calculateEntityAnimation(true);
	}

	// =========================================================================
	// Jumping (reflection helper — Fabric/Yarn field name fallback)
	// =========================================================================

	private static java.lang.reflect.Field jumpingField;
	private static boolean getJumping(LivingEntity entity) {
		if (jumpingField == null) {
			for (String name : new String[]{"jumping", "field_6224"}) {
				try {
					jumpingField = LivingEntity.class.getDeclaredField(name);
					jumpingField.setAccessible(true);
					break;
				} catch (NoSuchFieldException ignored) {}
			}
		}
		if (jumpingField != null) {
			try { return jumpingField.getBoolean(entity); } catch (Exception ignored) {}
		}
		return false;
	}

	// =========================================================================
	// Attributes
	// =========================================================================

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder b = Mob.createMobAttributes();
		b = b.add(Attributes.MOVEMENT_SPEED, ${data.speed});
		b = b.add(Attributes.MAX_HEALTH, ${data.maxHealth});
		b = b.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
		<#if data.transportType == "AIR">
		b = b.add(Attributes.FLYING_SPEED, ${data.speed});
		</#if>
		return b;
	}
}
<#-- @formatter:on -->
