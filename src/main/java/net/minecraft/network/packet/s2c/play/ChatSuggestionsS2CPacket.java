package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public record ChatSuggestionsS2CPacket(Action action, List entries) implements Packet {
   public ChatSuggestionsS2CPacket(PacketByteBuf buf) {
      this((Action)buf.readEnumConstant(Action.class), buf.readList(PacketByteBuf::readString));
   }

   public ChatSuggestionsS2CPacket(Action arg, List list) {
      this.action = arg;
      this.entries = list;
   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumConstant(this.action);
      buf.writeCollection(this.entries, PacketByteBuf::writeString);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onChatSuggestions(this);
   }

   public Action action() {
      return this.action;
   }

   public List entries() {
      return this.entries;
   }

   public static enum Action {
      ADD,
      REMOVE,
      SET;

      // $FF: synthetic method
      private static Action[] method_44784() {
         return new Action[]{ADD, REMOVE, SET};
      }
   }
}
