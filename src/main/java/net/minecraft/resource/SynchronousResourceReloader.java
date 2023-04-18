package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;

public interface SynchronousResourceReloader extends ResourceReloader {
   default CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
         applyProfiler.startTick();
         applyProfiler.push("listener");
         this.reload(manager);
         applyProfiler.pop();
         applyProfiler.endTick();
      }, applyExecutor);
   }

   void reload(ResourceManager manager);
}
