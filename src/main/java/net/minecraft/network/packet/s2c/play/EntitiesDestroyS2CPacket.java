package net.minecraft.network.packet.s2c.play;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class EntitiesDestroyS2CPacket implements Packet {
   private final IntList entityIds;

   public EntitiesDestroyS2CPacket(IntList entityIds) {
      this.entityIds = new IntArrayList(entityIds);
   }

   public EntitiesDestroyS2CPacket(int... entityIds) {
      this.entityIds = new IntArrayList(entityIds);
   }

   public EntitiesDestroyS2CPacket(PacketByteBuf buf) {
      this.entityIds = buf.readIntList();
   }

   public void write(PacketByteBuf buf) {
      buf.writeIntList(this.entityIds);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEntitiesDestroy(this);
   }

   public IntList getEntityIds() {
      return this.entityIds;
   }
}
