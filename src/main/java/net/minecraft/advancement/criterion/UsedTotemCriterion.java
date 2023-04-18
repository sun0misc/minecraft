package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UsedTotemCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("used_totem");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(stack);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;

      public Conditions(EntityPredicate.Extended player, ItemPredicate item) {
         super(UsedTotemCriterion.ID, player);
         this.item = item;
      }

      public static Conditions create(ItemPredicate itemPredicate) {
         return new Conditions(EntityPredicate.Extended.EMPTY, itemPredicate);
      }

      public static Conditions create(ItemConvertible item) {
         return new Conditions(EntityPredicate.Extended.EMPTY, ItemPredicate.Builder.create().items(item).build());
      }

      public boolean matches(ItemStack stack) {
         return this.item.test(stack);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         return jsonObject;
      }
   }
}
