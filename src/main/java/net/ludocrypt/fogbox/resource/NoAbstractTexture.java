package net.ludocrypt.fogbox.resource;

import java.io.IOException;
import java.util.concurrent.Executor;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class NoAbstractTexture extends AbstractTexture {

	@Override
	public void load(ResourceManager manager) throws IOException {
	}

	@Override
	public void setFilter(boolean bilinear, boolean mipmap) {
	}

	@Override
	public int getGlId() {
		return 0;
	}

	@Override
	public void bindTexture() {
	}

	@Override
	public void registerTexture(TextureManager textureManager, ResourceManager resourceManager, Identifier identifier, Executor executor) {
	}

}
