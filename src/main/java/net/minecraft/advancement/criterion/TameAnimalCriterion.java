package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TameAnimalCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("tame_animal");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityPredicate.Extended lv = EntityPredicate.Extended.getInJson(jsonObject, "entity", arg2);
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, AnimalEntity entity) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, (conditions) -> {
         return conditions.matches(lv);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityPredicate.Extended entity;

      public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended entity) {
         super(TameAnimalCriterion.ID, player);
         this.entity = entity;
      }

      public static Conditions any() {
         return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions create(EntityPredicate entity) {
         return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.ofLegacy(entity));
      }

      public boolean matches(LootContext tamedEntityContext) {
         return this.entity.test(tamedEntityContext);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("entity", this.entity.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
