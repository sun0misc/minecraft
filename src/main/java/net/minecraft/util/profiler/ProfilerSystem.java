/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.ProfileLocationInfo;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ProfileResultImpl;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.SampleType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ProfilerSystem
implements ReadableProfiler {
    private static final long TIMEOUT_NANOSECONDS = Duration.ofMillis(100L).toNanos();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<String> path = Lists.newArrayList();
    private final LongList timeList = new LongArrayList();
    private final Map<String, LocatedInfo> locationInfos = Maps.newHashMap();
    private final IntSupplier endTickGetter;
    private final LongSupplier timeGetter;
    private final long startTime;
    private final int startTick;
    private String fullPath = "";
    private boolean tickStarted;
    @Nullable
    private LocatedInfo currentInfo;
    private final boolean checkTimeout;
    private final Set<Pair<String, SampleType>> sampleTypes = new ObjectArraySet<Pair<String, SampleType>>();

    public ProfilerSystem(LongSupplier timeGetter, IntSupplier tickGetter, boolean checkTimeout) {
        this.startTime = timeGetter.getAsLong();
        this.timeGetter = timeGetter;
        this.startTick = tickGetter.getAsInt();
        this.endTickGetter = tickGetter;
        this.checkTimeout = checkTimeout;
    }

    @Override
    public void startTick() {
        if (this.tickStarted) {
            LOGGER.error("Profiler tick already started - missing endTick()?");
            return;
        }
        this.tickStarted = true;
        this.fullPath = "";
        this.path.clear();
        this.push("root");
    }

    @Override
    public void endTick() {
        if (!this.tickStarted) {
            LOGGER.error("Profiler tick already ended - missing startTick()?");
            return;
        }
        this.pop();
        this.tickStarted = false;
        if (!this.fullPath.isEmpty()) {
            LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", LogUtils.defer(() -> ProfileResult.getHumanReadableName(this.fullPath)));
        }
    }

    @Override
    public void push(String location) {
        if (!this.tickStarted) {
            LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", (Object)location);
            return;
        }
        if (!this.fullPath.isEmpty()) {
            this.fullPath = this.fullPath + "\u001e";
        }
        this.fullPath = this.fullPath + location;
        this.path.add(this.fullPath);
        this.timeList.add(Util.getMeasuringTimeNano());
        this.currentInfo = null;
    }

    @Override
    public void push(Supplier<String> locationGetter) {
        this.push(locationGetter.get());
    }

    @Override
    public void markSampleType(SampleType type) {
        this.sampleTypes.add(Pair.of(this.fullPath, type));
    }

    @Override
    public void pop() {
        if (!this.tickStarted) {
            LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
            return;
        }
        if (this.timeList.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        long l = Util.getMeasuringTimeNano();
        long m = this.timeList.removeLong(this.timeList.size() - 1);
        this.path.remove(this.path.size() - 1);
        long n = l - m;
        LocatedInfo lv = this.getCurrentInfo();
        lv.totalTime += n;
        ++lv.visits;
        lv.maxTime = Math.max(lv.maxTime, n);
        lv.minTime = Math.min(lv.minTime, n);
        if (this.checkTimeout && n > TIMEOUT_NANOSECONDS) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", LogUtils.defer(() -> ProfileResult.getHumanReadableName(this.fullPath)), LogUtils.defer(() -> (double)n / 1000000.0));
        }
        this.fullPath = this.path.isEmpty() ? "" : this.path.get(this.path.size() - 1);
        this.currentInfo = null;
    }

    @Override
    public void swap(String location) {
        this.pop();
        this.push(location);
    }

    @Override
    public void swap(Supplier<String> locationGetter) {
        this.pop();
        this.push(locationGetter);
    }

    private LocatedInfo getCurrentInfo() {
        if (this.currentInfo == null) {
            this.currentInfo = this.locationInfos.computeIfAbsent(this.fullPath, k -> new LocatedInfo());
        }
        return this.currentInfo;
    }

    @Override
    public void visit(String marker, int num) {
        this.getCurrentInfo().counts.addTo(marker, num);
    }

    @Override
    public void visit(Supplier<String> markerGetter, int num) {
        this.getCurrentInfo().counts.addTo(markerGetter.get(), num);
    }

    @Override
    public ProfileResult getResult() {
        return new ProfileResultImpl(this.locationInfos, this.startTime, this.startTick, this.timeGetter.getAsLong(), this.endTickGetter.getAsInt());
    }

    @Override
    @Nullable
    public LocatedInfo getInfo(String name) {
        return this.locationInfos.get(name);
    }

    @Override
    public Set<Pair<String, SampleType>> getSampleTargets() {
        return this.sampleTypes;
    }

    public static class LocatedInfo
    implements ProfileLocationInfo {
        long maxTime = Long.MIN_VALUE;
        long minTime = Long.MAX_VALUE;
        long totalTime;
        long visits;
        final Object2LongOpenHashMap<String> counts = new Object2LongOpenHashMap();

        @Override
        public long getTotalTime() {
            return this.totalTime;
        }

        @Override
        public long getMaxTime() {
            return this.maxTime;
        }

        @Override
        public long getVisitCount() {
            return this.visits;
        }

        @Override
        public Object2LongMap<String> getCounts() {
            return Object2LongMaps.unmodifiable(this.counts);
        }
    }
}

