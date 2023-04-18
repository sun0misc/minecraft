package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetTask;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.PacifyTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class HoglinBrain {
   public static final int field_30533 = 8;
   public static final int field_30534 = 4;
   private static final UniformIntProvider AVOID_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 20);
   private static final int field_30535 = 200;
   private static final int field_30536 = 8;
   private static final int field_30537 = 15;
   private static final int field_30538 = 40;
   private static final int field_30539 = 15;
   private static final int field_30540 = 200;
   private static final UniformIntProvider WALK_TOWARD_CLOSEST_ADULT_RANGE = UniformIntProvider.create(5, 16);
   private static final float field_30541 = 1.0F;
   private static final float field_30542 = 1.3F;
   private static final float field_30543 = 0.6F;
   private static final float field_30544 = 0.4F;
   private static final float field_30545 = 0.6F;

   protected static Brain create(Brain brain) {
      addCoreTasks(brain);
      addIdleTasks(brain);
      addFightTasks(brain);
      addAvoidTasks(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   private static void addCoreTasks(Brain brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
   }

   private static void addIdleTasks(Brain brain) {
      brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(PacifyTask.create(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6F), GoToRememberedPositionTask.createPosBased(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true), UpdateAttackTargetTask.create(HoglinBrain::getNearestVisibleTargetablePlayer), TaskTriggerer.runIf(HoglinEntity::isAdult, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)), LookAtMobWithIntervalTask.follow(8.0F, UniformIntProvider.create(30, 60)), WalkTowardClosestAdultTask.create(WALK_TOWARD_CLOSEST_ADULT_RANGE, 0.6F), makeRandomWalkTask()));
   }

   private static void addFightTasks(Brain brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(PacifyTask.create(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6F), RangedApproachTask.create(1.0F), TaskTriggerer.runIf(HoglinEntity::isAdult, MeleeAttackTask.create(40)), TaskTriggerer.runIf(PassiveEntity::isBaby, MeleeAttackTask.create(15)), ForgetAttackTargetTask.create(), ForgetTask.create(HoglinBrain::hasBreedTarget, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void addAvoidTasks(Brain brain) {
      brain.setTaskList(Activity.AVOID, 10, ImmutableList.of(GoToRememberedPositionTask.createEntityBased(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false), makeRandomWalkTask(), LookAtMobWithIntervalTask.follow(8.0F, UniformIntProvider.create(30, 60)), ForgetTask.create(HoglinBrain::isLoneAdult, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static RandomTask makeRandomWalkTask() {
      return new RandomTask(ImmutableList.of(Pair.of(StrollTask.create(0.4F), 2), Pair.of(GoTowardsLookTargetTask.create(0.4F, 3), 2), Pair.of(new WaitTask(30, 60), 1)));
   }

   protected static void refreshActivities(HoglinEntity hoglin) {
      Brain lv = hoglin.getBrain();
      Activity lv2 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      lv.resetPossibleActivities((List)ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
      Activity lv3 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (lv2 != lv3) {
         Optional var10000 = getSoundEvent(hoglin);
         Objects.requireNonNull(hoglin);
         var10000.ifPresent(hoglin::playSound);
      }

      hoglin.setAttacking(lv.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
   }

   protected static void onAttacking(HoglinEntity hoglin, LivingEntity target) {
      if (!hoglin.isBaby()) {
         if (target.getType() == EntityType.PIGLIN && hasMoreHoglinsAround(hoglin)) {
            avoid(hoglin, target);
            askAdultsToAvoid(hoglin, target);
         } else {
            askAdultsForHelp(hoglin, target);
         }
      }
   }

   private static void askAdultsToAvoid(HoglinEntity hoglin, LivingEntity target) {
      getAdultHoglinsAround(hoglin).forEach((hoglinx) -> {
         avoidEnemy(hoglinx, target);
      });
   }

   private static void avoidEnemy(HoglinEntity hoglin, LivingEntity target) {
      Brain lv2 = hoglin.getBrain();
      LivingEntity lv = LookTargetUtil.getCloserEntity(hoglin, (Optional)lv2.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET), target);
      lv = LookTargetUtil.getCloserEntity(hoglin, (Optional)lv2.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET), lv);
      avoid(hoglin, lv);
   }

   private static void avoid(HoglinEntity hoglin, LivingEntity target) {
      hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
      hoglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
      hoglin.getBrain().remember(MemoryModuleType.AVOID_TARGET, target, (long)AVOID_MEMORY_DURATION.get(hoglin.world.random));
   }

   private static Optional getNearestVisibleTargetablePlayer(HoglinEntity hoglin) {
      return !isNearPlayer(hoglin) && !hasBreedTarget(hoglin) ? hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
   }

   static boolean isWarpedFungusAround(HoglinEntity hoglin, BlockPos pos) {
      Optional optional = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_REPELLENT);
      return optional.isPresent() && ((BlockPos)optional.get()).isWithinDistance(pos, 8.0);
   }

   private static boolean isLoneAdult(HoglinEntity hoglin) {
      return hoglin.isAdult() && !hasMoreHoglinsAround(hoglin);
   }

   private static boolean hasMoreHoglinsAround(HoglinEntity hoglin) {
      if (hoglin.isBaby()) {
         return false;
      } else {
         int i = (Integer)hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
         int j = (Integer)hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
         return i > j;
      }
   }

   protected static void onAttacked(HoglinEntity hoglin, LivingEntity attacker) {
      Brain lv = hoglin.getBrain();
      lv.forget(MemoryModuleType.PACIFIED);
      lv.forget(MemoryModuleType.BREED_TARGET);
      if (hoglin.isBaby()) {
         avoidEnemy(hoglin, attacker);
      } else {
         targetEnemy(hoglin, attacker);
      }
   }

   private static void targetEnemy(HoglinEntity hoglin, LivingEntity target) {
      if (!hoglin.getBrain().hasActivity(Activity.AVOID) || target.getType() != EntityType.PIGLIN) {
         if (target.getType() != EntityType.HOGLIN) {
            if (!LookTargetUtil.isNewTargetTooFar(hoglin, target, 4.0)) {
               if (Sensor.testAttackableTargetPredicate(hoglin, target)) {
                  setAttackTarget(hoglin, target);
                  askAdultsForHelp(hoglin, target);
               }
            }
         }
      }
   }

   private static void setAttackTarget(HoglinEntity hoglin, LivingEntity target) {
      Brain lv = hoglin.getBrain();
      lv.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      lv.forget(MemoryModuleType.BREED_TARGET);
      lv.remember(MemoryModuleType.ATTACK_TARGET, target, 200L);
   }

   private static void askAdultsForHelp(HoglinEntity hoglin, LivingEntity target) {
      getAdultHoglinsAround(hoglin).forEach((hoglinx) -> {
         setAttackTargetIfCloser(hoglinx, target);
      });
   }

   private static void setAttackTargetIfCloser(HoglinEntity hoglin, LivingEntity targetCandidate) {
      if (!isNearPlayer(hoglin)) {
         Optional optional = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
         LivingEntity lv = LookTargetUtil.getCloserEntity(hoglin, (Optional)optional, targetCandidate);
         setAttackTarget(hoglin, lv);
      }
   }

   public static Optional getSoundEvent(HoglinEntity hoglin) {
      return hoglin.getBrain().getFirstPossibleNonCoreActivity().map((activity) -> {
         return getSoundEvent(hoglin, activity);
      });
   }

   private static SoundEvent getSoundEvent(HoglinEntity hoglin, Activity activity) {
      if (activity != Activity.AVOID && !hoglin.canConvert()) {
         if (activity == Activity.FIGHT) {
            return SoundEvents.ENTITY_HOGLIN_ANGRY;
         } else {
            return hasNearestRepellent(hoglin) ? SoundEvents.ENTITY_HOGLIN_RETREAT : SoundEvents.ENTITY_HOGLIN_AMBIENT;
         }
      } else {
         return SoundEvents.ENTITY_HOGLIN_RETREAT;
      }
   }

   private static List getAdultHoglinsAround(HoglinEntity hoglin) {
      return (List)hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
   }

   private static boolean hasNearestRepellent(HoglinEntity hoglin) {
      return hoglin.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean hasBreedTarget(HoglinEntity hoglin) {
      return hoglin.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET);
   }

   protected static boolean isNearPlayer(HoglinEntity hoglin) {
      return hoglin.getBrain().hasMemoryModule(MemoryModuleType.PACIFIED);
   }
}
