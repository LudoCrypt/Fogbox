package net.ludocrypt.fogbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.ludocrypt.fogbox.resource.NoAbstractTexture;
import net.ludocrypt.fogbox.shader.PatchedSampler;
import net.ludocrypt.fogbox.shader.PatchedUniform;
import net.ludocrypt.fogbox.shader.ShaderPatch;
import net.ludocrypt.fogbox.shader.ShaderPatchManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;

public class Fogbox implements ClientModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("Fogbox");

	public static final PatchedSampler SKYBOX_ATLAS_SAMPLER = new PatchedSampler("SkyboxAtlasSampler", new NoAbstractTexture());

	public static Vector4f UV_PRIMARY = new Vector4f(0, 0, 1, 1);
	public static Vector4f UV_SECONDARY = new Vector4f(0, 0, 1, 1);
	public static Vector4f UV_TERTIARY = new Vector4f(0, 0, 1, 1);
	public static Vector4f UV_QUATERNARY = new Vector4f(0, 0, 1, 1);
	public static Vector4f UV_QUINARY = new Vector4f(0, 0, 1, 1);
	public static Vector4f UV_SENARY = new Vector4f(0, 0, 1, 1);

	public static final PatchedUniform UV_UNIFORM_PRIMARY = PatchedUniform.ofFloat4("UVPrimary", 0, 0, 1, 1);
	public static final PatchedUniform UV_UNIFORM_SECONDARY = PatchedUniform.ofFloat4("UVSecondary", 0, 0, 1, 1);
	public static final PatchedUniform UV_UNIFORM_TERTIARY = PatchedUniform.ofFloat4("UVTertiary", 0, 0, 1, 1);
	public static final PatchedUniform UV_UNIFORM_QUATERNARY = PatchedUniform.ofFloat4("UVQuaternary", 0, 0, 1, 1);
	public static final PatchedUniform UV_UNIFORM_QUINARY = PatchedUniform.ofFloat4("UVQuinary", 0, 0, 1, 1);
	public static final PatchedUniform UV_UNIFORM_SENARY = PatchedUniform.ofFloat4("UVSenary", 0, 0, 1, 1);

	public static float DISTANCE_SECONDARY = 0.0F;
	public static float DISTANCE_TERTIARY = 0.0F;
	public static float DISTANCE_QUATERNARY = 0.0F;
	public static float DISTANCE_QUINARY = 0.0F;
	public static float DISTANCE_SENARY = 0.0F;

	public static final PatchedUniform DISTANCE_UNIFORM_SECONDARY = PatchedUniform.ofFloat("DistanceSecondary", 0.0F);
	public static final PatchedUniform DISTANCE_UNIFORM_TERTIARY = PatchedUniform.ofFloat("DistanceTertiary", 0.0F);
	public static final PatchedUniform DISTANCE_UNIFORM_QUATERNARY = PatchedUniform.ofFloat("DistanceQuaternary", 0.0F);
	public static final PatchedUniform DISTANCE_UNIFORM_QUINARY = PatchedUniform.ofFloat("DistanceQuinary", 0.0F);
	public static final PatchedUniform DISTANCE_UNIFORM_SENARY = PatchedUniform.ofFloat("DistanceSenary", 0.0F);

	public static final PatchedUniform MODEL_MATRIX = PatchedUniform.ofMat4x4("ModelMatrix", new MatrixStack().peek().getModel());

	@Override
	public void onInitializeClient() {
		ShaderPatchManager.INSTANCE.addToAll(ShaderPatch.builder().sampler(SKYBOX_ATLAS_SAMPLER).vertex().declare("out vec4 glPos;").insertAfter((s) -> s.contains("texCoord0 = "), "glPos = gl_Position;").end().fragment().declare("#moj_import <shift_to_skybox.glsl>").declare("in vec4 glPos;").declare("uniform mat4 ProjMat;").addUniform(DISTANCE_UNIFORM_SECONDARY).addUniform(DISTANCE_UNIFORM_TERTIARY).addUniform(DISTANCE_UNIFORM_QUATERNARY).addUniform(DISTANCE_UNIFORM_QUINARY).addUniform(DISTANCE_UNIFORM_SENARY).addUniform(UV_UNIFORM_PRIMARY).addUniform(UV_UNIFORM_SECONDARY).addUniform(UV_UNIFORM_TERTIARY).addUniform(UV_UNIFORM_QUATERNARY).addUniform(UV_UNIFORM_QUINARY).addUniform(UV_UNIFORM_SENARY).addUniform(MODEL_MATRIX).wrapCall("linear_fog", "shift_to_skybox_from_fog", MODEL_MATRIX.getName(), "ProjMat", "color", "vertexDistance", "vertexColor", "ColorModulator", "FogStart", "FogEnd", "FogColor", "glPos", UV_UNIFORM_PRIMARY.getName(), SKYBOX_ATLAS_SAMPLER.samplerName, UV_UNIFORM_SECONDARY.getName(), DISTANCE_UNIFORM_SECONDARY.getName(), UV_UNIFORM_TERTIARY.getName(), DISTANCE_UNIFORM_TERTIARY.getName(), UV_UNIFORM_QUATERNARY.getName(), DISTANCE_UNIFORM_QUATERNARY.getName(), UV_UNIFORM_QUINARY.getName(), DISTANCE_UNIFORM_QUINARY.getName(), UV_UNIFORM_SENARY.getName(), DISTANCE_UNIFORM_SENARY.getName()).end().build(), "position_tex_color_normal", "rendertype_solid", "rendertype_cutout", "rendertype_cutout_mipped", "rendertype_translucent", "rendertype_tripwire");
	}

}
