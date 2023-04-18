package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

@Unmodifiable
public class BlockPos extends Vec3i {
   public static final Codec CODEC;
   private static final Logger LOGGER;
   public static final BlockPos ORIGIN;
   private static final int SIZE_BITS_X;
   private static final int SIZE_BITS_Z;
   public static final int SIZE_BITS_Y;
   private static final long BITS_X;
   private static final long BITS_Y;
   private static final long BITS_Z;
   private static final int field_33083 = 0;
   private static final int BIT_SHIFT_Z;
   private static final int BIT_SHIFT_X;

   public BlockPos(int i, int j, int k) {
      super(i, j, k);
   }

   public BlockPos(Vec3i pos) {
      this(pos.getX(), pos.getY(), pos.getZ());
   }

   public static long offset(long value, Direction direction) {
      return add(value, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
   }

   public static long add(long value, int x, int y, int z) {
      return asLong(unpackLongX(value) + x, unpackLongY(value) + y, unpackLongZ(value) + z);
   }

   public static int unpackLongX(long packedPos) {
      return (int)(packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_X >> 64 - SIZE_BITS_X);
   }

   public static int unpackLongY(long packedPos) {
      return (int)(packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y);
   }

   public static int unpackLongZ(long packedPos) {
      return (int)(packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_Z >> 64 - SIZE_BITS_Z);
   }

   public static BlockPos fromLong(long packedPos) {
      return new BlockPos(unpackLongX(packedPos), unpackLongY(packedPos), unpackLongZ(packedPos));
   }

   public static BlockPos ofFloored(double x, double y, double z) {
      return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
   }

   public static BlockPos ofFloored(Position pos) {
      return ofFloored(pos.getX(), pos.getY(), pos.getZ());
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int x, int y, int z) {
      long l = 0L;
      l |= ((long)x & BITS_X) << BIT_SHIFT_X;
      l |= ((long)y & BITS_Y) << 0;
      l |= ((long)z & BITS_Z) << BIT_SHIFT_Z;
      return l;
   }

   public static long removeChunkSectionLocalY(long y) {
      return y & -16L;
   }

   public BlockPos add(int i, int j, int k) {
      return i == 0 && j == 0 && k == 0 ? this : new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
   }

   public Vec3d toCenterPos() {
      return Vec3d.ofCenter(this);
   }

   public BlockPos add(Vec3i arg) {
      return this.add(arg.getX(), arg.getY(), arg.getZ());
   }

   public BlockPos subtract(Vec3i arg) {
      return this.add(-arg.getX(), -arg.getY(), -arg.getZ());
   }

   public BlockPos multiply(int i) {
      if (i == 1) {
         return this;
      } else {
         return i == 0 ? ORIGIN : new BlockPos(this.getX() * i, this.getY() * i, this.getZ() * i);
      }
   }

   public BlockPos up() {
      return this.offset(Direction.UP);
   }

   public BlockPos up(int distance) {
      return this.offset(Direction.UP, distance);
   }

   public BlockPos down() {
      return this.offset(Direction.DOWN);
   }

   public BlockPos down(int i) {
      return this.offset(Direction.DOWN, i);
   }

   public BlockPos north() {
      return this.offset(Direction.NORTH);
   }

   public BlockPos north(int distance) {
      return this.offset(Direction.NORTH, distance);
   }

   public BlockPos south() {
      return this.offset(Direction.SOUTH);
   }

   public BlockPos south(int distance) {
      return this.offset(Direction.SOUTH, distance);
   }

   public BlockPos west() {
      return this.offset(Direction.WEST);
   }

   public BlockPos west(int distance) {
      return this.offset(Direction.WEST, distance);
   }

   public BlockPos east() {
      return this.offset(Direction.EAST);
   }

   public BlockPos east(int distance) {
      return this.offset(Direction.EAST, distance);
   }

   public BlockPos offset(Direction arg) {
      return new BlockPos(this.getX() + arg.getOffsetX(), this.getY() + arg.getOffsetY(), this.getZ() + arg.getOffsetZ());
   }

   public BlockPos offset(Direction arg, int i) {
      return i == 0 ? this : new BlockPos(this.getX() + arg.getOffsetX() * i, this.getY() + arg.getOffsetY() * i, this.getZ() + arg.getOffsetZ() * i);
   }

   public BlockPos offset(Direction.Axis arg, int i) {
      if (i == 0) {
         return this;
      } else {
         int j = arg == Direction.Axis.X ? i : 0;
         int k = arg == Direction.Axis.Y ? i : 0;
         int l = arg == Direction.Axis.Z ? i : 0;
         return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
      }
   }

   public BlockPos rotate(BlockRotation rotation) {
      switch (rotation) {
         case NONE:
         default:
            return this;
         case CLOCKWISE_90:
            return new BlockPos(-this.getZ(), this.getY(), this.getX());
         case CLOCKWISE_180:
            return new BlockPos(-this.getX(), this.getY(), -this.getZ());
         case COUNTERCLOCKWISE_90:
            return new BlockPos(this.getZ(), this.getY(), -this.getX());
      }
   }

   public BlockPos crossProduct(Vec3i pos) {
      return new BlockPos(this.getY() * pos.getZ() - this.getZ() * pos.getY(), this.getZ() * pos.getX() - this.getX() * pos.getZ(), this.getX() * pos.getY() - this.getY() * pos.getX());
   }

   public BlockPos withY(int y) {
      return new BlockPos(this.getX(), y, this.getZ());
   }

   public BlockPos toImmutable() {
      return this;
   }

   public Mutable mutableCopy() {
      return new Mutable(this.getX(), this.getY(), this.getZ());
   }

   public static Iterable iterateRandomly(Random random, int count, BlockPos around, int range) {
      return iterateRandomly(random, count, around.getX() - range, around.getY() - range, around.getZ() - range, around.getX() + range, around.getY() + range, around.getZ() + range);
   }

   public static Iterable iterateRandomly(Random random, int count, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      int p = maxX - minX + 1;
      int q = maxY - minY + 1;
      int r = maxZ - minZ + 1;
      return () -> {
         return new AbstractIterator() {
            final Mutable pos = new Mutable();
            int remaining = i;

            protected BlockPos computeNext() {
               if (this.remaining <= 0) {
                  return (BlockPos)this.endOfData();
               } else {
                  BlockPos lv = this.pos.set(j + arg.nextInt(k), l + arg.nextInt(m), n + arg.nextInt(o));
                  --this.remaining;
                  return lv;
               }
            }

            // $FF: synthetic method
            protected Object computeNext() {
               return this.computeNext();
            }
         };
      };
   }

   public static Iterable iterateOutwards(BlockPos center, int rangeX, int rangeY, int rangeZ) {
      int l = rangeX + rangeY + rangeZ;
      int m = center.getX();
      int n = center.getY();
      int o = center.getZ();
      return () -> {
         return new AbstractIterator() {
            private final Mutable pos = new Mutable();
            private int manhattanDistance;
            private int limitX;
            private int limitY;
            private int dx;
            private int dy;
            private boolean swapZ;

            protected BlockPos computeNext() {
               if (this.swapZ) {
                  this.swapZ = false;
                  this.pos.setZ(i - (this.pos.getZ() - i));
                  return this.pos;
               } else {
                  Mutable lv;
                  for(lv = null; lv == null; ++this.dy) {
                     if (this.dy > this.limitY) {
                        ++this.dx;
                        if (this.dx > this.limitX) {
                           ++this.manhattanDistance;
                           if (this.manhattanDistance > j) {
                              return (BlockPos)this.endOfData();
                           }

                           this.limitX = Math.min(k, this.manhattanDistance);
                           this.dx = -this.limitX;
                        }

                        this.limitY = Math.min(l, this.manhattanDistance - Math.abs(this.dx));
                        this.dy = -this.limitY;
                     }

                     int ix = this.dx;
                     int jx = this.dy;
                     int kx = this.manhattanDistance - Math.abs(ix) - Math.abs(jx);
                     if (kx <= m) {
                        this.swapZ = kx != 0;
                        lv = this.pos.set(n + ix, o + jx, i + kx);
                     }
                  }

                  return lv;
               }
            }

            // $FF: synthetic method
            protected Object computeNext() {
               return this.computeNext();
            }
         };
      };
   }

   public static Optional findClosest(BlockPos pos, int horizontalRange, int verticalRange, Predicate condition) {
      Iterator var4 = iterateOutwards(pos, horizontalRange, verticalRange, horizontalRange).iterator();

      BlockPos lv;
      do {
         if (!var4.hasNext()) {
            return Optional.empty();
         }

         lv = (BlockPos)var4.next();
      } while(!condition.test(lv));

      return Optional.of(lv);
   }

   public static Stream streamOutwards(BlockPos center, int maxX, int maxY, int maxZ) {
      return StreamSupport.stream(iterateOutwards(center, maxX, maxY, maxZ).spliterator(), false);
   }

   public static Iterable iterate(BlockPos start, BlockPos end) {
      return iterate(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()), Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
   }

   public static Stream stream(BlockPos start, BlockPos end) {
      return StreamSupport.stream(iterate(start, end).spliterator(), false);
   }

   public static Stream stream(BlockBox box) {
      return stream(Math.min(box.getMinX(), box.getMaxX()), Math.min(box.getMinY(), box.getMaxY()), Math.min(box.getMinZ(), box.getMaxZ()), Math.max(box.getMinX(), box.getMaxX()), Math.max(box.getMinY(), box.getMaxY()), Math.max(box.getMinZ(), box.getMaxZ()));
   }

   public static Stream stream(Box box) {
      return stream(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ));
   }

