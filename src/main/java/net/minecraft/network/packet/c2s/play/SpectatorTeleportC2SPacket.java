package net.minecraft.network.packet.c2s.play;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class SpectatorTeleportC2SPacket implements Packet {
   private final UUID targetUuid;

   public SpectatorTeleportC2SPacket(UUID targetUuid) {
      this.targetUuid = targetUuid;
   }

   public SpectatorTeleportC2SPacket(PacketByteBuf buf) {
      this.targetUuid = buf.readUuid();
   }

   public void write(PacketByteBuf buf) {
      buf.writeUuid(this.targetUuid);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onSpectatorTeleport(this);
   }

   @Nullable
   public Entity getTarget(ServerWorld world) {
      return world.getEntity(this.targetUuid);
   }
}
