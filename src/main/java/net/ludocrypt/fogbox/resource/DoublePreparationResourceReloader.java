package net.ludocrypt.fogbox.resource;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

public abstract class DoublePreparationResourceReloader<A, B> implements ResourceReloader {
	public final CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		CompletableFuture<Pair<A, B>> future = CompletableFuture.supplyAsync(() -> {
			A a = this.prepareFirst(manager, applyProfiler);
			B b = this.prepareSecond(manager, applyProfiler);
			return Pair.of(a, b);
		}, prepareExecutor);
		Objects.requireNonNull(synchronizer);
		return future.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((pair) -> {
			this.applyFirst(pair.getFirst(), manager, applyProfiler);
			this.applySecond(pair.getSecond(), manager, applyProfiler);
		}, applyExecutor);
	}

	protected abstract A prepareFirst(ResourceManager manager, Profiler profiler);

	protected abstract void applyFirst(A prepared, ResourceManager manager, Profiler profiler);

	protected abstract B prepareSecond(ResourceManager manager, Profiler profiler);

	protected abstract void applySecond(B prepared, ResourceManager manager, Profiler profiler);
}