   public static Stream stream(int startX, int startY, int startZ, int endX, int endY, int endZ) {
      return StreamSupport.stream(iterate(startX, startY, startZ, endX, endY, endZ).spliterator(), false);
   }

   public static Iterable iterate(int startX, int startY, int startZ, int endX, int endY, int endZ) {
      int o = endX - startX + 1;
      int p = endY - startY + 1;
      int q = endZ - startZ + 1;
      int r = o * p * q;
      return () -> {
         return new AbstractIterator() {
            private final Mutable pos = new Mutable();
            private int index;

            protected BlockPos computeNext() {
               if (this.index == i) {
                  return (BlockPos)this.endOfData();
               } else {
                  int ix = this.index % j;
                  int jx = this.index / j;
                  int kx = jx % k;
                  int lx = jx / k;
                  ++this.index;
                  return this.pos.set(l + ix, m + kx, n + lx);
               }
            }

            // $FF: synthetic method
            protected Object computeNext() {
               return this.computeNext();
            }
         };
      };
   }

   public static Iterable iterateInSquare(BlockPos center, int radius, Direction firstDirection, Direction secondDirection) {
      Validate.validState(firstDirection.getAxis() != secondDirection.getAxis(), "The two directions cannot be on the same axis", new Object[0]);
      return () -> {
         return new AbstractIterator() {
            private final Direction[] directions = new Direction[]{arg, arg2, arg.getOpposite(), arg2.getOpposite()};
            private final Mutable pos = arg3.mutableCopy().move(arg2);
            private final int maxDirectionChanges = 4 * i;
            private int directionChangeCount = -1;
            private int maxSteps;
            private int steps;
            private int currentX;
            private int currentY;
            private int currentZ;

            {
               this.currentX = this.pos.getX();
               this.currentY = this.pos.getY();
               this.currentZ = this.pos.getZ();
            }

            protected Mutable computeNext() {
               this.pos.set(this.currentX, this.currentY, this.currentZ).move(this.directions[(this.directionChangeCount + 4) % 4]);
               this.currentX = this.pos.getX();
               this.currentY = this.pos.getY();
               this.currentZ = this.pos.getZ();
               if (this.steps >= this.maxSteps) {
                  if (this.directionChangeCount >= this.maxDirectionChanges) {
                     return (Mutable)this.endOfData();
                  }

                  ++this.directionChangeCount;
                  this.steps = 0;
                  this.maxSteps = this.directionChangeCount / 2 + 1;
               }

               ++this.steps;
               return this.pos;
            }

            // $FF: synthetic method
            protected Object computeNext() {
               return this.computeNext();
            }
         };
      };
   }

