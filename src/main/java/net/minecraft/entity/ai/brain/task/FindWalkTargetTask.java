/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

public class FindWalkTargetTask {
    private static final int DEFAULT_HORIZONTAL_RANGE = 10;
    private static final int DEFAULT_VERTICAL_RANGE = 7;

    public static SingleTickTask<PathAwareEntity> create(float walkSpeed) {
        return FindWalkTargetTask.create(walkSpeed, 10, 7);
    }

    public static SingleTickTask<PathAwareEntity> create(float walkSpeed, int horizontalRange, int verticalRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, walkTarget -> (world, entity, time) -> {
            ChunkSectionPos lv3;
            ChunkSectionPos lv4;
            BlockPos lv = entity.getBlockPos();
            Vec3d lv2 = world.isNearOccupiedPointOfInterest(lv) ? FuzzyTargeting.find(entity, horizontalRange, verticalRange) : ((lv4 = LookTargetUtil.getPosClosestToOccupiedPointOfInterest(world, lv3 = ChunkSectionPos.from(lv), 2)) != lv3 ? NoPenaltyTargeting.findTo(entity, horizontalRange, verticalRange, Vec3d.ofBottomCenter(lv4.getCenterPos()), 1.5707963705062866) : FuzzyTargeting.find(entity, horizontalRange, verticalRange));
            walkTarget.remember(Optional.ofNullable(lv2).map(pos -> new WalkTarget((Vec3d)pos, walkSpeed, 0)));
            return true;
        }));
    }
}

