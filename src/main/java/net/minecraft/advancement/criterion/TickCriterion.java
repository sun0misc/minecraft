package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TickCriterion extends AbstractCriterion {
   final Identifier id;

   public TickCriterion(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      return new Conditions(this.id, arg);
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
      public Conditions(Identifier arg, EntityPredicate.Extended arg2) {
         super(arg, arg2);
      }

      public static Conditions createLocation(LocationPredicate location) {
         return new Conditions(Criteria.LOCATION.id, EntityPredicate.Extended.ofLegacy(EntityPredicate.Builder.create().location(location).build()));
      }

      public static Conditions createLocation(EntityPredicate entity) {
         return new Conditions(Criteria.LOCATION.id, EntityPredicate.Extended.ofLegacy(entity));
      }

      public static Conditions createSleptInBed() {
         return new Conditions(Criteria.SLEPT_IN_BED.id, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions createHeroOfTheVillage() {
         return new Conditions(Criteria.HERO_OF_THE_VILLAGE.id, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions createAvoidVibration() {
         return new Conditions(Criteria.AVOID_VIBRATION.id, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions createTick() {
         return new Conditions(Criteria.TICK.id, EntityPredicate.Extended.EMPTY);
      }

      public static Conditions createLocation(Block block, Item item) {
         return createLocation(EntityPredicate.Builder.create().equipment(EntityEquipmentPredicate.Builder.create().feet(ItemPredicate.Builder.create().items(item).build()).build()).steppingOn(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks(block).build()).build()).build());
      }
   }
}
