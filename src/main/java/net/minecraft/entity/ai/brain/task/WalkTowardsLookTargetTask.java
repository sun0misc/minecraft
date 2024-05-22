/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class WalkTowardsLookTargetTask {
    public static Task<LivingEntity> create(Function<LivingEntity, Optional<LookTarget>> lookTargetFunction, Predicate<LivingEntity> predicate, int completionRange, int searchRange, float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET)).apply(context, (lookTarget, walkTarget) -> (world, entity, time) -> {
            Optional optional = (Optional)lookTargetFunction.apply(entity);
            if (optional.isEmpty() || !predicate.test(entity)) {
                return false;
            }
            LookTarget lv = (LookTarget)optional.get();
            if (entity.getPos().isInRange(lv.getPos(), searchRange)) {
                return false;
            }
            LookTarget lv2 = (LookTarget)optional.get();
            lookTarget.remember(lv2);
            walkTarget.remember(new WalkTarget(lv2, speed, completionRange));
            return true;
        }));
    }
}

