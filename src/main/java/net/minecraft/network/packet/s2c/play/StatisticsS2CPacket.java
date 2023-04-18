package net.minecraft.network.packet.s2c.play;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;

public class StatisticsS2CPacket implements Packet {
   private final Object2IntMap stats;

   public StatisticsS2CPacket(Object2IntMap stats) {
      this.stats = stats;
   }

   public StatisticsS2CPacket(PacketByteBuf buf) {
      this.stats = (Object2IntMap)buf.readMap(Object2IntOpenHashMap::new, (bufx) -> {
         StatType lv = (StatType)bufx.readRegistryValue(Registries.STAT_TYPE);
         return getOrCreateStat(buf, lv);
      }, PacketByteBuf::readVarInt);
   }

   private static Stat getOrCreateStat(PacketByteBuf buf, StatType statType) {
      return statType.getOrCreateStat(buf.readRegistryValue(statType.getRegistry()));
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onStatistics(this);
   }

   public void write(PacketByteBuf buf) {
      buf.writeMap(this.stats, StatisticsS2CPacket::write, PacketByteBuf::writeVarInt);
   }

   private static void write(PacketByteBuf buf, Stat stat) {
      buf.writeRegistryValue(Registries.STAT_TYPE, stat.getType());
      buf.writeRegistryValue(stat.getType().getRegistry(), stat.getValue());
   }

   public Map getStatMap() {
      return this.stats;
   }
}
