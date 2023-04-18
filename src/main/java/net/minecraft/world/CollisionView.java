package net.minecraft.world;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

public interface CollisionView extends BlockView {
   WorldBorder getWorldBorder();

   @Nullable
   BlockView getChunkAsView(int chunkX, int chunkZ);

   default boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
      return true;
   }

   default boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
      VoxelShape lv = state.getCollisionShape(this, pos, context);
      return lv.isEmpty() || this.doesNotIntersectEntities((Entity)null, lv.offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()));
   }

   default boolean doesNotIntersectEntities(Entity entity) {
      return this.doesNotIntersectEntities(entity, VoxelShapes.cuboid(entity.getBoundingBox()));
   }

   default boolean isSpaceEmpty(Box box) {
      return this.isSpaceEmpty((Entity)null, box);
   }

   default boolean isSpaceEmpty(Entity entity) {
      return this.isSpaceEmpty(entity, entity.getBoundingBox());
   }

   default boolean isSpaceEmpty(@Nullable Entity entity, Box box) {
      Iterator var3 = this.getBlockCollisions(entity, box).iterator();

      while(var3.hasNext()) {
         VoxelShape lv = (VoxelShape)var3.next();
         if (!lv.isEmpty()) {
            return false;
         }
      }

      if (!this.getEntityCollisions(entity, box).isEmpty()) {
         return false;
      } else if (entity == null) {
         return true;
      } else {
         VoxelShape lv2 = this.getWorldBorderCollisions(entity, box);
         return lv2 == null || !VoxelShapes.matchesAnywhere(lv2, VoxelShapes.cuboid(box), BooleanBiFunction.AND);
      }
   }

   List getEntityCollisions(@Nullable Entity entity, Box box);

   default Iterable getCollisions(@Nullable Entity entity, Box box) {
      List list = this.getEntityCollisions(entity, box);
      Iterable iterable = this.getBlockCollisions(entity, box);
      return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
   }

   default Iterable getBlockCollisions(@Nullable Entity entity, Box box) {
      return () -> {
         return new BlockCollisionSpliterator(this, entity, box);
      };
   }

   @Nullable
   private VoxelShape getWorldBorderCollisions(Entity entity, Box box) {
      WorldBorder lv = this.getWorldBorder();
      return lv.canCollide(entity, box) ? lv.asVoxelShape() : null;
   }

   default boolean canCollide(@Nullable Entity entity, Box box) {
      BlockCollisionSpliterator lv = new BlockCollisionSpliterator(this, entity, box, true);

      do {
         if (!lv.hasNext()) {
            return false;
         }
      } while(((VoxelShape)lv.next()).isEmpty());

      return true;
   }

   default Optional findClosestCollision(@Nullable Entity entity, VoxelShape shape, Vec3d target, double x, double y, double z) {
      if (shape.isEmpty()) {
         return Optional.empty();
      } else {
         Box lv = shape.getBoundingBox().expand(x, y, z);
         VoxelShape lv2 = (VoxelShape)StreamSupport.stream(this.getBlockCollisions(entity, lv).spliterator(), false).filter((arg) -> {
            return this.getWorldBorder() == null || this.getWorldBorder().contains(arg.getBoundingBox());
         }).flatMap((arg) -> {
            return arg.getBoundingBoxes().stream();
         }).map((arg) -> {
            return arg.expand(x / 2.0, y / 2.0, z / 2.0);
         }).map(VoxelShapes::cuboid).reduce(VoxelShapes.empty(), VoxelShapes::union);
         VoxelShape lv3 = VoxelShapes.combineAndSimplify(shape, lv2, BooleanBiFunction.ONLY_FIRST);
         return lv3.getClosestPointTo(target);
      }
   }
}
