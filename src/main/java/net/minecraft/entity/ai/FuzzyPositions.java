package net.minecraft.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class FuzzyPositions {
   private static final int GAUSS_RANGE = 10;

   public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange) {
      int k = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
      int l = random.nextInt(2 * verticalRange + 1) - verticalRange;
      int m = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
      return new BlockPos(k, l, m);
   }

   @Nullable
   public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double angleRange) {
      double g = MathHelper.atan2(directionZ, directionX) - 1.5707963705062866;
      double h = g + (double)(2.0F * random.nextFloat() - 1.0F) * angleRange;
      double l = Math.sqrt(random.nextDouble()) * (double)MathHelper.SQUARE_ROOT_OF_TWO * (double)horizontalRange;
      double m = -l * Math.sin(h);
      double n = l * Math.cos(h);
      if (!(Math.abs(m) > (double)horizontalRange) && !(Math.abs(n) > (double)horizontalRange)) {
         int o = random.nextInt(2 * verticalRange + 1) - verticalRange + startHeight;
         return BlockPos.ofFloored(m, (double)o, n);
      } else {
         return null;
      }
   }

   @VisibleForTesting
   public static BlockPos upWhile(BlockPos pos, int maxY, Predicate condition) {
      if (!condition.test(pos)) {
         return pos;
      } else {
         BlockPos lv;
         for(lv = pos.up(); lv.getY() < maxY && condition.test(lv); lv = lv.up()) {
         }

         return lv;
      }
   }

   @VisibleForTesting
   public static BlockPos upWhile(BlockPos pos, int extraAbove, int max, Predicate condition) {
      if (extraAbove < 0) {
         throw new IllegalArgumentException("aboveSolidAmount was " + extraAbove + ", expected >= 0");
      } else if (!condition.test(pos)) {
         return pos;
      } else {
         BlockPos lv;
         for(lv = pos.up(); lv.getY() < max && condition.test(lv); lv = lv.up()) {
         }

         BlockPos lv2;
         BlockPos lv3;
         for(lv2 = lv; lv2.getY() < max && lv2.getY() - lv.getY() < extraAbove; lv2 = lv3) {
            lv3 = lv2.up();
            if (condition.test(lv3)) {
               break;
            }
         }

         return lv2;
      }
   }

   @Nullable
   public static Vec3d guessBestPathTarget(PathAwareEntity entity, Supplier factory) {
      Objects.requireNonNull(entity);
      return guessBest(factory, entity::getPathfindingFavor);
   }

   @Nullable
   public static Vec3d guessBest(Supplier factory, ToDoubleFunction scorer) {
      double d = Double.NEGATIVE_INFINITY;
      BlockPos lv = null;

      for(int i = 0; i < 10; ++i) {
         BlockPos lv2 = (BlockPos)factory.get();
         if (lv2 != null) {
            double e = scorer.applyAsDouble(lv2);
            if (e > d) {
               d = e;
               lv = lv2;
            }
         }
      }

      return lv != null ? Vec3d.ofBottomCenter(lv) : null;
   }

   public static BlockPos towardTarget(PathAwareEntity entity, int horizontalRange, Random random, BlockPos fuzz) {
      int j = fuzz.getX();
      int k = fuzz.getZ();
      if (entity.hasPositionTarget() && horizontalRange > 1) {
         BlockPos lv = entity.getPositionTarget();
         if (entity.getX() > (double)lv.getX()) {
            j -= random.nextInt(horizontalRange / 2);
         } else {
            j += random.nextInt(horizontalRange / 2);
         }

         if (entity.getZ() > (double)lv.getZ()) {
            k -= random.nextInt(horizontalRange / 2);
         } else {
            k += random.nextInt(horizontalRange / 2);
         }
      }

      return BlockPos.ofFloored((double)j + entity.getX(), (double)fuzz.getY() + entity.getY(), (double)k + entity.getZ());
   }
}
