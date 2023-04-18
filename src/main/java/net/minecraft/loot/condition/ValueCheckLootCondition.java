package net.minecraft.loot.condition;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class ValueCheckLootCondition implements LootCondition {
   final LootNumberProvider value;
   final BoundedIntUnaryOperator range;

   ValueCheckLootCondition(LootNumberProvider value, BoundedIntUnaryOperator range) {
      this.value = value;
      this.range = range;
   }

   public LootConditionType getType() {
      return LootConditionTypes.VALUE_CHECK;
   }

   public Set getRequiredParameters() {
      return Sets.union(this.value.getRequiredParameters(), this.range.getRequiredParameters());
   }

   public boolean test(LootContext arg) {
      return this.range.test(arg, this.value.nextInt(arg));
   }

   public static LootCondition.Builder builder(LootNumberProvider value, BoundedIntUnaryOperator range) {
      return () -> {
         return new ValueCheckLootCondition(value, range);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, ValueCheckLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("value", jsonSerializationContext.serialize(arg.value));
         jsonObject.add("range", jsonSerializationContext.serialize(arg.range));
      }

      public ValueCheckLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "value", jsonDeserializationContext, LootNumberProvider.class);
         BoundedIntUnaryOperator lv2 = (BoundedIntUnaryOperator)JsonHelper.deserialize(jsonObject, "range", jsonDeserializationContext, BoundedIntUnaryOperator.class);
         return new ValueCheckLootCondition(lv, lv2);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
