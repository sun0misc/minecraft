package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class DisconnectS2CPacket implements Packet {
   private final Text reason;

   public DisconnectS2CPacket(Text reason) {
      this.reason = reason;
   }

   public DisconnectS2CPacket(PacketByteBuf buf) {
      this.reason = buf.readText();
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.reason);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onDisconnect(this);
   }

   public Text getReason() {
      return this.reason;
   }
}
