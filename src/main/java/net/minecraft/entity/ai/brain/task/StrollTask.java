/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class StrollTask {
    private static final int DEFAULT_HORIZONTAL_RADIUS = 10;
    private static final int DEFAULT_VERTICAL_RADIUS = 7;
    private static final int[][] RADII = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public static SingleTickTask<PathAwareEntity> create(float speed) {
        return StrollTask.create(speed, true);
    }

    public static SingleTickTask<PathAwareEntity> create(float speed, boolean strollInsideWater) {
        return StrollTask.create(speed, entity -> FuzzyTargeting.find(entity, 10, 7), strollInsideWater ? entity -> true : entity -> !entity.isInsideWaterOrBubbleColumn());
    }

    public static Task<PathAwareEntity> create(float speed, int horizontalRadius, int verticalRadius) {
        return StrollTask.create(speed, entity -> FuzzyTargeting.find(entity, horizontalRadius, verticalRadius), entity -> true);
    }

    public static Task<PathAwareEntity> createSolidTargeting(float speed) {
        return StrollTask.create(speed, entity -> StrollTask.findTargetPos(entity, 10, 7), entity -> true);
    }

    public static Task<PathAwareEntity> createDynamicRadius(float speed) {
        return StrollTask.create(speed, StrollTask::findTargetPos, Entity::isInsideWaterOrBubbleColumn);
    }

    private static SingleTickTask<PathAwareEntity> create(float speed, Function<PathAwareEntity, Vec3d> targetGetter, Predicate<PathAwareEntity> shouldRun) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, walkTarget -> (world, entity, time) -> {
            if (!shouldRun.test((PathAwareEntity)entity)) {
                return false;
            }
            Optional<Vec3d> optional = Optional.ofNullable((Vec3d)targetGetter.apply((PathAwareEntity)entity));
            walkTarget.remember(optional.map(pos -> new WalkTarget((Vec3d)pos, speed, 0)));
            return true;
        }));
    }

    @Nullable
    private static Vec3d findTargetPos(PathAwareEntity entity) {
        Vec3d lv = null;
        Vec3d lv2 = null;
        for (int[] is : RADII) {
            lv2 = lv == null ? LookTargetUtil.find(entity, is[0], is[1]) : entity.getPos().add(entity.getPos().relativize(lv).normalize().multiply(is[0], is[1], is[0]));
            if (lv2 == null || entity.getWorld().getFluidState(BlockPos.ofFloored(lv2)).isEmpty()) {
                return lv;
            }
            lv = lv2;
        }
        return lv2;
    }

    @Nullable
    private static Vec3d findTargetPos(PathAwareEntity entity, int horizontalRadius, int verticalRadius) {
        Vec3d lv = entity.getRotationVec(0.0f);
        return NoPenaltySolidTargeting.find(entity, horizontalRadius, verticalRadius, -2, lv.x, lv.z, 1.5707963705062866);
    }
}

