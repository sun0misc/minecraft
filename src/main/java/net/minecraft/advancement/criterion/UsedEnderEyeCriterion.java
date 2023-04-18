package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class UsedEnderEyeCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("used_ender_eye");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      NumberRange.FloatRange lv = NumberRange.FloatRange.fromJson(jsonObject.get("distance"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, BlockPos strongholdPos) {
      double d = player.getX() - (double)strongholdPos.getX();
      double e = player.getZ() - (double)strongholdPos.getZ();
      double f = d * d + e * e;
      this.trigger(player, (conditions) -> {
         return conditions.matches(f);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final NumberRange.FloatRange distance;

      public Conditions(EntityPredicate.Extended player, NumberRange.FloatRange distance) {
         super(UsedEnderEyeCriterion.ID, player);
         this.distance = distance;
      }

      public boolean matches(double distance) {
         return this.distance.testSqrt(distance);
      }
   }
}
