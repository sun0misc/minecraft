package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class HugeFungusFeature extends Feature {
   private static final float field_31507 = 0.06F;

   public HugeFungusFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      ChunkGenerator lv4 = context.getGenerator();
      HugeFungusFeatureConfig lv5 = (HugeFungusFeatureConfig)context.getConfig();
      Block lv6 = lv5.validBaseBlock.getBlock();
      BlockPos lv7 = null;
      BlockState lv8 = lv.getBlockState(lv2.down());
      if (lv8.isOf(lv6)) {
         lv7 = lv2;
      }

      if (lv7 == null) {
         return false;
      } else {
         int i = MathHelper.nextInt(lv3, 4, 13);
         if (lv3.nextInt(12) == 0) {
            i *= 2;
         }

         if (!lv5.planted) {
            int j = lv4.getWorldHeight();
            if (lv7.getY() + i + 1 >= j) {
               return false;
            }
         }

         boolean bl = !lv5.planted && lv3.nextFloat() < 0.06F;
         lv.setBlockState(lv2, Blocks.AIR.getDefaultState(), Block.NO_REDRAW);
         this.generateStem(lv, lv3, lv5, lv7, i, bl);
         this.generateHat(lv, lv3, lv5, lv7, i, bl);
         return true;
      }
   }

   private static boolean isReplaceable(WorldAccess world, BlockPos pos, boolean replacePlants) {
      return world.testBlockState(pos, (state) -> {
         Material lv = state.getMaterial();
         return state.isReplaceable() || replacePlants && lv == Material.PLANT;
      });
   }

   private void generateStem(WorldAccess world, Random random, HugeFungusFeatureConfig config, BlockPos pos, int stemHeight, boolean thickStem) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      BlockState lv2 = config.stemState;
      int j = thickStem ? 1 : 0;

      for(int k = -j; k <= j; ++k) {
         for(int l = -j; l <= j; ++l) {
            boolean bl2 = thickStem && MathHelper.abs(k) == j && MathHelper.abs(l) == j;

            for(int m = 0; m < stemHeight; ++m) {
               lv.set((Vec3i)pos, k, m, l);
               if (isReplaceable(world, lv, true)) {
                  if (config.planted) {
                     if (!world.getBlockState(lv.down()).isAir()) {
                        world.breakBlock(lv, true);
                     }

                     world.setBlockState(lv, lv2, Block.NOTIFY_ALL);
                  } else if (bl2) {
                     if (random.nextFloat() < 0.1F) {
                        this.setBlockState(world, lv, lv2);
                     }
                  } else {
                     this.setBlockState(world, lv, lv2);
                  }
               }
            }
         }
      }

   }

   private void generateHat(WorldAccess world, Random random, HugeFungusFeatureConfig config, BlockPos pos, int hatHeight, boolean thickStem) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      boolean bl2 = config.hatState.isOf(Blocks.NETHER_WART_BLOCK);
      int j = Math.min(random.nextInt(1 + hatHeight / 3) + 5, hatHeight);
      int k = hatHeight - j;

      for(int l = k; l <= hatHeight; ++l) {
         int m = l < hatHeight - random.nextInt(3) ? 2 : 1;
         if (j > 8 && l < k + 4) {
            m = 3;
         }

         if (thickStem) {
            ++m;
         }

         for(int n = -m; n <= m; ++n) {
            for(int o = -m; o <= m; ++o) {
               boolean bl3 = n == -m || n == m;
               boolean bl4 = o == -m || o == m;
               boolean bl5 = !bl3 && !bl4 && l != hatHeight;
               boolean bl6 = bl3 && bl4;
               boolean bl7 = l < k + 3;
               lv.set((Vec3i)pos, n, l, o);
               if (isReplaceable(world, lv, false)) {
                  if (config.planted && !world.getBlockState(lv.down()).isAir()) {
                     world.breakBlock(lv, true);
                  }

                  if (bl7) {
                     if (!bl5) {
                        this.placeWithOptionalVines(world, random, lv, config.hatState, bl2);
                     }
                  } else if (bl5) {
                     this.placeHatBlock(world, random, config, lv, 0.1F, 0.2F, bl2 ? 0.1F : 0.0F);
                  } else if (bl6) {
                     this.placeHatBlock(world, random, config, lv, 0.01F, 0.7F, bl2 ? 0.083F : 0.0F);
                  } else {
                     this.placeHatBlock(world, random, config, lv, 5.0E-4F, 0.98F, bl2 ? 0.07F : 0.0F);
                  }
               }
            }
         }
      }

   }

   private void placeHatBlock(WorldAccess world, Random random, HugeFungusFeatureConfig config, BlockPos.Mutable pos, float decorationChance, float generationChance, float vineChance) {
      if (random.nextFloat() < decorationChance) {
         this.setBlockState(world, pos, config.decorationState);
      } else if (random.nextFloat() < generationChance) {
         this.setBlockState(world, pos, config.hatState);
         if (random.nextFloat() < vineChance) {
            generateVines(pos, world, random);
         }
      }

   }

   private void placeWithOptionalVines(WorldAccess world, Random random, BlockPos pos, BlockState state, boolean vines) {
      if (world.getBlockState(pos.down()).isOf(state.getBlock())) {
         this.setBlockState(world, pos, state);
      } else if ((double)random.nextFloat() < 0.15) {
         this.setBlockState(world, pos, state);
         if (vines && random.nextInt(11) == 0) {
            generateVines(pos, world, random);
         }
      }

   }

   private static void generateVines(BlockPos pos, WorldAccess world, Random random) {
      BlockPos.Mutable lv = pos.mutableCopy().move(Direction.DOWN);
      if (world.isAir(lv)) {
         int i = MathHelper.nextInt(random, 1, 5);
         if (random.nextInt(7) == 0) {
            i *= 2;
         }

         int j = true;
         int k = true;
         WeepingVinesFeature.generateVineColumn(world, random, lv, i, 23, 25);
      }
   }
}
