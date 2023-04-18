package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class TallFlowerBlock extends TallPlantBlock implements Fertilizable {
   public TallFlowerBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return false;
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      dropStack(world, pos, new ItemStack(this));
   }
}
