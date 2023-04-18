package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class PickFromInventoryC2SPacket implements Packet {
   private final int slot;

   public PickFromInventoryC2SPacket(int slot) {
      this.slot = slot;
   }

   public PickFromInventoryC2SPacket(PacketByteBuf buf) {
      this.slot = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.slot);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onPickFromInventory(this);
   }

   public int getSlot() {
      return this.slot;
   }
}
