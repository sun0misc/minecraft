/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class WalkTowardsPosTask {
    private static BlockPos fuzz(MobEntity mob, BlockPos pos) {
        Random lv = mob.getWorld().random;
        return pos.add(WalkTowardsPosTask.fuzz(lv), 0, WalkTowardsPosTask.fuzz(lv));
    }

    private static int fuzz(Random random) {
        return random.nextInt(3) - 1;
    }

    public static <E extends MobEntity> SingleTickTask<E> create(MemoryModuleType<BlockPos> posModule, int completionRange, float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(posModule), context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (pos, attackTarget, walkTarget, lookTarget) -> (world, entity, time) -> {
            BlockPos lv = (BlockPos)context.getValue(pos);
            boolean bl = lv.isWithinDistance(entity.getBlockPos(), (double)completionRange);
            if (!bl) {
                LookTargetUtil.walkTowards(entity, WalkTowardsPosTask.fuzz(entity, lv), speed, completionRange);
            }
            return true;
        }));
    }
}

