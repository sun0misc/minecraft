package net.minecraft.resource;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfilerSystem;
import org.slf4j.Logger;

public class ProfiledResourceReload extends SimpleResourceReload {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Stopwatch reloadTimer = Stopwatch.createUnstarted();

   public ProfiledResourceReload(ResourceManager manager, List reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture initialStage) {
      super(prepareExecutor, applyExecutor, manager, reloaders, (synchronizer, resourceManager, reloader, prepare, apply) -> {
         AtomicLong atomicLong = new AtomicLong();
         AtomicLong atomicLong2 = new AtomicLong();
         ProfilerSystem lv = new ProfilerSystem(Util.nanoTimeSupplier, () -> {
            return 0;
         }, false);
         ProfilerSystem lv2 = new ProfilerSystem(Util.nanoTimeSupplier, () -> {
            return 0;
         }, false);
         CompletableFuture completableFuture = reloader.reload(synchronizer, resourceManager, lv, lv2, (preparation) -> {
            prepare.execute(() -> {
               long l = Util.getMeasuringTimeNano();
               preparation.run();
               atomicLong.addAndGet(Util.getMeasuringTimeNano() - l);
            });
         }, (application) -> {
            apply.execute(() -> {
               long l = Util.getMeasuringTimeNano();
               application.run();
               atomicLong2.addAndGet(Util.getMeasuringTimeNano() - l);
            });
         });
         return completableFuture.thenApplyAsync((dummy) -> {
            LOGGER.debug("Finished reloading " + reloader.getName());
            return new Summary(reloader.getName(), lv.getResult(), lv2.getResult(), atomicLong, atomicLong2);
         }, applyExecutor);
      }, initialStage);
      this.reloadTimer.start();
      this.applyStageFuture = this.applyStageFuture.thenApplyAsync(this::finish, applyExecutor);
   }

   private List finish(List summaries) {
      this.reloadTimer.stop();
      long l = 0L;
      LOGGER.info("Resource reload finished after {} ms", this.reloadTimer.elapsed(TimeUnit.MILLISECONDS));

      long n;
      for(Iterator var4 = summaries.iterator(); var4.hasNext(); l += n) {
         Summary lv = (Summary)var4.next();
         ProfileResult lv2 = lv.prepareProfile;
         ProfileResult lv3 = lv.applyProfile;
         long m = TimeUnit.NANOSECONDS.toMillis(lv.prepareTimeMs.get());
         n = TimeUnit.NANOSECONDS.toMillis(lv.applyTimeMs.get());
         long o = m + n;
         String string = lv.name;
         LOGGER.info("{} took approximately {} ms ({} ms preparing, {} ms applying)", new Object[]{string, o, m, n});
      }

      LOGGER.info("Total blocking time: {} ms", l);
      return summaries;
   }

   public static class Summary {
      final String name;
      final ProfileResult prepareProfile;
      final ProfileResult applyProfile;
      final AtomicLong prepareTimeMs;
      final AtomicLong applyTimeMs;

      Summary(String name, ProfileResult prepareProfile, ProfileResult applyProfile, AtomicLong prepareTimeMs, AtomicLong applyTimeMs) {
         this.name = name;
         this.prepareProfile = prepareProfile;
         this.applyProfile = applyProfile;
         this.prepareTimeMs = prepareTimeMs;
         this.applyTimeMs = applyTimeMs;
      }
   }
}
