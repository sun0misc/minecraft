package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.util.JsonHelper;

public class LimitCountLootFunction extends ConditionalLootFunction {
   final BoundedIntUnaryOperator limit;

   LimitCountLootFunction(LootCondition[] conditions, BoundedIntUnaryOperator limit) {
      super(conditions);
      this.limit = limit;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.LIMIT_COUNT;
   }

   public Set getRequiredParameters() {
      return this.limit.getRequiredParameters();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      int i = this.limit.apply(context, stack.getCount());
      stack.setCount(i);
      return stack;
   }

   public static ConditionalLootFunction.Builder builder(BoundedIntUnaryOperator limit) {
      return builder((conditions) -> {
         return new LimitCountLootFunction(conditions, limit);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, LimitCountLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("limit", jsonSerializationContext.serialize(arg.limit));
      }

      public LimitCountLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         BoundedIntUnaryOperator lv = (BoundedIntUnaryOperator)JsonHelper.deserialize(jsonObject, "limit", jsonDeserializationContext, BoundedIntUnaryOperator.class);
         return new LimitCountLootFunction(args, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
