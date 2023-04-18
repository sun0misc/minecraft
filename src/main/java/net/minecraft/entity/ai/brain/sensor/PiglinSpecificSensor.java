package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PiglinSpecificSensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Brain lv = entity.getBrain();
      lv.remember(MemoryModuleType.NEAREST_REPELLENT, findPiglinRepellent(world, entity));
      Optional optional = Optional.empty();
      Optional optional2 = Optional.empty();
      Optional optional3 = Optional.empty();
      Optional optional4 = Optional.empty();
      Optional optional5 = Optional.empty();
      Optional optional6 = Optional.empty();
      Optional optional7 = Optional.empty();
      int i = 0;
      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      LivingTargetCache lv2 = (LivingTargetCache)lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
      Iterator var15 = lv2.iterate((arg) -> {
         return true;
      }).iterator();

      while(true) {
         while(true) {
            while(var15.hasNext()) {
               LivingEntity lv3 = (LivingEntity)var15.next();
               if (lv3 instanceof HoglinEntity lv4) {
                  if (lv4.isBaby() && optional3.isEmpty()) {
                     optional3 = Optional.of(lv4);
                  } else if (lv4.isAdult()) {
                     ++i;
                     if (optional2.isEmpty() && lv4.canBeHunted()) {
                        optional2 = Optional.of(lv4);
                     }
                  }
               } else if (lv3 instanceof PiglinBruteEntity lv5) {
                  list.add(lv5);
               } else if (lv3 instanceof PiglinEntity lv6) {
                  if (lv6.isBaby() && optional4.isEmpty()) {
                     optional4 = Optional.of(lv6);
                  } else if (lv6.isAdult()) {
                     list.add(lv6);
                  }
               } else if (lv3 instanceof PlayerEntity lv7) {
                  if (optional6.isEmpty() && !PiglinBrain.wearsGoldArmor(lv7) && entity.canTarget(lv3)) {
                     optional6 = Optional.of(lv7);
                  }

                  if (optional7.isEmpty() && !lv7.isSpectator() && PiglinBrain.isGoldHoldingPlayer(lv7)) {
                     optional7 = Optional.of(lv7);
                  }
               } else if (optional.isEmpty() && (lv3 instanceof WitherSkeletonEntity || lv3 instanceof WitherEntity)) {
                  optional = Optional.of((MobEntity)lv3);
               } else if (optional5.isEmpty() && PiglinBrain.isZombified(lv3.getType())) {
                  optional5 = Optional.of(lv3);
               }
            }

            List list3 = (List)lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
            Iterator var22 = list3.iterator();

            while(var22.hasNext()) {
               LivingEntity lv8 = (LivingEntity)var22.next();
               if (lv8 instanceof AbstractPiglinEntity lv9) {
                  if (lv9.isAdult()) {
                     list2.add(lv9);
                  }
               }
            }

            lv.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
            lv.remember(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
            lv.remember(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
            lv.remember(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
            lv.remember(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
            lv.remember(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
            lv.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, (Object)list2);
            lv.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, (Object)list);
            lv.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, (Object)list.size());
            lv.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, (Object)i);
            return;
         }
      }
   }

   private static Optional findPiglinRepellent(ServerWorld world, LivingEntity entity) {
      return BlockPos.findClosest(entity.getBlockPos(), 8, 4, (pos) -> {
         return isPiglinRepellent(world, pos);
      });
   }

   private static boolean isPiglinRepellent(ServerWorld world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      boolean bl = lv.isIn(BlockTags.PIGLIN_REPELLENTS);
      return bl && lv.isOf(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(lv) : bl;
   }
}
