package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SynchronizeRecipesS2CPacket implements Packet {
   private final List recipes;

   public SynchronizeRecipesS2CPacket(Collection recipes) {
      this.recipes = Lists.newArrayList(recipes);
   }

   public SynchronizeRecipesS2CPacket(PacketByteBuf buf) {
      this.recipes = buf.readList(SynchronizeRecipesS2CPacket::readRecipe);
   }

   public void write(PacketByteBuf buf) {
      buf.writeCollection(this.recipes, SynchronizeRecipesS2CPacket::writeRecipe);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onSynchronizeRecipes(this);
   }

   public List getRecipes() {
      return this.recipes;
   }

   public static Recipe readRecipe(PacketByteBuf buf) {
      Identifier lv = buf.readIdentifier();
      Identifier lv2 = buf.readIdentifier();
      return ((RecipeSerializer)Registries.RECIPE_SERIALIZER.getOrEmpty(lv).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown recipe serializer " + lv);
      })).read(lv2, buf);
   }

   public static void writeRecipe(PacketByteBuf buf, Recipe recipe) {
      buf.writeIdentifier(Registries.RECIPE_SERIALIZER.getId(recipe.getSerializer()));
      buf.writeIdentifier(recipe.getId());
      recipe.getSerializer().write(buf, recipe);
   }
}
