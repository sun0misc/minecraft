/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.profiler.SamplerFactory;
import net.minecraft.util.profiler.SamplerSource;
import net.minecraft.util.thread.ExecutorSampling;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerSamplerSource
implements SamplerSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<Sampler> samplers = new ObjectOpenHashSet<Sampler>();
    private final SamplerFactory factory = new SamplerFactory();

    public ServerSamplerSource(LongSupplier nanoTimeSupplier, boolean includeSystem) {
        this.samplers.add(ServerSamplerSource.createTickTimeTracker(nanoTimeSupplier));
        if (includeSystem) {
            this.samplers.addAll(ServerSamplerSource.createSystemSamplers());
        }
    }

    public static Set<Sampler> createSystemSamplers() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        try {
            CpuUsageFetcher lv = new CpuUsageFetcher();
            IntStream.range(0, lv.logicalProcessorCount).mapToObj(index -> Sampler.create("cpu#" + index, SampleType.CPU, () -> lv.getCpuUsage(index))).forEach(builder::add);
        } catch (Throwable throwable) {
            LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", throwable);
        }
        builder.add(Sampler.create("heap MiB", SampleType.JVM, () -> SystemDetails.toMebibytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
        builder.addAll(ExecutorSampling.INSTANCE.createSamplers());
        return builder.build();
    }

    @Override
    public Set<Sampler> getSamplers(Supplier<ReadableProfiler> profilerSupplier) {
        this.samplers.addAll(this.factory.createSamplers(profilerSupplier));
        return this.samplers;
    }

    public static Sampler createTickTimeTracker(final LongSupplier nanoTimeSupplier) {
        Stopwatch stopwatch = Stopwatch.createUnstarted(new Ticker(){

            @Override
            public long read() {
                return nanoTimeSupplier.getAsLong();
            }
        });
        ToDoubleFunction<Stopwatch> toDoubleFunction = watch -> {
            if (watch.isRunning()) {
                watch.stop();
            }
            long l = watch.elapsed(TimeUnit.NANOSECONDS);
            watch.reset();
            return l;
        };
        Sampler.RatioDeviationChecker lv = new Sampler.RatioDeviationChecker(2.0f);
        return Sampler.builder("ticktime", SampleType.TICK_LOOP, toDoubleFunction, stopwatch).startAction(Stopwatch::start).deviationChecker(lv).build();
    }

    static class CpuUsageFetcher {
        private final SystemInfo systemInfo = new SystemInfo();
        private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
        public final int logicalProcessorCount = this.processor.getLogicalProcessorCount();
        private long[][] loadTicks = this.processor.getProcessorCpuLoadTicks();
        private double[] loadBetweenTicks = this.processor.getProcessorCpuLoadBetweenTicks(this.loadTicks);
        private long lastCheckTime;

        CpuUsageFetcher() {
        }

        public double getCpuUsage(int index) {
            long l = System.currentTimeMillis();
            if (this.lastCheckTime == 0L || this.lastCheckTime + 501L < l) {
                this.loadBetweenTicks = this.processor.getProcessorCpuLoadBetweenTicks(this.loadTicks);
                this.loadTicks = this.processor.getProcessorCpuLoadTicks();
                this.lastCheckTime = l;
            }
            return this.loadBetweenTicks[index] * 100.0;
        }
    }
}

