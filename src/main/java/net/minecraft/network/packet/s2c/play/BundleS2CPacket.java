package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.BundlePacket;

public class BundleS2CPacket extends BundlePacket {
   public BundleS2CPacket(Iterable iterable) {
      super(iterable);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onBundle(this);
   }
}
