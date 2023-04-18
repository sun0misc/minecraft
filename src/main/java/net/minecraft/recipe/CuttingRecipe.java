package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public abstract class CuttingRecipe implements Recipe {
   protected final Ingredient input;
   protected final ItemStack output;
   private final RecipeType type;
   private final RecipeSerializer serializer;
   protected final Identifier id;
   protected final String group;

   public CuttingRecipe(RecipeType type, RecipeSerializer serializer, Identifier id, String group, Ingredient input, ItemStack output) {
      this.type = type;
      this.serializer = serializer;
      this.id = id;
      this.group = group;
      this.input = input;
      this.output = output;
   }

   public RecipeType getType() {
      return this.type;
   }

   public RecipeSerializer getSerializer() {
      return this.serializer;
   }

   public Identifier getId() {
      return this.id;
   }

   public String getGroup() {
      return this.group;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return this.output;
   }

   public DefaultedList getIngredients() {
      DefaultedList lv = DefaultedList.of();
      lv.add(this.input);
      return lv;
   }

   public boolean fits(int width, int height) {
      return true;
   }

   public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
      return this.output.copy();
   }

   public static class Serializer implements RecipeSerializer {
      final RecipeFactory recipeFactory;

      protected Serializer(RecipeFactory recipeFactory) {
         this.recipeFactory = recipeFactory;
      }

      public CuttingRecipe read(Identifier arg, JsonObject jsonObject) {
         String string = JsonHelper.getString(jsonObject, "group", "");
         Ingredient lv;
         if (JsonHelper.hasArray(jsonObject, "ingredient")) {
            lv = Ingredient.fromJson(JsonHelper.getArray(jsonObject, "ingredient"));
         } else {
            lv = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient"));
         }

         String string2 = JsonHelper.getString(jsonObject, "result");
         int i = JsonHelper.getInt(jsonObject, "count");
         ItemStack lv2 = new ItemStack((ItemConvertible)Registries.ITEM.get(new Identifier(string2)), i);
         return this.recipeFactory.create(arg, string, lv, lv2);
      }

      public CuttingRecipe read(Identifier arg, PacketByteBuf arg2) {
         String string = arg2.readString();
         Ingredient lv = Ingredient.fromPacket(arg2);
         ItemStack lv2 = arg2.readItemStack();
         return this.recipeFactory.create(arg, string, lv, lv2);
      }

      public void write(PacketByteBuf arg, CuttingRecipe arg2) {
         arg.writeString(arg2.group);
         arg2.input.write(arg);
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

      interface RecipeFactory {
         CuttingRecipe create(Identifier id, String group, Ingredient input, ItemStack output);
      }
   }
}
