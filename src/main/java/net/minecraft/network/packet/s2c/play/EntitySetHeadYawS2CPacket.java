package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;

public class EntitySetHeadYawS2CPacket implements Packet {
   private final int entity;
   private final byte headYaw;

   public EntitySetHeadYawS2CPacket(Entity entity, byte headYaw) {
      this.entity = entity.getId();
      this.headYaw = headYaw;
   }

   public EntitySetHeadYawS2CPacket(PacketByteBuf buf) {
      this.entity = buf.readVarInt();
      this.headYaw = buf.readByte();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.entity);
      buf.writeByte(this.headYaw);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntitySetHeadYaw(this);
   }

   public Entity getEntity(World world) {
      return world.getEntityById(this.entity);
   }

   public byte getHeadYaw() {
      return this.headYaw;
   }
}
