package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class CloseHandledScreenC2SPacket implements Packet {
   private final int syncId;

   public CloseHandledScreenC2SPacket(int syncId) {
      this.syncId = syncId;
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onCloseHandledScreen(this);
   }

   public CloseHandledScreenC2SPacket(PacketByteBuf buf) {
      this.syncId = buf.readByte();
   }

   public void write(PacketByteBuf buf) {
      buf.writeByte(this.syncId);
   }

   public int getSyncId() {
      return this.syncId;
   }
}
