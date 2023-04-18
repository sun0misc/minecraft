package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.util.Identifier;

public interface Criterion {
   Identifier getId();

   void beginTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer conditions);

   void endTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer conditions);

   void endTracking(PlayerAdvancementTracker tracker);

   CriterionConditions conditionsFromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer);

   public static class ConditionsContainer {
      private final CriterionConditions conditions;
      private final Advancement advancement;
      private final String id;

      public ConditionsContainer(CriterionConditions conditions, Advancement advancement, String id) {
         this.conditions = conditions;
         this.advancement = advancement;
         this.id = id;
      }

      public CriterionConditions getConditions() {
         return this.conditions;
      }

      public void grant(PlayerAdvancementTracker tracker) {
         tracker.grantCriterion(this.advancement, this.id);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ConditionsContainer lv = (ConditionsContainer)o;
            if (!this.conditions.equals(lv.conditions)) {
               return false;
            } else {
               return !this.advancement.equals(lv.advancement) ? false : this.id.equals(lv.id);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.conditions.hashCode();
         i = 31 * i + this.advancement.hashCode();
         i = 31 * i + this.id.hashCode();
         return i;
      }
   }
}
