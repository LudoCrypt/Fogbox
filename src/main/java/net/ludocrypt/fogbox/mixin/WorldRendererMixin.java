package net.ludocrypt.fogbox.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.ludocrypt.fogbox.Fogbox;
import net.ludocrypt.fogbox.shader.PatchedSampler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;startDrawing()V", shift = At.Shift.AFTER))
	private void fogbox$renderLayer(RenderLayer layer, MatrixStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci) {
		fogbox$updateShader(transform);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V", ordinal = 0, shift = Shift.BEFORE))
	private void fogbox$renderSky$overworld(MatrixStack matrices, Matrix4f matrix4f, float f, Runnable runnable, CallbackInfo ci) {
		fogbox$drawFogSky(matrices);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", ordinal = 0, shift = Shift.AFTER))
	private void fogbox$renderSky$nether(MatrixStack matrices, Matrix4f matrix4f, float f, Runnable runnable, CallbackInfo ci) {
		if (this.client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL && this.client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.END) {
			fogbox$drawFogSky(matrices);
		}
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEndSky(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 0, shift = Shift.AFTER))
	private void fogbox$renderSky$end(MatrixStack matrices, Matrix4f matrix4f, float f, Runnable runnable, CallbackInfo ci) {
		fogbox$drawFogSky(matrices);
	}

	@Inject(method = "renderClouds", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal = 0, shift = Shift.AFTER))
	private void fogbox$renderClouds(MatrixStack matrices, Matrix4f matrix4f, float f, double d, double e, double g, CallbackInfo ci) {
		fogbox$updateShader(matrices);
	}

	@Unique
	private void fogbox$drawFogSky(MatrixStack matrices) {
		RenderSystem.setShaderTexture(0, new Identifier("fogbox", "textures/skies/mask.png"));
		RenderSystem.setShader(GameRenderer::getRenderTypeSolidShader);
		fogbox$updateShader(matrices);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		for (int i = 0; i < 6; ++i) {
			matrices.push();

			if (i == 0) {
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
				matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			}

			if (i == 1) {
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
			}

			if (i == 2) {
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
			}

			if (i == 3) {
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
			}

			if (i == 4) {
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
			}

			Matrix4f matrix4f2 = matrices.peek().getModel();

			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
			bufferBuilder.vertex(matrix4f2, -100.0F, -100.0F, -100.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(-1, -1, -1).next();
			bufferBuilder.vertex(matrix4f2, -100.0F, -100.0F, 100.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(-1, -1, 1).next();
			bufferBuilder.vertex(matrix4f2, 100.0F, -100.0F, 100.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(1, -1, 1).next();
			bufferBuilder.vertex(matrix4f2, 100.0F, -100.0F, -100.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(1, -1, -1).next();
			tessellator.draw();
			matrices.pop();
		}
	}

	@Unique
	private void fogbox$updateShader(MatrixStack stack) {
		Shader shader = RenderSystem.getShader();
		Collection<PatchedSampler> samplers = PatchedSampler.getPatchedSamplers(shader);
		samplers.forEach((sampler) -> {
			sampler.texture.setFilter(sampler.blur, sampler.mipmap);
			shader.addSampler(sampler.samplerName, sampler.texture);
		});
		Fogbox.UV_UNIFORM_PRIMARY.get(shader).set(Fogbox.UV_PRIMARY);
		Fogbox.UV_UNIFORM_SECONDARY.get(shader).set(Fogbox.UV_SECONDARY);
		Fogbox.UV_UNIFORM_TERTIARY.get(shader).set(Fogbox.UV_TERTIARY);
		Fogbox.UV_UNIFORM_QUATERNARY.get(shader).set(Fogbox.UV_QUATERNARY);
		Fogbox.UV_UNIFORM_QUINARY.get(shader).set(Fogbox.UV_QUINARY);
		Fogbox.UV_UNIFORM_SENARY.get(shader).set(Fogbox.UV_SENARY);
		Fogbox.DISTANCE_UNIFORM_SECONDARY.get(shader).set(Fogbox.DISTANCE_SECONDARY);
		Fogbox.DISTANCE_UNIFORM_TERTIARY.get(shader).set(Fogbox.DISTANCE_TERTIARY);
		Fogbox.DISTANCE_UNIFORM_QUATERNARY.get(shader).set(Fogbox.DISTANCE_QUATERNARY);
		Fogbox.DISTANCE_UNIFORM_QUINARY.get(shader).set(Fogbox.DISTANCE_QUINARY);
		Fogbox.DISTANCE_UNIFORM_SENARY.get(shader).set(Fogbox.DISTANCE_SENARY);
		Fogbox.MODEL_MATRIX.get(shader).set(stack.peek().getModel());
	}

}
