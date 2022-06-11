package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@ModifyVariable(method = "Lnet/minecraft/client/render/BackgroundRenderer;render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at = @At("STORE"), index = 7)
	private static float fogbox$getSkyColor(float in, Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
		double localY = camera.getPos().y - world.getLevelProperties().getSkyDarknessHeight(world);
		double delta = Math.min(1 + localY / 20.0F, 1.0F);
		return (float) Math.max(delta, 0.01D);
	}
}