   public static int iterateRecursively(BlockPos pos, int maxDepth, int maxIterations, BiConsumer nextQueuer, Predicate callback) {
      Queue queue = new ArrayDeque();
      LongSet longSet = new LongOpenHashSet();
      queue.add(Pair.of(pos, 0));
      int k = 0;

      while(!queue.isEmpty()) {
         Pair pair = (Pair)queue.poll();
         BlockPos lv = (BlockPos)pair.getLeft();
         int l = (Integer)pair.getRight();
         long m = lv.asLong();
         if (longSet.add(m) && callback.test(lv)) {
            ++k;
            if (k >= maxIterations) {
               return k;
            }

            if (l < maxDepth) {
               nextQueuer.accept(lv, (queuedPos) -> {
                  queue.add(Pair.of(queuedPos, l + 1));
               });
            }
         }
      }

      return k;
   }

   // $FF: synthetic method
   public Vec3i crossProduct(Vec3i vec) {
      return this.crossProduct(vec);
   }

   // $FF: synthetic method
   public Vec3i offset(Direction.Axis axis, int distance) {
      return this.offset(axis, distance);
   }

   // $FF: synthetic method
   public Vec3i offset(Direction direction, int distance) {
      return this.offset(direction, distance);
   }

   // $FF: synthetic method
   public Vec3i offset(Direction direction) {
      return this.offset(direction);
   }

