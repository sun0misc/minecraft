package net.minecraft.entity.ai.control;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class BodyControl implements Control {
   private final MobEntity entity;
   private static final int BODY_KEEP_UP_THRESHOLD = 15;
   private static final int ROTATE_BODY_START_TICK = 10;
   private static final int ROTATION_INCREMENTS = 10;
   private int bodyAdjustTicks;
   private float lastHeadYaw;

   public BodyControl(MobEntity entity) {
      this.entity = entity;
   }

   public void tick() {
      if (this.isMoving()) {
         this.entity.bodyYaw = this.entity.getYaw();
         this.keepUpHead();
         this.lastHeadYaw = this.entity.headYaw;
         this.bodyAdjustTicks = 0;
      } else {
         if (this.isIndependent()) {
            if (Math.abs(this.entity.headYaw - this.lastHeadYaw) > 15.0F) {
               this.bodyAdjustTicks = 0;
               this.lastHeadYaw = this.entity.headYaw;
               this.keepUpBody();
            } else {
               ++this.bodyAdjustTicks;
               if (this.bodyAdjustTicks > 10) {
                  this.slowlyAdjustBody();
               }
            }
         }

      }
   }

   private void keepUpBody() {
      this.entity.bodyYaw = MathHelper.clampAngle(this.entity.bodyYaw, this.entity.headYaw, (float)this.entity.getMaxHeadRotation());
   }

   private void keepUpHead() {
      this.entity.headYaw = MathHelper.clampAngle(this.entity.headYaw, this.entity.bodyYaw, (float)this.entity.getMaxHeadRotation());
   }

   private void slowlyAdjustBody() {
      int i = this.bodyAdjustTicks - 10;
      float f = MathHelper.clamp((float)i / 10.0F, 0.0F, 1.0F);
      float g = (float)this.entity.getMaxHeadRotation() * (1.0F - f);
      this.entity.bodyYaw = MathHelper.clampAngle(this.entity.bodyYaw, this.entity.headYaw, g);
   }

   private boolean isIndependent() {
      return !(this.entity.getFirstPassenger() instanceof MobEntity);
   }

   private boolean isMoving() {
      double d = this.entity.getX() - this.entity.prevX;
      double e = this.entity.getZ() - this.entity.prevZ;
      return d * d + e * e > 2.500000277905201E-7;
   }
}
