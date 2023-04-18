package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;

public class UntamedActiveTargetGoal extends ActiveTargetGoal {
   private final TameableEntity tameable;

   public UntamedActiveTargetGoal(TameableEntity tameable, Class targetClass, boolean checkVisibility, @Nullable Predicate targetPredicate) {
      super(tameable, targetClass, 10, checkVisibility, false, targetPredicate);
      this.tameable = tameable;
   }

   public boolean canStart() {
      return !this.tameable.isTamed() && super.canStart();
   }

   public boolean shouldContinue() {
      return this.targetPredicate != null ? this.targetPredicate.test(this.mob, this.targetEntity) : super.shouldContinue();
   }
}
