package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderSizeChangedS2CPacket implements Packet {
   private final double sizeLerpTarget;

   public WorldBorderSizeChangedS2CPacket(WorldBorder worldBorder) {
      this.sizeLerpTarget = worldBorder.getSizeLerpTarget();
   }

   public WorldBorderSizeChangedS2CPacket(PacketByteBuf buf) {
      this.sizeLerpTarget = buf.readDouble();
   }

   public void write(PacketByteBuf buf) {
      buf.writeDouble(this.sizeLerpTarget);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onWorldBorderSizeChanged(this);
   }

   public double getSizeLerpTarget() {
      return this.sizeLerpTarget;
   }
}
