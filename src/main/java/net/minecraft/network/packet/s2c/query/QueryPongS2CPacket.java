package net.minecraft.network.packet.s2c.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryPongS2CPacket implements Packet {
   private final long startTime;

   public QueryPongS2CPacket(long startTime) {
      this.startTime = startTime;
   }

   public QueryPongS2CPacket(PacketByteBuf buf) {
      this.startTime = buf.readLong();
   }

   public void write(PacketByteBuf buf) {
      buf.writeLong(this.startTime);
   }

   public void apply(ClientQueryPacketListener arg) {
      arg.onPong(this);
   }

   public long getStartTime() {
      return this.startTime;
   }
}
