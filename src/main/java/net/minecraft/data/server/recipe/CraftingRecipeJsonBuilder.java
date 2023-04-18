package net.minecraft.data.server.recipe;

import java.util.function.Consumer;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface CraftingRecipeJsonBuilder {
   Identifier ROOT = new Identifier("recipes/root");

   CraftingRecipeJsonBuilder criterion(String name, CriterionConditions conditions);

   CraftingRecipeJsonBuilder group(@Nullable String group);

   Item getOutputItem();

   void offerTo(Consumer exporter, Identifier recipeId);

   default void offerTo(Consumer exporter) {
      this.offerTo(exporter, getItemId(this.getOutputItem()));
   }

   default void offerTo(Consumer exporter, String recipePath) {
      Identifier lv = getItemId(this.getOutputItem());
      Identifier lv2 = new Identifier(recipePath);
      if (lv2.equals(lv)) {
         throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.offerTo(exporter, lv2);
      }
   }

   static Identifier getItemId(ItemConvertible item) {
      return Registries.ITEM.getId(item.asItem());
   }
}
