package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.jetbrains.annotations.Nullable;

public class LookAtEntityGoal extends Goal {
   public static final float DEFAULT_CHANCE = 0.02F;
   protected final MobEntity mob;
   @Nullable
   protected Entity target;
   protected final float range;
   private int lookTime;
   protected final float chance;
   private final boolean lookForward;
   protected final Class targetType;
   protected final TargetPredicate targetPredicate;

   public LookAtEntityGoal(MobEntity mob, Class targetType, float range) {
      this(mob, targetType, range, 0.02F);
   }

   public LookAtEntityGoal(MobEntity mob, Class targetType, float range, float chance) {
      this(mob, targetType, range, chance, false);
   }

   public LookAtEntityGoal(MobEntity mob, Class targetType, float range, float chance, boolean lookForward) {
      this.mob = mob;
      this.targetType = targetType;
      this.range = range;
      this.chance = chance;
      this.lookForward = lookForward;
      this.setControls(EnumSet.of(Goal.Control.LOOK));
      if (targetType == PlayerEntity.class) {
         this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance((double)range).setPredicate((entity) -> {
            return EntityPredicates.rides(mob).test(entity);
         });
      } else {
         this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance((double)range);
      }

   }

   public boolean canStart() {
      if (this.mob.getRandom().nextFloat() >= this.chance) {
         return false;
      } else {
         if (this.mob.getTarget() != null) {
            this.target = this.mob.getTarget();
         }

         if (this.targetType == PlayerEntity.class) {
            this.target = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
         } else {
            this.target = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand((double)this.range, 3.0, (double)this.range), (arg) -> {
               return true;
            }), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
         }

         return this.target != null;
      }
   }

   public boolean shouldContinue() {
      if (!this.target.isAlive()) {
         return false;
      } else if (this.mob.squaredDistanceTo(this.target) > (double)(this.range * this.range)) {
         return false;
      } else {
         return this.lookTime > 0;
      }
   }

   public void start() {
      this.lookTime = this.getTickCount(40 + this.mob.getRandom().nextInt(40));
   }

   public void stop() {
      this.target = null;
   }

   public void tick() {
      if (this.target.isAlive()) {
         double d = this.lookForward ? this.mob.getEyeY() : this.target.getEyeY();
         this.mob.getLookControl().lookAt(this.target.getX(), d, this.target.getZ());
         --this.lookTime;
      }
   }
}
