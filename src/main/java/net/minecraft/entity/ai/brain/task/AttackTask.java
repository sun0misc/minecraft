/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class AttackTask {
    public static SingleTickTask<MobEntity> create(int distance, float forwardMovement) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)).apply(context, (walkTarget, lookTarget, attackTarget, visibleMobs) -> (world, entity, time) -> {
            LivingEntity lv = (LivingEntity)context.getValue(attackTarget);
            if (lv.isInRange(entity, distance) && ((LivingTargetCache)context.getValue(visibleMobs)).contains(lv)) {
                lookTarget.remember(new EntityLookTarget(lv, true));
                entity.getMoveControl().strafeTo(-forwardMovement, 0.0f);
                entity.setYaw(MathHelper.clampAngle(entity.getYaw(), entity.headYaw, 0.0f));
                return true;
            }
            return false;
        }));
    }
}

