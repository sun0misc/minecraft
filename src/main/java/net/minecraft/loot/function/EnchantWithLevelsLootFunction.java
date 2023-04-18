package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;

public class EnchantWithLevelsLootFunction extends ConditionalLootFunction {
   final LootNumberProvider range;
   final boolean treasureEnchantmentsAllowed;

   EnchantWithLevelsLootFunction(LootCondition[] conditions, LootNumberProvider range, boolean treasureEnchantmentsAllowed) {
      super(conditions);
      this.range = range;
      this.treasureEnchantmentsAllowed = treasureEnchantmentsAllowed;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.ENCHANT_WITH_LEVELS;
   }

   public Set getRequiredParameters() {
      return this.range.getRequiredParameters();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Random lv = context.getRandom();
      return EnchantmentHelper.enchant(lv, stack, this.range.nextInt(context), this.treasureEnchantmentsAllowed);
   }

   public static Builder builder(LootNumberProvider range) {
      return new Builder(range);
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final LootNumberProvider range;
      private boolean treasureEnchantmentsAllowed;

      public Builder(LootNumberProvider range) {
         this.range = range;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder allowTreasureEnchantments() {
         this.treasureEnchantmentsAllowed = true;
         return this;
      }

      public LootFunction build() {
         return new EnchantWithLevelsLootFunction(this.getConditions(), this.range, this.treasureEnchantmentsAllowed);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, EnchantWithLevelsLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("levels", jsonSerializationContext.serialize(arg.range));
         jsonObject.addProperty("treasure", arg.treasureEnchantmentsAllowed);
      }

      public EnchantWithLevelsLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "levels", jsonDeserializationContext, LootNumberProvider.class);
         boolean bl = JsonHelper.getBoolean(jsonObject, "treasure", false);
         return new EnchantWithLevelsLootFunction(args, lv, bl);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
