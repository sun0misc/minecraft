/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.WardenBrain;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;

public class RoarTask
extends MultiTickTask<WardenEntity> {
    private static final int SOUND_DELAY = 25;
    private static final int ANGER_INCREASE = 20;

    public RoarTask() {
        super(ImmutableMap.of(MemoryModuleType.ROAR_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.ROAR_SOUND_COOLDOWN, MemoryModuleState.REGISTERED, MemoryModuleType.ROAR_SOUND_DELAY, MemoryModuleState.REGISTERED), WardenBrain.ROAR_DURATION);
    }

    @Override
    protected void run(ServerWorld arg, WardenEntity arg2, long l) {
        Brain<WardenEntity> lv = arg2.getBrain();
        lv.remember(MemoryModuleType.ROAR_SOUND_DELAY, Unit.INSTANCE, 25L);
        lv.forget(MemoryModuleType.WALK_TARGET);
        LivingEntity lv2 = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ROAR_TARGET).get();
        LookTargetUtil.lookAt(arg2, lv2);
        arg2.setPose(EntityPose.ROARING);
        arg2.increaseAngerAt(lv2, 20, false);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
        return true;
    }

    @Override
    protected void keepRunning(ServerWorld arg, WardenEntity arg2, long l) {
        if (arg2.getBrain().hasMemoryModule(MemoryModuleType.ROAR_SOUND_DELAY) || arg2.getBrain().hasMemoryModule(MemoryModuleType.ROAR_SOUND_COOLDOWN)) {
            return;
        }
        arg2.getBrain().remember(MemoryModuleType.ROAR_SOUND_COOLDOWN, Unit.INSTANCE, WardenBrain.ROAR_DURATION - 25);
        arg2.playSound(SoundEvents.ENTITY_WARDEN_ROAR, 3.0f, 1.0f);
    }

    @Override
    protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
        if (arg2.isInPose(EntityPose.ROARING)) {
            arg2.setPose(EntityPose.STANDING);
        }
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ROAR_TARGET).ifPresent(arg2::updateAttackTarget);
        arg2.getBrain().forget(MemoryModuleType.ROAR_TARGET);
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (WardenEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (WardenEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (WardenEntity)entity, time);
    }
}

