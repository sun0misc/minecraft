/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class WalkTowardClosestAdultTask {
    public static SingleTickTask<PassiveEntity> create(UniformIntProvider executionRange, float speed) {
        return WalkTowardClosestAdultTask.create(executionRange, entity -> Float.valueOf(speed));
    }

    public static SingleTickTask<PassiveEntity> create(UniformIntProvider executionRange, Function<LivingEntity, Float> speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ADULT), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (nearestVisibleAdult, lookTarget, walkTarget) -> (world, entity, time) -> {
            if (!entity.isBaby()) {
                return false;
            }
            PassiveEntity lv = (PassiveEntity)context.getValue(nearestVisibleAdult);
            if (entity.isInRange(lv, executionRange.getMax() + 1) && !entity.isInRange(lv, executionRange.getMin())) {
                WalkTarget lv2 = new WalkTarget(new EntityLookTarget(lv, false), ((Float)speed.apply(entity)).floatValue(), executionRange.getMin() - 1);
                lookTarget.remember(new EntityLookTarget(lv, true));
                walkTarget.remember(lv2);
                return true;
            }
            return false;
        }));
    }
}

