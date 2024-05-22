/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class NearestLivingEntitiesSensor<T extends LivingEntity>
extends Sensor<T> {
    @Override
    protected void sense(ServerWorld world, T entity) {
        Box lv = ((Entity)entity).getBoundingBox().expand(this.getHorizontalExpansion(), this.getHeightExpansion(), this.getHorizontalExpansion());
        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, lv, e -> e != entity && e.isAlive());
        list.sort(Comparator.comparingDouble(arg_0 -> entity.squaredDistanceTo(arg_0)));
        Brain<?> lv2 = ((LivingEntity)entity).getBrain();
        lv2.remember(MemoryModuleType.MOBS, list);
        lv2.remember(MemoryModuleType.VISIBLE_MOBS, new LivingTargetCache((LivingEntity)entity, list));
    }

    protected int getHorizontalExpansion() {
        return 16;
    }

    protected int getHeightExpansion() {
        return 16;
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);
    }
}

