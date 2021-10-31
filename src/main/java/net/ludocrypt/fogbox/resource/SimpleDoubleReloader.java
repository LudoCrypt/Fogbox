package net.ludocrypt.fogbox.resource;

import net.ludocrypt.fogbox.mixin.SinglePreparationResourceReloaderAccessor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.profiler.Profiler;

@SuppressWarnings("unchecked")
public class SimpleDoubleReloader<C, D, A extends SinglePreparationResourceReloader<C>, B extends SinglePreparationResourceReloader<D>> extends DoublePreparationResourceReloader<C, D> {

	private A first;
	private B second;

	public SimpleDoubleReloader(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override
	protected C prepareFirst(ResourceManager manager, Profiler profiler) {
		return ((SinglePreparationResourceReloaderAccessor<C>) (this.first)).callPrepare(manager, profiler);
	}

	@Override
	protected void applyFirst(C prepared, ResourceManager manager, Profiler profiler) {
		((SinglePreparationResourceReloaderAccessor<C>) (this.first)).callApply(prepared, manager, profiler);
	}

	@Override
	protected D prepareSecond(ResourceManager manager, Profiler profiler) {
		return ((SinglePreparationResourceReloaderAccessor<D>) (this.second)).callPrepare(manager, profiler);
	}

	@Override
	protected void applySecond(D prepared, ResourceManager manager, Profiler profiler) {
		((SinglePreparationResourceReloaderAccessor<D>) (this.second)).callApply(prepared, manager, profiler);
	}

}
