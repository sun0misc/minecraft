package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

public class VillagerWalkTowardsTask {
   public static SingleTickTask create(MemoryModuleType destination, float speed, int completionRange, int maxDistance, int maxRunTime) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(destination)).apply(context, (cantReachWalkTargetSince, walkTarget, destinationResult) -> {
            return (world, entity, time) -> {
               GlobalPos lv = (GlobalPos)context.getValue(destinationResult);
               Optional optional = context.getOptionalValue(cantReachWalkTargetSince);
               if (lv.getDimension() == world.getRegistryKey() && (!optional.isPresent() || world.getTime() - (Long)optional.get() <= (long)maxRunTime)) {
                  if (lv.getPos().getManhattanDistance(entity.getBlockPos()) > maxDistance) {
                     Vec3d lv2 = null;
                     int m = 0;
                     int n = true;

                     while(lv2 == null || BlockPos.ofFloored(lv2).getManhattanDistance(entity.getBlockPos()) > maxDistance) {
                        lv2 = NoPenaltyTargeting.findTo(entity, 15, 7, Vec3d.ofBottomCenter(lv.getPos()), 1.5707963705062866);
                        ++m;
                        if (m == 1000) {
                           entity.releaseTicketFor(destination);
                           destinationResult.forget();
                           cantReachWalkTargetSince.remember((Object)time);
                           return true;
                        }
                     }

                     walkTarget.remember((Object)(new WalkTarget(lv2, speed, completionRange)));
                  } else if (lv.getPos().getManhattanDistance(entity.getBlockPos()) > completionRange) {
                     walkTarget.remember((Object)(new WalkTarget(lv.getPos(), speed, completionRange)));
                  }
               } else {
                  entity.releaseTicketFor(destination);
                  destinationResult.forget();
                  cantReachWalkTargetSince.remember((Object)time);
               }

               return true;
            };
         });
      });
   }
}
