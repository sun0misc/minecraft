package net.minecraft.network;

import java.util.function.Supplier;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public interface PacketCallbacks {
   static PacketCallbacks always(final Runnable runnable) {
      return new PacketCallbacks() {
         public void onSuccess() {
            runnable.run();
         }

         @Nullable
         public Packet getFailurePacket() {
            runnable.run();
            return null;
         }
      };
   }

   static PacketCallbacks of(final Supplier failurePacket) {
      return new PacketCallbacks() {
         @Nullable
         public Packet getFailurePacket() {
            return (Packet)failurePacket.get();
         }
      };
   }

   default void onSuccess() {
   }

   @Nullable
   default Packet getFailurePacket() {
      return null;
   }
}
