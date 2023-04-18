package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EntityStatusS2CPacket implements Packet {
   private final int id;
   private final byte status;

   public EntityStatusS2CPacket(Entity entity, byte status) {
      this.id = entity.getId();
      this.status = status;
   }

   public EntityStatusS2CPacket(PacketByteBuf buf) {
      this.id = buf.readInt();
      this.status = buf.readByte();
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.id);
      buf.writeByte(this.status);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntityStatus(this);
   }

   @Nullable
   public Entity getEntity(World world) {
      return world.getEntityById(this.id);
   }

   public byte getStatus() {
      return this.status;
   }
}
