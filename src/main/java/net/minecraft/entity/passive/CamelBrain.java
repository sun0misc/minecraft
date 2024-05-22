/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.RandomLookAroundTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TemptationCooldownTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class CamelBrain {
    private static final float WALK_SPEED = 4.0f;
    private static final float field_40153 = 2.0f;
    private static final float field_40154 = 2.5f;
    private static final float field_40155 = 2.5f;
    private static final float BREED_SPEED = 1.0f;
    private static final UniformIntProvider WALK_TOWARD_ADULT_RANGE = UniformIntProvider.create(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super CamelEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.CAMEL_TEMPTATIONS, SensorType.NEAREST_ADULT);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT});

    protected static void initialize(CamelEntity camel, Random random) {
    }

    public static Brain.Profile<CamelEntity> createProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected static Brain<?> create(Brain<CamelEntity> brain) {
        CamelBrain.addCoreActivities(brain);
        CamelBrain.addIdleActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<CamelEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new CamelWalkTask(4.0f), new LookAroundTask(45, 90), new WanderAroundTask(), new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new TemptationCooldownTask(MemoryModuleType.GAZE_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<CamelEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), Pair.of(1, new BreedTask(EntityType.CAMEL)), Pair.of(2, new RandomTask(ImmutableList.of(Pair.of(new TemptTask(entity -> Float.valueOf(2.5f), entity -> entity.isBaby() ? 2.5 : 3.5), 1), Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), WalkTowardClosestAdultTask.create(WALK_TOWARD_ADULT_RANGE, 2.5f)), 1)))), Pair.of(3, new RandomLookAroundTask(UniformIntProvider.create(150, 250), 30.0f, 0.0f, 0.0f)), Pair.of(4, new RandomTask(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), StrollTask.create(2.0f)), 1), Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), GoTowardsLookTargetTask.create(2.0f, 3)), 1), Pair.of(new SitOrStandTask(20), 1), Pair.of(new WaitTask(30, 60), 1))))));
    }

    public static void updateActivities(CamelEntity camel) {
        camel.getBrain().resetPossibleActivities(ImmutableList.of(Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptItemPredicate() {
        return stack -> stack.isIn(ItemTags.CAMEL_FOOD);
    }

    public static class CamelWalkTask
    extends FleeTask<CamelEntity> {
        public CamelWalkTask(float f) {
            super(f);
        }

        @Override
        protected void run(ServerWorld arg, CamelEntity arg2, long l) {
            arg2.setStanding();
            super.run(arg, arg2, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld arg, PathAwareEntity arg2, long l) {
            this.run(arg, (CamelEntity)arg2, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (CamelEntity)entity, time);
        }
    }

    public static class SitOrStandTask
    extends MultiTickTask<CamelEntity> {
        private final int lastPoseTickDelta;

        public SitOrStandTask(int lastPoseSecondsDelta) {
            super(ImmutableMap.of());
            this.lastPoseTickDelta = lastPoseSecondsDelta * 20;
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, CamelEntity arg2) {
            return !arg2.isTouchingWater() && arg2.getLastPoseTickDelta() >= (long)this.lastPoseTickDelta && !arg2.isLeashed() && arg2.isOnGround() && !arg2.hasControllingPassenger() && arg2.canChangePose();
        }

        @Override
        protected void run(ServerWorld arg, CamelEntity arg2, long l) {
            if (arg2.isSitting()) {
                arg2.startStanding();
            } else if (!arg2.isPanicking()) {
                arg2.startSitting();
            }
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (CamelEntity)entity, time);
        }
    }
}

