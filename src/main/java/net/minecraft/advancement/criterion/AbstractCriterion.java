package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractCriterion implements Criterion {
   private final Map progressions = Maps.newIdentityHashMap();

   public final void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer conditions) {
      ((Set)this.progressions.computeIfAbsent(manager, (managerx) -> {
         return Sets.newHashSet();
      })).add(conditions);
   }

   public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer conditions) {
      Set set = (Set)this.progressions.get(manager);
      if (set != null) {
         set.remove(conditions);
         if (set.isEmpty()) {
            this.progressions.remove(manager);
         }
      }

   }

   public final void endTracking(PlayerAdvancementTracker tracker) {
      this.progressions.remove(tracker);
   }

   protected abstract AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer);

   public final AbstractCriterionConditions conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer arg) {
      EntityPredicate.Extended lv = EntityPredicate.Extended.getInJson(jsonObject, "player", arg);
      return this.conditionsFromJson(jsonObject, lv, arg);
   }

   protected void trigger(ServerPlayerEntity player, Predicate predicate) {
      PlayerAdvancementTracker lv = player.getAdvancementTracker();
      Set set = (Set)this.progressions.get(lv);
      if (set != null && !set.isEmpty()) {
         LootContext lv2 = EntityPredicate.createAdvancementEntityLootContext(player, player);
         List list = null;
         Iterator var7 = set.iterator();

         Criterion.ConditionsContainer lv3;
         while(var7.hasNext()) {
            lv3 = (Criterion.ConditionsContainer)var7.next();
            AbstractCriterionConditions lv4 = (AbstractCriterionConditions)lv3.getConditions();
            if (predicate.test(lv4) && lv4.getPlayerPredicate().test(lv2)) {
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(lv3);
            }
         }

         if (list != null) {
            var7 = list.iterator();

            while(var7.hasNext()) {
               lv3 = (Criterion.ConditionsContainer)var7.next();
               lv3.grant(lv);
            }
         }

      }
   }

   // $FF: synthetic method
   public CriterionConditions conditionsFromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, predicateDeserializer);
   }
}
