/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.NeighborUpdater;

public class SimpleNeighborUpdater
implements NeighborUpdater {
    private final World world;

    public SimpleNeighborUpdater(World world) {
        this.world = world;
    }

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
        NeighborUpdater.replaceWithStateForNeighborUpdate(this.world, direction, neighborState, pos, neighborPos, flags, maxUpdateDepth - 1);
    }

    @Override
    public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
        BlockState lv = this.world.getBlockState(pos);
        this.updateNeighbor(lv, pos, sourceBlock, sourcePos, false);
    }

    @Override
    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        NeighborUpdater.tryNeighborUpdate(this.world, state, pos, sourceBlock, sourcePos, notify);
    }
}

