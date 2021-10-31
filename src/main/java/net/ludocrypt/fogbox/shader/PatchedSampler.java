package net.ludocrypt.fogbox.shader;

import java.util.Collection;

import com.google.common.collect.Lists;

import net.ludocrypt.fogbox.access.ShaderAccess;
import net.minecraft.client.render.Shader;
import net.minecraft.client.texture.AbstractTexture;

public class PatchedSampler {

	public final String samplerName;
	public AbstractTexture texture;
	public final boolean blur;
	public final boolean mipmap;

	public PatchedSampler(String samplerName, AbstractTexture texture, boolean blur, boolean mipmap) {
		this.samplerName = samplerName;
		this.texture = texture;
		this.blur = blur;
		this.mipmap = mipmap;
	}

	public PatchedSampler(String samplerName, AbstractTexture texture) {
		this(samplerName, texture, false, false);
	}

	public static Collection<PatchedSampler> getPatchedSamplers(Shader shader) {
		if (shader instanceof ShaderAccess patchedSamplerAccess) {
			return patchedSamplerAccess.getPatchedSamplers();
		}
		return Lists.newArrayList();
	}

}
