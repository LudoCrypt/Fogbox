package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.ludocrypt.fogbox.Fogbox;
import net.ludocrypt.fogbox.access.WorldRendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;

@Mixin(value = WorldRenderer.class, priority = 19)
public abstract class WorldRendererMixin implements WorldRendererAccess {

	@Shadow
	private ClientWorld world;

	@Shadow
	private VertexBuffer lightSkyBuffer;

	@Shadow
	private VertexBuffer darkSkyBuffer;

	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private Framebuffer skyboxBuffer;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fogbox$init(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
		this.skyboxBuffer = new SimpleFramebuffer(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"))
	private void fogbox$renderSky$head(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		this.skyboxBuffer.beginWrite(false);
		RenderSystem.disableBlend();
		RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("RETURN"))
	private void fogbox$renderSky$tail(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		this.skyboxBuffer.endWrite();
		Framebuffer framebuffer = this.client.getFramebuffer();
		framebuffer.beginWrite(false);
		Fogbox.SKYBOX_SAMPLER.texture = this.skyboxBuffer;
		this.skyboxBuffer.draw(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
		framebuffer.copyDepthFrom(this.skyboxBuffer);
		framebuffer.beginWrite(false);
	}

	@Redirect(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;setShader(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V", ordinal = 2))
	private void fogbox$renderDarkSky(VertexBuffer darkSkyBuffer, Matrix4f viewMatrix, Matrix4f projectionMatrix, Shader shader, MatrixStack matrices, Matrix4f projectionMatrixIn, float tickDelta, Camera cameraIn, boolean blIn, Runnable runnableIn) {
		// Stop doing that
	}

	@Override
	public void updateSkyboxResolution() {
		this.skyboxBuffer.resize(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
	}

}
