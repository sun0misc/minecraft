/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityLookTarget
implements LookTarget {
    private final Entity entity;
    private final boolean useEyeHeight;

    public EntityLookTarget(Entity entity, boolean useEyeHeight) {
        this.entity = entity;
        this.useEyeHeight = useEyeHeight;
    }

    @Override
    public Vec3d getPos() {
        return this.useEyeHeight ? this.entity.getPos().add(0.0, this.entity.getStandingEyeHeight(), 0.0) : this.entity.getPos();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.entity.getBlockPos();
    }

    @Override
    public boolean isSeenBy(LivingEntity entity) {
        Entity entity2 = this.entity;
        if (!(entity2 instanceof LivingEntity)) {
            return true;
        }
        LivingEntity lv = (LivingEntity)entity2;
        if (!lv.isAlive()) {
            return false;
        }
        Optional<LivingTargetCache> optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
        return optional.isPresent() && optional.get().contains(lv);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public String toString() {
        return "EntityTracker for " + String.valueOf(this.entity);
    }
}

