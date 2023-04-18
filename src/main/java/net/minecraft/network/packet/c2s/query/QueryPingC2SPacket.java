package net.minecraft.network.packet.c2s.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryPingC2SPacket implements Packet {
   private final long startTime;

   public QueryPingC2SPacket(long startTime) {
      this.startTime = startTime;
   }

   public QueryPingC2SPacket(PacketByteBuf buf) {
      this.startTime = buf.readLong();
   }

   public void write(PacketByteBuf buf) {
      buf.writeLong(this.startTime);
   }

   public void apply(ServerQueryPacketListener arg) {
      arg.onPing(this);
   }

   public long getStartTime() {
      return this.startTime;
   }
}
