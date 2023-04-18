package net.minecraft.resource;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiler.Profiler;

public abstract class SinglePreparationResourceReloader implements ResourceReloader {
   public final CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture var10000 = CompletableFuture.supplyAsync(() -> {
         return this.prepare(manager, prepareProfiler);
      }, prepareExecutor);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((prepared) -> {
         this.apply(prepared, manager, applyProfiler);
      }, applyExecutor);
   }

   protected abstract Object prepare(ResourceManager manager, Profiler profiler);

   protected abstract void apply(Object prepared, ResourceManager manager, Profiler profiler);
}
