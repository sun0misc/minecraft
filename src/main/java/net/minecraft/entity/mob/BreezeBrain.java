/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.BreezeJumpTask;
import net.minecraft.entity.ai.brain.task.BreezeShootIfStuckTask;
import net.minecraft.entity.ai.brain.task.BreezeShootTask;
import net.minecraft.entity.ai.brain.task.BreezeSlideTowardsTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;

public class BreezeBrain {
    public static final float field_47283 = 0.6f;
    public static final float field_47284 = 4.0f;
    public static final float field_47285 = 8.0f;
    public static final float field_47286 = 20.0f;
    static final List<SensorType<? extends Sensor<? super BreezeEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.BREEZE_ATTACK_ENTITY_SENSOR);
    static final List<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleType.BREEZE_SHOOT, MemoryModuleType.BREEZE_SHOOT_CHARGING, MemoryModuleType.BREEZE_SHOOT_RECOVER, MemoryModuleType.BREEZE_SHOOT_COOLDOWN, new MemoryModuleType[]{MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PATH});

    protected static Brain<?> create(BreezeEntity arg, Brain<BreezeEntity> arg2) {
        BreezeBrain.addCoreTasks(arg2);
        BreezeBrain.addIdleTasks(arg2);
        BreezeBrain.addFightTasks(arg, arg2);
        arg2.setCoreActivities(Set.of(Activity.CORE));
        arg2.setDefaultActivity(Activity.FIGHT);
        arg2.resetPossibleActivities();
        return arg2;
    }

    private static void addCoreTasks(Brain<BreezeEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new LookAroundTask(45, 90)));
    }

    private static void addIdleTasks(Brain<BreezeEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, UpdateAttackTargetTask.create(breeze -> breeze.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE))), Pair.of(1, UpdateAttackTargetTask.create(BreezeEntity::getHurtBy)), Pair.of(2, new SlideAroundTask(20, 40)), Pair.of(3, new RandomTask(ImmutableList.of(Pair.of(new WaitTask(20, 100), 1), Pair.of(StrollTask.create(0.6f), 2))))));
    }

    private static void addFightTasks(BreezeEntity arg, Brain<BreezeEntity> arg22) {
        arg22.setTaskList(Activity.FIGHT, ImmutableList.of(Pair.of(0, ForgetAttackTargetTask.create(arg2 -> !Sensor.testAttackableTargetPredicate(arg, arg2))), Pair.of(1, new BreezeShootTask()), Pair.of(2, new BreezeJumpTask()), Pair.of(3, new BreezeShootIfStuckTask()), Pair.of(4, new BreezeSlideTowardsTargetTask())), ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT)));
    }

    static void updateActivities(BreezeEntity breeze) {
        breeze.getBrain().resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    public static class SlideAroundTask
    extends WanderAroundTask {
        @VisibleForTesting
        public SlideAroundTask(int i, int j) {
            super(i, j);
        }

        @Override
        protected void run(ServerWorld arg, MobEntity arg2, long l) {
            super.run(arg, arg2, l);
            arg2.playSoundIfNotSilent(SoundEvents.ENTITY_BREEZE_SLIDE);
            arg2.setPose(EntityPose.SLIDING);
        }

        @Override
        protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
            super.finishRunning(arg, arg2, l);
            arg2.setPose(EntityPose.STANDING);
            if (arg2.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
                arg2.getBrain().remember(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
            }
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (MobEntity)entity, time);
        }
    }
}

