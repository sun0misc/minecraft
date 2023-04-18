package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import java.nio.file.Path;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface FlightProfiler {
   FlightProfiler INSTANCE = Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent() ? JfrProfiler.getInstance() : new NoopProfiler();

   boolean start(InstanceType instanceType);

   Path stop();

   boolean isProfiling();

   boolean isAvailable();

   void onTick(float tickTime);

   void onPacketReceived(int protocolId, int packetId, SocketAddress remoteAddress, int bytes);

   void onPacketSent(int protocolId, int packetId, SocketAddress remoteAddress, int bytes);

   @Nullable
   Finishable startWorldLoadProfiling();

   @Nullable
   Finishable startChunkGenerationProfiling(ChunkPos chunkPos, RegistryKey world, String targetStatus);

   public static class NoopProfiler implements FlightProfiler {
      private static final Logger LOGGER = LogUtils.getLogger();
      static final Finishable NOOP = () -> {
      };

      public boolean start(InstanceType instanceType) {
         LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
         return false;
      }

      public Path stop() {
         throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
      }

      public boolean isProfiling() {
         return false;
      }

      public boolean isAvailable() {
         return false;
      }

      public void onPacketReceived(int protocolId, int packetId, SocketAddress remoteAddress, int bytes) {
      }

      public void onPacketSent(int protocolId, int packetId, SocketAddress remoteAddress, int bytes) {
      }

      public void onTick(float tickTime) {
      }

      public Finishable startWorldLoadProfiling() {
         return NOOP;
      }

      @Nullable
      public Finishable startChunkGenerationProfiling(ChunkPos chunkPos, RegistryKey world, String targetStatus) {
         return null;
      }
   }
}
