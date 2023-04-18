package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SetCameraEntityS2CPacket implements Packet {
   private final int entityId;

   public SetCameraEntityS2CPacket(Entity entity) {
      this.entityId = entity.getId();
   }

   public SetCameraEntityS2CPacket(PacketByteBuf buf) {
      this.entityId = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.entityId);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onSetCameraEntity(this);
   }

   @Nullable
   public Entity getEntity(World world) {
      return world.getEntityById(this.entityId);
   }
}
