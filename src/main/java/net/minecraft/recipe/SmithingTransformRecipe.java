package net.minecraft.recipe;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class SmithingTransformRecipe implements SmithingRecipe {
   private final Identifier id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;
   final ItemStack result;

   public SmithingTransformRecipe(Identifier id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
      this.id = id;
      this.template = template;
      this.base = base;
      this.addition = addition;
      this.result = result;
   }

   public boolean matches(Inventory inventory, World world) {
      return this.template.test(inventory.getStack(0)) && this.base.test(inventory.getStack(1)) && this.addition.test(inventory.getStack(2));
   }

   public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
      ItemStack lv = this.result.copy();
      NbtCompound lv2 = inventory.getStack(1).getNbt();
      if (lv2 != null) {
         lv.setNbt(lv2.copy());
      }

      return lv;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return this.result;
   }

   public boolean testTemplate(ItemStack stack) {
      return this.template.test(stack);
   }

   public boolean testBase(ItemStack stack) {
      return this.base.test(stack);
   }

   public boolean testAddition(ItemStack stack) {
      return this.addition.test(stack);
   }

   public Identifier getId() {
      return this.id;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SMITHING_TRANSFORM;
   }

   public boolean isEmpty() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
   }

   public static class Serializer implements RecipeSerializer {
      public SmithingTransformRecipe read(Identifier arg, JsonObject jsonObject) {
         Ingredient lv = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "template"));
         Ingredient lv2 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "base"));
         Ingredient lv3 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "addition"));
         ItemStack lv4 = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
         return new SmithingTransformRecipe(arg, lv, lv2, lv3, lv4);
      }

      public SmithingTransformRecipe read(Identifier arg, PacketByteBuf arg2) {
         Ingredient lv = Ingredient.fromPacket(arg2);
         Ingredient lv2 = Ingredient.fromPacket(arg2);
         Ingredient lv3 = Ingredient.fromPacket(arg2);
         ItemStack lv4 = arg2.readItemStack();
         return new SmithingTransformRecipe(arg, lv, lv2, lv3, lv4);
      }

      public void write(PacketByteBuf arg, SmithingTransformRecipe arg2) {
         arg2.template.write(arg);
         arg2.base.write(arg);
         arg2.addition.write(arg);
         arg.writeItemStack(arg2.result);
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
