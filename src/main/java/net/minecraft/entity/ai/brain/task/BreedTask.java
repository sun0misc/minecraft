package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;

public class BreedTask extends MultiTickTask {
   private static final int MAX_RANGE = 3;
   private static final int MIN_BREED_TIME = 60;
   private static final int RUN_TIME = 110;
   private final EntityType targetType;
   private final float speed;
   private long breedTime;

   public BreedTask(EntityType targetType, float speed) {
      super(ImmutableMap.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), 110);
      this.targetType = targetType;
      this.speed = speed;
   }

   protected boolean shouldRun(ServerWorld arg, AnimalEntity arg2) {
      return arg2.isInLove() && this.findBreedTarget(arg2).isPresent();
   }

   protected void run(ServerWorld arg, AnimalEntity arg2, long l) {
      AnimalEntity lv = (AnimalEntity)this.findBreedTarget(arg2).get();
      arg2.getBrain().remember(MemoryModuleType.BREED_TARGET, (Object)lv);
      lv.getBrain().remember(MemoryModuleType.BREED_TARGET, (Object)arg2);
      LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, this.speed);
      int i = 60 + arg2.getRandom().nextInt(50);
      this.breedTime = l + (long)i;
   }

   protected boolean shouldKeepRunning(ServerWorld arg, AnimalEntity arg2, long l) {
      if (!this.hasBreedTarget(arg2)) {
         return false;
      } else {
         AnimalEntity lv = this.getBreedTarget(arg2);
         return lv.isAlive() && arg2.canBreedWith(lv) && LookTargetUtil.canSee(arg2.getBrain(), lv) && l <= this.breedTime;
      }
   }

   protected void keepRunning(ServerWorld arg, AnimalEntity arg2, long l) {
      AnimalEntity lv = this.getBreedTarget(arg2);
      LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, this.speed);
      if (arg2.isInRange(lv, 3.0)) {
         if (l >= this.breedTime) {
            arg2.breed(arg, lv);
            arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
            lv.getBrain().forget(MemoryModuleType.BREED_TARGET);
         }

      }
   }

   protected void finishRunning(ServerWorld arg, AnimalEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
      arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
      arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
      this.breedTime = 0L;
   }

   private AnimalEntity getBreedTarget(AnimalEntity animal) {
      return (AnimalEntity)animal.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
   }

   private boolean hasBreedTarget(AnimalEntity animal) {
      Brain lv = animal.getBrain();
      return lv.hasMemoryModule(MemoryModuleType.BREED_TARGET) && ((PassiveEntity)lv.getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get()).getType() == this.targetType;
   }

   private Optional findBreedTarget(AnimalEntity animal) {
      Optional var10000 = ((LivingTargetCache)animal.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).get()).findFirst((entity) -> {
         boolean var10000;
         if (entity.getType() == this.targetType && entity instanceof AnimalEntity lv) {
            if (animal.canBreedWith(lv)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      Objects.requireNonNull(AnimalEntity.class);
      return var10000.map(AnimalEntity.class::cast);
   }

   // $FF: synthetic method
   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.shouldKeepRunning(world, (AnimalEntity)entity, time);
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (AnimalEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (AnimalEntity)entity, time);
   }
}
