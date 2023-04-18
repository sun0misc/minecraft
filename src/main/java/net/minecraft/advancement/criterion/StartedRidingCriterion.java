package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class StartedRidingCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("started_riding");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      return new Conditions(arg);
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, (conditions) -> {
         return true;
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      public Conditions(EntityPredicate.Extended player) {
         super(StartedRidingCriterion.ID, player);
      }

      public static Conditions create(EntityPredicate.Builder player) {
         return new Conditions(EntityPredicate.Extended.ofLegacy(player.build()));
      }
   }
}
