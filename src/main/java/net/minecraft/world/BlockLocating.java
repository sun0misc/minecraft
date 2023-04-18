package net.minecraft.world;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockLocating {
   public static Rectangle getLargestRectangle(BlockPos center, Direction.Axis primaryAxis, int primaryMaxBlocks, Direction.Axis secondaryAxis, int secondaryMaxBlocks, Predicate predicate) {
      BlockPos.Mutable lv = center.mutableCopy();
      Direction lv2 = Direction.get(Direction.AxisDirection.NEGATIVE, primaryAxis);
      Direction lv3 = lv2.getOpposite();
      Direction lv4 = Direction.get(Direction.AxisDirection.NEGATIVE, secondaryAxis);
      Direction lv5 = lv4.getOpposite();
      int k = moveWhile(predicate, lv.set(center), lv2, primaryMaxBlocks);
      int l = moveWhile(predicate, lv.set(center), lv3, primaryMaxBlocks);
      int m = k;
      IntBounds[] lvs = new IntBounds[k + 1 + l];
      lvs[k] = new IntBounds(moveWhile(predicate, lv.set(center), lv4, secondaryMaxBlocks), moveWhile(predicate, lv.set(center), lv5, secondaryMaxBlocks));
      int n = lvs[k].min;

      int o;
      IntBounds lv6;
      for(o = 1; o <= k; ++o) {
         lv6 = lvs[m - (o - 1)];
         lvs[m - o] = new IntBounds(moveWhile(predicate, lv.set(center).move(lv2, o), lv4, lv6.min), moveWhile(predicate, lv.set(center).move(lv2, o), lv5, lv6.max));
      }

      for(o = 1; o <= l; ++o) {
         lv6 = lvs[m + o - 1];
         lvs[m + o] = new IntBounds(moveWhile(predicate, lv.set(center).move(lv3, o), lv4, lv6.min), moveWhile(predicate, lv.set(center).move(lv3, o), lv5, lv6.max));
      }

      o = 0;
      int p = 0;
      int q = 0;
      int r = 0;
      int[] is = new int[lvs.length];

      for(int s = n; s >= 0; --s) {
         IntBounds lv7;
         int u;
         int v;
         for(int t = 0; t < lvs.length; ++t) {
            lv7 = lvs[t];
            u = n - lv7.min;
            v = n + lv7.max;
            is[t] = s >= u && s <= v ? v + 1 - s : 0;
         }

         Pair pair = findLargestRectangle(is);
         lv7 = (IntBounds)pair.getFirst();
         u = 1 + lv7.max - lv7.min;
         v = (Integer)pair.getSecond();
         if (u * v > q * r) {
            o = lv7.min;
            p = s;
            q = u;
            r = v;
         }
      }

      return new Rectangle(center.offset(primaryAxis, o - m).offset(secondaryAxis, p - n), q, r);
   }

   private static int moveWhile(Predicate predicate, BlockPos.Mutable pos, Direction direction, int max) {
      int j;
      for(j = 0; j < max && predicate.test(pos.move(direction)); ++j) {
      }

      return j;
   }

   @VisibleForTesting
   static Pair findLargestRectangle(int[] heights) {
      int i = 0;
      int j = 0;
      int k = 0;
      IntStack intStack = new IntArrayList();
      intStack.push(0);

      for(int l = 1; l <= heights.length; ++l) {
         int m = l == heights.length ? 0 : heights[l];

         while(!intStack.isEmpty()) {
            int n = heights[intStack.topInt()];
            if (m >= n) {
               intStack.push(l);
               break;
            }

            intStack.popInt();
            int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
            if (n * (l - o) > k * (j - i)) {
               j = l;
               i = o;
               k = n;
            }
         }

         if (intStack.isEmpty()) {
            intStack.push(l);
         }
      }

      return new Pair(new IntBounds(i, j - 1), k);
   }

   public static Optional findColumnEnd(BlockView world, BlockPos pos, Block intermediateBlock, Direction direction, Block endBlock) {
      BlockPos.Mutable lv = pos.mutableCopy();

      BlockState lv2;
      do {
         lv.move(direction);
         lv2 = world.getBlockState(lv);
      } while(lv2.isOf(intermediateBlock));

      return lv2.isOf(endBlock) ? Optional.of(lv) : Optional.empty();
   }

   public static class IntBounds {
      public final int min;
      public final int max;

      public IntBounds(int min, int max) {
         this.min = min;
         this.max = max;
      }

      public String toString() {
         return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
      }
   }

   public static class Rectangle {
      public final BlockPos lowerLeft;
      public final int width;
      public final int height;

      public Rectangle(BlockPos lowerLeft, int width, int height) {
         this.lowerLeft = lowerLeft;
         this.width = width;
         this.height = height;
      }
   }
}
