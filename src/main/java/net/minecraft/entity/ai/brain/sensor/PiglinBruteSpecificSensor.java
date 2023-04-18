package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.server.world.ServerWorld;

public class PiglinBruteSpecificSensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Brain lv = entity.getBrain();
      List list = Lists.newArrayList();
      LivingTargetCache lv2 = (LivingTargetCache)lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
      Optional var10000 = lv2.findFirst((arg) -> {
         return arg instanceof WitherSkeletonEntity || arg instanceof WitherEntity;
      });
      Objects.requireNonNull(MobEntity.class);
      Optional optional = var10000.map(MobEntity.class::cast);
      List list2 = (List)lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
      Iterator var8 = list2.iterator();

      while(var8.hasNext()) {
         LivingEntity lv3 = (LivingEntity)var8.next();
         if (lv3 instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)lv3).isAdult()) {
            list.add((AbstractPiglinEntity)lv3);
         }
      }

      lv.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
      lv.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, (Object)list);
   }
}
