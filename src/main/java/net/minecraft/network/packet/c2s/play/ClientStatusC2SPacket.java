package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class ClientStatusC2SPacket implements Packet {
   private final Mode mode;

   public ClientStatusC2SPacket(Mode mode) {
      this.mode = mode;
   }

   public ClientStatusC2SPacket(PacketByteBuf buf) {
      this.mode = (Mode)buf.readEnumConstant(Mode.class);
   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumConstant(this.mode);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onClientStatus(this);
   }

   public Mode getMode() {
      return this.mode;
   }

   public static enum Mode {
      PERFORM_RESPAWN,
      REQUEST_STATS;

      // $FF: synthetic method
      private static Mode[] method_36955() {
         return new Mode[]{PERFORM_RESPAWN, REQUEST_STATS};
      }
   }
}
