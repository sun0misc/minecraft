/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.profiler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.util.profiler.Deviation;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.EmptyProfileResult;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.RecordDumper;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.profiler.SamplerSource;
import net.minecraft.util.profiler.TickTimeTracker;
import org.jetbrains.annotations.Nullable;

public class DebugRecorder
implements Recorder {
    public static final int MAX_DURATION_IN_SECONDS = 10;
    @Nullable
    private static Consumer<Path> globalDumpConsumer = null;
    private final Map<Sampler, List<Deviation>> deviations = new Object2ObjectOpenHashMap<Sampler, List<Deviation>>();
    private final TickTimeTracker timeTracker;
    private final Executor dumpExecutor;
    private final RecordDumper dumper;
    private final Consumer<ProfileResult> resultConsumer;
    private final Consumer<Path> dumpConsumer;
    private final SamplerSource samplerSource;
    private final LongSupplier timeGetter;
    private final long endTime;
    private int ticks;
    private ReadableProfiler profiler;
    private volatile boolean stopping;
    private Set<Sampler> samplers = ImmutableSet.of();

    private DebugRecorder(SamplerSource samplerSource, LongSupplier timeGetter, Executor dumpExecutor, RecordDumper dumper, Consumer<ProfileResult> resultConsumer, Consumer<Path> dumpConsumer) {
        this.samplerSource = samplerSource;
        this.timeGetter = timeGetter;
        this.timeTracker = new TickTimeTracker(timeGetter, () -> this.ticks);
        this.dumpExecutor = dumpExecutor;
        this.dumper = dumper;
        this.resultConsumer = resultConsumer;
        this.dumpConsumer = globalDumpConsumer == null ? dumpConsumer : dumpConsumer.andThen(globalDumpConsumer);
        this.endTime = timeGetter.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
        this.profiler = new ProfilerSystem(this.timeGetter, () -> this.ticks, false);
        this.timeTracker.enable();
    }

    public static DebugRecorder of(SamplerSource source, LongSupplier timeGetter, Executor dumpExecutor, RecordDumper dumper, Consumer<ProfileResult> resultConsumer, Consumer<Path> dumpConsumer) {
        return new DebugRecorder(source, timeGetter, dumpExecutor, dumper, resultConsumer, dumpConsumer);
    }

    @Override
    public synchronized void stop() {
        if (!this.isActive()) {
            return;
        }
        this.stopping = true;
    }

    @Override
    public synchronized void forceStop() {
        if (!this.isActive()) {
            return;
        }
        this.profiler = DummyProfiler.INSTANCE;
        this.resultConsumer.accept(EmptyProfileResult.INSTANCE);
        this.forceStop(this.samplers);
    }

    @Override
    public void startTick() {
        this.checkState();
        this.samplers = this.samplerSource.getSamplers(() -> this.profiler);
        for (Sampler lv : this.samplers) {
            lv.start();
        }
        ++this.ticks;
    }

    @Override
    public void endTick() {
        this.checkState();
        if (this.ticks == 0) {
            return;
        }
        for (Sampler lv : this.samplers) {
            lv.sample(this.ticks);
            if (!lv.hasDeviated()) continue;
            Deviation lv2 = new Deviation(Instant.now(), this.ticks, this.profiler.getResult());
            this.deviations.computeIfAbsent(lv, s -> Lists.newArrayList()).add(lv2);
        }
        if (this.stopping || this.timeGetter.getAsLong() > this.endTime) {
            this.stopping = false;
            ProfileResult lv3 = this.timeTracker.getResult();
            this.profiler = DummyProfiler.INSTANCE;
            this.resultConsumer.accept(lv3);
            this.dump(lv3);
            return;
        }
        this.profiler = new ProfilerSystem(this.timeGetter, () -> this.ticks, false);
    }

    @Override
    public boolean isActive() {
        return this.timeTracker.isActive();
    }

    @Override
    public Profiler getProfiler() {
        return Profiler.union(this.timeTracker.getProfiler(), this.profiler);
    }

    private void checkState() {
        if (!this.isActive()) {
            throw new IllegalStateException("Not started!");
        }
    }

    private void dump(ProfileResult result) {
        HashSet<Sampler> hashSet = new HashSet<Sampler>(this.samplers);
        this.dumpExecutor.execute(() -> {
            Path path = this.dumper.createDump(hashSet, this.deviations, result);
            this.forceStop(hashSet);
            this.dumpConsumer.accept(path);
        });
    }

    private void forceStop(Collection<Sampler> samplers) {
        for (Sampler lv : samplers) {
            lv.stop();
        }
        this.deviations.clear();
        this.timeTracker.disable();
    }

    public static void setGlobalDumpConsumer(Consumer<Path> consumer) {
        globalDumpConsumer = consumer;
    }
}

