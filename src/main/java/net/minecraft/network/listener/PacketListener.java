package net.minecraft.network.listener;

import net.minecraft.text.Text;

public interface PacketListener {
   void onDisconnected(Text reason);

   boolean isConnectionOpen();

   default boolean shouldCrashOnException() {
      return true;
   }
}
