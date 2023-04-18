package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

public class FindWalkTargetTask {
   private static final int DEFAULT_HORIZONTAL_RANGE = 10;
   private static final int DEFAULT_VERTICAL_RANGE = 7;

   public static SingleTickTask create(float walkSpeed) {
      return create(walkSpeed, 10, 7);
   }

   public static SingleTickTask create(float walkSpeed, int horizontalRange, int verticalRange) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (walkTarget) -> {
            return (world, entity, time) -> {
               BlockPos lv = entity.getBlockPos();
               Vec3d lv2;
               if (world.isNearOccupiedPointOfInterest(lv)) {
                  lv2 = FuzzyTargeting.find(entity, horizontalRange, verticalRange);
               } else {
                  ChunkSectionPos lv3 = ChunkSectionPos.from(lv);
                  ChunkSectionPos lv4 = LookTargetUtil.getPosClosestToOccupiedPointOfInterest(world, lv3, 2);
                  if (lv4 != lv3) {
                     lv2 = NoPenaltyTargeting.findTo(entity, horizontalRange, verticalRange, Vec3d.ofBottomCenter(lv4.getCenterPos()), 1.5707963705062866);
                  } else {
                     lv2 = FuzzyTargeting.find(entity, horizontalRange, verticalRange);
                  }
               }

               walkTarget.remember(Optional.ofNullable(lv2).map((pos) -> {
                  return new WalkTarget(pos, walkSpeed, 0);
               }));
               return true;
            };
         });
      });
   }
}
