/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.control;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.control.Control;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

public class MoveControl
implements Control {
    public static final float field_30197 = 5.0E-4f;
    public static final float REACHED_DESTINATION_DISTANCE_SQUARED = 2.5000003E-7f;
    protected static final int field_30199 = 90;
    protected final MobEntity entity;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected double speed;
    protected float forwardMovement;
    protected float sidewaysMovement;
    protected State state = State.WAIT;

    public MoveControl(MobEntity entity) {
        this.entity = entity;
    }

    public boolean isMoving() {
        return this.state == State.MOVE_TO;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void moveTo(double x, double y, double z, double speed) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.speed = speed;
        if (this.state != State.JUMPING) {
            this.state = State.MOVE_TO;
        }
    }

    public void strafeTo(float forward, float sideways) {
        this.state = State.STRAFE;
        this.forwardMovement = forward;
        this.sidewaysMovement = sideways;
        this.speed = 0.25;
    }

    public void tick() {
        if (this.state == State.STRAFE) {
            float n;
            float f = (float)this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            float g = (float)this.speed * f;
            float h = this.forwardMovement;
            float i = this.sidewaysMovement;
            float j = MathHelper.sqrt(h * h + i * i);
            if (j < 1.0f) {
                j = 1.0f;
            }
            j = g / j;
            float k = MathHelper.sin(this.entity.getYaw() * ((float)Math.PI / 180));
            float l = MathHelper.cos(this.entity.getYaw() * ((float)Math.PI / 180));
            float m = (h *= j) * l - (i *= j) * k;
            if (!this.isPosWalkable(m, n = i * l + h * k)) {
                this.forwardMovement = 1.0f;
                this.sidewaysMovement = 0.0f;
            }
            this.entity.setMovementSpeed(g);
            this.entity.setForwardSpeed(this.forwardMovement);
            this.entity.setSidewaysSpeed(this.sidewaysMovement);
            this.state = State.WAIT;
        } else if (this.state == State.MOVE_TO) {
            this.state = State.WAIT;
            double d = this.targetX - this.entity.getX();
            double e = this.targetZ - this.entity.getZ();
            double o = this.targetY - this.entity.getY();
            double p = d * d + o * o + e * e;
            if (p < 2.500000277905201E-7) {
                this.entity.setForwardSpeed(0.0f);
                return;
            }
            float n = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
            this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), n, 90.0f));
            this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            BlockPos lv = this.entity.getBlockPos();
            BlockState lv2 = this.entity.getWorld().getBlockState(lv);
            VoxelShape lv3 = lv2.getCollisionShape(this.entity.getWorld(), lv);
            if (o > (double)this.entity.getStepHeight() && d * d + e * e < (double)Math.max(1.0f, this.entity.getWidth()) || !lv3.isEmpty() && this.entity.getY() < lv3.getMax(Direction.Axis.Y) + (double)lv.getY() && !lv2.isIn(BlockTags.DOORS) && !lv2.isIn(BlockTags.FENCES)) {
                this.entity.getJumpControl().setActive();
                this.state = State.JUMPING;
            }
        } else if (this.state == State.JUMPING) {
            this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            if (this.entity.isOnGround()) {
                this.state = State.WAIT;
            }
        } else {
            this.entity.setForwardSpeed(0.0f);
        }
    }

    private boolean isPosWalkable(float x, float z) {
        PathNodeMaker lv2;
        EntityNavigation lv = this.entity.getNavigation();
        return lv == null || (lv2 = lv.getNodeMaker()) == null || lv2.getDefaultNodeType(this.entity, BlockPos.ofFloored(this.entity.getX() + (double)x, this.entity.getBlockY(), this.entity.getZ() + (double)z)) == PathNodeType.WALKABLE;
    }

    protected float wrapDegrees(float from, float to, float max) {
        float j;
        float i = MathHelper.wrapDegrees(to - from);
        if (i > max) {
            i = max;
        }
        if (i < -max) {
            i = -max;
        }
        if ((j = from + i) < 0.0f) {
            j += 360.0f;
        } else if (j > 360.0f) {
            j -= 360.0f;
        }
        return j;
    }

    public double getTargetX() {
        return this.targetX;
    }

    public double getTargetY() {
        return this.targetY;
    }

    public double getTargetZ() {
        return this.targetZ;
    }

    protected static enum State {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;

    }
}

