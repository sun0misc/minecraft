package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.PlayDeadTask;
import net.minecraft.entity.ai.brain.task.PlayDeadTimerTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.SeekWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TemptationCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class AxolotlBrain {
   private static final UniformIntProvider WALK_TOWARD_ADULT_RANGE = UniformIntProvider.create(5, 16);
   private static final float BREEDING_SPEED = 0.2F;
   private static final float ON_LAND_SPEED = 0.15F;
   private static final float IDLE_SPEED = 0.5F;
   private static final float TARGET_APPROACHING_SPEED = 0.6F;
   private static final float ADULT_FOLLOWING_SPEED = 0.6F;

   protected static Brain create(Brain brain) {
      addCoreActivities(brain);
      addIdleActivities(brain);
      addFightActivities(brain);
      addPlayDeadActivities(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   private static void addPlayDeadActivities(Brain brain) {
      brain.setTaskList(Activity.PLAY_DEAD, ImmutableList.of(Pair.of(0, new PlayDeadTask()), Pair.of(1, ForgetTask.create(LookTargetUtil::hasBreedTarget, MemoryModuleType.PLAY_DEAD_TICKS))), ImmutableSet.of(Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleState.VALUE_PRESENT)), ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS));
   }

   private static void addFightActivities(Brain brain) {
      brain.setTaskList(Activity.FIGHT, 0, ImmutableList.of(ForgetAttackTargetTask.create(AxolotlEntity::appreciatePlayer), RangedApproachTask.create(AxolotlBrain::getTargetApproachingSpeed), MeleeAttackTask.create(20), ForgetTask.create(LookTargetUtil::hasBreedTarget, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void addCoreActivities(Brain brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask(), PlayDeadTimerTask.create(), new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
   }

   private static void addIdleActivities(Brain brain) {
      brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0F, UniformIntProvider.create(30, 60))), Pair.of(1, new BreedTask(EntityType.AXOLOTL, 0.2F)), Pair.of(2, new RandomTask(ImmutableList.of(Pair.of(new TemptTask(AxolotlBrain::getTemptedSpeed), 1), Pair.of(WalkTowardClosestAdultTask.create(WALK_TOWARD_ADULT_RANGE, AxolotlBrain::getAdultFollowingSpeed), 1)))), Pair.of(3, UpdateAttackTargetTask.create(AxolotlBrain::getAttackTarget)), Pair.of(3, SeekWaterTask.create(6, 0.15F)), Pair.of(4, new CompositeTask(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), ImmutableSet.of(), CompositeTask.Order.ORDERED, CompositeTask.RunMode.TRY_ALL, ImmutableList.of(Pair.of(StrollTask.createDynamicRadius(0.5F), 2), Pair.of(StrollTask.create(0.15F, false), 2), Pair.of(GoTowardsLookTargetTask.create(AxolotlBrain::canGoToLookTarget, AxolotlBrain::getTemptedSpeed, 3), 3), Pair.of(TaskTriggerer.predicate(Entity::isInsideWaterOrBubbleColumn), 5), Pair.of(TaskTriggerer.predicate(Entity::isOnGround), 5))))));
   }

   private static boolean canGoToLookTarget(LivingEntity entity) {
      World lv = entity.world;
      Optional optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LOOK_TARGET);
      if (optional.isPresent()) {
         BlockPos lv2 = ((LookTarget)optional.get()).getBlockPos();
         return lv.isWater(lv2) == entity.isInsideWaterOrBubbleColumn();
      } else {
         return false;
      }
   }

   public static void updateActivities(AxolotlEntity axolotl) {
      Brain lv = axolotl.getBrain();
      Activity lv2 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (lv2 != Activity.PLAY_DEAD) {
         lv.resetPossibleActivities((List)ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, Activity.IDLE));
         if (lv2 == Activity.FIGHT && lv.getFirstPossibleNonCoreActivity().orElse((Object)null) != Activity.FIGHT) {
            lv.remember(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
         }
      }

   }

   private static float getTargetApproachingSpeed(LivingEntity entity) {
      return entity.isInsideWaterOrBubbleColumn() ? 0.6F : 0.15F;
   }

   private static float getAdultFollowingSpeed(LivingEntity entity) {
      return entity.isInsideWaterOrBubbleColumn() ? 0.6F : 0.15F;
   }

   private static float getTemptedSpeed(LivingEntity entity) {
      return entity.isInsideWaterOrBubbleColumn() ? 0.5F : 0.15F;
   }

   private static Optional getAttackTarget(AxolotlEntity axolotl) {
      return LookTargetUtil.hasBreedTarget(axolotl) ? Optional.empty() : axolotl.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE);
   }

   public static Ingredient getTemptItems() {
      return Ingredient.fromTag(ItemTags.AXOLOTL_TEMPT_ITEMS);
   }
}
