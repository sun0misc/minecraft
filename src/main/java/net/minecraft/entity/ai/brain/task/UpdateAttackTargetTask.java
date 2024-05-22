/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;

public class UpdateAttackTargetTask {
    public static <E extends MobEntity> Task<E> create(Function<E, Optional<? extends LivingEntity>> targetGetter) {
        return UpdateAttackTargetTask.create(entity -> true, targetGetter);
    }

    public static <E extends MobEntity> Task<E> create(Predicate<E> startCondition, Function<E, Optional<? extends LivingEntity>> targetGetter) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> (world, entity, time) -> {
            if (!startCondition.test(entity)) {
                return false;
            }
            Optional optional = (Optional)targetGetter.apply(entity);
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity lv = (LivingEntity)optional.get();
            if (!entity.canTarget(lv)) {
                return false;
            }
            attackTarget.remember(lv);
            cantReachWalkTargetSince.forget();
            return true;
        }));
    }
}

