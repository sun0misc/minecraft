package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class LevitationCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("levitation");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      DistancePredicate lv = DistancePredicate.fromJson(jsonObject.get("distance"));
      NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(jsonObject.get("duration"));
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, Vec3d startPos, int duration) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(player, startPos, duration);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final DistancePredicate distance;
      private final NumberRange.IntRange duration;

      public Conditions(EntityPredicate.Extended player, DistancePredicate distance, NumberRange.IntRange duration) {
         super(LevitationCriterion.ID, player);
         this.distance = distance;
         this.duration = duration;
      }

      public static Conditions create(DistancePredicate distance) {
         return new Conditions(EntityPredicate.Extended.EMPTY, distance, NumberRange.IntRange.ANY);
      }

      public boolean matches(ServerPlayerEntity player, Vec3d startPos, int duration) {
         if (!this.distance.test(startPos.x, startPos.y, startPos.z, player.getX(), player.getY(), player.getZ())) {
            return false;
         } else {
            return this.duration.test(duration);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("distance", this.distance.toJson());
         jsonObject.add("duration", this.duration.toJson());
         return jsonObject;
      }
   }
}
