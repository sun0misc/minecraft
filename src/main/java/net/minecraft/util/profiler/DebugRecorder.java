package net.minecraft.util.profiler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import org.jetbrains.annotations.Nullable;

public class DebugRecorder implements Recorder {
   public static final int MAX_DURATION_IN_SECONDS = 10;
   @Nullable
   private static Consumer globalDumpConsumer = null;
   private final Map deviations = new Object2ObjectOpenHashMap();
   private final TickTimeTracker timeTracker;
   private final Executor dumpExecutor;
   private final RecordDumper dumper;
   private final Consumer resultConsumer;
   private final Consumer dumpConsumer;
   private final SamplerSource samplerSource;
   private final LongSupplier timeGetter;
   private final long endTime;
   private int ticks;
   private ReadableProfiler profiler;
   private volatile boolean stopping;
   private Set samplers = ImmutableSet.of();

   private DebugRecorder(SamplerSource samplerSource, LongSupplier timeGetter, Executor dumpExecutor, RecordDumper dumper, Consumer resultConsumer, Consumer dumpConsumer) {
      this.samplerSource = samplerSource;
      this.timeGetter = timeGetter;
      this.timeTracker = new TickTimeTracker(timeGetter, () -> {
         return this.ticks;
      });
      this.dumpExecutor = dumpExecutor;
      this.dumper = dumper;
      this.resultConsumer = resultConsumer;
      this.dumpConsumer = globalDumpConsumer == null ? dumpConsumer : dumpConsumer.andThen(globalDumpConsumer);
      this.endTime = timeGetter.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
      this.profiler = new ProfilerSystem(this.timeGetter, () -> {
         return this.ticks;
      }, false);
      this.timeTracker.enable();
   }

   public static DebugRecorder of(SamplerSource source, LongSupplier timeGetter, Executor dumpExecutor, RecordDumper dumper, Consumer resultConsumer, Consumer dumpConsumer) {
      return new DebugRecorder(source, timeGetter, dumpExecutor, dumper, resultConsumer, dumpConsumer);
   }

   public synchronized void stop() {
      if (this.isActive()) {
         this.stopping = true;
      }
   }

   public synchronized void forceStop() {
      if (this.isActive()) {
         this.profiler = DummyProfiler.INSTANCE;
         this.resultConsumer.accept(EmptyProfileResult.INSTANCE);
         this.forceStop(this.samplers);
      }
   }

   public void startTick() {
      this.checkState();
      this.samplers = this.samplerSource.getSamplers(() -> {
         return this.profiler;
      });
      Iterator var1 = this.samplers.iterator();

      while(var1.hasNext()) {
         Sampler lv = (Sampler)var1.next();
         lv.start();
      }

      ++this.ticks;
   }

   public void endTick() {
      this.checkState();
      if (this.ticks != 0) {
         Iterator var1 = this.samplers.iterator();

         while(var1.hasNext()) {
            Sampler lv = (Sampler)var1.next();
            lv.sample(this.ticks);
            if (lv.hasDeviated()) {
               Deviation lv2 = new Deviation(Instant.now(), this.ticks, this.profiler.getResult());
               ((List)this.deviations.computeIfAbsent(lv, (s) -> {
                  return Lists.newArrayList();
               })).add(lv2);
            }
         }

         if (!this.stopping && this.timeGetter.getAsLong() <= this.endTime) {
            this.profiler = new ProfilerSystem(this.timeGetter, () -> {
               return this.ticks;
            }, false);
         } else {
            this.stopping = false;
            ProfileResult lv3 = this.timeTracker.getResult();
            this.profiler = DummyProfiler.INSTANCE;
            this.resultConsumer.accept(lv3);
            this.dump(lv3);
         }
      }
   }

   public boolean isActive() {
      return this.timeTracker.isActive();
   }

   public Profiler getProfiler() {
      return Profiler.union(this.timeTracker.getProfiler(), this.profiler);
   }

   private void checkState() {
      if (!this.isActive()) {
         throw new IllegalStateException("Not started!");
      }
   }

   private void dump(ProfileResult result) {
      HashSet hashSet = new HashSet(this.samplers);
      this.dumpExecutor.execute(() -> {
         Path path = this.dumper.createDump(hashSet, this.deviations, result);
         this.forceStop(hashSet);
         this.dumpConsumer.accept(path);
      });
   }

   private void forceStop(Collection samplers) {
      Iterator var2 = samplers.iterator();

      while(var2.hasNext()) {
         Sampler lv = (Sampler)var2.next();
         lv.stop();
      }

      this.deviations.clear();
      this.timeTracker.disable();
   }

   public static void setGlobalDumpConsumer(Consumer consumer) {
      globalDumpConsumer = consumer;
   }
}
