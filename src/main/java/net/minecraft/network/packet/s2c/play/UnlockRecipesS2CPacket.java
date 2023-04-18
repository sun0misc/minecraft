package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.recipe.book.RecipeBookOptions;

public class UnlockRecipesS2CPacket implements Packet {
   private final Action action;
   private final List recipeIdsToChange;
   private final List recipeIdsToInit;
   private final RecipeBookOptions options;

   public UnlockRecipesS2CPacket(Action action, Collection recipeIdsToChange, Collection recipeIdsToInit, RecipeBookOptions options) {
      this.action = action;
      this.recipeIdsToChange = ImmutableList.copyOf(recipeIdsToChange);
      this.recipeIdsToInit = ImmutableList.copyOf(recipeIdsToInit);
      this.options = options;
   }

   public UnlockRecipesS2CPacket(PacketByteBuf buf) {
      this.action = (Action)buf.readEnumConstant(Action.class);
      this.options = RecipeBookOptions.fromPacket(buf);
      this.recipeIdsToChange = buf.readList(PacketByteBuf::readIdentifier);
      if (this.action == UnlockRecipesS2CPacket.Action.INIT) {
         this.recipeIdsToInit = buf.readList(PacketByteBuf::readIdentifier);
      } else {
         this.recipeIdsToInit = ImmutableList.of();
      }

   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumConstant(this.action);
      this.options.toPacket(buf);
      buf.writeCollection(this.recipeIdsToChange, PacketByteBuf::writeIdentifier);
      if (this.action == UnlockRecipesS2CPacket.Action.INIT) {
         buf.writeCollection(this.recipeIdsToInit, PacketByteBuf::writeIdentifier);
      }

   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onUnlockRecipes(this);
   }

   public List getRecipeIdsToChange() {
      return this.recipeIdsToChange;
   }

   public List getRecipeIdsToInit() {
      return this.recipeIdsToInit;
   }

   public RecipeBookOptions getOptions() {
      return this.options;
   }

   public Action getAction() {
      return this.action;
   }

   public static enum Action {
      INIT,
      ADD,
      REMOVE;

      // $FF: synthetic method
      private static Action[] method_36953() {
         return new Action[]{INIT, ADD, REMOVE};
      }
   }
}
