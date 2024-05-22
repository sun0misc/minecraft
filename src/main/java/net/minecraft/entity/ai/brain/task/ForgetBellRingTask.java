/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableInt;

public class ForgetBellRingTask {
    private static final int MIN_HEARD_BELL_TIME = 300;

    public static Task<LivingEntity> create(int maxHiddenSeconds, int distance) {
        int k = maxHiddenSeconds * 20;
        MutableInt mutableInt = new MutableInt(0);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.HIDING_PLACE), context.queryMemoryValue(MemoryModuleType.HEARD_BELL_TIME)).apply(context, (hidingPlace, heardBellTime) -> (world, entity, time) -> {
            boolean bl;
            long m = (Long)context.getValue(heardBellTime);
            boolean bl2 = bl = m + 300L <= time;
            if (mutableInt.getValue() > k || bl) {
                heardBellTime.forget();
                hidingPlace.forget();
                entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
                mutableInt.setValue(0);
                return true;
            }
            BlockPos lv = ((GlobalPos)context.getValue(hidingPlace)).pos();
            if (lv.isWithinDistance(entity.getBlockPos(), (double)distance)) {
                mutableInt.increment();
            }
            return true;
        }));
    }
}

