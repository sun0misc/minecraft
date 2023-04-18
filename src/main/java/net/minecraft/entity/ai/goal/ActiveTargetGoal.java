package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public class ActiveTargetGoal extends TrackTargetGoal {
   private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
   protected final Class targetClass;
   protected final int reciprocalChance;
   @Nullable
   protected LivingEntity targetEntity;
   protected TargetPredicate targetPredicate;

   public ActiveTargetGoal(MobEntity mob, Class targetClass, boolean checkVisibility) {
      this(mob, targetClass, 10, checkVisibility, false, (Predicate)null);
   }

   public ActiveTargetGoal(MobEntity mob, Class targetClass, boolean checkVisibility, Predicate targetPredicate) {
      this(mob, targetClass, 10, checkVisibility, false, targetPredicate);
   }

   public ActiveTargetGoal(MobEntity mob, Class targetClass, boolean checkVisibility, boolean checkCanNavigate) {
      this(mob, targetClass, 10, checkVisibility, checkCanNavigate, (Predicate)null);
   }

   public ActiveTargetGoal(MobEntity mob, Class targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate targetPredicate) {
      super(mob, checkVisibility, checkCanNavigate);
      this.targetClass = targetClass;
      this.reciprocalChance = toGoalTicks(reciprocalChance);
      this.setControls(EnumSet.of(Goal.Control.TARGET));
      this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
   }

   public boolean canStart() {
      if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
         return false;
      } else {
         this.findClosestTarget();
         return this.targetEntity != null;
      }
   }

   protected Box getSearchBox(double distance) {
      return this.mob.getBoundingBox().expand(distance, 4.0, distance);
   }

   protected void findClosestTarget() {
      if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
         this.targetEntity = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), (arg) -> {
            return true;
         }), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      } else {
         this.targetEntity = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      }

   }

   public void start() {
      this.mob.setTarget(this.targetEntity);
      super.start();
   }

   public void setTargetEntity(@Nullable LivingEntity targetEntity) {
      this.targetEntity = targetEntity;
   }
}
