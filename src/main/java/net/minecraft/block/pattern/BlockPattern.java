package net.minecraft.block.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BlockPattern {
   private final Predicate[][][] pattern;
   private final int depth;
   private final int height;
   private final int width;

   public BlockPattern(Predicate[][][] pattern) {
      this.pattern = pattern;
      this.depth = pattern.length;
      if (this.depth > 0) {
         this.height = pattern[0].length;
         if (this.height > 0) {
            this.width = pattern[0][0].length;
         } else {
            this.width = 0;
         }
      } else {
         this.height = 0;
         this.width = 0;
      }

   }

   public int getDepth() {
      return this.depth;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   @VisibleForTesting
   public Predicate[][][] getPattern() {
      return this.pattern;
   }

   @Nullable
   @VisibleForTesting
   public Result testTransform(WorldView world, BlockPos frontTopLeft, Direction forwards, Direction up) {
      LoadingCache loadingCache = makeCache(world, false);
      return this.testTransform(frontTopLeft, forwards, up, loadingCache);
   }

   @Nullable
   private Result testTransform(BlockPos frontTopLeft, Direction forwards, Direction up, LoadingCache cache) {
      for(int i = 0; i < this.width; ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               if (!this.pattern[k][j][i].test((CachedBlockPosition)cache.getUnchecked(translate(frontTopLeft, forwards, up, i, j, k)))) {
                  return null;
               }
            }
         }
      }

      return new Result(frontTopLeft, forwards, up, cache, this.width, this.height, this.depth);
   }

   @Nullable
   public Result searchAround(WorldView world, BlockPos pos) {
      LoadingCache loadingCache = makeCache(world, false);
      int i = Math.max(Math.max(this.width, this.height), this.depth);
      Iterator var5 = BlockPos.iterate(pos, pos.add(i - 1, i - 1, i - 1)).iterator();

      while(var5.hasNext()) {
         BlockPos lv = (BlockPos)var5.next();
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction lv2 = var7[var9];
            Direction[] var11 = Direction.values();
            int var12 = var11.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               Direction lv3 = var11[var13];
               if (lv3 != lv2 && lv3 != lv2.getOpposite()) {
                  Result lv4 = this.testTransform(lv, lv2, lv3, loadingCache);
                  if (lv4 != null) {
                     return lv4;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache makeCache(WorldView world, boolean forceLoad) {
      return CacheBuilder.newBuilder().build(new BlockStateCacheLoader(world, forceLoad));
   }

   protected static BlockPos translate(BlockPos pos, Direction forwards, Direction up, int offsetLeft, int offsetDown, int offsetForwards) {
      if (forwards != up && forwards != up.getOpposite()) {
         Vec3i lv = new Vec3i(forwards.getOffsetX(), forwards.getOffsetY(), forwards.getOffsetZ());
         Vec3i lv2 = new Vec3i(up.getOffsetX(), up.getOffsetY(), up.getOffsetZ());
         Vec3i lv3 = lv.crossProduct(lv2);
         return pos.add(lv2.getX() * -offsetDown + lv3.getX() * offsetLeft + lv.getX() * offsetForwards, lv2.getY() * -offsetDown + lv3.getY() * offsetLeft + lv.getY() * offsetForwards, lv2.getZ() * -offsetDown + lv3.getZ() * offsetLeft + lv.getZ() * offsetForwards);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   public static class Result {
      private final BlockPos frontTopLeft;
      private final Direction forwards;
      private final Direction up;
      private final LoadingCache cache;
      private final int width;
      private final int height;
      private final int depth;

      public Result(BlockPos frontTopLeft, Direction forwards, Direction up, LoadingCache cache, int width, int height, int depth) {
         this.frontTopLeft = frontTopLeft;
         this.forwards = forwards;
         this.up = up;
         this.cache = cache;
         this.width = width;
         this.height = height;
         this.depth = depth;
      }

      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public Direction getForwards() {
         return this.forwards;
      }

      public Direction getUp() {
         return this.up;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public int getDepth() {
         return this.depth;
      }

      public CachedBlockPosition translate(int offsetLeft, int offsetDown, int offsetForwards) {
         return (CachedBlockPosition)this.cache.getUnchecked(BlockPattern.translate(this.frontTopLeft, this.getForwards(), this.getUp(), offsetLeft, offsetDown, offsetForwards));
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }
   }

   static class BlockStateCacheLoader extends CacheLoader {
      private final WorldView world;
      private final boolean forceLoad;

      public BlockStateCacheLoader(WorldView world, boolean forceLoad) {
         this.world = world;
         this.forceLoad = forceLoad;
      }

      public CachedBlockPosition load(BlockPos arg) {
         return new CachedBlockPosition(this.world, arg, this.forceLoad);
      }

      // $FF: synthetic method
      public Object load(Object pos) throws Exception {
         return this.load((BlockPos)pos);
      }
   }
}
