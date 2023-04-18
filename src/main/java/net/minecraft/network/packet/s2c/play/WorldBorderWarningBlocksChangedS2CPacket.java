package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderWarningBlocksChangedS2CPacket implements Packet {
   private final int warningBlocks;

   public WorldBorderWarningBlocksChangedS2CPacket(WorldBorder worldBorder) {
      this.warningBlocks = worldBorder.getWarningBlocks();
   }

   public WorldBorderWarningBlocksChangedS2CPacket(PacketByteBuf buf) {
      this.warningBlocks = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.warningBlocks);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onWorldBorderWarningBlocksChanged(this);
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }
}
