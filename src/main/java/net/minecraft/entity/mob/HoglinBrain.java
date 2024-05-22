/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
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
import net.minecraft.entity.mob.HoglinEntity;
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
    private static final int ADULT_MELEE_ATTACK_COOLDOWN = 40;
    private static final int BABY_MELEE_ATTACK_COOLDOWN = 15;
    private static final int field_30540 = 200;
    private static final UniformIntProvider WALK_TOWARD_CLOSEST_ADULT_RANGE = UniformIntProvider.create(5, 16);
    private static final float field_30541 = 1.0f;
    private static final float AVOID_TARGET_SPEED = 1.3f;
    private static final float field_30543 = 0.6f;
    private static final float field_30544 = 0.4f;
    private static final float field_30545 = 0.6f;

    protected static Brain<?> create(Brain<HoglinEntity> brain) {
        HoglinBrain.addCoreTasks(brain);
        HoglinBrain.addIdleTasks(brain);
        HoglinBrain.addFightTasks(brain);
        HoglinBrain.addAvoidTasks(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreTasks(Brain<HoglinEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
    }

    private static void addIdleTasks(Brain<HoglinEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(PacifyTask.create(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6f, 2), GoToRememberedPositionTask.createPosBased(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, true), UpdateAttackTargetTask.create(HoglinBrain::getNearestVisibleTargetablePlayer), TaskTriggerer.runIf(HoglinEntity::isAdult, GoToRememberedPositionTask.createEntityBased(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4f, 8, false)), LookAtMobWithIntervalTask.follow(8.0f, UniformIntProvider.create(30, 60)), WalkTowardClosestAdultTask.create(WALK_TOWARD_CLOSEST_ADULT_RANGE, 0.6f), HoglinBrain.makeRandomWalkTask()));
    }

    private static void addFightTasks(Brain<HoglinEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(PacifyTask.create(MemoryModuleType.NEAREST_REPELLENT, 200), new BreedTask(EntityType.HOGLIN, 0.6f, 2), RangedApproachTask.create(1.0f), TaskTriggerer.runIf(HoglinEntity::isAdult, MeleeAttackTask.create(40)), TaskTriggerer.runIf(PassiveEntity::isBaby, MeleeAttackTask.create(15)), ForgetAttackTargetTask.create(), ForgetTask.create(HoglinBrain::hasBreedTarget, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void addAvoidTasks(Brain<HoglinEntity> brain) {
        brain.setTaskList(Activity.AVOID, 10, ImmutableList.of(GoToRememberedPositionTask.createEntityBased(MemoryModuleType.AVOID_TARGET, 1.3f, 15, false), HoglinBrain.makeRandomWalkTask(), LookAtMobWithIntervalTask.follow(8.0f, UniformIntProvider.create(30, 60)), ForgetTask.create(HoglinBrain::isLoneAdult, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static RandomTask<HoglinEntity> makeRandomWalkTask() {
        return new RandomTask<HoglinEntity>(ImmutableList.of(Pair.of(StrollTask.create(0.4f), 2), Pair.of(GoTowardsLookTargetTask.create(0.4f, 3), 2), Pair.of(new WaitTask(30, 60), 1)));
    }

    protected static void refreshActivities(HoglinEntity hoglin) {
        Brain<HoglinEntity> lv = hoglin.getBrain();
        Activity lv2 = lv.getFirstPossibleNonCoreActivity().orElse(null);
        lv.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity lv3 = lv.getFirstPossibleNonCoreActivity().orElse(null);
        if (lv2 != lv3) {
            HoglinBrain.getSoundEvent(hoglin).ifPresent(hoglin::playSound);
        }
        hoglin.setAttacking(lv.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onAttacking(HoglinEntity hoglin, LivingEntity target) {
        if (hoglin.isBaby()) {
            return;
        }
        if (target.getType() == EntityType.PIGLIN && HoglinBrain.hasMoreHoglinsAround(hoglin)) {
            HoglinBrain.avoid(hoglin, target);
            HoglinBrain.askAdultsToAvoid(hoglin, target);
            return;
        }
        HoglinBrain.askAdultsForHelp(hoglin, target);
    }

    private static void askAdultsToAvoid(HoglinEntity hoglin, LivingEntity target) {
        HoglinBrain.getAdultHoglinsAround(hoglin).forEach(hoglinx -> HoglinBrain.avoidEnemy(hoglinx, target));
    }

    private static void avoidEnemy(HoglinEntity hoglin, LivingEntity target) {
        LivingEntity lv = target;
        Brain<HoglinEntity> lv2 = hoglin.getBrain();
        lv = LookTargetUtil.getCloserEntity((LivingEntity)hoglin, lv2.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET), lv);
        lv = LookTargetUtil.getCloserEntity((LivingEntity)hoglin, lv2.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET), lv);
        HoglinBrain.avoid(hoglin, lv);
    }

    private static void avoid(HoglinEntity hoglin, LivingEntity target) {
        hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        hoglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
        hoglin.getBrain().remember(MemoryModuleType.AVOID_TARGET, target, AVOID_MEMORY_DURATION.get(hoglin.getWorld().random));
    }

    private static Optional<? extends LivingEntity> getNearestVisibleTargetablePlayer(HoglinEntity hoglin) {
        if (HoglinBrain.isNearPlayer(hoglin) || HoglinBrain.hasBreedTarget(hoglin)) {
            return Optional.empty();
        }
        return hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    static boolean isWarpedFungusAround(HoglinEntity hoglin, BlockPos pos) {
        Optional<BlockPos> optional = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_REPELLENT);
        return optional.isPresent() && optional.get().isWithinDistance(pos, 8.0);
    }

    private static boolean isLoneAdult(HoglinEntity hoglin) {
        return hoglin.isAdult() && !HoglinBrain.hasMoreHoglinsAround(hoglin);
    }

    private static boolean hasMoreHoglinsAround(HoglinEntity hoglin) {
        int j;
        if (hoglin.isBaby()) {
            return false;
        }
        int i = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
        return i > (j = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1);
    }

    protected static void onAttacked(HoglinEntity hoglin, LivingEntity attacker) {
        Brain<HoglinEntity> lv = hoglin.getBrain();
        lv.forget(MemoryModuleType.PACIFIED);
        lv.forget(MemoryModuleType.BREED_TARGET);
        if (hoglin.isBaby()) {
            HoglinBrain.avoidEnemy(hoglin, attacker);
            return;
        }
        HoglinBrain.targetEnemy(hoglin, attacker);
    }

    private static void targetEnemy(HoglinEntity hoglin, LivingEntity target) {
        if (hoglin.getBrain().hasActivity(Activity.AVOID) && target.getType() == EntityType.PIGLIN) {
            return;
        }
        if (target.getType() == EntityType.HOGLIN) {
            return;
        }
        if (LookTargetUtil.isNewTargetTooFar(hoglin, target, 4.0)) {
            return;
        }
        if (!Sensor.testAttackableTargetPredicate(hoglin, target)) {
            return;
        }
        HoglinBrain.setAttackTarget(hoglin, target);
        HoglinBrain.askAdultsForHelp(hoglin, target);
    }

    private static void setAttackTarget(HoglinEntity hoglin, LivingEntity target) {
        Brain<HoglinEntity> lv = hoglin.getBrain();
        lv.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        lv.forget(MemoryModuleType.BREED_TARGET);
        lv.remember(MemoryModuleType.ATTACK_TARGET, target, 200L);
    }

    private static void askAdultsForHelp(HoglinEntity hoglin, LivingEntity target) {
        HoglinBrain.getAdultHoglinsAround(hoglin).forEach(hoglinx -> HoglinBrain.setAttackTargetIfCloser(hoglinx, target));
    }

    private static void setAttackTargetIfCloser(HoglinEntity hoglin, LivingEntity targetCandidate) {
        if (HoglinBrain.isNearPlayer(hoglin)) {
            return;
        }
        Optional<LivingEntity> optional = hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
        LivingEntity lv = LookTargetUtil.getCloserEntity((LivingEntity)hoglin, optional, targetCandidate);
        HoglinBrain.setAttackTarget(hoglin, lv);
    }

    public static Optional<SoundEvent> getSoundEvent(HoglinEntity hoglin) {
        return hoglin.getBrain().getFirstPossibleNonCoreActivity().map(activity -> HoglinBrain.getSoundEvent(hoglin, activity));
    }

    private static SoundEvent getSoundEvent(HoglinEntity hoglin, Activity activity) {
        if (activity == Activity.AVOID || hoglin.canConvert()) {
            return SoundEvents.ENTITY_HOGLIN_RETREAT;
        }
        if (activity == Activity.FIGHT) {
            return SoundEvents.ENTITY_HOGLIN_ANGRY;
        }
        if (HoglinBrain.hasNearestRepellent(hoglin)) {
            return SoundEvents.ENTITY_HOGLIN_RETREAT;
        }
        return SoundEvents.ENTITY_HOGLIN_AMBIENT;
    }

    private static List<HoglinEntity> getAdultHoglinsAround(HoglinEntity hoglin) {
        return hoglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
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

