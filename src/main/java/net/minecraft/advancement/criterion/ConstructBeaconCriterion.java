package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConstructBeaconCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("construct_beacon");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject.get("level"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, int level) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(level);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final NumberRange.IntRange level;

      public Conditions(EntityPredicate.Extended player, NumberRange.IntRange level) {
         super(ConstructBeaconCriterion.ID, player);
         this.level = level;
      }

      public static Conditions create() {
         return new Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.ANY);
      }

      public static Conditions level(NumberRange.IntRange level) {
         return new Conditions(EntityPredicate.Extended.EMPTY, level);
      }

      public boolean matches(int level) {
         return this.level.test(level);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("level", this.level.toJson());
         return jsonObject;
      }
   }
}
