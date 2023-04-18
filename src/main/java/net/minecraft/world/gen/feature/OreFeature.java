package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class OreFeature extends Feature {
   public OreFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      Random lv = context.getRandom();
      BlockPos lv2 = context.getOrigin();
      StructureWorldAccess lv3 = context.getWorld();
      OreFeatureConfig lv4 = (OreFeatureConfig)context.getConfig();
      float f = lv.nextFloat() * 3.1415927F;
      float g = (float)lv4.size / 8.0F;
      int i = MathHelper.ceil(((float)lv4.size / 16.0F * 2.0F + 1.0F) / 2.0F);
      double d = (double)lv2.getX() + Math.sin((double)f) * (double)g;
      double e = (double)lv2.getX() - Math.sin((double)f) * (double)g;
      double h = (double)lv2.getZ() + Math.cos((double)f) * (double)g;
      double j = (double)lv2.getZ() - Math.cos((double)f) * (double)g;
      int k = true;
      double l = (double)(lv2.getY() + lv.nextInt(3) - 2);
      double m = (double)(lv2.getY() + lv.nextInt(3) - 2);
      int n = lv2.getX() - MathHelper.ceil(g) - i;
      int o = lv2.getY() - 2 - i;
      int p = lv2.getZ() - MathHelper.ceil(g) - i;
      int q = 2 * (MathHelper.ceil(g) + i);
      int r = 2 * (2 + i);

      for(int s = n; s <= n + q; ++s) {
         for(int t = p; t <= p + q; ++t) {
            if (o <= lv3.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, s, t)) {
               return this.generateVeinPart(lv3, lv, lv4, d, e, h, j, l, m, n, o, p, q, r);
            }
         }
      }

      return false;
   }

   protected boolean generateVeinPart(StructureWorldAccess world, Random arg2, OreFeatureConfig config, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int horizontalSize, int verticalSize) {
      int o = 0;
      BitSet bitSet = new BitSet(horizontalSize * verticalSize * horizontalSize);
      BlockPos.Mutable lv = new BlockPos.Mutable();
      int p = config.size;
      double[] ds = new double[p * 4];

      int q;
      double s;
      double t;
      double u;
      double v;
      for(q = 0; q < p; ++q) {
         float r = (float)q / (float)p;
         s = MathHelper.lerp((double)r, startX, endX);
         t = MathHelper.lerp((double)r, startY, endY);
         u = MathHelper.lerp((double)r, startZ, endZ);
         v = arg2.nextDouble() * (double)p / 16.0;
         double w = ((double)(MathHelper.sin(3.1415927F * r) + 1.0F) * v + 1.0) / 2.0;
         ds[q * 4 + 0] = s;
         ds[q * 4 + 1] = t;
         ds[q * 4 + 2] = u;
         ds[q * 4 + 3] = w;
      }

      int x;
      for(q = 0; q < p - 1; ++q) {
         if (!(ds[q * 4 + 3] <= 0.0)) {
            for(x = q + 1; x < p; ++x) {
               if (!(ds[x * 4 + 3] <= 0.0)) {
                  s = ds[q * 4 + 0] - ds[x * 4 + 0];
                  t = ds[q * 4 + 1] - ds[x * 4 + 1];
                  u = ds[q * 4 + 2] - ds[x * 4 + 2];
                  v = ds[q * 4 + 3] - ds[x * 4 + 3];
                  if (v * v > s * s + t * t + u * u) {
                     if (v > 0.0) {
                        ds[x * 4 + 3] = -1.0;
                     } else {
                        ds[q * 4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      ChunkSectionCache lv2 = new ChunkSectionCache(world);

      try {
         for(x = 0; x < p; ++x) {
            s = ds[x * 4 + 3];
            if (!(s < 0.0)) {
               t = ds[x * 4 + 0];
               u = ds[x * 4 + 1];
               v = ds[x * 4 + 2];
               int y = Math.max(MathHelper.floor(t - s), x);
               int z = Math.max(MathHelper.floor(u - s), y);
               int aa = Math.max(MathHelper.floor(v - s), z);
               int ab = Math.max(MathHelper.floor(t + s), y);
               int ac = Math.max(MathHelper.floor(u + s), z);
               int ad = Math.max(MathHelper.floor(v + s), aa);

               for(int ae = y; ae <= ab; ++ae) {
                  double af = ((double)ae + 0.5 - t) / s;
                  if (af * af < 1.0) {
                     for(int ag = z; ag <= ac; ++ag) {
                        double ah = ((double)ag + 0.5 - u) / s;
                        if (af * af + ah * ah < 1.0) {
                           for(int ai = aa; ai <= ad; ++ai) {
                              double aj = ((double)ai + 0.5 - v) / s;
                              if (af * af + ah * ah + aj * aj < 1.0 && !world.isOutOfHeightLimit(ag)) {
                                 int ak = ae - x + (ag - y) * horizontalSize + (ai - z) * horizontalSize * verticalSize;
                                 if (!bitSet.get(ak)) {
                                    bitSet.set(ak);
                                    lv.set(ae, ag, ai);
                                    if (world.isValidForSetBlock(lv)) {
                                       ChunkSection lv3 = lv2.getSection(lv);
                                       if (lv3 != null) {
                                          int al = ChunkSectionPos.getLocalCoord(ae);
                                          int am = ChunkSectionPos.getLocalCoord(ag);
                                          int an = ChunkSectionPos.getLocalCoord(ai);
                                          BlockState lv4 = lv3.getBlockState(al, am, an);
                                          Iterator var57 = config.targets.iterator();

                                          while(var57.hasNext()) {
                                             OreFeatureConfig.Target lv5 = (OreFeatureConfig.Target)var57.next();
                                             Objects.requireNonNull(lv2);
                                             if (shouldPlace(lv4, lv2::getBlockState, arg2, config, lv5, lv)) {
                                                lv3.setBlockState(al, am, an, lv5.state, false);
                                                ++o;
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var60) {
         try {
            lv2.close();
         } catch (Throwable var59) {
            var60.addSuppressed(var59);
         }

         throw var60;
      }

      lv2.close();
      return o > 0;
   }

   public static boolean shouldPlace(BlockState state, Function posToState, Random arg2, OreFeatureConfig config, OreFeatureConfig.Target target, BlockPos.Mutable pos) {
      if (!target.target.test(state, arg2)) {
         return false;
      } else if (shouldNotDiscard(arg2, config.discardOnAirChance)) {
         return true;
      } else {
         return !isExposedToAir(posToState, pos);
      }
   }

   protected static boolean shouldNotDiscard(Random arg, float chance) {
      if (chance <= 0.0F) {
         return true;
      } else if (chance >= 1.0F) {
         return false;
      } else {
         return arg.nextFloat() >= chance;
      }
   }
}
