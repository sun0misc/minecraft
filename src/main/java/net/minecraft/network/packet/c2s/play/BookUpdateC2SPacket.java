package net.minecraft.network.packet.c2s.play;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class BookUpdateC2SPacket implements Packet {
   public static final int field_34038 = 4;
   private static final int MAX_TITLE_LENGTH = 128;
   private static final int MAX_PAGE_LENGTH = 8192;
   private static final int MAX_PAGES = 200;
   private final int slot;
   private final List pages;
   private final Optional title;

   public BookUpdateC2SPacket(int slot, List pages, Optional title) {
      this.slot = slot;
      this.pages = ImmutableList.copyOf(pages);
      this.title = title;
   }

   public BookUpdateC2SPacket(PacketByteBuf buf) {
      this.slot = buf.readVarInt();
      this.pages = (List)buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 200), (buf2) -> {
         return buf2.readString(8192);
      });
      this.title = buf.readOptional((buf2) -> {
         return buf2.readString(128);
      });
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.slot);
      buf.writeCollection(this.pages, (buf2, page) -> {
         buf2.writeString(page, 8192);
      });
      buf.writeOptional(this.title, (buf2, title) -> {
         buf2.writeString(title, 128);
      });
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onBookUpdate(this);
   }

   public List getPages() {
      return this.pages;
   }

   public Optional getTitle() {
      return this.title;
   }

   public int getSlot() {
      return this.slot;
   }
}
