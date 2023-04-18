package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;

public class LootingEnchantLootFunction extends ConditionalLootFunction {
   public static final int field_31854 = 0;
   final LootNumberProvider countRange;
   final int limit;

   LootingEnchantLootFunction(LootCondition[] conditions, LootNumberProvider countRange, int limit) {
      super(conditions);
      this.countRange = countRange;
      this.limit = limit;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.LOOTING_ENCHANT;
   }

   public Set getRequiredParameters() {
      return Sets.union(ImmutableSet.of(LootContextParameters.KILLER_ENTITY), this.countRange.getRequiredParameters());
   }

   boolean hasLimit() {
      return this.limit > 0;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Entity lv = (Entity)context.get(LootContextParameters.KILLER_ENTITY);
      if (lv instanceof LivingEntity) {
         int i = EnchantmentHelper.getLooting((LivingEntity)lv);
         if (i == 0) {
            return stack;
         }

         float f = (float)i * this.countRange.nextFloat(context);
         stack.increment(Math.round(f));
         if (this.hasLimit() && stack.getCount() > this.limit) {
            stack.setCount(this.limit);
         }
      }

      return stack;
   }

   public static Builder builder(LootNumberProvider countRange) {
      return new Builder(countRange);
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final LootNumberProvider countRange;
      private int limit = 0;

      public Builder(LootNumberProvider countRange) {
         this.countRange = countRange;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder withLimit(int limit) {
         this.limit = limit;
         return this;
      }

      public LootFunction build() {
         return new LootingEnchantLootFunction(this.getConditions(), this.countRange, this.limit);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, LootingEnchantLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("count", jsonSerializationContext.serialize(arg.countRange));
         if (arg.hasLimit()) {
            jsonObject.add("limit", jsonSerializationContext.serialize(arg.limit));
         }

      }

      public LootingEnchantLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         int i = JsonHelper.getInt(jsonObject, "limit", 0);
         return new LootingEnchantLootFunction(args, (LootNumberProvider)JsonHelper.deserialize(jsonObject, "count", jsonDeserializationContext, LootNumberProvider.class), i);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
