package net.minecraft.block;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;

public class NyliumBlock extends Block implements Fertilizable {
   protected NyliumBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   private static boolean stayAlive(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.up();
      BlockState lv2 = world.getBlockState(lv);
      int i = ChunkLightProvider.getRealisticOpacity(world, state, pos, lv2, lv, Direction.UP, lv2.getOpacity(world, lv));
      return i < world.getMaxLightLevel();
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!stayAlive(state, world, pos)) {
         world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState());
      }

   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return world.getBlockState(pos.up()).isAir();
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockState lv = world.getBlockState(pos);
      BlockPos lv2 = pos.up();
      ChunkGenerator lv3 = world.getChunkManager().getChunkGenerator();
      Registry lv4 = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
      if (lv.isOf(Blocks.CRIMSON_NYLIUM)) {
         this.generate(lv4, NetherConfiguredFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL, world, lv3, random, lv2);
      } else if (lv.isOf(Blocks.WARPED_NYLIUM)) {
         this.generate(lv4, NetherConfiguredFeatures.WARPED_FOREST_VEGETATION_BONEMEAL, world, lv3, random, lv2);
         this.generate(lv4, NetherConfiguredFeatures.NETHER_SPROUTS_BONEMEAL, world, lv3, random, lv2);
         if (random.nextInt(8) == 0) {
            this.generate(lv4, NetherConfiguredFeatures.TWISTING_VINES_BONEMEAL, world, lv3, random, lv2);
         }
      }

   }

   private void generate(Registry registry, RegistryKey key, ServerWorld world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
      registry.getEntry(key).ifPresent((entry) -> {
         ((ConfiguredFeature)entry.value()).generate(world, chunkGenerator, random, pos);
      });
   }
}
