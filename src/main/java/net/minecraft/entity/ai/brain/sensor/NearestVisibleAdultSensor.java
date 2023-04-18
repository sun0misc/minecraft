package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;

public class NearestVisibleAdultSensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.VISIBLE_MOBS);
   }

   protected void sense(ServerWorld arg, PassiveEntity arg2) {
      arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent((arg2x) -> {
         this.findNearestVisibleAdult(arg2, arg2x);
      });
   }

   private void findNearestVisibleAdult(PassiveEntity entity, LivingTargetCache arg2) {
      Optional var10000 = arg2.findFirst((arg2x) -> {
         return arg2x.getType() == entity.getType() && !arg2x.isBaby();
      });
      Objects.requireNonNull(PassiveEntity.class);
      Optional optional = var10000.map(PassiveEntity.class::cast);
      entity.getBrain().remember(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
   }
}
