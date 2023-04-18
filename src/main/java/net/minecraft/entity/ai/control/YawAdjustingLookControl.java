package net.minecraft.entity.ai.control;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class YawAdjustingLookControl extends LookControl {
   private final int yawAdjustThreshold;
   private static final int ADDED_PITCH = 10;
   private static final int ADDED_YAW = 20;

   public YawAdjustingLookControl(MobEntity entity, int yawAdjustThreshold) {
      super(entity);
      this.yawAdjustThreshold = yawAdjustThreshold;
   }

   public void tick() {
      if (this.lookAtTimer > 0) {
         --this.lookAtTimer;
         this.getTargetYaw().ifPresent((yaw) -> {
            this.entity.headYaw = this.changeAngle(this.entity.headYaw, yaw + 20.0F, this.maxYawChange);
         });
         this.getTargetPitch().ifPresent((pitch) -> {
            this.entity.setPitch(this.changeAngle(this.entity.getPitch(), pitch + 10.0F, this.maxPitchChange));
         });
      } else {
         if (this.entity.getNavigation().isIdle()) {
            this.entity.setPitch(this.changeAngle(this.entity.getPitch(), 0.0F, 5.0F));
         }

         this.entity.headYaw = this.changeAngle(this.entity.headYaw, this.entity.bodyYaw, this.maxYawChange);
      }

      float f = MathHelper.wrapDegrees(this.entity.headYaw - this.entity.bodyYaw);
      MobEntity var10000;
      if (f < (float)(-this.yawAdjustThreshold)) {
         var10000 = this.entity;
         var10000.bodyYaw -= 4.0F;
      } else if (f > (float)this.yawAdjustThreshold) {
         var10000 = this.entity;
         var10000.bodyYaw += 4.0F;
      }

   }
}
