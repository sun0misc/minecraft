package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerInteractedWithEntityCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("player_interacted_with_entity");

   public Identifier getId() {
      return ID;
   }

   protected Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      EntityPredicate.Extended lv2 = EntityPredicate.Extended.getInJson(jsonObject, "entity", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack, Entity entity) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, (conditions) -> {
         return conditions.test(stack, lv);
      });
   }

   // $FF: synthetic method
   protected AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;
      private final EntityPredicate.Extended entity;

      public Conditions(EntityPredicate.Extended player, ItemPredicate item, EntityPredicate.Extended entity) {
         super(PlayerInteractedWithEntityCriterion.ID, player);
         this.item = item;
         this.entity = entity;
      }

      public static Conditions create(EntityPredicate.Extended player, ItemPredicate.Builder itemBuilder, EntityPredicate.Extended entity) {
         return new Conditions(player, itemBuilder.build(), entity);
      }

      public static Conditions create(ItemPredicate.Builder itemBuilder, EntityPredicate.Extended entity) {
         return create(EntityPredicate.Extended.EMPTY, itemBuilder, entity);
      }

      public boolean test(ItemStack stack, LootContext context) {
         return !this.item.test(stack) ? false : this.entity.test(context);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("entity", this.entity.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
