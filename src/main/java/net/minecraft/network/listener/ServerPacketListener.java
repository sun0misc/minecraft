package net.minecraft.network.listener;

public interface ServerPacketListener extends PacketListener {
   default boolean shouldCrashOnException() {
      return false;
   }
}
