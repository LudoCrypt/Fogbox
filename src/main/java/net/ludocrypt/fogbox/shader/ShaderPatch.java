package net.ludocrypt.fogbox.shader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;

public final class ShaderPatch {

	private final Collection<PatchedUniform> uniforms;
	private final Collection<PatchedSampler> samplers;
	private final ShaderSourcePatcher vertex;
	private final ShaderSourcePatcher fragment;

	ShaderPatch(Collection<PatchedUniform> uniforms, Collection<PatchedSampler> samplers, ShaderSourcePatcher vertex, ShaderSourcePatcher fragment) {
		this.uniforms = uniforms;
		this.samplers = samplers;
		this.vertex = vertex;
		this.fragment = fragment;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addUniforms(GlShader shader, BiConsumer<PatchedUniform, GlUniform> consumer) {
		for (var uniform : this.uniforms) {
			consumer.accept(uniform, uniform.toGlUniform(shader));
		}
	}

	public void addSamplers(GlShader shader, Consumer<PatchedSampler> consumer) {
		for (PatchedSampler sampler : this.samplers) {
			consumer.accept(sampler);
		}
	}

	public String applyToSource(String source, Program.Type type) {
		ShaderSourcePatcher patcher = type == Program.Type.VERTEX ? this.vertex : this.fragment;
		return patcher.apply(source);
	}

	public static final class Builder {
		private final Map<String, PatchedUniform> uniforms = new Object2ObjectLinkedOpenHashMap<>();
		private final List<PatchedSampler> extraSamplers = Lists.newArrayList();
		private SourceBuilder vertex;
		private SourceBuilder fragment;

		Builder() {
		}

		void addUniform(PatchedUniform uniform) {
			this.uniforms.put(uniform.name, uniform);
		}

		public SourceBuilder vertex() {
			if (this.vertex == null) {
				this.vertex = new SourceBuilder(this);
			}
			return this.vertex;
		}

		public SourceBuilder fragment() {
			if (this.fragment == null) {
				this.fragment = new SourceBuilder(this);
			}
			return this.fragment;
		}

		public Builder sampler(PatchedSampler sampler) {
			this.extraSamplers.add(sampler);
			this.fragment = this.fragment().declare("uniform sampler2D " + sampler.samplerName + ";");
			return this;
		}

		public ShaderPatch build() {
			ShaderSourcePatcher vertex = this.vertex != null ? this.vertex.build() : ShaderSourcePatcher.NO;
			ShaderSourcePatcher fragment = this.fragment != null ? this.fragment.build() : ShaderSourcePatcher.NO;
			return new ShaderPatch(this.uniforms.values(), this.extraSamplers, vertex, fragment);
		}
	}

	public static final class SourceBuilder {
		private final Builder parent;
		private final List<ShaderSourcePatcher> patchers = new ArrayList<>();

		SourceBuilder(Builder parent) {
			this.parent = parent;
		}

		public SourceBuilder addUniform(PatchedUniform uniform) {
			this.parent.addUniform(uniform);
			return this.declare("uniform " + uniform.type.glslType + " " + uniform.name + ";");
		}

		public SourceBuilder declare(String... declarations) {
			return this.patch(ShaderSourcePatcher.insertDeclarations(declarations));
		}

		public SourceBuilder insertBefore(Predicate<String> match, String... insert) {
			return this.patch(ShaderSourcePatcher.insertBefore(match, insert));
		}

		public SourceBuilder insertAfter(Predicate<String> match, String... insert) {
			return this.patch(ShaderSourcePatcher.insertAfter(match, insert));
		}

		public SourceBuilder wrapCall(String targetFunction, String wrapperFunction, String... additionalArguments) {
			return this.patch(ShaderSourcePatcher.wrapCall(targetFunction, wrapperFunction, additionalArguments));
		}

		public SourceBuilder patch(ShaderSourcePatcher patcher) {
			this.patchers.add(patcher);
			return this;
		}

		public Builder end() {
			return this.parent;
		}

		ShaderSourcePatcher build() {
			return source -> {
				for (ShaderSourcePatcher patcher : this.patchers) {
					source = patcher.apply(source);
				}
				return source;
			};
		}
	}
}
