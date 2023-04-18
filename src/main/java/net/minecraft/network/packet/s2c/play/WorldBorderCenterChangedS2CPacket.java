package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderCenterChangedS2CPacket implements Packet {
   private final double centerX;
   private final double centerZ;

   public WorldBorderCenterChangedS2CPacket(WorldBorder worldBorder) {
      this.centerX = worldBorder.getCenterX();
      this.centerZ = worldBorder.getCenterZ();
   }

   public WorldBorderCenterChangedS2CPacket(PacketByteBuf buf) {
      this.centerX = buf.readDouble();
      this.centerZ = buf.readDouble();
   }

   public void write(PacketByteBuf buf) {
      buf.writeDouble(this.centerX);
      buf.writeDouble(this.centerZ);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onWorldBorderCenterChanged(this);
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public double getCenterX() {
      return this.centerX;
   }
}
