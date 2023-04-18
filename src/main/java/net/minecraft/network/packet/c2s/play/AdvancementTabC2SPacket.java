package net.minecraft.network.packet.c2s.play;

import net.minecraft.advancement.Advancement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabC2SPacket implements Packet {
   private final Action action;
   @Nullable
   private final Identifier tabToOpen;

   public AdvancementTabC2SPacket(Action action, @Nullable Identifier tab) {
      this.action = action;
      this.tabToOpen = tab;
   }

   public static AdvancementTabC2SPacket open(Advancement advancement) {
      return new AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action.OPENED_TAB, advancement.getId());
   }

   public static AdvancementTabC2SPacket close() {
      return new AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action.CLOSED_SCREEN, (Identifier)null);
   }

   public AdvancementTabC2SPacket(PacketByteBuf buf) {
      this.action = (Action)buf.readEnumConstant(Action.class);
      if (this.action == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         this.tabToOpen = buf.readIdentifier();
      } else {
         this.tabToOpen = null;
      }

   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumConstant(this.action);
      if (this.action == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         buf.writeIdentifier(this.tabToOpen);
      }

   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onAdvancementTab(this);
   }

   public Action getAction() {
      return this.action;
   }

   @Nullable
   public Identifier getTabToOpen() {
      return this.tabToOpen;
   }

   public static enum Action {
      OPENED_TAB,
      CLOSED_SCREEN;

      // $FF: synthetic method
      private static Action[] method_36962() {
         return new Action[]{OPENED_TAB, CLOSED_SCREEN};
      }
   }
}
