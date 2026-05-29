(new Object() {
	public double getSpeed(net.minecraft.world.entity.Entity entity) {
		if (entity != null) {
			net.minecraft.world.entity.Entity target = entity;
			net.minecraft.world.entity.Entity vehicle = target.getVehicle();
			if (vehicle != null) {
				target = vehicle;
			}
			net.minecraft.world.phys.Vec3 vel = target.getDeltaMovement();
			return Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z) * 20.0;
		}
		return 0;
	}
}.getSpeed(${input$entity}))
