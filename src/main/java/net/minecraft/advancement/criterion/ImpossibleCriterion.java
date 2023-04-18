package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.util.Identifier;

public class ImpossibleCriterion implements Criterion {
   static final Identifier ID = new Identifier("impossible");

   public Identifier getId() {
      return ID;
   }

   public void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer conditions) {
   }

   public void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer conditions) {
   }

   public void endTracking(PlayerAdvancementTracker tracker) {
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer arg) {
      return new Conditions();
   }

   // $FF: synthetic method
   public CriterionConditions conditionsFromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, predicateDeserializer);
   }

   public static class Conditions implements CriterionConditions {
      public Identifier getId() {
         return ImpossibleCriterion.ID;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         return new JsonObject();
      }
   }
}
