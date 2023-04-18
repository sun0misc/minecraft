package net.minecraft.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CookingRecipeSerializer implements RecipeSerializer {
   private final int cookingTime;
   private final RecipeFactory recipeFactory;

   public CookingRecipeSerializer(RecipeFactory recipeFactory, int cookingTime) {
      this.cookingTime = cookingTime;
      this.recipeFactory = recipeFactory;
   }

   public AbstractCookingRecipe read(Identifier arg, JsonObject jsonObject) {
      String string = JsonHelper.getString(jsonObject, "group", "");
      CookingRecipeCategory lv = (CookingRecipeCategory)CookingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", (String)null), CookingRecipeCategory.MISC);
      JsonElement jsonElement = JsonHelper.hasArray(jsonObject, "ingredient") ? JsonHelper.getArray(jsonObject, "ingredient") : JsonHelper.getObject(jsonObject, "ingredient");
      Ingredient lv2 = Ingredient.fromJson((JsonElement)jsonElement);
      String string2 = JsonHelper.getString(jsonObject, "result");
      Identifier lv3 = new Identifier(string2);
      ItemStack lv4 = new ItemStack((ItemConvertible)Registries.ITEM.getOrEmpty(lv3).orElseThrow(() -> {
         return new IllegalStateException("Item: " + string2 + " does not exist");
      }));
      float f = JsonHelper.getFloat(jsonObject, "experience", 0.0F);
      int i = JsonHelper.getInt(jsonObject, "cookingtime", this.cookingTime);
      return this.recipeFactory.create(arg, string, lv, lv2, lv4, f, i);
   }

   public AbstractCookingRecipe read(Identifier arg, PacketByteBuf arg2) {
      String string = arg2.readString();
      CookingRecipeCategory lv = (CookingRecipeCategory)arg2.readEnumConstant(CookingRecipeCategory.class);
      Ingredient lv2 = Ingredient.fromPacket(arg2);
      ItemStack lv3 = arg2.readItemStack();
      float f = arg2.readFloat();
      int i = arg2.readVarInt();
      return this.recipeFactory.create(arg, string, lv, lv2, lv3, f, i);
   }

   public void write(PacketByteBuf arg, AbstractCookingRecipe arg2) {
      arg.writeString(arg2.group);
      arg.writeEnumConstant(arg2.getCategory());
      arg2.input.write(arg);
      arg.writeItemStack(arg2.output);
      arg.writeFloat(arg2.experience);
      arg.writeVarInt(arg2.cookTime);
   }

   // $FF: synthetic method
   public Recipe read(Identifier id, PacketByteBuf buf) {
      return this.read(id, buf);
   }

   // $FF: synthetic method
   public Recipe read(Identifier id, JsonObject json) {
      return this.read(id, json);
   }

   interface RecipeFactory {
      AbstractCookingRecipe create(Identifier id, String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime);
   }
}
