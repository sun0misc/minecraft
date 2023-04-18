package net.minecraft.block.sapling;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public abstract class LargeTreeSaplingGenerator extends SaplingGenerator {
   public boolean generate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
      for(int i = 0; i >= -1; --i) {
         for(int j = 0; j >= -1; --j) {
            if (canGenerateLargeTree(state, world, pos, i, j)) {
               return this.generateLargeTree(world, chunkGenerator, pos, state, random, i, j);
            }
         }
      }

      return super.generate(world, chunkGenerator, pos, state, random);
   }

   @Nullable
   protected abstract RegistryKey getLargeTreeFeature(Random random);

   public boolean generateLargeTree(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random, int x, int z) {
      RegistryKey lv = this.getLargeTreeFeature(random);
      if (lv == null) {
         return false;
      } else {
         RegistryEntry lv2 = (RegistryEntry)world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(lv).orElse((Object)null);
         if (lv2 == null) {
            return false;
         } else {
            ConfiguredFeature lv3 = (ConfiguredFeature)lv2.value();
            BlockState lv4 = Blocks.AIR.getDefaultState();
            world.setBlockState(pos.add(x, 0, z), lv4, Block.NO_REDRAW);
            world.setBlockState(pos.add(x + 1, 0, z), lv4, Block.NO_REDRAW);
            world.setBlockState(pos.add(x, 0, z + 1), lv4, Block.NO_REDRAW);
            world.setBlockState(pos.add(x + 1, 0, z + 1), lv4, Block.NO_REDRAW);
            if (lv3.generate(world, chunkGenerator, random, pos.add(x, 0, z))) {
               return true;
            } else {
               world.setBlockState(pos.add(x, 0, z), state, Block.NO_REDRAW);
               world.setBlockState(pos.add(x + 1, 0, z), state, Block.NO_REDRAW);
               world.setBlockState(pos.add(x, 0, z + 1), state, Block.NO_REDRAW);
               world.setBlockState(pos.add(x + 1, 0, z + 1), state, Block.NO_REDRAW);
               return false;
            }
         }
      }
   }

   public static boolean canGenerateLargeTree(BlockState state, BlockView world, BlockPos pos, int x, int z) {
      Block lv = state.getBlock();
      return world.getBlockState(pos.add(x, 0, z)).isOf(lv) && world.getBlockState(pos.add(x + 1, 0, z)).isOf(lv) && world.getBlockState(pos.add(x, 0, z + 1)).isOf(lv) && world.getBlockState(pos.add(x + 1, 0, z + 1)).isOf(lv);
   }
}
