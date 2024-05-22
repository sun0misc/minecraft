/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;

public class ForgetAttackTargetTask {
    private static final int REMEMBER_TIME = 200;

    public static <E extends MobEntity> Task<E> create(BiConsumer<E, LivingEntity> forgetCallback) {
        return ForgetAttackTargetTask.create(entity -> false, forgetCallback, true);
    }

    public static <E extends MobEntity> Task<E> create(Predicate<LivingEntity> alternativeCondition) {
        return ForgetAttackTargetTask.create(alternativeCondition, (entity, target) -> {}, true);
    }

    public static <E extends MobEntity> Task<E> create() {
        return ForgetAttackTargetTask.create(entity -> false, (entity, target) -> {}, true);
    }

    public static <E extends MobEntity> Task<E> create(Predicate<LivingEntity> alternativeCondition, BiConsumer<E, LivingEntity> forgetCallback, boolean shouldForgetIfTargetUnreachable) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> (world, entity, time) -> {
            LivingEntity lv = (LivingEntity)context.getValue(attackTarget);
            if (!entity.canTarget(lv) || shouldForgetIfTargetUnreachable && ForgetAttackTargetTask.cannotReachTarget(entity, context.getOptionalValue(cantReachWalkTargetSince)) || !lv.isAlive() || lv.getWorld() != entity.getWorld() || alternativeCondition.test(lv)) {
                forgetCallback.accept(entity, lv);
                attackTarget.forget();
                return true;
            }
            return true;
        }));
    }

    private static boolean cannotReachTarget(LivingEntity arg, Optional<Long> optional) {
        return optional.isPresent() && arg.getWorld().getTime() - optional.get() > 200L;
    }
}

