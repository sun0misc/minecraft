package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class NearestLivingEntitiesSensor extends Sensor {
   protected void sense(ServerWorld world, LivingEntity entity) {
      Box lv = entity.getBoundingBox().expand((double)this.getHorizontalExpansion(), (double)this.getHeightExpansion(), (double)this.getHorizontalExpansion());
      List list = world.getEntitiesByClass(LivingEntity.class, lv, (e) -> {
         return e != entity && e.isAlive();
      });
      Objects.requireNonNull(entity);
      list.sort(Comparator.comparingDouble(entity::squaredDistanceTo));
      Brain lv2 = entity.getBrain();
      lv2.remember(MemoryModuleType.MOBS, (Object)list);
      lv2.remember(MemoryModuleType.VISIBLE_MOBS, (Object)(new LivingTargetCache(entity, list)));
   }

   protected int getHorizontalExpansion() {
      return 16;
   }

   protected int getHeightExpansion() {
      return 16;
   }

   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);
   }
}
