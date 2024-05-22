/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Collections;
import java.util.List;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

public class WanderIndoorsTask {
    public static Task<PathAwareEntity> create(float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, walkTarget -> (world, entity, time) -> {
            if (world.isSkyVisible(entity.getBlockPos())) {
                return false;
            }
            BlockPos lv = entity.getBlockPos();
            List list = BlockPos.stream(lv.add(-1, -1, -1), lv.add(1, 1, 1)).map(BlockPos::toImmutable).collect(Util.toArrayList());
            Collections.shuffle(list);
            list.stream().filter(pos -> !world.isSkyVisible((BlockPos)pos)).filter(pos -> world.isTopSolid((BlockPos)pos, entity)).filter(pos -> world.isSpaceEmpty(entity)).findFirst().ifPresent(pos -> walkTarget.remember(new WalkTarget((BlockPos)pos, speed, 0)));
            return true;
        }));
    }
}

