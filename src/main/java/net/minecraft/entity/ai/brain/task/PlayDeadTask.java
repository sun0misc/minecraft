package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayDeadTask extends MultiTickTask {
   public PlayDeadTask() {
      super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleState.VALUE_PRESENT), 200);
   }

   protected boolean shouldRun(ServerWorld arg, AxolotlEntity arg2) {
      return arg2.isInsideWaterOrBubbleColumn();
   }

   protected boolean shouldKeepRunning(ServerWorld arg, AxolotlEntity arg2, long l) {
      return arg2.isInsideWaterOrBubbleColumn() && arg2.getBrain().hasMemoryModule(MemoryModuleType.PLAY_DEAD_TICKS);
   }

   protected void run(ServerWorld arg, AxolotlEntity arg2, long l) {
      Brain lv = arg2.getBrain();
      lv.forget(MemoryModuleType.WALK_TARGET);
      lv.forget(MemoryModuleType.LOOK_TARGET);
      arg2.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (AxolotlEntity)entity, time);
   }
}
