package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public record ProfilelessChatMessageS2CPacket(Text message, MessageType.Serialized chatType) implements Packet {
   public ProfilelessChatMessageS2CPacket(PacketByteBuf buf) {
      this(buf.readText(), new MessageType.Serialized(buf));
   }

   public ProfilelessChatMessageS2CPacket(Text arg, MessageType.Serialized arg2) {
      this.message = arg;
      this.chatType = arg2;
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.message);
      this.chatType.write(buf);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onProfilelessChatMessage(this);
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }

   public Text message() {
      return this.message;
   }

   public MessageType.Serialized chatType() {
      return this.chatType;
   }
}
