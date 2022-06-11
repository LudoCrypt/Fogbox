package net.ludocrypt.fogbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.ludocrypt.fogbox.shader.NoAbstractTexture;
import net.ludocrypt.fogbox.shader.PatchedSampler;
import net.ludocrypt.fogbox.shader.ShaderPatch;
import net.ludocrypt.fogbox.shader.ShaderPatchManager;

public class Fogbox implements ClientModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("Fogbox");
	public static final PatchedSampler SKYBOX_SAMPLER = new PatchedSampler("SkyboxSampler", new NoAbstractTexture());

	public static final boolean RENDERING_MOD = (FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("optifine") || FabricLoader.getInstance().isModLoaded("optifabric") || FabricLoader.getInstance().isModLoaded("canvas"));

	@Override
	public void onInitializeClient() {
		if (!RENDERING_MOD) {
			ShaderPatchManager.INSTANCE.addToAll(ShaderPatch.builder().sampler(SKYBOX_SAMPLER).fragment().declare("#moj_import <shift_to_skybox.glsl>").wrapCall("linear_fog", "shift_to_skybox_from_fog", "color", "vertexDistance", "FogStart", "FogEnd", "gl_FragCoord", SKYBOX_SAMPLER.samplerName).end().build(), "rendertype_solid", "rendertype_cutout", "rendertype_cutout_mipped", "rendertype_translucent", "rendertype_tripwire");
		}
	}

}
