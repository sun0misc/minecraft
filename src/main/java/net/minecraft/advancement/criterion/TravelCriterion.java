package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TravelCriterion extends AbstractCriterion {
   final Identifier id;

   public TravelCriterion(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      LocationPredicate lv = LocationPredicate.fromJson(jsonObject.get("start_position"));
      DistancePredicate lv2 = DistancePredicate.fromJson(jsonObject.get("distance"));
      return new Conditions(this.id, arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, Vec3d startPos) {
      Vec3d lv = player.getPos();
      this.trigger(player, (conditions) -> {
         return conditions.matches(player.getWorld(), startPos, lv);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final LocationPredicate startPos;
      private final DistancePredicate distance;

      public Conditions(Identifier id, EntityPredicate.Extended entity, LocationPredicate startPos, DistancePredicate distance) {
         super(id, entity);
         this.startPos = startPos;
         this.distance = distance;
      }

      public static Conditions fallFromHeight(EntityPredicate.Builder entity, DistancePredicate distance, LocationPredicate startPos) {
         return new Conditions(Criteria.FALL_FROM_HEIGHT.id, EntityPredicate.Extended.ofLegacy(entity.build()), startPos, distance);
      }

      public static Conditions rideEntityInLava(EntityPredicate.Builder entity, DistancePredicate distance) {
         return new Conditions(Criteria.RIDE_ENTITY_IN_LAVA.id, EntityPredicate.Extended.ofLegacy(entity.build()), LocationPredicate.ANY, distance);
      }

      public static Conditions netherTravel(DistancePredicate distance) {
         return new Conditions(Criteria.NETHER_TRAVEL.id, EntityPredicate.Extended.EMPTY, LocationPredicate.ANY, distance);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("start_position", this.startPos.toJson());
         jsonObject.add("distance", this.distance.toJson());
         return jsonObject;
      }

      public boolean matches(ServerWorld world, Vec3d startPos, Vec3d endPos) {
         if (!this.startPos.test(world, startPos.x, startPos.y, startPos.z)) {
            return false;
         } else {
            return this.distance.test(startPos.x, startPos.y, startPos.z, endPos.x, endPos.y, endPos.z);
         }
      }
   }
}
