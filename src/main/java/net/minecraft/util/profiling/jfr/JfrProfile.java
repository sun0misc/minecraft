package net.minecraft.util.profiling.jfr;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.jfr.sample.ChunkGenerationSample;
import net.minecraft.util.profiling.jfr.sample.FileIoSample;
import net.minecraft.util.profiling.jfr.sample.GcHeapSummarySample;
import net.minecraft.util.profiling.jfr.sample.LongRunningSampleStatistics;
import net.minecraft.util.profiling.jfr.sample.NetworkIoStatistics;
import net.minecraft.util.profiling.jfr.sample.ThreadAllocationStatisticsSample;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public record JfrProfile(Instant startTime, Instant endTime, Duration duration, @Nullable Duration worldGenDuration, List serverTickTimeSamples, List cpuLoadSamples, GcHeapSummarySample.Statistics gcHeapSummaryStatistics, ThreadAllocationStatisticsSample.AllocationMap threadAllocationMap, NetworkIoStatistics packetReadStatistics, NetworkIoStatistics packetSentStatistics, FileIoSample.Statistics fileWriteStatistics, FileIoSample.Statistics fileReadStatistics, List chunkGenerationSamples) {
   public JfrProfile(Instant instant, Instant instant2, Duration duration, @Nullable Duration duration2, List list, List list2, GcHeapSummarySample.Statistics arg, ThreadAllocationStatisticsSample.AllocationMap arg2, NetworkIoStatistics arg3, NetworkIoStatistics arg4, FileIoSample.Statistics arg5, FileIoSample.Statistics arg6, List list3) {
      this.startTime = instant;
      this.endTime = instant2;
      this.duration = duration;
      this.worldGenDuration = duration2;
      this.serverTickTimeSamples = list;
      this.cpuLoadSamples = list2;
      this.gcHeapSummaryStatistics = arg;
      this.threadAllocationMap = arg2;
      this.packetReadStatistics = arg3;
      this.packetSentStatistics = arg4;
      this.fileWriteStatistics = arg5;
      this.fileReadStatistics = arg6;
      this.chunkGenerationSamples = list3;
   }

   public List getChunkGenerationSampleStatistics() {
      Map map = (Map)this.chunkGenerationSamples.stream().collect(Collectors.groupingBy(ChunkGenerationSample::chunkStatus));
      return map.entrySet().stream().map((entry) -> {
         return Pair.of((ChunkStatus)entry.getKey(), LongRunningSampleStatistics.fromSamples((List)entry.getValue()));
      }).sorted(Comparator.comparing((pair) -> {
         return ((LongRunningSampleStatistics)pair.getSecond()).totalDuration();
      }).reversed()).toList();
   }

   public String toJson() {
      return (new JfrJsonReport()).toString(this);
   }

   public Instant startTime() {
      return this.startTime;
   }

   public Instant endTime() {
      return this.endTime;
   }

   public Duration duration() {
      return this.duration;
   }

   @Nullable
   public Duration worldGenDuration() {
      return this.worldGenDuration;
   }

   public List serverTickTimeSamples() {
      return this.serverTickTimeSamples;
   }

   public List cpuLoadSamples() {
      return this.cpuLoadSamples;
   }

   public GcHeapSummarySample.Statistics gcHeapSummaryStatistics() {
      return this.gcHeapSummaryStatistics;
   }

   public ThreadAllocationStatisticsSample.AllocationMap threadAllocationMap() {
      return this.threadAllocationMap;
   }

   public NetworkIoStatistics packetReadStatistics() {
      return this.packetReadStatistics;
   }

   public NetworkIoStatistics packetSentStatistics() {
      return this.packetSentStatistics;
   }

   public FileIoSample.Statistics fileWriteStatistics() {
      return this.fileWriteStatistics;
   }

   public FileIoSample.Statistics fileReadStatistics() {
      return this.fileReadStatistics;
   }

   public List chunkGenerationSamples() {
      return this.chunkGenerationSamples;
   }
}
