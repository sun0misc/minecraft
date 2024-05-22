/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiling.jfr.sample;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStatisticsSample(Instant time, String threadName, long allocated) {
    private static final String UNKNOWN = "unknown";

    public static ThreadAllocationStatisticsSample fromEvent(RecordedEvent event) {
        RecordedThread recordedThread = event.getThread("thread");
        String string = recordedThread == null ? UNKNOWN : MoreObjects.firstNonNull(recordedThread.getJavaName(), UNKNOWN);
        return new ThreadAllocationStatisticsSample(event.getStartTime(), string, event.getLong("allocated"));
    }

    public static AllocationMap toAllocationMap(List<ThreadAllocationStatisticsSample> samples) {
        TreeMap<String, Double> map = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStatisticsSample>> map2 = samples.stream().collect(Collectors.groupingBy(sample -> sample.threadName));
        map2.forEach((threadName, groupedSamples) -> {
            if (groupedSamples.size() < 2) {
                return;
            }
            ThreadAllocationStatisticsSample lv = (ThreadAllocationStatisticsSample)groupedSamples.get(0);
            ThreadAllocationStatisticsSample lv2 = (ThreadAllocationStatisticsSample)groupedSamples.get(groupedSamples.size() - 1);
            long l = Duration.between(lv.time, lv2.time).getSeconds();
            long m = lv2.allocated - lv.allocated;
            map.put((String)threadName, (double)m / (double)l);
        });
        return new AllocationMap(map);
    }

    public record AllocationMap(Map<String, Double> allocations) {
    }
}

