(new Object() {
	public double getFuel(net.minecraft.world.entity.Entity entity) {
		if (entity != null) {
			net.minecraft.world.entity.Entity target = entity;
			java.lang.reflect.Method m = null;
			try {
				m = target.getClass().getMethod("getFuel");
			} catch (Exception e) {
				net.minecraft.world.entity.Entity vehicle = target.getVehicle();
				if (vehicle != null) {
					try {
						m = vehicle.getClass().getMethod("getFuel");
						target = vehicle;
					} catch (Exception ex) {}
				}
			}
			if (m != null) {
				try {
					return ((Number) m.invoke(target)).doubleValue();
				} catch (Exception e) {}
			}
		}
		return 0;
	}
}.getFuel(${input$entity}))
