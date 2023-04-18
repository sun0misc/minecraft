package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/** @deprecated */
@Deprecated
public class LakeFeature extends Feature {
   private static final BlockState CAVE_AIR;

   public LakeFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      Config lv4 = (Config)context.getConfig();
      if (lv.getY() <= lv2.getBottomY() + 4) {
         return false;
      } else {
         lv = lv.down(4);
         boolean[] bls = new boolean[2048];
         int i = lv3.nextInt(4) + 4;

         for(int j = 0; j < i; ++j) {
            double d = lv3.nextDouble() * 6.0 + 3.0;
            double e = lv3.nextDouble() * 4.0 + 2.0;
            double f = lv3.nextDouble() * 6.0 + 3.0;
            double g = lv3.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double h = lv3.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
            double k = lv3.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;

            for(int l = 1; l < 15; ++l) {
               for(int m = 1; m < 15; ++m) {
                  for(int n = 1; n < 7; ++n) {
                     double o = ((double)l - g) / (d / 2.0);
                     double p = ((double)n - h) / (e / 2.0);
                     double q = ((double)m - k) / (f / 2.0);
                     double r = o * o + p * p + q * q;
                     if (r < 1.0) {
                        bls[(l * 16 + m) * 8 + n] = true;
                     }
                  }
               }
            }
         }

         BlockState lv5 = lv4.fluid().get(lv3, lv);

         int t;
         boolean v;
         int s;
         int u;
         for(s = 0; s < 16; ++s) {
            for(t = 0; t < 16; ++t) {
               for(u = 0; u < 8; ++u) {
                  v = !bls[(s * 16 + t) * 8 + u] && (s < 15 && bls[((s + 1) * 16 + t) * 8 + u] || s > 0 && bls[((s - 1) * 16 + t) * 8 + u] || t < 15 && bls[(s * 16 + t + 1) * 8 + u] || t > 0 && bls[(s * 16 + (t - 1)) * 8 + u] || u < 7 && bls[(s * 16 + t) * 8 + u + 1] || u > 0 && bls[(s * 16 + t) * 8 + (u - 1)]);
                  if (v) {
                     BlockState lv6 = lv2.getBlockState(lv.add(s, u, t));
                     Material lv7 = lv6.getMaterial();
                     if (u >= 4 && lv6.isLiquid()) {
                        return false;
                     }

                     if (u < 4 && !lv7.isSolid() && lv2.getBlockState(lv.add(s, u, t)) != lv5) {
                        return false;
                     }
                  }
               }
            }
         }

         boolean bl2;
         for(s = 0; s < 16; ++s) {
            for(t = 0; t < 16; ++t) {
               for(u = 0; u < 8; ++u) {
                  if (bls[(s * 16 + t) * 8 + u]) {
                     BlockPos lv8 = lv.add(s, u, t);
                     if (this.canReplace(lv2.getBlockState(lv8))) {
                        bl2 = u >= 4;
                        lv2.setBlockState(lv8, bl2 ? CAVE_AIR : lv5, Block.NOTIFY_LISTENERS);
                        if (bl2) {
                           lv2.scheduleBlockTick(lv8, CAVE_AIR.getBlock(), 0);
                           this.markBlocksAboveForPostProcessing(lv2, lv8);
                        }
                     }
                  }
               }
            }
         }

         BlockState lv9 = lv4.barrier().get(lv3, lv);
         if (!lv9.isAir()) {
            for(t = 0; t < 16; ++t) {
               for(u = 0; u < 16; ++u) {
                  for(int v = 0; v < 8; ++v) {
                     bl2 = !bls[(t * 16 + u) * 8 + v] && (t < 15 && bls[((t + 1) * 16 + u) * 8 + v] || t > 0 && bls[((t - 1) * 16 + u) * 8 + v] || u < 15 && bls[(t * 16 + u + 1) * 8 + v] || u > 0 && bls[(t * 16 + (u - 1)) * 8 + v] || v < 7 && bls[(t * 16 + u) * 8 + v + 1] || v > 0 && bls[(t * 16 + u) * 8 + (v - 1)]);
                     if (bl2 && (v < 4 || lv3.nextInt(2) != 0)) {
                        BlockState lv10 = lv2.getBlockState(lv.add(t, v, u));
                        if (lv10.getMaterial().isSolid() && !lv10.isIn(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                           BlockPos lv11 = lv.add(t, v, u);
                           lv2.setBlockState(lv11, lv9, Block.NOTIFY_LISTENERS);
                           this.markBlocksAboveForPostProcessing(lv2, lv11);
                        }
                     }
                  }
               }
            }
         }

         if (lv5.getFluidState().isIn(FluidTags.WATER)) {
            for(t = 0; t < 16; ++t) {
               for(u = 0; u < 16; ++u) {
                  v = true;
                  BlockPos lv12 = lv.add(t, 4, u);
                  if (((Biome)lv2.getBiome(lv12).value()).canSetIce(lv2, lv12, false) && this.canReplace(lv2.getBlockState(lv12))) {
                     lv2.setBlockState(lv12, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                  }
               }
            }
         }

         return true;
      }
   }

   private boolean canReplace(BlockState state) {
      return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
   }

   static {
      CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
   }

   public static record Config(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfig {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("fluid").forGetter(Config::fluid), BlockStateProvider.TYPE_CODEC.fieldOf("barrier").forGetter(Config::barrier)).apply(instance, Config::new);
      });

      public Config(BlockStateProvider arg, BlockStateProvider arg2) {
         this.fluid = arg;
         this.barrier = arg2;
      }

      public BlockStateProvider fluid() {
         return this.fluid;
      }

      public BlockStateProvider barrier() {
         return this.barrier;
      }
   }
}
