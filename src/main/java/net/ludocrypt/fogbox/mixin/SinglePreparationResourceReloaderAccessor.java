package net.ludocrypt.fogbox.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.profiler.Profiler;

@Mixin(SinglePreparationResourceReloader.class)
public interface SinglePreparationResourceReloaderAccessor<T> {

	@Invoker
	T callPrepare(ResourceManager manager, Profiler profiler);

	@Invoker
	void callApply(T prepared, ResourceManager manager, Profiler profiler);

}
