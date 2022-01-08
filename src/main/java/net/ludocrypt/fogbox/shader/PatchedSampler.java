package net.ludocrypt.fogbox.shader;

import java.util.Collection;

import com.google.common.collect.Lists;

import net.ludocrypt.fogbox.access.ShaderAccess;
import net.minecraft.client.render.Shader;

public class PatchedSampler {

	public final String samplerName;
	public Object texture;

	public PatchedSampler(String samplerName, Object texture) {
		this.samplerName = samplerName;
		this.texture = texture;
	}

	public static Collection<PatchedSampler> getPatchedSamplers(Shader shader) {
		if (shader instanceof ShaderAccess patchedSamplerAccess) {
			return patchedSamplerAccess.getPatchedSamplers();
		}
		return Lists.newArrayList();
	}

}
