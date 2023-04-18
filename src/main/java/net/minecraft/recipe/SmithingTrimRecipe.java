package net.minecraft.recipe;

import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class SmithingTrimRecipe implements SmithingRecipe {
   private final Identifier id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;

   public SmithingTrimRecipe(Identifier id, Ingredient template, Ingredient base, Ingredient addition) {
      this.id = id;
      this.template = template;
      this.base = base;
      this.addition = addition;
   }

   public boolean matches(Inventory inventory, World world) {
      return this.template.test(inventory.getStack(0)) && this.base.test(inventory.getStack(1)) && this.addition.test(inventory.getStack(2));
   }

   public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
      ItemStack lv = inventory.getStack(1);
      if (this.base.test(lv)) {
         Optional optional = ArmorTrimMaterials.get(registryManager, inventory.getStack(2));
         Optional optional2 = ArmorTrimPatterns.get(registryManager, inventory.getStack(0));
         if (optional.isPresent() && optional2.isPresent()) {
            Optional optional3 = ArmorTrim.getTrim(registryManager, lv);
            if (optional3.isPresent() && ((ArmorTrim)optional3.get()).equals((RegistryEntry)optional2.get(), (RegistryEntry)optional.get())) {
               return ItemStack.EMPTY;
            }

            ItemStack lv2 = lv.copy();
            lv2.setCount(1);
            ArmorTrim lv3 = new ArmorTrim((RegistryEntry)optional.get(), (RegistryEntry)optional2.get());
            if (ArmorTrim.apply(registryManager, lv2, lv3)) {
               return lv2;
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      ItemStack lv = new ItemStack(Items.IRON_CHESTPLATE);
      Optional optional = registryManager.get(RegistryKeys.TRIM_PATTERN).streamEntries().findFirst();
      if (optional.isPresent()) {
         Optional optional2 = registryManager.get(RegistryKeys.TRIM_MATERIAL).getEntry(ArmorTrimMaterials.REDSTONE);
         if (optional2.isPresent()) {
            ArmorTrim lv2 = new ArmorTrim((RegistryEntry)optional2.get(), (RegistryEntry)optional.get());
            ArmorTrim.apply(registryManager, lv, lv2);
         }
      }

      return lv;
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
      return RecipeSerializer.SMITHING_TRIM;
   }

   public boolean isEmpty() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
   }

   public static class Serializer implements RecipeSerializer {
      public SmithingTrimRecipe read(Identifier arg, JsonObject jsonObject) {
         Ingredient lv = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "template"));
         Ingredient lv2 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "base"));
         Ingredient lv3 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "addition"));
         return new SmithingTrimRecipe(arg, lv, lv2, lv3);
      }

      public SmithingTrimRecipe read(Identifier arg, PacketByteBuf arg2) {
         Ingredient lv = Ingredient.fromPacket(arg2);
         Ingredient lv2 = Ingredient.fromPacket(arg2);
         Ingredient lv3 = Ingredient.fromPacket(arg2);
         return new SmithingTrimRecipe(arg, lv, lv2, lv3);
      }

      public void write(PacketByteBuf arg, SmithingTrimRecipe arg2) {
         arg2.template.write(arg);
         arg2.base.write(arg);
         arg2.addition.write(arg);
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
