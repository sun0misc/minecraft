package net.minecraft.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

public interface Packet {
   void write(PacketByteBuf buf);

   void apply(PacketListener listener);

   default boolean isWritingErrorSkippable() {
      return false;
   }
}
