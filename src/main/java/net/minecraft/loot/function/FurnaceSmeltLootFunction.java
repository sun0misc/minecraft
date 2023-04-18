package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import org.slf4j.Logger;

public class FurnaceSmeltLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogUtils.getLogger();

   FurnaceSmeltLootFunction(LootCondition[] args) {
      super(args);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.FURNACE_SMELT;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isEmpty()) {
         return stack;
      } else {
         Optional optional = context.getWorld().getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(new ItemStack[]{stack}), context.getWorld());
         if (optional.isPresent()) {
            ItemStack lv = ((SmeltingRecipe)optional.get()).getOutput(context.getWorld().getRegistryManager());
            if (!lv.isEmpty()) {
               return lv.copyWithCount(stack.getCount());
            }
         }

         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", stack);
         return stack;
      }
   }

   public static ConditionalLootFunction.Builder builder() {
      return builder(FurnaceSmeltLootFunction::new);
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public FurnaceSmeltLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         return new FurnaceSmeltLootFunction(args);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
