package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerHurtEntityCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("player_hurt_entity");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      DamagePredicate lv = DamagePredicate.fromJson(jsonObject.get("damage"));
      EntityPredicate.Extended lv2 = EntityPredicate.Extended.getInJson(jsonObject, "entity", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, Entity entity, DamageSource damage, float dealt, float taken, boolean blocked) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, (conditions) -> {
         return conditions.matches(player, lv, damage, dealt, taken, blocked);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final DamagePredicate damage;
      private final EntityPredicate.Extended entity;

      public Conditions(EntityPredicate.Extended player, DamagePredicate damage, EntityPredicate.Extended entity) {
         super(PlayerHurtEntityCriterion.ID, player);
         this.damage = damage;
         this.entity = entity;
      }

      public static Conditions create() {
         return new Conditions(EntityPredicate.Extended.EMPTY, DamagePredicate.ANY, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions create(DamagePredicate damagePredicate) {
         return new Conditions(EntityPredicate.Extended.EMPTY, damagePredicate, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions create(DamagePredicate.Builder damagePredicateBuilder) {
         return new Conditions(EntityPredicate.Extended.EMPTY, damagePredicateBuilder.build(), EntityPredicate.Extended.EMPTY);
      }

      public static Conditions create(EntityPredicate hurtEntityPredicate) {
         return new Conditions(EntityPredicate.Extended.EMPTY, DamagePredicate.ANY, EntityPredicate.Extended.ofLegacy(hurtEntityPredicate));
      }

      public static Conditions create(DamagePredicate damagePredicate, EntityPredicate hurtEntityPredicate) {
         return new Conditions(EntityPredicate.Extended.EMPTY, damagePredicate, EntityPredicate.Extended.ofLegacy(hurtEntityPredicate));
      }

      public static Conditions create(DamagePredicate.Builder damagePredicateBuilder, EntityPredicate hurtEntityPredicate) {
         return new Conditions(EntityPredicate.Extended.EMPTY, damagePredicateBuilder.build(), EntityPredicate.Extended.ofLegacy(hurtEntityPredicate));
      }

      public boolean matches(ServerPlayerEntity player, LootContext entityContext, DamageSource source, float dealt, float taken, boolean blocked) {
         if (!this.damage.test(player, source, dealt, taken, blocked)) {
            return false;
         } else {
            return this.entity.test(entityContext);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("damage", this.damage.toJson());
         jsonObject.add("entity", this.entity.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
