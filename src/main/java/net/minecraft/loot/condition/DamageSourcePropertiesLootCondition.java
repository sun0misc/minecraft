package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.Vec3d;

public class DamageSourcePropertiesLootCondition implements LootCondition {
   final DamageSourcePredicate predicate;

   DamageSourcePropertiesLootCondition(DamageSourcePredicate predicate) {
      this.predicate = predicate;
   }

   public LootConditionType getType() {
      return LootConditionTypes.DAMAGE_SOURCE_PROPERTIES;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.ORIGIN, LootContextParameters.DAMAGE_SOURCE);
   }

   public boolean test(LootContext arg) {
      DamageSource lv = (DamageSource)arg.get(LootContextParameters.DAMAGE_SOURCE);
      Vec3d lv2 = (Vec3d)arg.get(LootContextParameters.ORIGIN);
      return lv2 != null && lv != null && this.predicate.test(arg.getWorld(), lv2, lv);
   }

   public static LootCondition.Builder builder(DamageSourcePredicate.Builder builder) {
      return () -> {
         return new DamageSourcePropertiesLootCondition(builder.build());
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, DamageSourcePropertiesLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("predicate", arg.predicate.toJson());
      }

      public DamageSourcePropertiesLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         DamageSourcePredicate lv = DamageSourcePredicate.fromJson(jsonObject.get("predicate"));
         return new DamageSourcePropertiesLootCondition(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
