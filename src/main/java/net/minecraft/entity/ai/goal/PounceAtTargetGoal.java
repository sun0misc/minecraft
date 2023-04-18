package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public class PounceAtTargetGoal extends Goal {
   private final MobEntity mob;
   private LivingEntity target;
   private final float velocity;

   public PounceAtTargetGoal(MobEntity mob, float velocity) {
      this.mob = mob;
      this.velocity = velocity;
      this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (this.mob.hasPassengers()) {
         return false;
      } else {
         this.target = this.mob.getTarget();
         if (this.target == null) {
            return false;
         } else {
            double d = this.mob.squaredDistanceTo(this.target);
            if (!(d < 4.0) && !(d > 16.0)) {
               if (!this.mob.isOnGround()) {
                  return false;
               } else {
                  return this.mob.getRandom().nextInt(toGoalTicks(5)) == 0;
               }
            } else {
               return false;
            }
         }
      }
   }

   public boolean shouldContinue() {
      return !this.mob.isOnGround();
   }

   public void start() {
      Vec3d lv = this.mob.getVelocity();
      Vec3d lv2 = new Vec3d(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
      if (lv2.lengthSquared() > 1.0E-7) {
         lv2 = lv2.normalize().multiply(0.4).add(lv.multiply(0.2));
      }

      this.mob.setVelocity(lv2.x, (double)this.velocity, lv2.z);
   }
}
