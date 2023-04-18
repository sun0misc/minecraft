package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public record GameMessageS2CPacket(Text content, boolean overlay) implements Packet {
   public GameMessageS2CPacket(PacketByteBuf buf) {
      this(buf.readText(), buf.readBoolean());
   }

   public GameMessageS2CPacket(Text arg, boolean bl) {
      this.content = arg;
      this.overlay = bl;
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.content);
      buf.writeBoolean(this.overlay);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onGameMessage(this);
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }

   public Text content() {
      return this.content;
   }

   public boolean overlay() {
      return this.overlay;
   }
}
