package net.minecraft.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapelessRecipe implements CraftingRecipe {
   private final Identifier id;
   final String group;
   final CraftingRecipeCategory category;
   final ItemStack output;
   final DefaultedList input;

   public ShapelessRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList input) {
      this.id = id;
      this.group = group;
      this.category = category;
      this.output = output;
      this.input = input;
   }

   public Identifier getId() {
      return this.id;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SHAPELESS;
   }

   public String getGroup() {
      return this.group;
   }

   public CraftingRecipeCategory getCategory() {
      return this.category;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return this.output;
   }

   public DefaultedList getIngredients() {
      return this.input;
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      RecipeMatcher lv = new RecipeMatcher();
      int i = 0;

      for(int j = 0; j < arg.size(); ++j) {
         ItemStack lv2 = arg.getStack(j);
         if (!lv2.isEmpty()) {
            ++i;
            lv.addInput(lv2, 1);
         }
      }

      return i == this.input.size() && lv.match(this, (IntList)null);
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      return this.output.copy();
   }

   public boolean fits(int width, int height) {
      return width * height >= this.input.size();
   }

   public static class Serializer implements RecipeSerializer {
      public ShapelessRecipe read(Identifier arg, JsonObject jsonObject) {
         String string = JsonHelper.getString(jsonObject, "group", "");
         CraftingRecipeCategory lv = (CraftingRecipeCategory)CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(jsonObject, "category", (String)null), CraftingRecipeCategory.MISC);
         DefaultedList lv2 = getIngredients(JsonHelper.getArray(jsonObject, "ingredients"));
         if (lv2.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else if (lv2.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
         } else {
            ItemStack lv3 = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
            return new ShapelessRecipe(arg, string, lv, lv3, lv2);
         }
      }

      private static DefaultedList getIngredients(JsonArray json) {
         DefaultedList lv = DefaultedList.of();

         for(int i = 0; i < json.size(); ++i) {
            Ingredient lv2 = Ingredient.fromJson(json.get(i));
            if (!lv2.isEmpty()) {
               lv.add(lv2);
            }
         }

         return lv;
      }

      public ShapelessRecipe read(Identifier arg, PacketByteBuf arg2) {
         String string = arg2.readString();
         CraftingRecipeCategory lv = (CraftingRecipeCategory)arg2.readEnumConstant(CraftingRecipeCategory.class);
         int i = arg2.readVarInt();
         DefaultedList lv2 = DefaultedList.ofSize(i, Ingredient.EMPTY);

         for(int j = 0; j < lv2.size(); ++j) {
            lv2.set(j, Ingredient.fromPacket(arg2));
         }

         ItemStack lv3 = arg2.readItemStack();
         return new ShapelessRecipe(arg, string, lv, lv3, lv2);
      }

      public void write(PacketByteBuf arg, ShapelessRecipe arg2) {
         arg.writeString(arg2.group);
         arg.writeEnumConstant(arg2.category);
         arg.writeVarInt(arg2.input.size());
         Iterator var3 = arg2.input.iterator();

         while(var3.hasNext()) {
            Ingredient lv = (Ingredient)var3.next();
            lv.write(arg);
         }

         arg.writeItemStack(arg2.output);
      }

      // $FF: synthetic method
      public Recipe read(Identifier id, PacketByteBuf buf) {
         return this.read(id, buf);
      }

      // $FF: synthetic method
      public Recipe read(Identifier id, JsonObject json) {
         return this.read(id, json);
      }
   }
}
