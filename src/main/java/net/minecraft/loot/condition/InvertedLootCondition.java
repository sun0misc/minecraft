package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class InvertedLootCondition implements LootCondition {
   final LootCondition term;

   InvertedLootCondition(LootCondition term) {
      this.term = term;
   }

   public LootConditionType getType() {
      return LootConditionTypes.INVERTED;
   }

   public final boolean test(LootContext arg) {
      return !this.term.test(arg);
   }

   public Set getRequiredParameters() {
      return this.term.getRequiredParameters();
   }

   public void validate(LootTableReporter reporter) {
      LootCondition.super.validate(reporter);
      this.term.validate(reporter);
   }

   public static LootCondition.Builder builder(LootCondition.Builder term) {
      InvertedLootCondition lv = new InvertedLootCondition(term.build());
      return () -> {
         return lv;
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, InvertedLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("term", jsonSerializationContext.serialize(arg.term));
      }

      public InvertedLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition lv = (LootCondition)JsonHelper.deserialize(jsonObject, "term", jsonDeserializationContext, LootCondition.class);
         return new InvertedLootCondition(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
