package net.minecraft.util.profiling.jfr.sample;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;

public final class NetworkIoStatistics {
   private final PacketStatistics combinedStatistics;
   private final List topContributors;
   private final Duration duration;

   public NetworkIoStatistics(Duration duration, List packetsToStatistics) {
      this.duration = duration;
      this.combinedStatistics = (PacketStatistics)packetsToStatistics.stream().map(Pair::getSecond).reduce(PacketStatistics::add).orElseGet(() -> {
         return new PacketStatistics(0L, 0L);
      });
      this.topContributors = packetsToStatistics.stream().sorted(Comparator.comparing(Pair::getSecond, NetworkIoStatistics.PacketStatistics.COMPARATOR)).limit(10L).toList();
   }

   public double getCountPerSecond() {
      return (double)this.combinedStatistics.totalCount / (double)this.duration.getSeconds();
   }

   public double getBytesPerSecond() {
      return (double)this.combinedStatistics.totalSize / (double)this.duration.getSeconds();
   }

   public long getTotalCount() {
      return this.combinedStatistics.totalCount;
   }

   public long getTotalSize() {
      return this.combinedStatistics.totalSize;
   }

   public List getTopContributors() {
      return this.topContributors;
   }

   public static record PacketStatistics(long totalCount, long totalSize) {
      final long totalCount;
      final long totalSize;
      static final Comparator COMPARATOR = Comparator.comparing(PacketStatistics::totalSize).thenComparing(PacketStatistics::totalCount).reversed();

      public PacketStatistics(long l, long m) {
         this.totalCount = l;
         this.totalSize = m;
      }

      PacketStatistics add(PacketStatistics statistics) {
         return new PacketStatistics(this.totalCount + statistics.totalCount, this.totalSize + statistics.totalSize);
      }

      public long totalCount() {
         return this.totalCount;
      }

      public long totalSize() {
         return this.totalSize;
      }
   }

   public static record Packet(NetworkSide side, int protocolId, int packetId) {
      private static final Map PACKET_TO_NAME;

      public Packet(NetworkSide arg, int i, int j) {
         this.side = arg;
         this.protocolId = i;
         this.packetId = j;
      }

      public String getName() {
         return (String)PACKET_TO_NAME.getOrDefault(this, "unknown");
      }

      public static Packet fromEvent(RecordedEvent event) {
         return new Packet(event.getEventType().getName().equals("minecraft.PacketSent") ? NetworkSide.CLIENTBOUND : NetworkSide.SERVERBOUND, event.getInt("protocolId"), event.getInt("packetId"));
      }

      public NetworkSide side() {
         return this.side;
      }

      public int protocolId() {
         return this.protocolId;
      }

      public int packetId() {
         return this.packetId;
      }

      static {
         ImmutableMap.Builder builder = ImmutableMap.builder();
         NetworkState[] var1 = NetworkState.values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            NetworkState lv = var1[var3];
            NetworkSide[] var5 = NetworkSide.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               NetworkSide lv2 = var5[var7];
               Int2ObjectMap int2ObjectMap = lv.getPacketIdToPacketMap(lv2);
               int2ObjectMap.forEach((packetId, clazz) -> {
                  builder.put(new Packet(lv2, lv.getId(), packetId), clazz.getSimpleName());
               });
            }
         }

         PACKET_TO_NAME = builder.build();
      }
   }
}
