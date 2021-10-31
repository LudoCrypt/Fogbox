package net.ludocrypt.fogbox.access;

import net.ludocrypt.fogbox.resource.FogboxSkyboxManager;
import net.ludocrypt.fogbox.resource.FogboxAtlasManager;

public interface MinecraftClientAccess {

	public FogboxSkyboxManager getFogboxSkyboxManager();

	public FogboxAtlasManager getFogboxAtlasManager();

}
