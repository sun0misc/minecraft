package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record PlayerSessionC2SPacket(PublicPlayerSession.Serialized chatSession) implements Packet {
   public PlayerSessionC2SPacket(PacketByteBuf buf) {
      this(PublicPlayerSession.Serialized.fromBuf(buf));
   }

   public PlayerSessionC2SPacket(PublicPlayerSession.Serialized arg) {
      this.chatSession = arg;
   }

   public void write(PacketByteBuf buf) {
      PublicPlayerSession.Serialized.write(buf, this.chatSession);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onPlayerSession(this);
   }

   public PublicPlayerSession.Serialized chatSession() {
      return this.chatSession;
   }
}
