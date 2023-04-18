package net.minecraft.entity;

import java.util.Iterator;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import org.jetbrains.annotations.Nullable;

public class Dismounting {
   public static int[][] getDismountOffsets(Direction movementDirection) {
      Direction lv = movementDirection.rotateYClockwise();
      Direction lv2 = lv.getOpposite();
      Direction lv3 = movementDirection.getOpposite();
      return new int[][]{{lv.getOffsetX(), lv.getOffsetZ()}, {lv2.getOffsetX(), lv2.getOffsetZ()}, {lv3.getOffsetX() + lv.getOffsetX(), lv3.getOffsetZ() + lv.getOffsetZ()}, {lv3.getOffsetX() + lv2.getOffsetX(), lv3.getOffsetZ() + lv2.getOffsetZ()}, {movementDirection.getOffsetX() + lv.getOffsetX(), movementDirection.getOffsetZ() + lv.getOffsetZ()}, {movementDirection.getOffsetX() + lv2.getOffsetX(), movementDirection.getOffsetZ() + lv2.getOffsetZ()}, {lv3.getOffsetX(), lv3.getOffsetZ()}, {movementDirection.getOffsetX(), movementDirection.getOffsetZ()}};
   }

   public static boolean canDismountInBlock(double height) {
      return !Double.isInfinite(height) && height < 1.0;
   }

   public static boolean canPlaceEntityAt(CollisionView world, LivingEntity entity, Box targetBox) {
      Iterable iterable = world.getBlockCollisions(entity, targetBox);
      Iterator var4 = iterable.iterator();

      VoxelShape lv;
      do {
         if (!var4.hasNext()) {
            if (!world.getWorldBorder().contains(targetBox)) {
               return false;
            }

            return true;
         }

         lv = (VoxelShape)var4.next();
      } while(lv.isEmpty());

      return false;
   }

   public static boolean canPlaceEntityAt(CollisionView world, Vec3d offset, LivingEntity entity, EntityPose pose) {
      return canPlaceEntityAt(world, entity, entity.getBoundingBox(pose).offset(offset));
   }

   public static VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return !lv.isIn(BlockTags.CLIMBABLE) && (!(lv.getBlock() instanceof TrapdoorBlock) || !(Boolean)lv.get(TrapdoorBlock.OPEN)) ? lv.getCollisionShape(world, pos) : VoxelShapes.empty();
   }

   public static double getCeilingHeight(BlockPos pos, int maxDistance, Function collisionShapeGetter) {
      BlockPos.Mutable lv = pos.mutableCopy();
      int j = 0;

      while(j < maxDistance) {
         VoxelShape lv2 = (VoxelShape)collisionShapeGetter.apply(lv);
         if (!lv2.isEmpty()) {
            return (double)(pos.getY() + j) + lv2.getMin(Direction.Axis.Y);
         }

         ++j;
         lv.move(Direction.UP);
      }

      return Double.POSITIVE_INFINITY;
   }

   @Nullable
   public static Vec3d findRespawnPos(EntityType entityType, CollisionView world, BlockPos pos, boolean ignoreInvalidPos) {
      if (ignoreInvalidPos && entityType.isInvalidSpawn(world.getBlockState(pos))) {
         return null;
      } else {
         double d = world.getDismountHeight(getCollisionShape(world, pos), () -> {
            return getCollisionShape(world, pos.down());
         });
         if (!canDismountInBlock(d)) {
            return null;
         } else if (ignoreInvalidPos && d <= 0.0 && entityType.isInvalidSpawn(world.getBlockState(pos.down()))) {
            return null;
         } else {
            Vec3d lv = Vec3d.ofCenter(pos, d);
            Box lv2 = entityType.getDimensions().getBoxAt(lv);
            Iterable iterable = world.getBlockCollisions((Entity)null, lv2);
            Iterator var9 = iterable.iterator();

            while(var9.hasNext()) {
               VoxelShape lv3 = (VoxelShape)var9.next();
               if (!lv3.isEmpty()) {
                  return null;
               }
            }

            if (entityType != EntityType.PLAYER || !world.getBlockState(pos).isIn(BlockTags.INVALID_SPAWN_INSIDE) && !world.getBlockState(pos.up()).isIn(BlockTags.INVALID_SPAWN_INSIDE)) {
               return !world.getWorldBorder().contains(lv2) ? null : lv;
            } else {
               return null;
            }
         }
      }
   }
}
