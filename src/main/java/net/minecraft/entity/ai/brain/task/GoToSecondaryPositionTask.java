/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.List;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableLong;

public class GoToSecondaryPositionTask {
    public static Task<VillagerEntity> create(MemoryModuleType<List<GlobalPos>> secondaryPositions, float speed, int completionRange, int primaryPositionActivationDistance, MemoryModuleType<GlobalPos> primaryPosition) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(secondaryPositions), context.queryMemoryValue(primaryPosition)).apply(context, (walkTarget, secondary, primary) -> (world, entity, time) -> {
            List list = (List)context.getValue(secondary);
            GlobalPos lv = (GlobalPos)context.getValue(primary);
            if (list.isEmpty()) {
                return false;
            }
            GlobalPos lv2 = (GlobalPos)list.get(world.getRandom().nextInt(list.size()));
            if (lv2 == null || world.getRegistryKey() != lv2.dimension() || !lv.pos().isWithinDistance(entity.getPos(), (double)primaryPositionActivationDistance)) {
                return false;
            }
            if (time > mutableLong.getValue()) {
                walkTarget.remember(new WalkTarget(lv2.pos(), speed, completionRange));
                mutableLong.setValue(time + 100L);
            }
            return true;
        }));
    }
}

