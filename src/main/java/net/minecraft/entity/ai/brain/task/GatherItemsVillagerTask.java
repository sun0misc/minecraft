package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

public class GatherItemsVillagerTask extends MultiTickTask {
   private static final int MAX_RANGE = 5;
   private static final float WALK_TOGETHER_SPEED = 0.5F;
   private Set items = ImmutableSet.of();

   public GatherItemsVillagerTask() {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
   }

   protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
      return LookTargetUtil.canSee(arg2.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      return this.shouldRun(arg, arg2);
   }

   protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
      VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
      LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5F);
      this.items = getGatherableItems(arg2, lv);
   }

   protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.INTERACTION_TARGET).get();
      if (!(arg2.squaredDistanceTo(lv) > 5.0)) {
         LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5F);
         arg2.talkWithVillager(arg, lv, l);
         if (arg2.wantsToStartBreeding() && (arg2.getVillagerData().getProfession() == VillagerProfession.FARMER || lv.canBreed())) {
            giveHalfOfStack(arg2, VillagerEntity.ITEM_FOOD_VALUES.keySet(), lv);
         }

         if (lv.getVillagerData().getProfession() == VillagerProfession.FARMER && arg2.getInventory().count(Items.WHEAT) > Items.WHEAT.getMaxCount() / 2) {
            giveHalfOfStack(arg2, ImmutableSet.of(Items.WHEAT), lv);
         }

         if (!this.items.isEmpty() && arg2.getInventory().containsAny(this.items)) {
            giveHalfOfStack(arg2, this.items, lv);
         }

      }
   }

   protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
   }

   private static Set getGatherableItems(VillagerEntity entity, VillagerEntity target) {
      ImmutableSet immutableSet = target.getVillagerData().getProfession().gatherableItems();
      ImmutableSet immutableSet2 = entity.getVillagerData().getProfession().gatherableItems();
      return (Set)immutableSet.stream().filter((item) -> {
         return !immutableSet2.contains(item);
      }).collect(Collectors.toSet());
   }

   private static void giveHalfOfStack(VillagerEntity villager, Set validItems, LivingEntity target) {
      SimpleInventory lv = villager.getInventory();
      ItemStack lv2 = ItemStack.EMPTY;
      int i = 0;

      while(i < lv.size()) {
         ItemStack lv3;
         Item lv4;
         int j;
         label28: {
            lv3 = lv.getStack(i);
            if (!lv3.isEmpty()) {
               lv4 = lv3.getItem();
               if (validItems.contains(lv4)) {
                  if (lv3.getCount() > lv3.getMaxCount() / 2) {
                     j = lv3.getCount() / 2;
                     break label28;
                  }

                  if (lv3.getCount() > 24) {
                     j = lv3.getCount() - 24;
                     break label28;
                  }
               }
            }

            ++i;
            continue;
         }

         lv3.decrement(j);
         lv2 = new ItemStack(lv4, j);
         break;
      }

      if (!lv2.isEmpty()) {
         LookTargetUtil.give(villager, lv2, target.getPos());
      }

   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (VillagerEntity)entity, time);
   }
}
