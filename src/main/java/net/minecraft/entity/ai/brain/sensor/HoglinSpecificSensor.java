package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class HoglinSpecificSensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, new MemoryModuleType[0]);
   }

   protected void sense(ServerWorld arg, HoglinEntity arg2) {
      Brain lv = arg2.getBrain();
      lv.remember(MemoryModuleType.NEAREST_REPELLENT, this.findNearestWarpedFungus(arg, arg2));
      Optional optional = Optional.empty();
      int i = 0;
      List list = Lists.newArrayList();
      LivingTargetCache lv2 = (LivingTargetCache)lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
      Iterator var8 = lv2.iterate((argx) -> {
         return !argx.isBaby() && (argx instanceof PiglinEntity || argx instanceof HoglinEntity);
      }).iterator();

      while(var8.hasNext()) {
         LivingEntity lv3 = (LivingEntity)var8.next();
         if (lv3 instanceof PiglinEntity lv4) {
            ++i;
            if (optional.isEmpty()) {
               optional = Optional.of(lv4);
            }
         }

         if (lv3 instanceof HoglinEntity lv5) {
            list.add(lv5);
         }
      }

      lv.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
      lv.remember(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, (Object)list);
      lv.remember(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, (Object)i);
      lv.remember(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, (Object)list.size());
   }

   private Optional findNearestWarpedFungus(ServerWorld world, HoglinEntity hoglin) {
      return BlockPos.findClosest(hoglin.getBlockPos(), 8, 4, (pos) -> {
         return world.getBlockState(pos).isIn(BlockTags.HOGLIN_REPELLENTS);
      });
   }
}
