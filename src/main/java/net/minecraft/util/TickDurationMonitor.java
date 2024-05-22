/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TickDurationMonitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LongSupplier timeGetter;
    private final long overtime;
    private int tickCount;
    private final File tickResultsDirectory;
    private ReadableProfiler profiler = DummyProfiler.INSTANCE;

    public TickDurationMonitor(LongSupplier timeGetter, String filename, long overtime) {
        this.timeGetter = timeGetter;
        this.tickResultsDirectory = new File("debug", filename);
        this.overtime = overtime;
    }

    public Profiler nextProfiler() {
        this.profiler = new ProfilerSystem(this.timeGetter, () -> this.tickCount, false);
        ++this.tickCount;
        return this.profiler;
    }

    public void endTick() {
        if (this.profiler == DummyProfiler.INSTANCE) {
            return;
        }
        ProfileResult lv = this.profiler.getResult();
        this.profiler = DummyProfiler.INSTANCE;
        if (lv.getTimeSpan() >= this.overtime) {
            File file = new File(this.tickResultsDirectory, "tick-results-" + Util.getFormattedCurrentTime() + ".txt");
            lv.save(file.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", (Object)file.getAbsolutePath());
        }
    }

    @Nullable
    public static TickDurationMonitor create(String name) {
        return null;
    }

    public static Profiler tickProfiler(Profiler profiler, @Nullable TickDurationMonitor monitor) {
        if (monitor != null) {
            return Profiler.union(monitor.nextProfiler(), profiler);
        }
        return profiler;
    }
}

