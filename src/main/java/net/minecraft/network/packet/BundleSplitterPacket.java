package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

public class BundleSplitterPacket implements Packet {
   public final void write(PacketByteBuf buf) {
   }

   public final void apply(PacketListener listener) {
      throw new AssertionError("This packet should be handled by pipeline");
   }
}
