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
import org.jetbrains.annotations.Nullable;

public class ThrownItemPickedUpByEntityCriterion extends AbstractCriterion {
   private final Identifier id;

   public ThrownItemPickedUpByEntityCriterion(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   protected Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      EntityPredicate.Extended lv2 = EntityPredicate.Extended.getInJson(jsonObject, "entity", arg2);
      return new Conditions(this.id, arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack, @Nullable Entity entity) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, (conditions) -> {
         return conditions.test(player, stack, lv);
      });
   }

   // $FF: synthetic method
   protected AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;
      private final EntityPredicate.Extended entity;

      public Conditions(Identifier id, EntityPredicate.Extended player, ItemPredicate item, EntityPredicate.Extended entity) {
         super(id, player);
         this.item = item;
         this.entity = entity;
      }

      public static Conditions createThrownItemPickedUpByEntity(EntityPredicate.Extended player, ItemPredicate item, EntityPredicate.Extended entity) {
         return new Conditions(Criteria.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), player, item, entity);
      }

      public static Conditions createThrownItemPickedUpByPlayer(EntityPredicate.Extended player, ItemPredicate item, EntityPredicate.Extended entity) {
         return new Conditions(Criteria.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), player, item, entity);
      }

      public boolean test(ServerPlayerEntity player, ItemStack stack, LootContext entityContext) {
         if (!this.item.test(stack)) {
            return false;
         } else {
            return this.entity.test(entityContext);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("entity", this.entity.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
