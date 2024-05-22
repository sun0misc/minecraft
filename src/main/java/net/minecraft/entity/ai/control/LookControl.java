/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.control;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.Control;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LookControl
implements Control {
    protected final MobEntity entity;
    protected float maxYawChange;
    protected float maxPitchChange;
    protected int lookAtTimer;
    protected double x;
    protected double y;
    protected double z;

    public LookControl(MobEntity entity) {
        this.entity = entity;
    }

    public void lookAt(Vec3d direction) {
        this.lookAt(direction.x, direction.y, direction.z);
    }

    public void lookAt(Entity entity) {
        this.lookAt(entity.getX(), LookControl.getLookingHeightFor(entity), entity.getZ());
    }

    public void lookAt(Entity entity, float maxYawChange, float maxPitchChange) {
        this.lookAt(entity.getX(), LookControl.getLookingHeightFor(entity), entity.getZ(), maxYawChange, maxPitchChange);
    }

    public void lookAt(double x, double y, double z) {
        this.lookAt(x, y, z, this.entity.getMaxLookYawChange(), this.entity.getMaxLookPitchChange());
    }

    public void lookAt(double x, double y, double z, float maxYawChange, float maxPitchChange) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxYawChange = maxYawChange;
        this.maxPitchChange = maxPitchChange;
        this.lookAtTimer = 2;
    }

    public void tick() {
        if (this.shouldStayHorizontal()) {
            this.entity.setPitch(0.0f);
        }
        if (this.lookAtTimer > 0) {
            --this.lookAtTimer;
            this.getTargetYaw().ifPresent(yaw -> {
                this.entity.headYaw = this.changeAngle(this.entity.headYaw, yaw.floatValue(), this.maxYawChange);
            });
            this.getTargetPitch().ifPresent(pitch -> this.entity.setPitch(this.changeAngle(this.entity.getPitch(), pitch.floatValue(), this.maxPitchChange)));
        } else {
            this.entity.headYaw = this.changeAngle(this.entity.headYaw, this.entity.bodyYaw, 10.0f);
        }
        this.clampHeadYaw();
    }

    protected void clampHeadYaw() {
        if (!this.entity.getNavigation().isIdle()) {
            this.entity.headYaw = MathHelper.clampAngle(this.entity.headYaw, this.entity.bodyYaw, this.entity.getMaxHeadRotation());
        }
    }

    protected boolean shouldStayHorizontal() {
        return true;
    }

    public boolean isLookingAtSpecificPosition() {
        return this.lookAtTimer > 0;
    }

    public double getLookX() {
        return this.x;
    }

    public double getLookY() {
        return this.y;
    }

    public double getLookZ() {
        return this.z;
    }

    protected Optional<Float> getTargetPitch() {
        double d = this.x - this.entity.getX();
        double e = this.y - this.entity.getEyeY();
        double f = this.z - this.entity.getZ();
        double g = Math.sqrt(d * d + f * f);
        return Math.abs(e) > (double)1.0E-5f || Math.abs(g) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(-(MathHelper.atan2(e, g) * 57.2957763671875)))) : Optional.empty();
    }

    protected Optional<Float> getTargetYaw() {
        double d = this.x - this.entity.getX();
        double e = this.z - this.entity.getZ();
        return Math.abs(e) > (double)1.0E-5f || Math.abs(d) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f)) : Optional.empty();
    }

    protected float changeAngle(float from, float to, float max) {
        float i = MathHelper.subtractAngles(from, to);
        float j = MathHelper.clamp(i, -max, max);
        return from + j;
    }

    private static double getLookingHeightFor(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity.getEyeY();
        }
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
    }
}

