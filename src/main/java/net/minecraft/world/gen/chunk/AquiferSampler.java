package net.minecraft.world.gen.chunk;

import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

public interface AquiferSampler {
   static AquiferSampler aquifer(ChunkNoiseSampler chunkNoiseSampler, ChunkPos chunkPos, NoiseRouter noiseRouter, RandomSplitter randomSplitter, int minimumY, int height, FluidLevelSampler fluidLevelSampler) {
      return new Impl(chunkNoiseSampler, chunkPos, noiseRouter, randomSplitter, minimumY, height, fluidLevelSampler);
   }

   static AquiferSampler seaLevel(final FluidLevelSampler fluidLevelSampler) {
      return new AquiferSampler() {
         @Nullable
         public BlockState apply(DensityFunction.NoisePos pos, double density) {
            return density > 0.0 ? null : fluidLevelSampler.getFluidLevel(pos.blockX(), pos.blockY(), pos.blockZ()).getBlockState(pos.blockY());
         }

         public boolean needsFluidTick() {
            return false;
         }
      };
   }

   @Nullable
   BlockState apply(DensityFunction.NoisePos pos, double density);

   boolean needsFluidTick();

   public static class Impl implements AquiferSampler {
      private static final int field_31451 = 10;
      private static final int field_31452 = 9;
      private static final int field_31453 = 10;
      private static final int field_31454 = 6;
      private static final int field_31455 = 3;
      private static final int field_31456 = 6;
      private static final int field_31457 = 16;
      private static final int field_31458 = 12;
      private static final int field_31459 = 16;
      private static final int field_36220 = 11;
      private static final double NEEDS_FLUID_TICK_DISTANCE_THRESHOLD = maxDistance(MathHelper.square(10), MathHelper.square(12));
      private final ChunkNoiseSampler chunkNoiseSampler;
      private final DensityFunction barrierNoise;
      private final DensityFunction fluidLevelFloodednessNoise;
      private final DensityFunction fluidLevelSpreadNoise;
      private final DensityFunction fluidTypeNoise;
      private final RandomSplitter randomDeriver;
      private final FluidLevel[] waterLevels;
      private final long[] blockPositions;
      private final FluidLevelSampler fluidLevelSampler;
      private final DensityFunction erosionDensityFunction;
      private final DensityFunction depthDensityFunction;
      private boolean needsFluidTick;
      private final int startX;
      private final int startY;
      private final int startZ;
      private final int sizeX;
      private final int sizeZ;
      private static final int[][] CHUNK_POS_OFFSETS = new int[][]{{0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

      Impl(ChunkNoiseSampler chunkNoiseSampler, ChunkPos chunkPos, NoiseRouter noiseRouter, RandomSplitter randomSplitter, int minimumY, int height, FluidLevelSampler fluidLevelSampler) {
         this.chunkNoiseSampler = chunkNoiseSampler;
         this.barrierNoise = noiseRouter.barrierNoise();
         this.fluidLevelFloodednessNoise = noiseRouter.fluidLevelFloodednessNoise();
         this.fluidLevelSpreadNoise = noiseRouter.fluidLevelSpreadNoise();
         this.fluidTypeNoise = noiseRouter.lavaNoise();
         this.erosionDensityFunction = noiseRouter.erosion();
         this.depthDensityFunction = noiseRouter.depth();
         this.randomDeriver = randomSplitter;
         this.startX = this.getLocalX(chunkPos.getStartX()) - 1;
         this.fluidLevelSampler = fluidLevelSampler;
         int k = this.getLocalX(chunkPos.getEndX()) + 1;
         this.sizeX = k - this.startX + 1;
         this.startY = this.getLocalY(minimumY) - 1;
         int l = this.getLocalY(minimumY + height) + 1;
         int m = l - this.startY + 1;
         this.startZ = this.getLocalZ(chunkPos.getStartZ()) - 1;
         int n = this.getLocalZ(chunkPos.getEndZ()) + 1;
         this.sizeZ = n - this.startZ + 1;
         int o = this.sizeX * m * this.sizeZ;
         this.waterLevels = new FluidLevel[o];
         this.blockPositions = new long[o];
         Arrays.fill(this.blockPositions, Long.MAX_VALUE);
      }

      private int index(int x, int y, int z) {
         int l = x - this.startX;
         int m = y - this.startY;
         int n = z - this.startZ;
         return (m * this.sizeZ + n) * this.sizeX + l;
      }

      @Nullable
      public BlockState apply(DensityFunction.NoisePos pos, double density) {
         int i = pos.blockX();
         int j = pos.blockY();
         int k = pos.blockZ();
         if (density > 0.0) {
            this.needsFluidTick = false;
            return null;
         } else {
            FluidLevel lv = this.fluidLevelSampler.getFluidLevel(i, j, k);
            if (lv.getBlockState(j).isOf(Blocks.LAVA)) {
               this.needsFluidTick = false;
               return Blocks.LAVA.getDefaultState();
            } else {
               int l = Math.floorDiv(i - 5, 16);
               int m = Math.floorDiv(j + 1, 12);
               int n = Math.floorDiv(k - 5, 16);
               int o = Integer.MAX_VALUE;
               int p = Integer.MAX_VALUE;
               int q = Integer.MAX_VALUE;
               long r = 0L;
               long s = 0L;
               long t = 0L;

               for(int u = 0; u <= 1; ++u) {
                  for(int v = -1; v <= 1; ++v) {
                     for(int w = 0; w <= 1; ++w) {
                        int x = l + u;
                        int y = m + v;
                        int z = n + w;
                        int aa = this.index(x, y, z);
                        long ab = this.blockPositions[aa];
                        long ac;
                        if (ab != Long.MAX_VALUE) {
                           ac = ab;
                        } else {
                           Random lv2 = this.randomDeriver.split(x, y, z);
                           ac = BlockPos.asLong(x * 16 + lv2.nextInt(10), y * 12 + lv2.nextInt(9), z * 16 + lv2.nextInt(10));
                           this.blockPositions[aa] = ac;
                        }

                        int ad = BlockPos.unpackLongX(ac) - i;
                        int ae = BlockPos.unpackLongY(ac) - j;
                        int af = BlockPos.unpackLongZ(ac) - k;
                        int ag = ad * ad + ae * ae + af * af;
                        if (o >= ag) {
                           t = s;
                           s = r;
                           r = ac;
                           q = p;
                           p = o;
                           o = ag;
                        } else if (p >= ag) {
                           t = s;
                           s = ac;
                           q = p;
                           p = ag;
                        } else if (q >= ag) {
                           t = ac;
                           q = ag;
                        }
                     }
                  }
               }

               FluidLevel lv3 = this.getWaterLevel(r);
               double e = maxDistance(o, p);
               BlockState lv4 = lv3.getBlockState(j);
               if (e <= 0.0) {
                  this.needsFluidTick = e >= NEEDS_FLUID_TICK_DISTANCE_THRESHOLD;
                  return lv4;
               } else if (lv4.isOf(Blocks.WATER) && this.fluidLevelSampler.getFluidLevel(i, j - 1, k).getBlockState(j - 1).isOf(Blocks.LAVA)) {
                  this.needsFluidTick = true;
                  return lv4;
               } else {
                  MutableDouble mutableDouble = new MutableDouble(Double.NaN);
                  FluidLevel lv6 = this.getWaterLevel(s);
                  double f = e * this.calculateDensity(pos, mutableDouble, lv3, lv6);
                  if (density + f > 0.0) {
                     this.needsFluidTick = false;
                     return null;
                  } else {
                     FluidLevel lv7 = this.getWaterLevel(t);
                     double g = maxDistance(o, q);
                     double h;
                     if (g > 0.0) {
                        h = e * g * this.calculateDensity(pos, mutableDouble, lv3, lv7);
                        if (density + h > 0.0) {
                           this.needsFluidTick = false;
                           return null;
                        }
                     }

                     h = maxDistance(p, q);
                     if (h > 0.0) {
                        double ah = e * h * this.calculateDensity(pos, mutableDouble, lv6, lv7);
                        if (density + ah > 0.0) {
                           this.needsFluidTick = false;
                           return null;
                        }
                     }

                     this.needsFluidTick = true;
                     return lv4;
                  }
               }
            }
         }
      }

      public boolean needsFluidTick() {
         return this.needsFluidTick;
      }

      private static double maxDistance(int i, int a) {
         double d = 25.0;
         return 1.0 - (double)Math.abs(a - i) / 25.0;
      }

      private double calculateDensity(DensityFunction.NoisePos pos, MutableDouble mutableDouble, FluidLevel arg2, FluidLevel arg3) {
         int i = pos.blockY();
         BlockState lv = arg2.getBlockState(i);
         BlockState lv2 = arg3.getBlockState(i);
         if ((!lv.isOf(Blocks.LAVA) || !lv2.isOf(Blocks.WATER)) && (!lv.isOf(Blocks.WATER) || !lv2.isOf(Blocks.LAVA))) {
            int j = Math.abs(arg2.y - arg3.y);
            if (j == 0) {
               return 0.0;
            } else {
               double d = 0.5 * (double)(arg2.y + arg3.y);
               double e = (double)i + 0.5 - d;
               double f = (double)j / 2.0;
               double g = 0.0;
               double h = 2.5;
               double k = 1.5;
               double l = 3.0;
               double m = 10.0;
               double n = 3.0;
               double o = f - Math.abs(e);
               double q;
               double p;
               if (e > 0.0) {
                  p = 0.0 + o;
                  if (p > 0.0) {
                     q = p / 1.5;
                  } else {
                     q = p / 2.5;
                  }
               } else {
                  p = 3.0 + o;
                  if (p > 0.0) {
                     q = p / 3.0;
                  } else {
                     q = p / 10.0;
                  }
               }

               p = 2.0;
               double r;
               if (!(q < -2.0) && !(q > 2.0)) {
                  double s = mutableDouble.getValue();
                  if (Double.isNaN(s)) {
                     double t = this.barrierNoise.sample(pos);
                     mutableDouble.setValue(t);
                     r = t;
                  } else {
                     r = s;
                  }
               } else {
                  r = 0.0;
               }

               return 2.0 * (r + q);
            }
         } else {
            return 2.0;
         }
      }

      private int getLocalX(int x) {
         return Math.floorDiv(x, 16);
      }

      private int getLocalY(int y) {
         return Math.floorDiv(y, 12);
      }

      private int getLocalZ(int z) {
         return Math.floorDiv(z, 16);
      }

      private FluidLevel getWaterLevel(long pos) {
         int i = BlockPos.unpackLongX(pos);
         int j = BlockPos.unpackLongY(pos);
         int k = BlockPos.unpackLongZ(pos);
         int m = this.getLocalX(i);
         int n = this.getLocalY(j);
         int o = this.getLocalZ(k);
         int p = this.index(m, n, o);
         FluidLevel lv = this.waterLevels[p];
         if (lv != null) {
            return lv;
         } else {
            FluidLevel lv2 = this.getFluidLevel(i, j, k);
            this.waterLevels[p] = lv2;
            return lv2;
         }
      }

      private FluidLevel getFluidLevel(int blockX, int blockY, int blockZ) {
         FluidLevel lv = this.fluidLevelSampler.getFluidLevel(blockX, blockY, blockZ);
         int l = Integer.MAX_VALUE;
         int m = blockY + 12;
         int n = blockY - 12;
         boolean bl = false;
         int[][] var9 = CHUNK_POS_OFFSETS;
         int var10 = var9.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            int[] is = var9[var11];
            int o = blockX + ChunkSectionPos.getBlockCoord(is[0]);
            int p = blockZ + ChunkSectionPos.getBlockCoord(is[1]);
            int q = this.chunkNoiseSampler.estimateSurfaceHeight(o, p);
            int r = q + 8;
            boolean bl2 = is[0] == 0 && is[1] == 0;
            if (bl2 && n > r) {
               return lv;
            }

            boolean bl3 = m > r;
            if (bl3 || bl2) {
               FluidLevel lv2 = this.fluidLevelSampler.getFluidLevel(o, r, p);
               if (!lv2.getBlockState(r).isAir()) {
                  if (bl2) {
                     bl = true;
                  }

                  if (bl3) {
                     return lv2;
                  }
               }
            }

            l = Math.min(l, q);
         }

         int s = this.getFluidBlockY(blockX, blockY, blockZ, lv, l, bl);
         return new FluidLevel(s, this.getFluidBlockState(blockX, blockY, blockZ, lv, s));
      }

      private int getFluidBlockY(int blockX, int blockY, int blockZ, FluidLevel defaultFluidLevel, int surfaceHeightEstimate, boolean bl) {
         DensityFunction.UnblendedNoisePos lv = new DensityFunction.UnblendedNoisePos(blockX, blockY, blockZ);
         double d;
         double e;
         int m;
         if (VanillaBiomeParameters.method_43718(this.erosionDensityFunction, this.depthDensityFunction, lv)) {
            d = -1.0;
            e = -1.0;
         } else {
            m = surfaceHeightEstimate + 8 - blockY;
            int n = true;
            double f = bl ? MathHelper.clampedMap((double)m, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double g = MathHelper.clamp(this.fluidLevelFloodednessNoise.sample(lv), -1.0, 1.0);
            double h = MathHelper.map(f, 1.0, 0.0, -0.3, 0.8);
            double o = MathHelper.map(f, 1.0, 0.0, -0.8, 0.4);
            d = g - o;
            e = g - h;
         }

         if (e > 0.0) {
            m = defaultFluidLevel.y;
         } else if (d > 0.0) {
            m = this.getNoiseBasedFluidLevel(blockX, blockY, blockZ, surfaceHeightEstimate);
         } else {
            m = DimensionType.field_35479;
         }

         return m;
      }

      private int getNoiseBasedFluidLevel(int blockX, int blockY, int blockZ, int surfaceHeightEstimate) {
         int m = true;
         int n = true;
         int o = Math.floorDiv(blockX, 16);
         int p = Math.floorDiv(blockY, 40);
         int q = Math.floorDiv(blockZ, 16);
         int r = p * 40 + 20;
         int s = true;
         double d = this.fluidLevelSpreadNoise.sample(new DensityFunction.UnblendedNoisePos(o, p, q)) * 10.0;
         int t = MathHelper.roundDownToMultiple(d, 3);
         int u = r + t;
         return Math.min(surfaceHeightEstimate, u);
      }

      private BlockState getFluidBlockState(int blockX, int blockY, int blockZ, FluidLevel defaultFluidLevel, int fluidLevel) {
         BlockState lv = defaultFluidLevel.state;
         if (fluidLevel <= -10 && fluidLevel != DimensionType.field_35479 && defaultFluidLevel.state != Blocks.LAVA.getDefaultState()) {
            int m = true;
            int n = true;
            int o = Math.floorDiv(blockX, 64);
            int p = Math.floorDiv(blockY, 40);
            int q = Math.floorDiv(blockZ, 64);
            double d = this.fluidTypeNoise.sample(new DensityFunction.UnblendedNoisePos(o, p, q));
            if (Math.abs(d) > 0.3) {
               lv = Blocks.LAVA.getDefaultState();
            }
         }

         return lv;
      }
   }

   public interface FluidLevelSampler {
      FluidLevel getFluidLevel(int x, int y, int z);
   }

   public static final class FluidLevel {
      final int y;
      final BlockState state;

      public FluidLevel(int y, BlockState state) {
         this.y = y;
         this.state = state;
      }

      public BlockState getBlockState(int y) {
         return y < this.y ? this.state : Blocks.AIR.getDefaultState();
      }
   }
}
