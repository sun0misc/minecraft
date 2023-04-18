package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SpecialRecipeSerializer implements RecipeSerializer {
   private final Factory factory;

   public SpecialRecipeSerializer(Factory factory) {
      this.factory = factory;
   }

   public CraftingRecipe read(Identifier arg, JsonObject jsonObject) {
      CraftingRecipeCategory lv = (CraftingRecipeCategory)CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", (String)null), CraftingRecipeCategory.MISC);
      return this.factory.create(arg, lv);
   }

   public CraftingRecipe read(Identifier arg, PacketByteBuf arg2) {
      CraftingRecipeCategory lv = (CraftingRecipeCategory)arg2.readEnumConstant(CraftingRecipeCategory.class);
      return this.factory.create(arg, lv);
   }

   public void write(PacketByteBuf arg, CraftingRecipe arg2) {
      arg.writeEnumConstant(arg2.getCategory());
   }

   // $FF: synthetic method
   public Recipe read(Identifier id, PacketByteBuf buf) {
      return this.read(id, buf);
   }

   // $FF: synthetic method
   public Recipe read(Identifier id, JsonObject json) {
      return this.read(id, json);
   }

   @FunctionalInterface
   public interface Factory {
      CraftingRecipe create(Identifier id, CraftingRecipeCategory category);
   }
}
