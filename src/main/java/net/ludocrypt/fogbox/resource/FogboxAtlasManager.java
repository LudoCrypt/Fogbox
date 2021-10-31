package net.ludocrypt.fogbox.resource;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public class FogboxAtlasManager extends SpriteAtlasHolder {

	public static final Identifier ATLAS_TEXTURE = new Identifier("fogbox", "textures/atlas/skyboxes.png");
	private FogboxSkyboxManager fogboxManager;

	public FogboxAtlasManager(TextureManager manager, FogboxSkyboxManager fogboxManager) {
		super(manager, ATLAS_TEXTURE, "skies");
		this.fogboxManager = fogboxManager;
	}

	@Override
	public Stream<Identifier> getSprites() {
		return Stream.concat(fogboxManager.fogboxs.values().stream(), Stream.of(new Identifier("fogbox", "any")));
	}

	@Override
	public Sprite getSprite(Identifier id) {
		return super.getSprite(id);
	}

	public Sprite getSprite(Optional<RegistryKey<Biome>> biome) {
		if (biome.isPresent()) {
			return this.getSprite(this.fogboxManager.fogboxs.getOrDefault(biome.get().getValue(), new Identifier("fogbox", "any")));
		}
		return this.getSprite(new Identifier("fogbox", "any"));
	}

	@Override
	public SpriteAtlasTexture.Data prepare(ResourceManager resourceManager, Profiler profiler) {
		return super.prepare(resourceManager, profiler);
	}

	@Override
	public void apply(SpriteAtlasTexture.Data data, ResourceManager resourceManager, Profiler profiler) {
		super.apply(data, resourceManager, profiler);
	}

}
