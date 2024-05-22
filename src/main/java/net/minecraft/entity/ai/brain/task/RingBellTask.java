/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class RingBellTask {
    private static final float RUN_CHANCE = 0.95f;
    public static final int MAX_DISTANCE = 3;

    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.MEETING_POINT)).apply(context, meetingPoint -> (world, entity, time) -> {
            BlockState lv2;
            if (world.random.nextFloat() <= 0.95f) {
                return false;
            }
            BlockPos lv = ((GlobalPos)context.getValue(meetingPoint)).pos();
            if (lv.isWithinDistance(entity.getBlockPos(), 3.0) && (lv2 = world.getBlockState(lv)).isOf(Blocks.BELL)) {
                BellBlock lv3 = (BellBlock)lv2.getBlock();
                lv3.ring(entity, world, lv, null);
            }
            return true;
        }));
    }
}

