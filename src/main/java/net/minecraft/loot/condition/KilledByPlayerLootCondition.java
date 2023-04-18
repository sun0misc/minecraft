package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonSerializer;

public class KilledByPlayerLootCondition implements LootCondition {
   static final KilledByPlayerLootCondition INSTANCE = new KilledByPlayerLootCondition();

   private KilledByPlayerLootCondition() {
   }

   public LootConditionType getType() {
      return LootConditionTypes.KILLED_BY_PLAYER;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.LAST_DAMAGE_PLAYER);
   }

   public boolean test(LootContext arg) {
      return arg.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER);
   }

   public static LootCondition.Builder builder() {
      return () -> {
         return INSTANCE;
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, KilledByPlayerLootCondition arg, JsonSerializationContext jsonSerializationContext) {
      }

      public KilledByPlayerLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         return KilledByPlayerLootCondition.INSTANCE;
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
