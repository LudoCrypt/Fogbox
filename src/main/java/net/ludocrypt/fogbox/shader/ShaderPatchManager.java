package net.ludocrypt.fogbox.shader;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;

public final class ShaderPatchManager {

	public static final ShaderPatchManager INSTANCE = new ShaderPatchManager();

	private final Multimap<String, ShaderPatch> patches = HashMultimap.create();

	private final HashMap<ShaderPatch, List<String>> applyToAllExcept = Maps.newHashMap();

	private final ThreadLocal<Collection<ShaderPatch>> activePatches = new ThreadLocal<>();

	private ShaderPatchManager() {
	}

	public void add(String shader, ShaderPatch patch) {
		this.patches.put(shader, patch);
	}

	public void addToAll(ShaderPatch patch, String... all) {
		for (String shader : all) {
			if (!shader.equals("")) {
				this.patches.put(shader, patch);
			}
		}
	}

	public void addToAllExcept(ShaderPatch patch, String... except) {
		applyToAllExcept.put(patch, Lists.newArrayList(except));
	}

	public static void startPatching(String shader) {
		Collection<ShaderPatch> patches = Lists.newArrayList(INSTANCE.patches.get(shader));
		INSTANCE.applyToAllExcept.forEach((patch, except) -> {
			if (!except.contains(shader)) {
				patches.add(patch);
			}
		});
		INSTANCE.activePatches.set(patches);
	}

	public static void stopPatching() {
		INSTANCE.activePatches.remove();
	}

	public static String applySourcePatches(String source, Program.Type type) {
		Collection<ShaderPatch> activePatches = getActivePatches();
		if (activePatches != null && !activePatches.isEmpty()) {
			for (ShaderPatch patch : activePatches) {
				source = patch.applyToSource(source, type);
			}
		}

		return source;
	}

	public static void applyUniformPatches(GlShader shader, BiConsumer<PatchedUniform, GlUniform> consumer) {
		var activePatches = getActivePatches();
		if (activePatches != null) {
			for (ShaderPatch patch : activePatches) {
				patch.addUniforms(shader, consumer);
			}
		}
	}

	public static void applySamplerPatches(GlShader shader, Consumer<PatchedSampler> consumer) {
		Collection<ShaderPatch> activePatches = getActivePatches();
		if (activePatches != null) {
			for (ShaderPatch patch : activePatches) {
				patch.addSamplers(shader, consumer);
			}
		}
	}

	@Nullable
	private static Collection<ShaderPatch> getActivePatches() {
		return INSTANCE.activePatches.get();
	}
}
