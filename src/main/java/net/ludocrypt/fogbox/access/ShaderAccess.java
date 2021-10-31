package net.ludocrypt.fogbox.access;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.ludocrypt.fogbox.shader.PatchedSampler;
import net.ludocrypt.fogbox.shader.PatchedUniform;
import net.minecraft.client.gl.GlUniform;

public interface ShaderAccess {

	@Nullable
	GlUniform getPatchedUniform(PatchedUniform uniform);

	Collection<PatchedSampler> getPatchedSamplers();

}
