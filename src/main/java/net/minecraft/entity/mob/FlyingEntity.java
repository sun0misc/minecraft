/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class FlyingEntity
extends MobEntity {
    protected FlyingEntity(EntityType<? extends FlyingEntity> arg, World arg2) {
        super((EntityType<? extends MobEntity>)arg, arg2);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.isTouchingWater()) {
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.8f));
            } else if (this.isInLava()) {
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.5));
            } else {
                float f = 0.91f;
                if (this.isOnGround()) {
                    f = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91f;
                }
                float g = 0.16277137f / (f * f * f);
                f = 0.91f;
                if (this.isOnGround()) {
                    f = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91f;
                }
                this.updateVelocity(this.isOnGround() ? 0.1f * g : 0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(f));
            }
        }
        this.updateLimbs(false);
    }

    @Override
    public boolean isClimbing() {
        return false;
    }
}

