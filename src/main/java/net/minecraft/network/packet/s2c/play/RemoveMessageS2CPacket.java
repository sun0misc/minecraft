package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;

public record RemoveMessageS2CPacket(MessageSignatureData.Indexed messageSignature) implements Packet {
   public RemoveMessageS2CPacket(PacketByteBuf buf) {
      this(MessageSignatureData.Indexed.fromBuf(buf));
   }

   public RemoveMessageS2CPacket(MessageSignatureData.Indexed arg) {
      this.messageSignature = arg;
   }

   public void write(PacketByteBuf buf) {
      MessageSignatureData.Indexed.write(buf, this.messageSignature);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onRemoveMessage(this);
   }

   public MessageSignatureData.Indexed messageSignature() {
      return this.messageSignature;
   }
}
