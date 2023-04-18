package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SwimAroundGoal extends WanderAroundGoal {
   public SwimAroundGoal(PathAwareEntity arg, double d, int i) {
      super(arg, d, i);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      return LookTargetUtil.find(this.mob, 10, 7);
   }
}