   // $FF: synthetic method
   public Vec3i east(int distance) {
      return this.east(distance);
   }

   // $FF: synthetic method
   public Vec3i east() {
      return this.east();
   }

   // $FF: synthetic method
   public Vec3i west(int distance) {
      return this.west(distance);
   }

   // $FF: synthetic method
   public Vec3i west() {
      return this.west();
   }

   // $FF: synthetic method
   public Vec3i south(int distance) {
      return this.south(distance);
   }

   // $FF: synthetic method
   public Vec3i south() {
      return this.south();
   }

   // $FF: synthetic method
   public Vec3i north(int distance) {
      return this.north(distance);
   }

   // $FF: synthetic method
   public Vec3i north() {
      return this.north();
   }

   // $FF: synthetic method
   public Vec3i down(int distance) {
      return this.down(distance);
   }

   // $FF: synthetic method
   public Vec3i down() {
      return this.down();
   }

   // $FF: synthetic method
   public Vec3i up(int distance) {
      return this.up(distance);
   }

   // $FF: synthetic method
   public Vec3i up() {
      return this.up();
   }

   // $FF: synthetic method
   public Vec3i multiply(int scale) {
      return this.multiply(scale);
   }

   // $FF: synthetic method
   public Vec3i subtract(Vec3i vec) {
      return this.subtract(vec);
   }

   // $FF: synthetic method
   public Vec3i add(Vec3i vec) {
      return this.add(vec);
   }

   // $FF: synthetic method
   public Vec3i add(int x, int y, int z) {
      return this.add(x, y, z);
   }

   static {
      CODEC = Codec.INT_STREAM.comapFlatMap((stream) -> {
         return Util.toArray((IntStream)stream, 3).map((values) -> {
            return new BlockPos(values[0], values[1], values[2]);
         });
      }, (pos) -> {
         return IntStream.of(new int[]{pos.getX(), pos.getY(), pos.getZ()});
      }).stable();
      LOGGER = LogUtils.getLogger();
      ORIGIN = new BlockPos(0, 0, 0);
      SIZE_BITS_X = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
      SIZE_BITS_Z = SIZE_BITS_X;
      SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
      BITS_X = (1L << SIZE_BITS_X) - 1L;
      BITS_Y = (1L << SIZE_BITS_Y) - 1L;
      BITS_Z = (1L << SIZE_BITS_Z) - 1L;
      BIT_SHIFT_Z = SIZE_BITS_Y;
      BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
   }

   public static class Mutable extends BlockPos {
      public Mutable() {
         this(0, 0, 0);
      }

      public Mutable(int i, int j, int k) {
         super(i, j, k);
      }

      public Mutable(double x, double y, double z) {
         this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
      }

      public BlockPos add(int i, int j, int k) {
         return super.add(i, j, k).toImmutable();
      }

      public BlockPos multiply(int i) {
         return super.multiply(i).toImmutable();
      }

      public BlockPos offset(Direction arg, int i) {
         return super.offset(arg, i).toImmutable();
      }

      public BlockPos offset(Direction.Axis arg, int i) {
         return super.offset(arg, i).toImmutable();
      }

      public BlockPos rotate(BlockRotation rotation) {
         return super.rotate(rotation).toImmutable();
      }

      public Mutable set(int x, int y, int z) {
         this.setX(x);
         this.setY(y);
         this.setZ(z);
         return this;
      }

      public Mutable set(double x, double y, double z) {
         return this.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
      }

      public Mutable set(Vec3i pos) {
         return this.set(pos.getX(), pos.getY(), pos.getZ());
      }

