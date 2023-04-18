package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class ResourcePackStatusC2SPacket implements Packet {
   private final Status status;

   public ResourcePackStatusC2SPacket(Status status) {
      this.status = status;
   }

   public ResourcePackStatusC2SPacket(PacketByteBuf buf) {
      this.status = (Status)buf.readEnumConstant(Status.class);
   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumConstant(this.status);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onResourcePackStatus(this);
   }

   public Status getStatus() {
      return this.status;
   }

   public static enum Status {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;

      // $FF: synthetic method
      private static Status[] method_36961() {
         return new Status[]{SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED};
      }
   }
}
