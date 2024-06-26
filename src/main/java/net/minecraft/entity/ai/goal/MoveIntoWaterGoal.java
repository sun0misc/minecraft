/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class MoveIntoWaterGoal
extends Goal {
    private final PathAwareEntity mob;

    public MoveIntoWaterGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        return this.mob.isOnGround() && !this.mob.getWorld().getFluidState(this.mob.getBlockPos()).isIn(FluidTags.WATER);
    }

    @Override
    public void start() {
        Vec3i lv = null;
        Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 2.0), MathHelper.floor(this.mob.getY() - 2.0), MathHelper.floor(this.mob.getZ() - 2.0), MathHelper.floor(this.mob.getX() + 2.0), this.mob.getBlockY(), MathHelper.floor(this.mob.getZ() + 2.0));
        for (BlockPos lv2 : iterable) {
            if (!this.mob.getWorld().getFluidState(lv2).isIn(FluidTags.WATER)) continue;
            lv = lv2;
            break;
        }
        if (lv != null) {
            this.mob.getMoveControl().moveTo(lv.getX(), lv.getY(), lv.getZ(), 1.0);
        }
    }
}

