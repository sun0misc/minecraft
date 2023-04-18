package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class GrassBlock extends SpreadableBlock implements Fertilizable {
   public GrassBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return world.getBlockState(pos.up()).isAir();
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockPos lv = pos.up();
      BlockState lv2 = Blocks.GRASS.getDefaultState();
      Optional optional = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE).getEntry(VegetationPlacedFeatures.GRASS_BONEMEAL);

      label49:
      for(int i = 0; i < 128; ++i) {
         BlockPos lv3 = lv;

         for(int j = 0; j < i / 16; ++j) {
            lv3 = lv3.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
            if (!world.getBlockState(lv3.down()).isOf(this) || world.getBlockState(lv3).isFullCube(world, lv3)) {
               continue label49;
            }
         }

         BlockState lv4 = world.getBlockState(lv3);
         if (lv4.isOf(lv2.getBlock()) && random.nextInt(10) == 0) {
            ((Fertilizable)lv2.getBlock()).grow(world, random, lv3, lv4);
         }

         if (lv4.isAir()) {
            RegistryEntry lv5;
            if (random.nextInt(8) == 0) {
               List list = ((Biome)world.getBiome(lv3).value()).getGenerationSettings().getFlowerFeatures();
               if (list.isEmpty()) {
                  continue;
               }

               lv5 = ((RandomPatchFeatureConfig)((ConfiguredFeature)list.get(0)).config()).feature();
            } else {
               if (!optional.isPresent()) {
                  continue;
               }

               lv5 = (RegistryEntry)optional.get();
            }

            ((PlacedFeature)lv5.value()).generateUnregistered(world, world.getChunkManager().getChunkGenerator(), random, lv3);
         }
      }

   }
}
