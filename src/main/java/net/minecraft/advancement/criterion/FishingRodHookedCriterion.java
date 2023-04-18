package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class FishingRodHookedCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("fishing_rod_hooked");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("rod"));
      EntityPredicate.Extended lv2 = EntityPredicate.Extended.getInJson(jsonObject, "entity", arg2);
      ItemPredicate lv3 = ItemPredicate.fromJson(jsonObject.get("item"));
      return new Conditions(arg, lv, lv2, lv3);
   }

   public void trigger(ServerPlayerEntity player, ItemStack rod, FishingBobberEntity bobber, Collection fishingLoots) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, (Entity)(bobber.getHookedEntity() != null ? bobber.getHookedEntity() : bobber));
      this.trigger(player, (conditions) -> {
         return conditions.matches(rod, lv, fishingLoots);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate rod;
      private final EntityPredicate.Extended hookedEntity;
      private final ItemPredicate caughtItem;

      public Conditions(EntityPredicate.Extended player, ItemPredicate rod, EntityPredicate.Extended hookedEntity, ItemPredicate caughtItem) {
         super(FishingRodHookedCriterion.ID, player);
         this.rod = rod;
         this.hookedEntity = hookedEntity;
         this.caughtItem = caughtItem;
      }

      public static Conditions create(ItemPredicate rod, EntityPredicate bobber, ItemPredicate item) {
         return new Conditions(EntityPredicate.Extended.EMPTY, rod, EntityPredicate.Extended.ofLegacy(bobber), item);
      }

      public boolean matches(ItemStack rod, LootContext hookedEntityContext, Collection fishingLoots) {
         if (!this.rod.test(rod)) {
            return false;
         } else if (!this.hookedEntity.test(hookedEntityContext)) {
            return false;
         } else {
            if (this.caughtItem != ItemPredicate.ANY) {
               boolean bl = false;
               Entity lv = (Entity)hookedEntityContext.get(LootContextParameters.THIS_ENTITY);
               if (lv instanceof ItemEntity) {
                  ItemEntity lv2 = (ItemEntity)lv;
                  if (this.caughtItem.test(lv2.getStack())) {
                     bl = true;
                  }
               }

               Iterator var8 = fishingLoots.iterator();

               while(var8.hasNext()) {
                  ItemStack lv3 = (ItemStack)var8.next();
                  if (this.caughtItem.test(lv3)) {
                     bl = true;
                     break;
                  }
               }

               if (!bl) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("rod", this.rod.toJson());
         jsonObject.add("entity", this.hookedEntity.toJson(predicateSerializer));
         jsonObject.add("item", this.caughtItem.toJson());
         return jsonObject;
      }
   }
}
