/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class TemptTask
extends MultiTickTask<PathAwareEntity> {
    public static final int TEMPTATION_COOLDOWN_TICKS = 100;
    public static final double DEFAULT_STOP_DISTANCE = 2.5;
    public static final double LARGE_ENTITY_STOP_DISTANCE = 3.5;
    private final Function<LivingEntity, Float> speed;
    private final Function<LivingEntity, Double> stopDistanceGetter;

    public TemptTask(Function<LivingEntity, Float> speed) {
        this(speed, entity -> 2.5);
    }

    public TemptTask(Function<LivingEntity, Float> speed, Function<LivingEntity, Double> stopDistanceGetter) {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<Object>, MemoryModuleState> builder = ImmutableMap.builder();
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
        this.stopDistanceGetter = stopDistanceGetter;
    }

    protected float getSpeed(PathAwareEntity entity) {
        return this.speed.apply(entity).floatValue();
    }

    private Optional<PlayerEntity> getTemptingPlayer(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        return this.getTemptingPlayer(arg2).isPresent() && !arg2.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET) && !arg2.getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
        arg2.getBrain().remember(MemoryModuleType.IS_TEMPTED, true);
    }

    @Override
    protected void finishRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        Brain<?> lv = arg2.getBrain();
        lv.remember(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        lv.remember(MemoryModuleType.IS_TEMPTED, false);
        lv.forget(MemoryModuleType.WALK_TARGET);
        lv.forget(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void keepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        PlayerEntity lv = this.getTemptingPlayer(arg2).get();
        Brain<?> lv2 = arg2.getBrain();
        lv2.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(lv, true));
        double d = this.stopDistanceGetter.apply(arg2);
        if (arg2.squaredDistanceTo(lv) < MathHelper.square(d)) {
            lv2.forget(MemoryModuleType.WALK_TARGET);
        } else {
            lv2.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(lv, false), this.getSpeed(arg2), 2));
        }
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }
}

