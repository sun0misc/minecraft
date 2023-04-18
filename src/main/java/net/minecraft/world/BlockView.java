package net.minecraft.world;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface BlockView extends HeightLimitView {
   @Nullable
   BlockEntity getBlockEntity(BlockPos pos);

   default Optional getBlockEntity(BlockPos pos, BlockEntityType type) {
      BlockEntity lv = this.getBlockEntity(pos);
      return lv != null && lv.getType() == type ? Optional.of(lv) : Optional.empty();
   }

   BlockState getBlockState(BlockPos pos);

   FluidState getFluidState(BlockPos pos);

   default int getLuminance(BlockPos pos) {
      return this.getBlockState(pos).getLuminance();
   }

   default int getMaxLightLevel() {
      return 15;
   }

   default Stream getStatesInBox(Box box) {
      return BlockPos.stream(box).map(this::getBlockState);
   }

   default BlockHitResult raycast(BlockStateRaycastContext context) {
      return (BlockHitResult)raycast(context.getStart(), context.getEnd(), context, (contextx, pos) -> {
         BlockState lv = this.getBlockState(pos);
         Vec3d lv2 = contextx.getStart().subtract(contextx.getEnd());
         return contextx.getStatePredicate().test(lv) ? new BlockHitResult(contextx.getEnd(), Direction.getFacing(lv2.x, lv2.y, lv2.z), BlockPos.ofFloored(contextx.getEnd()), false) : null;
      }, (contextx) -> {
         Vec3d lv = contextx.getStart().subtract(contextx.getEnd());
         return BlockHitResult.createMissed(contextx.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(contextx.getEnd()));
      });
   }

   default BlockHitResult raycast(RaycastContext context) {
      return (BlockHitResult)raycast(context.getStart(), context.getEnd(), context, (contextx, pos) -> {
         BlockState lv = this.getBlockState(pos);
         FluidState lv2 = this.getFluidState(pos);
         Vec3d lv3 = contextx.getStart();
         Vec3d lv4 = contextx.getEnd();
         VoxelShape lv5 = contextx.getBlockShape(lv, this, pos);
         BlockHitResult lv6 = this.raycastBlock(lv3, lv4, pos, lv5, lv);
         VoxelShape lv7 = contextx.getFluidShape(lv2, this, pos);
         BlockHitResult lv8 = lv7.raycast(lv3, lv4, pos);
         double d = lv6 == null ? Double.MAX_VALUE : contextx.getStart().squaredDistanceTo(lv6.getPos());
         double e = lv8 == null ? Double.MAX_VALUE : contextx.getStart().squaredDistanceTo(lv8.getPos());
         return d <= e ? lv6 : lv8;
      }, (contextx) -> {
         Vec3d lv = contextx.getStart().subtract(contextx.getEnd());
         return BlockHitResult.createMissed(contextx.getEnd(), Direction.getFacing(lv.x, lv.y, lv.z), BlockPos.ofFloored(contextx.getEnd()));
      });
   }

   @Nullable
   default BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
      BlockHitResult lv = shape.raycast(start, end, pos);
      if (lv != null) {
         BlockHitResult lv2 = state.getRaycastShape(this, pos).raycast(start, end, pos);
         if (lv2 != null && lv2.getPos().subtract(start).lengthSquared() < lv.getPos().subtract(start).lengthSquared()) {
            return lv.withSide(lv2.getSide());
         }
      }

      return lv;
   }

   default double getDismountHeight(VoxelShape blockCollisionShape, Supplier belowBlockCollisionShapeGetter) {
      if (!blockCollisionShape.isEmpty()) {
         return blockCollisionShape.getMax(Direction.Axis.Y);
      } else {
         double d = ((VoxelShape)belowBlockCollisionShapeGetter.get()).getMax(Direction.Axis.Y);
         return d >= 1.0 ? d - 1.0 : Double.NEGATIVE_INFINITY;
      }
   }

   default double getDismountHeight(BlockPos pos) {
      return this.getDismountHeight(this.getBlockState(pos).getCollisionShape(this, pos), () -> {
         BlockPos lv = pos.down();
         return this.getBlockState(lv).getCollisionShape(this, lv);
      });
   }

   static Object raycast(Vec3d start, Vec3d end, Object context, BiFunction blockHitFactory, Function missFactory) {
      if (start.equals(end)) {
         return missFactory.apply(context);
      } else {
         double d = MathHelper.lerp(-1.0E-7, end.x, start.x);
         double e = MathHelper.lerp(-1.0E-7, end.y, start.y);
         double f = MathHelper.lerp(-1.0E-7, end.z, start.z);
         double g = MathHelper.lerp(-1.0E-7, start.x, end.x);
         double h = MathHelper.lerp(-1.0E-7, start.y, end.y);
         double i = MathHelper.lerp(-1.0E-7, start.z, end.z);
         int j = MathHelper.floor(g);
         int k = MathHelper.floor(h);
         int l = MathHelper.floor(i);
         BlockPos.Mutable lv = new BlockPos.Mutable(j, k, l);
         Object object2 = blockHitFactory.apply(context, lv);
         if (object2 != null) {
            return object2;
         } else {
            double m = d - g;
            double n = e - h;
            double o = f - i;
            int p = MathHelper.sign(m);
            int q = MathHelper.sign(n);
            int r = MathHelper.sign(o);
            double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
            double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
            double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
            double v = s * (p > 0 ? 1.0 - MathHelper.fractionalPart(g) : MathHelper.fractionalPart(g));
            double w = t * (q > 0 ? 1.0 - MathHelper.fractionalPart(h) : MathHelper.fractionalPart(h));
            double x = u * (r > 0 ? 1.0 - MathHelper.fractionalPart(i) : MathHelper.fractionalPart(i));

            Object object3;
            do {
               if (!(v <= 1.0) && !(w <= 1.0) && !(x <= 1.0)) {
                  return missFactory.apply(context);
               }

               if (v < w) {
                  if (v < x) {
                     j += p;
                     v += s;
                  } else {
                     l += r;
                     x += u;
                  }
               } else if (w < x) {
                  k += q;
                  w += t;
               } else {
                  l += r;
                  x += u;
               }

               object3 = blockHitFactory.apply(context, lv.set(j, k, l));
            } while(object3 == null);

            return object3;
         }
      }
   }
}
