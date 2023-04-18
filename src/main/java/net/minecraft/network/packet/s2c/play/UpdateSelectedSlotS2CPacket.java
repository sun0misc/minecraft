package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class UpdateSelectedSlotS2CPacket implements Packet {
   private final int selectedSlot;

   public UpdateSelectedSlotS2CPacket(int slot) {
      this.selectedSlot = slot;
   }

   public UpdateSelectedSlotS2CPacket(PacketByteBuf buf) {
      this.selectedSlot = buf.readByte();
   }

   public void write(PacketByteBuf buf) {
      buf.writeByte(this.selectedSlot);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onUpdateSelectedSlot(this);
   }

   public int getSlot() {
      return this.selectedSlot;
   }
}
