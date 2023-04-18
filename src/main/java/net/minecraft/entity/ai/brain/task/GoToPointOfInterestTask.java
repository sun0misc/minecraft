package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;

public class GoToPointOfInterestTask {
   public static Task create(float speed, int completionRange) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (walkTarget) -> {
            return (world, entity, time) -> {
               if (world.isNearOccupiedPointOfInterest(entity.getBlockPos())) {
                  return false;
               } else {
                  PointOfInterestStorage lv = world.getPointOfInterestStorage();
                  int j = lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(entity.getBlockPos()));
                  Vec3d lv2 = null;

                  for(int k = 0; k < 5; ++k) {
                     Vec3d lv3 = FuzzyTargeting.find(entity, 15, 7, (pos) -> {
                        return (double)(-lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(pos)));
                     });
                     if (lv3 != null) {
                        int m = lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(BlockPos.ofFloored(lv3)));
                        if (m < j) {
                           lv2 = lv3;
                           break;
                        }

                        if (m == j) {
                           lv2 = lv3;
                        }
                     }
                  }

                  if (lv2 != null) {
                     walkTarget.remember((Object)(new WalkTarget(lv2, speed, completionRange)));
                  }

                  return true;
               }
            };
         });
      });
   }
}
