package net.minecraft.entity.ai;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

public class TargetPredicate {
   public static final TargetPredicate DEFAULT = createAttackable();
   private static final double MIN_DISTANCE = 2.0;
   private final boolean attackable;
   private double baseMaxDistance = -1.0;
   private boolean respectsVisibility = true;
   private boolean useDistanceScalingFactor = true;
   @Nullable
   private Predicate predicate;

   private TargetPredicate(boolean attackable) {
      this.attackable = attackable;
   }

   public static TargetPredicate createAttackable() {
      return new TargetPredicate(true);
   }

   public static TargetPredicate createNonAttackable() {
      return new TargetPredicate(false);
   }

   public TargetPredicate copy() {
      TargetPredicate lv = this.attackable ? createAttackable() : createNonAttackable();
      lv.baseMaxDistance = this.baseMaxDistance;
      lv.respectsVisibility = this.respectsVisibility;
      lv.useDistanceScalingFactor = this.useDistanceScalingFactor;
      lv.predicate = this.predicate;
      return lv;
   }

   public TargetPredicate setBaseMaxDistance(double baseMaxDistance) {
      this.baseMaxDistance = baseMaxDistance;
      return this;
   }

   public TargetPredicate ignoreVisibility() {
      this.respectsVisibility = false;
      return this;
   }

   public TargetPredicate ignoreDistanceScalingFactor() {
      this.useDistanceScalingFactor = false;
      return this;
   }

   public TargetPredicate setPredicate(@Nullable Predicate predicate) {
      this.predicate = predicate;
      return this;
   }

   public boolean test(@Nullable LivingEntity baseEntity, LivingEntity targetEntity) {
      if (baseEntity == targetEntity) {
         return false;
      } else if (!targetEntity.isPartOfGame()) {
         return false;
      } else if (this.predicate != null && !this.predicate.test(targetEntity)) {
         return false;
      } else {
         if (baseEntity == null) {
            if (this.attackable && (!targetEntity.canTakeDamage() || targetEntity.world.getDifficulty() == Difficulty.PEACEFUL)) {
               return false;
            }
         } else {
            if (this.attackable && (!baseEntity.canTarget(targetEntity) || !baseEntity.canTarget(targetEntity.getType()) || baseEntity.isTeammate(targetEntity))) {
               return false;
            }

            if (this.baseMaxDistance > 0.0) {
               double d = this.useDistanceScalingFactor ? targetEntity.getAttackDistanceScalingFactor(baseEntity) : 1.0;
               double e = Math.max(this.baseMaxDistance * d, 2.0);
               double f = baseEntity.squaredDistanceTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
               if (f > e * e) {
                  return false;
               }
            }

            if (this.respectsVisibility && baseEntity instanceof MobEntity) {
               MobEntity lv = (MobEntity)baseEntity;
               if (!lv.getVisibilityCache().canSee(targetEntity)) {
                  return false;
               }
            }
         }

         return true;
      }
   }
}
