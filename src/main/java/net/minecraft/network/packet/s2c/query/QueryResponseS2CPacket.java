package net.minecraft.network.packet.s2c.query;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.ServerMetadata;

public record QueryResponseS2CPacket(ServerMetadata metadata) implements Packet {
   public QueryResponseS2CPacket(PacketByteBuf buf) {
      this((ServerMetadata)buf.decodeAsJson(ServerMetadata.CODEC));
   }

   public QueryResponseS2CPacket(ServerMetadata metadata) {
      this.metadata = metadata;
   }

   public void write(PacketByteBuf buf) {
      buf.encodeAsJson(ServerMetadata.CODEC, this.metadata);
   }

   public void apply(ClientQueryPacketListener arg) {
      arg.onResponse(this);
   }

   public ServerMetadata metadata() {
      return this.metadata;
   }
}
