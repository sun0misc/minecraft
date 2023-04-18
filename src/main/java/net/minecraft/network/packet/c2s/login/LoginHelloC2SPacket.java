package net.minecraft.network.packet.c2s.login;

import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.Packet;

public record LoginHelloC2SPacket(String name, Optional profileId) implements Packet {
   public LoginHelloC2SPacket(PacketByteBuf buf) {
      this(buf.readString(16), buf.readOptional(PacketByteBuf::readUuid));
   }

   public LoginHelloC2SPacket(String string, Optional optional) {
      this.name = string;
      this.profileId = optional;
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.name, 16);
      buf.writeOptional(this.profileId, PacketByteBuf::writeUuid);
   }

   public void apply(ServerLoginPacketListener arg) {
      arg.onHello(this);
   }

   public String name() {
      return this.name;
   }

   public Optional profileId() {
      return this.profileId;
   }
}
