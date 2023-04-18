package net.minecraft.util.profiling.jfr.sample;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.chunk.ChunkStatus;

public record ChunkGenerationSample(Duration duration, ChunkPos chunkPos, ColumnPos centerPos, ChunkStatus chunkStatus, String worldKey) implements LongRunningSample {
   public ChunkGenerationSample(Duration duration, ChunkPos arg, ColumnPos arg2, ChunkStatus arg3, String string) {
      this.duration = duration;
      this.chunkPos = arg;
      this.centerPos = arg2;
      this.chunkStatus = arg3;
      this.worldKey = string;
   }

   public static ChunkGenerationSample fromEvent(RecordedEvent event) {
      return new ChunkGenerationSample(event.getDuration(), new ChunkPos(event.getInt("chunkPosX"), event.getInt("chunkPosX")), new ColumnPos(event.getInt("worldPosX"), event.getInt("worldPosZ")), ChunkStatus.byId(event.getString("status")), event.getString("level"));
   }

   public Duration duration() {
      return this.duration;
   }

   public ChunkPos chunkPos() {
      return this.chunkPos;
   }

   public ColumnPos centerPos() {
      return this.centerPos;
   }

   public ChunkStatus chunkStatus() {
      return this.chunkStatus;
   }

   public String worldKey() {
      return this.worldKey;
   }
}
