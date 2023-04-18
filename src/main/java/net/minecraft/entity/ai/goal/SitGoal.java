package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;

public class SitGoal extends Goal {
   private final TameableEntity tameable;

   public SitGoal(TameableEntity tameable) {
      this.tameable = tameable;
      this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
   }

   public boolean shouldContinue() {
      return this.tameable.isSitting();
   }

   public boolean canStart() {
      if (!this.tameable.isTamed()) {
         return false;
      } else if (this.tameable.isInsideWaterOrBubbleColumn()) {
         return false;
      } else if (!this.tameable.isOnGround()) {
         return false;
      } else {
         LivingEntity lv = this.tameable.getOwner();
         if (lv == null) {
            return true;
         } else {
            return this.tameable.squaredDistanceTo(lv) < 144.0 && lv.getAttacker() != null ? false : this.tameable.isSitting();
         }
      }
   }

   public void start() {
      this.tameable.getNavigation().stop();
      this.tameable.setInSittingPose(true);
   }

   public void stop() {
      this.tameable.setInSittingPose(false);
   }
}
