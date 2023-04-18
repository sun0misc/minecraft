package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record PlayerActionResponseS2CPacket(int sequence) implements Packet {
   public PlayerActionResponseS2CPacket(PacketByteBuf buf) {
      this(buf.readVarInt());
   }

   public PlayerActionResponseS2CPacket(int i) {
      this.sequence = i;
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.sequence);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onPlayerActionResponse(this);
   }

   public int sequence() {
      return this.sequence;
   }
}
