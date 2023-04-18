package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class PlayPingS2CPacket implements Packet {
   private final int parameter;

   public PlayPingS2CPacket(int parameter) {
      this.parameter = parameter;
   }

   public PlayPingS2CPacket(PacketByteBuf buf) {
      this.parameter = buf.readInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.parameter);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onPing(this);
   }

   public int getParameter() {
      return this.parameter;
   }
}
