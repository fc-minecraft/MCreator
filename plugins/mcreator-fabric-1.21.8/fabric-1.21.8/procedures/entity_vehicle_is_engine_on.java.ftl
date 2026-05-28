(new Object() {
	public boolean isEngineOn(net.minecraft.world.entity.Entity entity) {
		if (entity != null) {
			net.minecraft.world.entity.Entity target = entity;
			java.lang.reflect.Method m = null;
			try {
				m = target.getClass().getMethod("isEngineOn");
			} catch (Exception e) {
				net.minecraft.world.entity.Entity vehicle = target.getVehicle();
				if (vehicle != null) {
					try {
						m = vehicle.getClass().getMethod("isEngineOn");
						target = vehicle;
					} catch (Exception ex) {}
				}
			}
			if (m != null) {
				try {
					return (Boolean) m.invoke(target);
				} catch (Exception e) {}
			}
		}
		return false;
	}
}.isEngineOn(${input$entity}))
