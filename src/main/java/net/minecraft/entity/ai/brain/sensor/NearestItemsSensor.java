/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class NearestItemsSensor
extends Sensor<MobEntity> {
    private static final long HORIZONTAL_RANGE = 32L;
    private static final long VERTICAL_RANGE = 16L;
    public static final int MAX_RANGE = 32;

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    @Override
    protected void sense(ServerWorld arg, MobEntity arg2) {
        Brain<?> lv = arg2.getBrain();
        List<ItemEntity> list = arg.getEntitiesByClass(ItemEntity.class, arg2.getBoundingBox().expand(32.0, 16.0, 32.0), itemEntity -> true);
        list.sort(Comparator.comparingDouble(arg2::squaredDistanceTo));
        Optional<ItemEntity> optional = list.stream().filter(itemEntity -> arg2.canGather(itemEntity.getStack())).filter(itemEntity -> itemEntity.isInRange(arg2, 32.0)).filter(arg2::canSee).findFirst();
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
    }
}

