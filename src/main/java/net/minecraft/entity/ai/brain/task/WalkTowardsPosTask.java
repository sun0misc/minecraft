package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class WalkTowardsPosTask {
   private static BlockPos fuzz(MobEntity mob, BlockPos pos) {
      Random lv = mob.world.random;
      return pos.add(fuzz(lv), 0, fuzz(lv));
   }

   private static int fuzz(Random random) {
      return random.nextInt(3) - 1;
   }

   public static SingleTickTask create(MemoryModuleType posModule, int completionRange, float speed) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(posModule), context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (pos, attackTarget, walkTarget, lookTarget) -> {
            return (world, entity, time) -> {
               BlockPos lv = (BlockPos)context.getValue(pos);
               boolean bl = lv.isWithinDistance(entity.getBlockPos(), (double)completionRange);
               if (!bl) {
                  LookTargetUtil.walkTowards(entity, (BlockPos)fuzz(entity, lv), speed, completionRange);
               }

               return true;
            };
         });
      });
   }
}
