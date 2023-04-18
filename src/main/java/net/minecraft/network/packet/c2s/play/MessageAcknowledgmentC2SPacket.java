package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record MessageAcknowledgmentC2SPacket(int offset) implements Packet {
   public MessageAcknowledgmentC2SPacket(PacketByteBuf buf) {
      this(buf.readVarInt());
   }

   public MessageAcknowledgmentC2SPacket(int i) {
      this.offset = i;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.offset);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onMessageAcknowledgment(this);
   }

   public int offset() {
      return this.offset;
   }
}
