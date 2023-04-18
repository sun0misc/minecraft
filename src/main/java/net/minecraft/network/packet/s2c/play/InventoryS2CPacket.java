package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.collection.DefaultedList;

public class InventoryS2CPacket implements Packet {
   private final int syncId;
   private final int revision;
   private final List contents;
   private final ItemStack cursorStack;

   public InventoryS2CPacket(int syncId, int revision, DefaultedList contents, ItemStack cursorStack) {
      this.syncId = syncId;
      this.revision = revision;
      this.contents = DefaultedList.ofSize(contents.size(), ItemStack.EMPTY);

      for(int k = 0; k < contents.size(); ++k) {
         this.contents.set(k, ((ItemStack)contents.get(k)).copy());
      }

      this.cursorStack = cursorStack.copy();
   }

   public InventoryS2CPacket(PacketByteBuf buf) {
      this.syncId = buf.readUnsignedByte();
      this.revision = buf.readVarInt();
      this.contents = (List)buf.readCollection(DefaultedList::ofSize, PacketByteBuf::readItemStack);
      this.cursorStack = buf.readItemStack();
   }

   public void write(PacketByteBuf buf) {
      buf.writeByte(this.syncId);
      buf.writeVarInt(this.revision);
      buf.writeCollection(this.contents, PacketByteBuf::writeItemStack);
      buf.writeItemStack(this.cursorStack);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onInventory(this);
   }

   public int getSyncId() {
      return this.syncId;
   }

   public List getContents() {
      return this.contents;
   }

   public ItemStack getCursorStack() {
      return this.cursorStack;
   }

   public int getRevision() {
      return this.revision;
   }
}
