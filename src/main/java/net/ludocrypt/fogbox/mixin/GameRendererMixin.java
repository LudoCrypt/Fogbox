package net.ludocrypt.fogbox.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;

import net.ludocrypt.fogbox.Fogbox;
import net.ludocrypt.fogbox.access.MinecraftClientAccess;
import net.ludocrypt.fogbox.resource.FogboxAtlasManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private Camera camera;

	@Inject(method = "renderWorld", at = @At("HEAD"))
	private void fogbox$renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		Fogbox.SKYBOX_ATLAS_SAMPLER.texture = MinecraftClient.getInstance().getTextureManager().getTexture(FogboxAtlasManager.ATLAS_TEXTURE);
		BlockPos cameraPos = camera.getBlockPos();
		List<Biome> seenBiomes = Lists.newArrayList();
		seenBiomes.add(client.world.getBiome(cameraPos));
		Fogbox.UV_PRIMARY = getSpriteBounds(((MinecraftClientAccess) client).getFogboxAtlasManager().getSprite(client.world.getRegistryManager().get(Registry.BIOME_KEY).getKey(client.world.getBiome(cameraPos))));
		for (int i = 1; i < 6; i++) {
			Optional<BlockPos> optional = BlockPos.findClosest(cameraPos, 16, 8, (pos) -> !seenBiomes.contains(client.world.getBiome(pos)));
			if (optional.isPresent()) {
				seenBiomes.add(client.world.getBiome(optional.get()));
			} else {
				seenBiomes.add(i, null);
				optional = Optional.empty();
			}

			float dist = 0.0F;

			if (optional.isPresent()) {
				dist = (float) Math.sqrt(camera.getPos().squaredDistanceTo(Vec3d.ofCenter(optional.get())));
			} else {
				dist = 512.0F;
			}

			Vector4f uv = getSpriteBounds(((MinecraftClientAccess) client).getFogboxAtlasManager().getSprite(client.world.getRegistryManager().get(Registry.BIOME_KEY).getKey(seenBiomes.get(i))));

			if (i == 1) {
				Fogbox.DISTANCE_SECONDARY = dist;
				Fogbox.UV_SECONDARY = uv;
			} else if (i == 2) {
				Fogbox.DISTANCE_TERTIARY = dist;
				Fogbox.UV_TERTIARY = uv;
			} else if (i == 3) {
				Fogbox.DISTANCE_QUATERNARY = dist;
				Fogbox.UV_QUATERNARY = uv;
			} else if (i == 4) {
				Fogbox.DISTANCE_QUINARY = dist;
				Fogbox.UV_QUINARY = uv;
			} else if (i == 5) {
				Fogbox.DISTANCE_SENARY = dist;
				Fogbox.UV_SENARY = uv;
			}
		}
	}

	private Vector4f getSpriteBounds(Sprite sprite) {
		return new Vector4f(sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
	}

}
