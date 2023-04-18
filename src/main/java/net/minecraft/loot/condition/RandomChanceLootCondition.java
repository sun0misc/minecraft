package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class RandomChanceLootCondition implements LootCondition {
   final float chance;

   RandomChanceLootCondition(float chance) {
      this.chance = chance;
   }

   public LootConditionType getType() {
      return LootConditionTypes.RANDOM_CHANCE;
   }

   public boolean test(LootContext arg) {
      return arg.getRandom().nextFloat() < this.chance;
   }

   public static LootCondition.Builder builder(float chance) {
      return () -> {
         return new RandomChanceLootCondition(chance);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, RandomChanceLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("chance", arg.chance);
      }

      public RandomChanceLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         return new RandomChanceLootCondition(JsonHelper.getFloat(jsonObject, "chance"));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
