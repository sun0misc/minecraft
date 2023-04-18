package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class UpdateSelectedSlotC2SPacket implements Packet {
   private final int selectedSlot;

   public UpdateSelectedSlotC2SPacket(int selectedSlot) {
      this.selectedSlot = selectedSlot;
   }

   public UpdateSelectedSlotC2SPacket(PacketByteBuf buf) {
      this.selectedSlot = buf.readShort();
   }

   public void write(PacketByteBuf buf) {
      buf.writeShort(this.selectedSlot);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onUpdateSelectedSlot(this);
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }
}
