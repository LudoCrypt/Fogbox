package net.ludocrypt.fogbox.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.ludocrypt.fogbox.access.ShaderAccess;
import net.ludocrypt.fogbox.shader.PatchedSampler;
import net.ludocrypt.fogbox.shader.PatchedUniform;
import net.ludocrypt.fogbox.shader.ShaderPatchManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;

@Mixin(Shader.class)
public abstract class ShaderMixin implements ShaderAccess {

	@Shadow
	@Final
	private List<String> samplerNames;

	@Shadow
	@Final
	private List<GlUniform> uniforms;

	@Unique
	private final Map<PatchedUniform, GlUniform> patchedUniforms = new Reference2ObjectOpenHashMap<>();

	@Unique
	private final Collection<PatchedSampler> patchedSamplers = Lists.newArrayList();

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceFactory;getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;"))
	private void fogbox$initEarly(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
		ShaderPatchManager.startPatching(name);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Shader;readBlendState(Lcom/google/gson/JsonObject;)Lnet/minecraft/client/gl/GlBlendState;"))
	private void fogbox$initUniforms(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
		ShaderPatchManager.applySamplerPatches((Shader) (Object) this, (patchedSampler) -> {
			this.patchedSamplers.add(patchedSampler);
			this.samplerNames.add(patchedSampler.samplerName);
			this.addSampler(patchedSampler.samplerName, (Supplier<?>) () -> patchedSampler.texture);
		});
		ShaderPatchManager.applyUniformPatches((Shader) (Object) this, (patchedUniform, glUniform) -> {
			this.uniforms.add(glUniform);
			this.patchedUniforms.put(patchedUniform, glUniform);
		});
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void fogbox$initLate(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
		ShaderPatchManager.stopPatching();
	}

	@ModifyVariable(method = "bind", at = @At("STORE"), ordinal = 0)
	private Object fogbox$bind(Object in) {
		if (in instanceof Supplier<?> supplier) {
			return supplier.get();
		}
		return in;
	}

	@Shadow
	public abstract void addSampler(String name, Object sampler);

	@Override
	public Collection<PatchedSampler> getPatchedSamplers() {
		return this.patchedSamplers;
	}

	@Override
	public @Nullable GlUniform getPatchedUniform(PatchedUniform uniform) {
		return this.patchedUniforms.get(uniform);
	}

}
