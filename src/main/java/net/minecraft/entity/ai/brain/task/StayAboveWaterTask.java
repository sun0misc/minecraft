/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;

public class StayAboveWaterTask
extends MultiTickTask<MobEntity> {
    private final float chance;

    public StayAboveWaterTask(float chance) {
        super(ImmutableMap.of());
        this.chance = chance;
    }

    public static boolean isUnderwater(MobEntity entity) {
        return entity.isTouchingWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getSwimHeight() || entity.isInLava();
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
        return StayAboveWaterTask.isUnderwater(arg2);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
        return this.shouldRun(arg, arg2);
    }

    @Override
    protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
        if (arg2.getRandom().nextFloat() < this.chance) {
            arg2.getJumpControl().setActive();
        }
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (MobEntity)entity, time);
    }
}

