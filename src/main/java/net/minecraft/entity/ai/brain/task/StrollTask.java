package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class StrollTask {
   private static final int DEFAULT_HORIZONTAL_RADIUS = 10;
   private static final int DEFAULT_VERTICAL_RADIUS = 7;
   private static final int[][] RADII = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

   public static SingleTickTask create(float speed) {
      return create(speed, true);
   }

   public static SingleTickTask create(float speed, boolean strollInsideWater) {
      return create(speed, (entity) -> {
         return FuzzyTargeting.find(entity, 10, 7);
      }, strollInsideWater ? (entity) -> {
         return true;
      } : (entity) -> {
         return !entity.isInsideWaterOrBubbleColumn();
      });
   }

   public static Task create(float speed, int horizontalRadius, int verticalRadius) {
      return create(speed, (entity) -> {
         return FuzzyTargeting.find(entity, horizontalRadius, verticalRadius);
      }, (entity) -> {
         return true;
      });
   }

   public static Task createSolidTargeting(float speed) {
      return create(speed, (entity) -> {
         return findTargetPos(entity, 10, 7);
      }, (entity) -> {
         return true;
      });
   }

   public static Task createDynamicRadius(float speed) {
      return create(speed, StrollTask::findTargetPos, Entity::isInsideWaterOrBubbleColumn);
   }

   private static SingleTickTask create(float speed, Function targetGetter, Predicate shouldRun) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (walkTarget) -> {
            return (world, entity, time) -> {
               if (!shouldRun.test(entity)) {
                  return false;
               } else {
                  Optional optional = Optional.ofNullable((Vec3d)targetGetter.apply(entity));
                  walkTarget.remember(optional.map((pos) -> {
                     return new WalkTarget(pos, speed, 0);
                  }));
                  return true;
               }
            };
         });
      });
   }

   @Nullable
   private static Vec3d findTargetPos(PathAwareEntity entity) {
      Vec3d lv = null;
      Vec3d lv2 = null;
      int[][] var3 = RADII;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int[] is = var3[var5];
         if (lv == null) {
            lv2 = LookTargetUtil.find(entity, is[0], is[1]);
         } else {
            lv2 = entity.getPos().add(entity.getPos().relativize(lv).normalize().multiply((double)is[0], (double)is[1], (double)is[0]));
         }

         if (lv2 == null || entity.world.getFluidState(BlockPos.ofFloored(lv2)).isEmpty()) {
            return lv;
         }

         lv = lv2;
      }

      return lv2;
   }

   @Nullable
   private static Vec3d findTargetPos(PathAwareEntity entity, int horizontalRadius, int verticalRadius) {
      Vec3d lv = entity.getRotationVec(0.0F);
      return NoPenaltySolidTargeting.find(entity, horizontalRadius, verticalRadius, -2, lv.x, lv.z, 1.5707963705062866);
   }
}