      public Mutable set(long pos) {
         return this.set(unpackLongX(pos), unpackLongY(pos), unpackLongZ(pos));
      }

      public Mutable set(AxisCycleDirection axis, int x, int y, int z) {
         return this.set(axis.choose(x, y, z, Direction.Axis.X), axis.choose(x, y, z, Direction.Axis.Y), axis.choose(x, y, z, Direction.Axis.Z));
      }

      public Mutable set(Vec3i pos, Direction direction) {
         return this.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());
      }

      public Mutable set(Vec3i pos, int x, int y, int z) {
         return this.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
      }

      public Mutable set(Vec3i vec1, Vec3i vec2) {
         return this.set(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY(), vec1.getZ() + vec2.getZ());
      }

      public Mutable move(Direction direction) {
         return this.move(direction, 1);
      }

      public Mutable move(Direction direction, int distance) {
         return this.set(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
      }

      public Mutable move(int dx, int dy, int dz) {
         return this.set(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
      }

      public Mutable move(Vec3i vec) {
         return this.set(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
      }

      public Mutable clamp(Direction.Axis axis, int min, int max) {
         switch (axis) {
            case X:
               return this.set(MathHelper.clamp(this.getX(), min, max), this.getY(), this.getZ());
            case Y:
               return this.set(this.getX(), MathHelper.clamp(this.getY(), min, max), this.getZ());
            case Z:
               return this.set(this.getX(), this.getY(), MathHelper.clamp(this.getZ(), min, max));
            default:
               throw new IllegalStateException("Unable to clamp axis " + axis);
         }
      }

      public Mutable setX(int i) {
         super.setX(i);
         return this;
      }

      public Mutable setY(int i) {
         super.setY(i);
         return this;
      }

      public Mutable setZ(int i) {
         super.setZ(i);
         return this;
      }

      public BlockPos toImmutable() {
         return new BlockPos(this);
      }

      // $FF: synthetic method
      public Vec3i crossProduct(Vec3i vec) {
         return super.crossProduct(vec);
      }

      // $FF: synthetic method
      public Vec3i offset(Direction.Axis axis, int distance) {
         return this.offset(axis, distance);
      }

      // $FF: synthetic method
      public Vec3i offset(Direction direction, int distance) {
         return this.offset(direction, distance);
      }

      // $FF: synthetic method
      public Vec3i offset(Direction direction) {
         return super.offset(direction);
      }

      // $FF: synthetic method
      public Vec3i east(int distance) {
         return super.east(distance);
      }

      // $FF: synthetic method
      public Vec3i east() {
         return super.east();
      }

      // $FF: synthetic method
      public Vec3i west(int distance) {
         return super.west(distance);
      }

      // $FF: synthetic method
      public Vec3i west() {
         return super.west();
      }

      // $FF: synthetic method
      public Vec3i south(int distance) {
         return super.south(distance);
      }

      // $FF: synthetic method
      public Vec3i south() {
         return super.south();
      }

      // $FF: synthetic method
      public Vec3i north(int distance) {
         return super.north(distance);
      }

      // $FF: synthetic method
      public Vec3i north() {
         return super.north();
      }

      // $FF: synthetic method
      public Vec3i down(int distance) {
         return super.down(distance);
      }

      // $FF: synthetic method
      public Vec3i down() {
         return super.down();
      }

      // $FF: synthetic method
      public Vec3i up(int distance) {
         return super.up(distance);
      }

      // $FF: synthetic method
      public Vec3i up() {
         return super.up();
      }

      // $FF: synthetic method
      public Vec3i multiply(int scale) {
         return this.multiply(scale);
      }

      // $FF: synthetic method
      public Vec3i subtract(Vec3i vec) {
         return super.subtract(vec);
      }

      // $FF: synthetic method
      public Vec3i add(Vec3i vec) {
         return super.add(vec);
      }

      // $FF: synthetic method
      public Vec3i add(int x, int y, int z) {
         return this.add(x, y, z);
      }

      // $FF: synthetic method
      public Vec3i setZ(int z) {
         return this.setZ(z);
      }

      // $FF: synthetic method
      public Vec3i setY(int y) {
         return this.setY(y);
      }

      // $FF: synthetic method
      public Vec3i setX(int x) {
         return this.setX(x);
      }
   }
}
