package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderAroundGoal extends Goal {
   public static final int DEFAULT_CHANCE = 120;
   protected final PathAwareEntity mob;
   protected double targetX;
   protected double targetY;
   protected double targetZ;
   protected final double speed;
   protected int chance;
   protected boolean ignoringChance;
   private final boolean canDespawn;

   public WanderAroundGoal(PathAwareEntity mob, double speed) {
      this(mob, speed, 120);
   }

   public WanderAroundGoal(PathAwareEntity mob, double speed, int chance) {
      this(mob, speed, chance, true);
   }

   public WanderAroundGoal(PathAwareEntity entity, double speed, int chance, boolean canDespawn) {
      this.mob = entity;
      this.speed = speed;
      this.chance = chance;
      this.canDespawn = canDespawn;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (this.mob.hasPassengers()) {
         return false;
      } else {
         if (!this.ignoringChance) {
            if (this.canDespawn && this.mob.getDespawnCounter() >= 100) {
               return false;
            }

            if (this.mob.getRandom().nextInt(toGoalTicks(this.chance)) != 0) {
               return false;
            }
         }

         Vec3d lv = this.getWanderTarget();
         if (lv == null) {
            return false;
         } else {
            this.targetX = lv.x;
            this.targetY = lv.y;
            this.targetZ = lv.z;
            this.ignoringChance = false;
            return true;
         }
      }
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      return NoPenaltyTargeting.find(this.mob, 10, 7);
   }

   public boolean shouldContinue() {
      return !this.mob.getNavigation().isIdle() && !this.mob.hasPassengers();
   }

   public void start() {
      this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
   }

   public void stop() {
      this.mob.getNavigation().stop();
      super.stop();
   }

   public void ignoreChanceOnce() {
      this.ignoringChance = true;
   }

   public void setChance(int chance) {
      this.chance = chance;
   }
}
