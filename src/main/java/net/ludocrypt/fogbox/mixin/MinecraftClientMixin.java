package net.ludocrypt.fogbox.mixin;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.ludocrypt.fogbox.access.MinecraftClientAccess;
import net.ludocrypt.fogbox.resource.FogboxAtlasManager;
import net.ludocrypt.fogbox.resource.FogboxSkyboxManager;
import net.ludocrypt.fogbox.resource.SimpleDoubleReloader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.util.Identifier;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements MinecraftClientAccess {

	@Unique
	private FogboxSkyboxManager fogboxSkyboxManager;

	@Unique
	private FogboxAtlasManager fogboxAtlasManager;

	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Shadow
	@Final
	private TextureManager textureManager;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;registerReloader(Lnet/minecraft/resource/ResourceReloader;)V", ordinal = 1, shift = Shift.AFTER))
	private void fogbox$init(RunArgs args, CallbackInfo ci) {
		this.fogboxSkyboxManager = new FogboxSkyboxManager();
		this.fogboxAtlasManager = new FogboxAtlasManager(this.textureManager, this.fogboxSkyboxManager);
		SimpleDoubleReloader<HashMap<Identifier, Identifier>, SpriteAtlasTexture.Data, FogboxSkyboxManager, FogboxAtlasManager> skyboxManager = new SimpleDoubleReloader<HashMap<Identifier, Identifier>, SpriteAtlasTexture.Data, FogboxSkyboxManager, FogboxAtlasManager>(this.fogboxSkyboxManager, this.fogboxAtlasManager);
		this.resourceManager.registerReloader(skyboxManager);
	}

	@Override
	public FogboxSkyboxManager getFogboxSkyboxManager() {
		return fogboxSkyboxManager;
	}

	@Override
	public FogboxAtlasManager getFogboxAtlasManager() {
		return fogboxAtlasManager;
	}

}
