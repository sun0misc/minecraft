package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;

public class SimpleResourceReload implements ResourceReload {
   private static final int FIRST_PREPARE_APPLY_WEIGHT = 2;
   private static final int SECOND_PREPARE_APPLY_WEIGHT = 2;
   private static final int RELOADER_WEIGHT = 1;
   protected final CompletableFuture prepareStageFuture = new CompletableFuture();
   protected CompletableFuture applyStageFuture;
   final Set waitingReloaders;
   private final int reloaderCount;
   private int toApplyCount;
   private int appliedCount;
   private final AtomicInteger toPrepareCount = new AtomicInteger();
   private final AtomicInteger preparedCount = new AtomicInteger();

   public static SimpleResourceReload create(ResourceManager manager, List reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture initialStage) {
      return new SimpleResourceReload(prepareExecutor, applyExecutor, manager, reloaders, (synchronizer, resourceManager, reloader, prepare, apply) -> {
         return reloader.reload(synchronizer, resourceManager, DummyProfiler.INSTANCE, DummyProfiler.INSTANCE, prepareExecutor, apply);
      }, initialStage);
   }

   protected SimpleResourceReload(Executor prepareExecutor, final Executor applyExecutor, ResourceManager manager, List reloaders, Factory factory, CompletableFuture initialStage) {
      this.reloaderCount = reloaders.size();
      this.toPrepareCount.incrementAndGet();
      AtomicInteger var10001 = this.preparedCount;
      Objects.requireNonNull(var10001);
      initialStage.thenRun(var10001::incrementAndGet);
      List list2 = Lists.newArrayList();
      final CompletableFuture completableFuture2 = initialStage;
      this.waitingReloaders = Sets.newHashSet(reloaders);

      CompletableFuture completableFuture4;
      for(Iterator var9 = reloaders.iterator(); var9.hasNext(); completableFuture2 = completableFuture4) {
         final ResourceReloader lv = (ResourceReloader)var9.next();
         completableFuture4 = factory.create(new ResourceReloader.Synchronizer() {
            public CompletableFuture whenPrepared(Object preparedObject) {
               applyExecutor.execute(() -> {
                  SimpleResourceReload.this.waitingReloaders.remove(lv);
                  if (SimpleResourceReload.this.waitingReloaders.isEmpty()) {
                     SimpleResourceReload.this.prepareStageFuture.complete(Unit.INSTANCE);
                  }

               });
               return SimpleResourceReload.this.prepareStageFuture.thenCombine(completableFuture2, (arg, object2) -> {
                  return preparedObject;
               });
            }
         }, manager, lv, (preparation) -> {
            this.toPrepareCount.incrementAndGet();
            prepareExecutor.execute(() -> {
               preparation.run();
               this.preparedCount.incrementAndGet();
            });
         }, (application) -> {
            ++this.toApplyCount;
            applyExecutor.execute(() -> {
               application.run();
               ++this.appliedCount;
            });
         });
         list2.add(completableFuture4);
      }

      this.applyStageFuture = Util.combine(list2);
   }

   public CompletableFuture whenComplete() {
      return this.applyStageFuture;
   }

   public float getProgress() {
      int i = this.reloaderCount - this.waitingReloaders.size();
      float f = (float)(this.preparedCount.get() * 2 + this.appliedCount * 2 + i * 1);
      float g = (float)(this.toPrepareCount.get() * 2 + this.toApplyCount * 2 + this.reloaderCount * 1);
      return f / g;
   }

   public static ResourceReload start(ResourceManager manager, List reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture initialStage, boolean profiled) {
      return (ResourceReload)(profiled ? new ProfiledResourceReload(manager, reloaders, prepareExecutor, applyExecutor, initialStage) : create(manager, reloaders, prepareExecutor, applyExecutor, initialStage));
   }

   protected interface Factory {
      CompletableFuture create(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, ResourceReloader reloader, Executor prepareExecutor, Executor applyExecutor);
   }
}
