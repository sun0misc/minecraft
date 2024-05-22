/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.EntityStatuses;
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
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TemptationCooldownTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ArmadilloBrain {
    private static final float field_47797 = 2.0f;
    private static final float field_47798 = 1.0f;
    private static final float field_47799 = 1.25f;
    private static final float field_47800 = 1.25f;
    private static final float field_47801 = 1.0f;
    private static final double field_48338 = 2.0;
    private static final double field_48339 = 1.0;
    private static final UniformIntProvider WALK_TOWARDS_CLOSEST_ADULT_RANGE = UniformIntProvider.create(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super ArmadilloEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.ARMADILLO_TEMPTATIONS, SensorType.NEAREST_ADULT, SensorType.ARMADILLO_SCARE_DETECTED);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULE_TYPES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.DANGER_DETECTED_RECENTLY});
    private static final SingleTickTask<ArmadilloEntity> UNROLL_TASK = TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.DANGER_DETECTED_RECENTLY)).apply(context, arg2 -> (arg, armadillo, l) -> {
        if (armadillo.isNotIdle()) {
            armadillo.unroll();
            return true;
        }
        return false;
    }));

    public static Brain.Profile<ArmadilloEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULE_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> create(Brain<ArmadilloEntity> brain) {
        ArmadilloBrain.addCoreActivities(brain);
        ArmadilloBrain.addIdleActivities(brain);
        ArmadilloBrain.addPanicActivities(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<ArmadilloEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new UnrollAndFleeTask(2.0f), new LookAroundTask(45, 90), new WanderAroundTask(){

            @Override
            protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
                ArmadilloEntity lv;
                if (arg2 instanceof ArmadilloEntity && (lv = (ArmadilloEntity)arg2).isNotIdle()) {
                    return false;
                }
                return super.shouldRun(arg, arg2);
            }
        }, new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new TemptationCooldownTask(MemoryModuleType.GAZE_COOLDOWN_TICKS), UNROLL_TASK));
    }

    private static void addIdleActivities(Brain<ArmadilloEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), Pair.of(1, new BreedTask(EntityType.ARMADILLO, 1.0f, 1)), Pair.of(2, new RandomTask(ImmutableList.of(Pair.of(new TemptTask(armadillo -> Float.valueOf(1.25f), armadillo -> armadillo.isBaby() ? 1.0 : 2.0), 1), Pair.of(WalkTowardClosestAdultTask.create(WALK_TOWARDS_CLOSEST_ADULT_RANGE, 1.25f), 1)))), Pair.of(3, new RandomLookAroundTask(UniformIntProvider.create(150, 250), 30.0f, 0.0f, 0.0f)), Pair.of(4, new RandomTask(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(Pair.of(StrollTask.create(1.0f), 1), Pair.of(GoTowardsLookTargetTask.create(1.0f, 3), 1), Pair.of(new WaitTask(30, 60), 1))))));
    }

    private static void addPanicActivities(Brain<ArmadilloEntity> brain) {
        brain.setTaskList(Activity.PANIC, ImmutableList.of(Pair.of(0, new RollUpTask())), Set.of(Pair.of(MemoryModuleType.DANGER_DETECTED_RECENTLY, MemoryModuleState.VALUE_PRESENT), Pair.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT)));
    }

    public static void updateActivities(ArmadilloEntity armadillo) {
        armadillo.getBrain().resetPossibleActivities(ImmutableList.of(Activity.PANIC, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptItemPredicate() {
        return stack -> stack.isIn(ItemTags.ARMADILLO_FOOD);
    }

    public static class UnrollAndFleeTask
    extends FleeTask<ArmadilloEntity> {
        public UnrollAndFleeTask(float f) {
            super(f, arg -> DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
        }

        @Override
        protected void run(ServerWorld arg, ArmadilloEntity arg2, long l) {
            arg2.unroll();
            super.run(arg, arg2, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld arg, PathAwareEntity arg2, long l) {
            this.run(arg, (ArmadilloEntity)arg2, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (ArmadilloEntity)entity, time);
        }
    }

    public static class RollUpTask
    extends MultiTickTask<ArmadilloEntity> {
        static final int RUN_TIME_IN_TICKS = 5 * TimeHelper.MINUTE_IN_SECONDS * 20;
        static final int field_49088 = 5;
        static final int field_49089 = 75;
        int ticksUntilPeek = 0;
        boolean considerPeeking;

        public RollUpTask() {
            super(Map.of(), RUN_TIME_IN_TICKS);
        }

        @Override
        protected void keepRunning(ServerWorld arg, ArmadilloEntity arg2, long l) {
            boolean bl;
            super.keepRunning(arg, arg2, l);
            if (this.ticksUntilPeek > 0) {
                --this.ticksUntilPeek;
            }
            if (arg2.shouldSwitchToScaredState()) {
                arg2.setState(ArmadilloEntity.State.SCARED);
                if (arg2.isOnGround()) {
                    arg2.playSoundIfNotSilent(SoundEvents.ENTITY_ARMADILLO_LAND);
                }
                return;
            }
            ArmadilloEntity.State lv = arg2.getState();
            long m = arg2.getBrain().getMemoryExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY);
            boolean bl2 = bl = m > 75L;
            if (bl != this.considerPeeking) {
                this.ticksUntilPeek = this.calculateTicksUntilPeek(arg2);
            }
            this.considerPeeking = bl;
            if (lv == ArmadilloEntity.State.SCARED) {
                if (this.ticksUntilPeek == 0 && arg2.isOnGround() && bl) {
                    arg.sendEntityStatus(arg2, EntityStatuses.PEEKING);
                    this.ticksUntilPeek = this.calculateTicksUntilPeek(arg2);
                }
                if (m < (long)ArmadilloEntity.State.UNROLLING.getLengthInTicks()) {
                    arg2.playSoundIfNotSilent(SoundEvents.ENTITY_ARMADILLO_UNROLL_START);
                    arg2.setState(ArmadilloEntity.State.UNROLLING);
                }
            } else if (lv == ArmadilloEntity.State.UNROLLING && m > (long)ArmadilloEntity.State.UNROLLING.getLengthInTicks()) {
                arg2.setState(ArmadilloEntity.State.SCARED);
            }
        }

        private int calculateTicksUntilPeek(ArmadilloEntity entity) {
            return ArmadilloEntity.State.SCARED.getLengthInTicks() + entity.getRandom().nextBetween(100, 400);
        }

        @Override
        protected boolean shouldRun(ServerWorld arg, ArmadilloEntity arg2) {
            return arg2.isOnGround();
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld arg, ArmadilloEntity arg2, long l) {
            return arg2.getState().shouldRunRollUpTask();
        }

        @Override
        protected void run(ServerWorld arg, ArmadilloEntity arg2, long l) {
            arg2.startRolling();
        }

        @Override
        protected void finishRunning(ServerWorld arg, ArmadilloEntity arg2, long l) {
            if (!arg2.canRollUp()) {
                arg2.unroll();
            }
        }

        @Override
        protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
            return this.shouldKeepRunning(world, (ArmadilloEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
            this.finishRunning(world, (ArmadilloEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
            this.keepRunning(world, (ArmadilloEntity)entity, time);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (ArmadilloEntity)entity, time);
        }
    }
}

