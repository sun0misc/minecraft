package net.minecraft.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.scoreboard.AbstractTeam;
import org.jetbrains.annotations.Nullable;

public abstract class TrackTargetGoal extends Goal {
   private static final int UNSET = 0;
   private static final int CAN_TRACK = 1;
   private static final int CANNOT_TRACK = 2;
   protected final MobEntity mob;
   protected final boolean checkVisibility;
   private final boolean checkCanNavigate;
   private int canNavigateFlag;
   private int checkCanNavigateCooldown;
   private int timeWithoutVisibility;
   @Nullable
   protected LivingEntity target;
   protected int maxTimeWithoutVisibility;

   public TrackTargetGoal(MobEntity mob, boolean checkVisibility) {
      this(mob, checkVisibility, false);
   }

   public TrackTargetGoal(MobEntity mob, boolean checkVisibility, boolean checkNavigable) {
      this.maxTimeWithoutVisibility = 60;
      this.mob = mob;
      this.checkVisibility = checkVisibility;
      this.checkCanNavigate = checkNavigable;
   }

   public boolean shouldContinue() {
      LivingEntity lv = this.mob.getTarget();
      if (lv == null) {
         lv = this.target;
      }

      if (lv == null) {
         return false;
      } else if (!this.mob.canTarget(lv)) {
         return false;
      } else {
         AbstractTeam lv2 = this.mob.getScoreboardTeam();
         AbstractTeam lv3 = lv.getScoreboardTeam();
         if (lv2 != null && lv3 == lv2) {
            return false;
         } else {
            double d = this.getFollowRange();
            if (this.mob.squaredDistanceTo(lv) > d * d) {
               return false;
            } else {
               if (this.checkVisibility) {
                  if (this.mob.getVisibilityCache().canSee(lv)) {
                     this.timeWithoutVisibility = 0;
                  } else if (++this.timeWithoutVisibility > toGoalTicks(this.maxTimeWithoutVisibility)) {
                     return false;
                  }
               }

               this.mob.setTarget(lv);
               return true;
            }
         }
      }
   }

   protected double getFollowRange() {
      return this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
   }

   public void start() {
      this.canNavigateFlag = 0;
      this.checkCanNavigateCooldown = 0;
      this.timeWithoutVisibility = 0;
   }

   public void stop() {
      this.mob.setTarget((LivingEntity)null);
      this.target = null;
   }

   protected boolean canTrack(@Nullable LivingEntity target, TargetPredicate targetPredicate) {
      if (target == null) {
         return false;
      } else if (!targetPredicate.test(this.mob, target)) {
         return false;
      } else if (!this.mob.isInWalkTargetRange(target.getBlockPos())) {
         return false;
      } else {
         if (this.checkCanNavigate) {
            if (--this.checkCanNavigateCooldown <= 0) {
               this.canNavigateFlag = 0;
            }

            if (this.canNavigateFlag == 0) {
               this.canNavigateFlag = this.canNavigateToEntity(target) ? 1 : 2;
            }

            if (this.canNavigateFlag == 2) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean canNavigateToEntity(LivingEntity entity) {
      this.checkCanNavigateCooldown = toGoalTicks(10 + this.mob.getRandom().nextInt(5));
      Path lv = this.mob.getNavigation().findPathTo((Entity)entity, 0);
      if (lv == null) {
         return false;
      } else {
         PathNode lv2 = lv.getEnd();
         if (lv2 == null) {
            return false;
         } else {
            int i = lv2.x - entity.getBlockX();
            int j = lv2.z - entity.getBlockZ();
            return (double)(i * i + j * j) <= 2.25;
         }
      }
   }

   public TrackTargetGoal setMaxTimeWithoutVisibility(int time) {
      this.maxTimeWithoutVisibility = time;
      return this;
   }
}
