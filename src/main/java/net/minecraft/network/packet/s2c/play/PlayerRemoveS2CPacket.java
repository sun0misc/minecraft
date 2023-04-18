package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record PlayerRemoveS2CPacket(List profileIds) implements Packet {
   public PlayerRemoveS2CPacket(PacketByteBuf buf) {
      this(buf.readList(PacketByteBuf::readUuid));
   }

   public PlayerRemoveS2CPacket(List list) {
      this.profileIds = list;
   }

   public void write(PacketByteBuf buf) {
      buf.writeCollection(this.profileIds, PacketByteBuf::writeUuid);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onPlayerRemove(this);
   }

   public List profileIds() {
      return this.profileIds;
   }
}
