package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.ludocrypt.fogbox.Fogbox;
import net.ludocrypt.fogbox.access.WorldRendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

@Mixin(value = WorldRenderer.class, priority = 19)
public abstract class WorldRendererMixin implements WorldRendererAccess {

	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private Framebuffer skyboxBuffer;

	@Unique
	private boolean writingToBuffer = true;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fogbox$init(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
		this.skyboxBuffer = new SimpleFramebuffer(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLjava/lang/Runnable;)V", at = @At("HEAD"))
	private void fogbox$renderSky$head(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Runnable runnable, CallbackInfo ci) {
		if (this.writingToBuffer) {
			this.skyboxBuffer.beginWrite(false);
			RenderSystem.disableBlend();
			RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
		}
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLjava/lang/Runnable;)V", at = @At("RETURN"))
	private void fogbox$renderSky$tail(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Runnable runnable, CallbackInfo ci) {
		if (this.writingToBuffer) {
			this.skyboxBuffer.endWrite();
			MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
			Fogbox.SKYBOX_SAMPLER.texture = this.skyboxBuffer;
			this.writingToBuffer = false;
			RenderSystem.setShader(GameRenderer::getPositionShader);
			this.renderSky(matrices, projectionMatrix, tickDelta, runnable);
			this.writingToBuffer = true;
		}
	}

	@Shadow
	public abstract void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Runnable runnable);

	@Override
	public void updateSkyboxResolution() {
		this.skyboxBuffer.resize(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
	}

}
