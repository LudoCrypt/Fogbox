package net.ludocrypt.fogbox.mixin;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.ludocrypt.fogbox.shader.ShaderPatchManager;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.client.gl.Program;

@Mixin(Program.class)
public class ProgramMixin {

	@ModifyVariable(method = "loadProgram", at = @At("STORE"), ordinal = 2)
	private static String fogbox$loadProgram(String in, Program.Type type, String name, InputStream stream, String domain, GLImportProcessor loader) {
		return ShaderPatchManager.applySourcePatches(in, type);
	}

}
