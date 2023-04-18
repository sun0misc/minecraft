package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class ChunkRenderDistanceCenterS2CPacket implements Packet {
   private final int chunkX;
   private final int chunkZ;

   public ChunkRenderDistanceCenterS2CPacket(int x, int z) {
      this.chunkX = x;
      this.chunkZ = z;
   }

   public ChunkRenderDistanceCenterS2CPacket(PacketByteBuf buf) {
      this.chunkX = buf.readVarInt();
      this.chunkZ = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.chunkX);
      buf.writeVarInt(this.chunkZ);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onChunkRenderDistanceCenter(this);
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }
}
