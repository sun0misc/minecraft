package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface BlockEntityTicker {
   void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity);
}
