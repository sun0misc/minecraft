package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.mob.MobEntity;

public class StopAndLookAtEntityGoal extends LookAtEntityGoal {
   public StopAndLookAtEntityGoal(MobEntity arg, Class class_, float f) {
      super(arg, class_, f);
      this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
   }

   public StopAndLookAtEntityGoal(MobEntity arg, Class class_, float f, float g) {
      super(arg, class_, f, g);
      this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
   }
}
