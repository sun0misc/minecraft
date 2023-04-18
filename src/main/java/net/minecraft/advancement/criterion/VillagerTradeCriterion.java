package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class VillagerTradeCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("villager_trade");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityPredicate.Extended lv = EntityPredicate.Extended.getInJson(jsonObject, "villager", arg2);
      ItemPredicate lv2 = ItemPredicate.fromJson(jsonObject.get("item"));
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, MerchantEntity merchant, ItemStack stack) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, merchant);
      this.trigger(player, (conditions) -> {
         return conditions.matches(lv, stack);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityPredicate.Extended villager;
      private final ItemPredicate item;

      public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended villager, ItemPredicate item) {
         super(VillagerTradeCriterion.ID, player);
         this.villager = villager;
         this.item = item;
      }

      public static Conditions any() {
         return new Conditions(EntityPredicate.Extended.EMPTY, EntityPredicate.Extended.EMPTY, ItemPredicate.ANY);
      }

      public static Conditions create(EntityPredicate.Builder playerPredicate) {
         return new Conditions(EntityPredicate.Extended.ofLegacy(playerPredicate.build()), EntityPredicate.Extended.EMPTY, ItemPredicate.ANY);
      }

      public boolean matches(LootContext merchantContext, ItemStack stack) {
         if (!this.villager.test(merchantContext)) {
            return false;
         } else {
            return this.item.test(stack);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("villager", this.villager.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
