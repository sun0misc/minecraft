package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;

public class MeleeAttackGoal extends Goal {
   protected final PathAwareEntity mob;
   private final double speed;
   private final boolean pauseWhenMobIdle;
   private Path path;
   private double targetX;
   private double targetY;
   private double targetZ;
   private int updateCountdownTicks;
   private int cooldown;
   private final int attackIntervalTicks = 20;
   private long lastUpdateTime;
   private static final long MAX_ATTACK_TIME = 20L;

   public MeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
      this.mob = mob;
      this.speed = speed;
      this.pauseWhenMobIdle = pauseWhenMobIdle;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
   }

   public boolean canStart() {
      long l = this.mob.world.getTime();
      if (l - this.lastUpdateTime < 20L) {
         return false;
      } else {
         this.lastUpdateTime = l;
         LivingEntity lv = this.mob.getTarget();
         if (lv == null) {
            return false;
         } else if (!lv.isAlive()) {
            return false;
         } else {
            this.path = this.mob.getNavigation().findPathTo((Entity)lv, 0);
            if (this.path != null) {
               return true;
            } else {
               return this.getSquaredMaxAttackDistance(lv) >= this.mob.squaredDistanceTo(lv.getX(), lv.getY(), lv.getZ());
            }
         }
      }
   }

   public boolean shouldContinue() {
      LivingEntity lv = this.mob.getTarget();
      if (lv == null) {
         return false;
      } else if (!lv.isAlive()) {
         return false;
      } else if (!this.pauseWhenMobIdle) {
         return !this.mob.getNavigation().isIdle();
      } else if (!this.mob.isInWalkTargetRange(lv.getBlockPos())) {
         return false;
      } else {
         return !(lv instanceof PlayerEntity) || !lv.isSpectator() && !((PlayerEntity)lv).isCreative();
      }
   }

   public void start() {
      this.mob.getNavigation().startMovingAlong(this.path, this.speed);
      this.mob.setAttacking(true);
      this.updateCountdownTicks = 0;
      this.cooldown = 0;
   }

   public void stop() {
      LivingEntity lv = this.mob.getTarget();
      if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(lv)) {
         this.mob.setTarget((LivingEntity)null);
      }

      this.mob.setAttacking(false);
      this.mob.getNavigation().stop();
   }

   public boolean shouldRunEveryTick() {
      return true;
   }

   public void tick() {
      LivingEntity lv = this.mob.getTarget();
      if (lv != null) {
         this.mob.getLookControl().lookAt(lv, 30.0F, 30.0F);
         double d = this.mob.getSquaredDistanceToAttackPosOf(lv);
         this.updateCountdownTicks = Math.max(this.updateCountdownTicks - 1, 0);
         if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(lv)) && this.updateCountdownTicks <= 0 && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || lv.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05F)) {
            this.targetX = lv.getX();
            this.targetY = lv.getY();
            this.targetZ = lv.getZ();
            this.updateCountdownTicks = 4 + this.mob.getRandom().nextInt(7);
            if (d > 1024.0) {
               this.updateCountdownTicks += 10;
            } else if (d > 256.0) {
               this.updateCountdownTicks += 5;
            }

            if (!this.mob.getNavigation().startMovingTo(lv, this.speed)) {
               this.updateCountdownTicks += 15;
            }

            this.updateCountdownTicks = this.getTickCount(this.updateCountdownTicks);
         }

         this.cooldown = Math.max(this.cooldown - 1, 0);
         this.attack(lv, d);
      }
   }

   protected void attack(LivingEntity target, double squaredDistance) {
      double e = this.getSquaredMaxAttackDistance(target);
      if (squaredDistance <= e && this.cooldown <= 0) {
         this.resetCooldown();
         this.mob.swingHand(Hand.MAIN_HAND);
         this.mob.tryAttack(target);
      }

   }

   protected void resetCooldown() {
      this.cooldown = this.getTickCount(20);
   }

   protected boolean isCooledDown() {
      return this.cooldown <= 0;
   }

   protected int getCooldown() {
      return this.cooldown;
   }

   protected int getMaxCooldown() {
      return this.getTickCount(20);
   }

   protected double getSquaredMaxAttackDistance(LivingEntity entity) {
      return (double)(this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F + entity.getWidth());
   }
}
