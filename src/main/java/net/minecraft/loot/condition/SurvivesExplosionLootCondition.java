package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.random.Random;

public class SurvivesExplosionLootCondition implements LootCondition {
   static final SurvivesExplosionLootCondition INSTANCE = new SurvivesExplosionLootCondition();

   private SurvivesExplosionLootCondition() {
   }

   public LootConditionType getType() {
      return LootConditionTypes.SURVIVES_EXPLOSION;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.EXPLOSION_RADIUS);
   }

   public boolean test(LootContext arg) {
      Float float_ = (Float)arg.get(LootContextParameters.EXPLOSION_RADIUS);
      if (float_ != null) {
         Random lv = arg.getRandom();
         float f = 1.0F / float_;
         return lv.nextFloat() <= f;
      } else {
         return true;
      }
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
      public void toJson(JsonObject jsonObject, SurvivesExplosionLootCondition arg, JsonSerializationContext jsonSerializationContext) {
      }

      public SurvivesExplosionLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         return SurvivesExplosionLootCondition.INSTANCE;
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
