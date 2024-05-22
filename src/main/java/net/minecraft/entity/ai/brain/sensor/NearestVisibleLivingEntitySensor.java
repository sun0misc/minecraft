/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.server.world.ServerWorld;

public abstract class NearestVisibleLivingEntitySensor
extends Sensor<LivingEntity> {
    protected abstract boolean matches(LivingEntity var1, LivingEntity var2);

    protected abstract MemoryModuleType<LivingEntity> getOutputMemoryModule();

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(this.getOutputMemoryModule());
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        entity.getBrain().remember(this.getOutputMemoryModule(), this.getNearestVisibleLivingEntity(entity));
    }

    private Optional<LivingEntity> getNearestVisibleLivingEntity(LivingEntity entity) {
        return this.getVisibleLivingEntities(entity).flatMap(arg22 -> arg22.findFirst(arg2 -> this.matches(entity, (LivingEntity)arg2)));
    }

    protected Optional<LivingTargetCache> getVisibleLivingEntities(LivingEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
    }
}

