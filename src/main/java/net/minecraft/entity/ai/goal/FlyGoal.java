package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FlyGoal extends WanderAroundFarGoal {
   public FlyGoal(PathAwareEntity arg, double d) {
      super(arg, d);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      Vec3d lv = this.mob.getRotationVec(0.0F);
      int i = true;
      Vec3d lv2 = AboveGroundTargeting.find(this.mob, 8, 7, lv.x, lv.z, 1.5707964F, 3, 1);
      return lv2 != null ? lv2 : NoPenaltySolidTargeting.find(this.mob, 8, 4, -2, lv.x, lv.z, 1.5707963705062866);
   }
}
