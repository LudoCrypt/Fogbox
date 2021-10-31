package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.SpriteAtlasTexture;

@Mixin(SpriteAtlasHolder.class)
public interface SpriteAtlasHolderAccessor {

	@Accessor
	SpriteAtlasTexture getAtlas();

}
