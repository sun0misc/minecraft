package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.JsonSerializer;

public class MatchToolLootCondition implements LootCondition {
   final ItemPredicate predicate;

   public MatchToolLootCondition(ItemPredicate predicate) {
      this.predicate = predicate;
   }

   public LootConditionType getType() {
      return LootConditionTypes.MATCH_TOOL;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.TOOL);
   }

   public boolean test(LootContext arg) {
      ItemStack lv = (ItemStack)arg.get(LootContextParameters.TOOL);
      return lv != null && this.predicate.test(lv);
   }

   public static LootCondition.Builder builder(ItemPredicate.Builder predicate) {
      return () -> {
         return new MatchToolLootCondition(predicate.build());
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, MatchToolLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("predicate", arg.predicate.toJson());
      }

      public MatchToolLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("predicate"));
         return new MatchToolLootCondition(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
