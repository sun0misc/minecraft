package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;

public class TemptTask extends MultiTickTask {
   public static final int TEMPTATION_COOLDOWN_TICKS = 100;
   public static final double field_30116 = 2.5;
   private final Function speed;

   public TemptTask(Function speed) {
      super((Map)Util.make(() -> {
         ImmutableMap.Builder builder = ImmutableMap.builder();
         builder.put(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED);
         builder.put(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED);
         builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT);
         builder.put(MemoryModuleType.IS_TEMPTED, MemoryModuleState.REGISTERED);
         builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_PRESENT);
         builder.put(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT);
         builder.put(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT);
         return builder.build();
      }));
      this.speed = speed;
   }

   protected float getSpeed(PathAwareEntity entity) {
      return (Float)this.speed.apply(entity);
   }

   private Optional getTemptingPlayer(PathAwareEntity entity) {
      return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.TEMPTING_PLAYER);
   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      return this.getTemptingPlayer(arg2).isPresent() && !arg2.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET) && !arg2.getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
   }

   protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
      arg2.getBrain().remember(MemoryModuleType.IS_TEMPTED, (Object)true);
   }

   protected void finishRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      Brain lv = arg2.getBrain();
      lv.remember(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (int)100);
      lv.remember(MemoryModuleType.IS_TEMPTED, (Object)false);
      lv.forget(MemoryModuleType.WALK_TARGET);
      lv.forget(MemoryModuleType.LOOK_TARGET);
   }

   protected void keepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      PlayerEntity lv = (PlayerEntity)this.getTemptingPlayer(arg2).get();
      Brain lv2 = arg2.getBrain();
      lv2.remember(MemoryModuleType.LOOK_TARGET, (Object)(new EntityLookTarget(lv, true)));
      if (arg2.squaredDistanceTo(lv) < 6.25) {
         lv2.forget(MemoryModuleType.WALK_TARGET);
      } else {
         lv2.remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(new EntityLookTarget(lv, false), this.getSpeed(arg2), 2)));
      }

   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (PathAwareEntity)entity, time);
   }
}
