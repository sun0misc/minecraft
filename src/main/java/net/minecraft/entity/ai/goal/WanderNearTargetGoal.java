package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderNearTargetGoal extends Goal {
   private final PathAwareEntity mob;
   @Nullable
   private LivingEntity target;
   private double x;
   private double y;
   private double z;
   private final double speed;
   private final float maxDistance;

   public WanderNearTargetGoal(PathAwareEntity mob, double speed, float maxDistance) {
      this.mob = mob;
      this.speed = speed;
      this.maxDistance = maxDistance;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      this.target = this.mob.getTarget();
      if (this.target == null) {
         return false;
      } else if (this.target.squaredDistanceTo(this.mob) > (double)(this.maxDistance * this.maxDistance)) {
         return false;
      } else {
         Vec3d lv = NoPenaltyTargeting.findTo(this.mob, 16, 7, this.target.getPos(), 1.5707963705062866);
         if (lv == null) {
            return false;
         } else {
            this.x = lv.x;
            this.y = lv.y;
            this.z = lv.z;
            return true;
         }
      }
   }

   public boolean shouldContinue() {
      return !this.mob.getNavigation().isIdle() && this.target.isAlive() && this.target.squaredDistanceTo(this.mob) < (double)(this.maxDistance * this.maxDistance);
   }

   public void stop() {
      this.target = null;
   }

   public void start() {
      this.mob.getNavigation().startMovingTo(this.x, this.y, this.z, this.speed);
   }
}
