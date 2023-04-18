package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class SeekSkyTask {
   public static SingleTickTask create(float speed) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (walkTarget) -> {
            return (world, entity, time) -> {
               if (world.isSkyVisible(entity.getBlockPos())) {
                  return false;
               } else {
                  Optional optional = Optional.ofNullable(findNearbySky(world, entity));
                  optional.ifPresent((pos) -> {
                     walkTarget.remember((Object)(new WalkTarget(pos, speed, 0)));
                  });
                  return true;
               }
            };
         });
      });
   }

   @Nullable
   private static Vec3d findNearbySky(ServerWorld world, LivingEntity entity) {
      Random lv = entity.getRandom();
      BlockPos lv2 = entity.getBlockPos();

      for(int i = 0; i < 10; ++i) {
         BlockPos lv3 = lv2.add(lv.nextInt(20) - 10, lv.nextInt(6) - 3, lv.nextInt(20) - 10);
         if (isSkyVisible(world, entity, lv3)) {
            return Vec3d.ofBottomCenter(lv3);
         }
      }

      return null;
   }

   public static boolean isSkyVisible(ServerWorld world, LivingEntity entity, BlockPos pos) {
      return world.isSkyVisible(pos) && (double)world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() <= entity.getY();
   }
}
