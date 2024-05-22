/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.goal.DiveJumpingGoal;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DolphinJumpGoal
extends DiveJumpingGoal {
    private static final int[] OFFSET_MULTIPLIERS = new int[]{0, 1, 4, 5, 6, 7};
    private final DolphinEntity dolphin;
    private final int chance;
    private boolean inWater;

    public DolphinJumpGoal(DolphinEntity dolphin, int chance) {
        this.dolphin = dolphin;
        this.chance = DolphinJumpGoal.toGoalTicks(chance);
    }

    @Override
    public boolean canStart() {
        if (this.dolphin.getRandom().nextInt(this.chance) != 0) {
            return false;
        }
        Direction lv = this.dolphin.getMovementDirection();
        int i = lv.getOffsetX();
        int j = lv.getOffsetZ();
        BlockPos lv2 = this.dolphin.getBlockPos();
        for (int k : OFFSET_MULTIPLIERS) {
            if (this.isWater(lv2, i, j, k) && this.isAirAbove(lv2, i, j, k)) continue;
            return false;
        }
        return true;
    }

    private boolean isWater(BlockPos pos, int offsetX, int offsetZ, int multiplier) {
        BlockPos lv = pos.add(offsetX * multiplier, 0, offsetZ * multiplier);
        return this.dolphin.getWorld().getFluidState(lv).isIn(FluidTags.WATER) && !this.dolphin.getWorld().getBlockState(lv).blocksMovement();
    }

    private boolean isAirAbove(BlockPos pos, int offsetX, int offsetZ, int multiplier) {
        return this.dolphin.getWorld().getBlockState(pos.add(offsetX * multiplier, 1, offsetZ * multiplier)).isAir() && this.dolphin.getWorld().getBlockState(pos.add(offsetX * multiplier, 2, offsetZ * multiplier)).isAir();
    }

    @Override
    public boolean shouldContinue() {
        double d = this.dolphin.getVelocity().y;
        return !(d * d < (double)0.03f && this.dolphin.getPitch() != 0.0f && Math.abs(this.dolphin.getPitch()) < 10.0f && this.dolphin.isTouchingWater() || this.dolphin.isOnGround());
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void start() {
        Direction lv = this.dolphin.getMovementDirection();
        this.dolphin.setVelocity(this.dolphin.getVelocity().add((double)lv.getOffsetX() * 0.6, 0.7, (double)lv.getOffsetZ() * 0.6));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setPitch(0.0f);
    }

    @Override
    public void tick() {
        boolean bl = this.inWater;
        if (!bl) {
            FluidState lv = this.dolphin.getWorld().getFluidState(this.dolphin.getBlockPos());
            this.inWater = lv.isIn(FluidTags.WATER);
        }
        if (this.inWater && !bl) {
            this.dolphin.playSound(SoundEvents.ENTITY_DOLPHIN_JUMP, 1.0f, 1.0f);
        }
        Vec3d lv2 = this.dolphin.getVelocity();
        if (lv2.y * lv2.y < (double)0.03f && this.dolphin.getPitch() != 0.0f) {
            this.dolphin.setPitch(MathHelper.lerpAngleDegrees(0.2f, this.dolphin.getPitch(), 0.0f));
        } else if (lv2.length() > (double)1.0E-5f) {
            double d = lv2.horizontalLength();
            double e = Math.atan2(-lv2.y, d) * 57.2957763671875;
            this.dolphin.setPitch((float)e);
        }
    }
}

