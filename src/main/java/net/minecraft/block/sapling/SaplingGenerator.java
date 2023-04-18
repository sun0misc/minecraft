package net.minecraft.block.sapling;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public abstract class SaplingGenerator {
   @Nullable
   protected abstract RegistryKey getTreeFeature(Random random, boolean bees);

   public boolean generate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
      RegistryKey lv = this.getTreeFeature(random, this.areFlowersNearby(world, pos));
      if (lv == null) {
         return false;
      } else {
         RegistryEntry lv2 = (RegistryEntry)world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(lv).orElse((Object)null);
         if (lv2 == null) {
            return false;
         } else {
            ConfiguredFeature lv3 = (ConfiguredFeature)lv2.value();
            BlockState lv4 = world.getFluidState(pos).getBlockState();
            world.setBlockState(pos, lv4, Block.NO_REDRAW);
            if (lv3.generate(world, chunkGenerator, random, pos)) {
               if (world.getBlockState(pos) == lv4) {
                  world.updateListeners(pos, state, lv4, Block.NOTIFY_LISTENERS);
               }

               return true;
            } else {
               world.setBlockState(pos, state, Block.NO_REDRAW);
               return false;
            }
         }
      }
   }

   private boolean areFlowersNearby(WorldAccess world, BlockPos pos) {
      Iterator var3 = BlockPos.Mutable.iterate(pos.down().north(2).west(2), pos.up().south(2).east(2)).iterator();

      BlockPos lv;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         lv = (BlockPos)var3.next();
      } while(!world.getBlockState(lv).isIn(BlockTags.FLOWERS));

      return true;
   }
}
