package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ItemDurabilityChangedCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("item_durability_changed");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(jsonObject.get("durability"));
      NumberRange.IntRange lv3 = NumberRange.IntRange.fromJson(jsonObject.get("delta"));
      return new Conditions(arg, lv, lv2, lv3);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack, int durability) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(stack, durability);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;
      private final NumberRange.IntRange durability;
      private final NumberRange.IntRange delta;

      public Conditions(EntityPredicate.Extended player, ItemPredicate item, NumberRange.IntRange durability, NumberRange.IntRange delta) {
         super(ItemDurabilityChangedCriterion.ID, player);
         this.item = item;
         this.durability = durability;
         this.delta = delta;
      }

      public static Conditions create(ItemPredicate item, NumberRange.IntRange durability) {
         return create(EntityPredicate.Extended.EMPTY, item, durability);
      }

      public static Conditions create(EntityPredicate.Extended player, ItemPredicate item, NumberRange.IntRange durability) {
         return new Conditions(player, item, durability, NumberRange.IntRange.ANY);
      }

      public boolean matches(ItemStack stack, int durability) {
         if (!this.item.test(stack)) {
            return false;
         } else if (!this.durability.test(stack.getMaxDamage() - durability)) {
            return false;
         } else {
            return this.delta.test(stack.getDamage() - durability);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("durability", this.durability.toJson());
         jsonObject.add("delta", this.delta.toJson());
         return jsonObject;
      }
   }
}
