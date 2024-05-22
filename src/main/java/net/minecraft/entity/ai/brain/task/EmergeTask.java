/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class EmergeTask<E extends WardenEntity>
extends MultiTickTask<E> {
    public EmergeTask(int duration) {
        super(ImmutableMap.of(MemoryModuleType.IS_EMERGING, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), duration);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, E arg2, long l) {
        return true;
    }

    @Override
    protected void run(ServerWorld arg, E arg2, long l) {
        ((Entity)arg2).setPose(EntityPose.EMERGING);
        ((Entity)arg2).playSound(SoundEvents.ENTITY_WARDEN_EMERGE, 5.0f, 1.0f);
    }

    @Override
    protected void finishRunning(ServerWorld arg, E arg2, long l) {
        if (((Entity)arg2).isInPose(EntityPose.EMERGING)) {
            ((Entity)arg2).setPose(EntityPose.STANDING);
        }
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (E)((WardenEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (E)((WardenEntity)entity), time);
    }
}

