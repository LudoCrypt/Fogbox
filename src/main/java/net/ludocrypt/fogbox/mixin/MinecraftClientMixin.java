package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.ludocrypt.fogbox.access.WorldRendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow
	@Final
	public WorldRenderer worldRenderer;

	@Inject(method = "onResolutionChanged", at = @At("RETURN"))
	private void fogbox$onResolutionChanged(CallbackInfo ci) {
		if (this.worldRenderer != null) {
			((WorldRendererAccess) this.worldRenderer).updateSkyboxResolution();
		}
	}
}
