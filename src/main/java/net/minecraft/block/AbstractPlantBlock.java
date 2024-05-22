/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantPartBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractPlantBlock
extends AbstractPlantPartBlock
implements Fertilizable {
    protected AbstractPlantBlock(AbstractBlock.Settings arg, Direction arg2, VoxelShape arg3, boolean bl) {
        super(arg, arg2, arg3, bl);
    }

    protected abstract MapCodec<? extends AbstractPlantBlock> getCodec();

    protected BlockState copyState(BlockState from, BlockState to) {
        return to;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == this.growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        AbstractPlantStemBlock lv = this.getStem();
        if (direction == this.growthDirection && !neighborState.isOf(this) && !neighborState.isOf(lv)) {
            return this.copyState(state, lv.getRandomGrowthState(world));
        }
        if (this.tickWater) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(this.getStem());
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        Optional<BlockPos> optional = this.getStemHeadPos(world, pos, state.getBlock());
        return optional.isPresent() && this.getStem().chooseStemState(world.getBlockState(optional.get().offset(this.growthDirection)));
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        Optional<BlockPos> optional = this.getStemHeadPos(world, pos, state.getBlock());
        if (optional.isPresent()) {
            BlockState lv = world.getBlockState(optional.get());
            ((AbstractPlantStemBlock)lv.getBlock()).grow(world, random, optional.get(), lv);
        }
    }

    private Optional<BlockPos> getStemHeadPos(BlockView world, BlockPos pos, Block block) {
        return BlockLocating.findColumnEnd(world, pos, block, this.growthDirection, this.getStem());
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        boolean bl = super.canReplace(state, context);
        if (bl && context.getStack().isOf(this.getStem().asItem())) {
            return false;
        }
        return bl;
    }

    @Override
    protected Block getPlant() {
        return this;
    }
}

