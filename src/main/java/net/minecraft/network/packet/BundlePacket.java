package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;

public abstract class BundlePacket implements Packet {
   private final Iterable packets;

   protected BundlePacket(Iterable packets) {
      this.packets = packets;
   }

   public final Iterable getPackets() {
      return this.packets;
   }

   public final void write(PacketByteBuf buf) {
   }
}
