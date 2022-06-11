package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

@Mixin(value = WorldRenderer.class, priority = 19)
public abstract class WorldRendererMixin implements WorldRendererAccess {

	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;

	@Shadow
	private VertexBuffer lightSkyBuffer;

	@Shadow
	private VertexBuffer darkSkyBuffer;

	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private boolean writingToBuffer = true;

	@Unique
	private Framebuffer skyFramebuffer;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fogbox$init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
		this.skyFramebuffer = new SimpleFramebuffer(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"))
	private void fogbox$renderSky$head(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		if (this.writingToBuffer) {
			this.skyFramebuffer.beginWrite(false);
			RenderSystem.disableBlend();
			RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
		}
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("RETURN"))
	private void fogbox$renderSky$tail(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
		if (this.writingToBuffer) {
			this.skyFramebuffer.endWrite();
			Framebuffer framebuffer = this.client.getFramebuffer();
			framebuffer.beginWrite(false);
			Fogbox.SKYBOX_SAMPLER.texture = this.skyFramebuffer;

			this.writingToBuffer = false;
			RenderSystem.setShader(GameRenderer::getPositionShader);
			this.renderSky(matrices, projectionMatrix, tickDelta, camera, bl, runnable);
			this.writingToBuffer = true;
		}
	}

	@Redirect(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V", ordinal = 2))
	private void fogbox$renderDarkSky(VertexBuffer darkSkyBuffer, Matrix4f viewMatrix, Matrix4f projectionMatrix, Shader shader, MatrixStack matrices, Matrix4f projectionMatrixIn, float tickDelta, Camera cameraIn, boolean blIn, Runnable runnableIn) {
		// Stop that
	}

	@ModifyVariable(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/DimensionEffects;getFogColorOverride(FF)[F"))
	private float[] fogbox$getFogColorOverride(float[] in, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable) {
		double localY = this.client.player.getCameraPosVec((float) tickDelta).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
		if (localY < 0.0D && in != null) {
			float[] out = new float[4];

			float delta = (float) Math.min(-localY / 20.0F, 1.0F);

			out[0] = MathHelper.lerp(delta, in[0], 5.0F / 255.0F);
			out[1] = MathHelper.lerp(delta, in[1], 8.0F / 255.0F);
			out[2] = MathHelper.lerp(delta, in[2], 17.0F / 255.0F);
			out[3] = in[3];

			return out;
		}
		return in;
	}

	@ModifyVariable(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
	private Vec3d fogbox$getSkyColor(Vec3d in, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable) {
		double localY = this.client.player.getCameraPosVec((float) tickDelta).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
		if (localY < 0.0D && in != null) {
			double delta = Math.min(-localY / 20.0F, 1.0F);
			return new Vec3d(MathHelper.lerp(delta, in.x, 5.0D / 255.0D), MathHelper.lerp(delta, in.y, 8.0D / 255.0D), MathHelper.lerp(delta, in.z, 17.0D / 255.0D));
		}
		return in;
	}

	@ModifyVariable(method = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "STORE", ordinal = 1), index = 15)
	private float fogbox$getRainGradient(float in, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable) {
		double localY = this.client.player.getCameraPosVec((float) tickDelta).y - this.world.getLevelProperties().getSkyDarknessHeight(this.world);
		if (localY < 0.0D) {
			float delta = (float) Math.min(-localY / 20.0F, 1.0F);
			return MathHelper.lerp(delta, in, 0.0F);
		}
		return in;
	}

	@Shadow
	public abstract void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable);

	@Override
	public void updateSkyboxResolution() {
		this.skyFramebuffer.resize(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
	}

}
