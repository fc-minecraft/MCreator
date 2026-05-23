<#-- @formatter:off -->
package ${package}.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import ${package}.entity.${name}Entity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

<#if data.modelName == "Biped">
	<#assign humanoid = true>
	<#assign model = "HumanoidModel<HumanoidRenderState>">
	<#assign renderState = "HumanoidRenderState">
	<#assign rootPart = "context.bakeLayer(ModelLayers.PLAYER)">
<#else>
	<#assign humanoid = false>
	<#assign model = data.modelName>
	<#assign renderState = "LivingEntityRenderState">
	<#assign rootPart = "context.bakeLayer(" + data.modelName + ".LAYER_LOCATION)">
	import ${package}.client.model.${data.modelName};
</#if>

@Environment(EnvType.CLIENT)
public class ${name}Renderer extends <#if humanoid>net.minecraft.client.renderer.entity.Humanoid</#if>MobRenderer<${name}Entity, ${renderState}, ${model}> {

	public ${name}Renderer(EntityRendererProvider.Context context) {
		super(context, new <#if humanoid>HumanoidModel<#else>${model}</#if>(${rootPart}), 0.5f);
		<#if humanoid>
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
						new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getEquipmentRenderer()));
		</#if>
	}

	@Override
	public ${renderState} createRenderState() {
		return new ${renderState}();
	}

	private java.util.List<net.minecraft.client.model.geom.ModelPart> spinningParts;
	private float spinAngle = 0.0f;

	@Override
	public void extractRenderState(${name}Entity entity, ${renderState} state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		
		if (this.spinningParts == null) {
			this.spinningParts = new java.util.ArrayList<>();
			String[] partNames = "${data.spinParts}".split(",");
			for (String name : partNames) {
				String trimmed = name.trim();
				if (!trimmed.isEmpty()) {
					try {
						java.lang.reflect.Field field = this.model.getClass().getDeclaredField(trimmed);
						field.setAccessible(true);
						Object val = field.get(this.model);
						if (val instanceof net.minecraft.client.model.geom.ModelPart) {
							this.spinningParts.add((net.minecraft.client.model.geom.ModelPart) val);
						}
					} catch (Exception e) {
						for (java.lang.reflect.Field f : this.model.getClass().getDeclaredFields()) {
							if (f.getName().equalsIgnoreCase(trimmed) && net.minecraft.client.model.geom.ModelPart.class.isAssignableFrom(f.getType())) {
								try {
									f.setAccessible(true);
									this.spinningParts.add((net.minecraft.client.model.geom.ModelPart) f.get(this.model));
								} catch (Exception ignored) {}
							}
						}
					}
				}
			}
		}

		boolean engineOn = entity.isEngineOn();
		if (engineOn) {
			this.spinAngle += (float) ${data.spinSpeed} * partialTicks;
			for (net.minecraft.client.model.geom.ModelPart part : this.spinningParts) {
				boolean isRotor = false;
				for (java.lang.reflect.Field f : this.model.getClass().getDeclaredFields()) {
					try {
						f.setAccessible(true);
						if (f.get(this.model) == part) {
							String name = f.getName().toLowerCase();
							if (name.contains("rotor") || name.contains("wheel") || name.contains("y")) {
								isRotor = true;
							}
							break;
						}
					} catch (Exception ignored) {}
				}
				if (isRotor) {
					part.yRot = this.spinAngle;
				} else {
					part.zRot = this.spinAngle;
				}
			}
		}
	}

	@Override
	public ResourceLocation getTextureLocation(${renderState} state) {
		return ResourceLocation.parse("${modid}:textures/entities/${data.texture}");
	}
}
