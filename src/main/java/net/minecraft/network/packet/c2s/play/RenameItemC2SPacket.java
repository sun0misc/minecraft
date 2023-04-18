package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class RenameItemC2SPacket implements Packet {
   private final String name;

   public RenameItemC2SPacket(String name) {
      this.name = name;
   }

   public RenameItemC2SPacket(PacketByteBuf buf) {
      this.name = buf.readString();
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.name);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onRenameItem(this);
   }

   public String getName() {
      return this.name;
   }
}
