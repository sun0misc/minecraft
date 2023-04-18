package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class SubtitleS2CPacket implements Packet {
   private final Text subtitle;

   public SubtitleS2CPacket(Text subtitle) {
      this.subtitle = subtitle;
   }

   public SubtitleS2CPacket(PacketByteBuf buf) {
      this.subtitle = buf.readText();
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.subtitle);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onSubtitle(this);
   }

   public Text getSubtitle() {
      return this.subtitle;
   }
}
