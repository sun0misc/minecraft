package net.minecraft.network.packet.c2s.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.Packet;

public class QueryRequestC2SPacket implements Packet {
   public QueryRequestC2SPacket() {
   }

   public QueryRequestC2SPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public void apply(ServerQueryPacketListener arg) {
      arg.onRequest(this);
   }
}
