/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantPartBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractPlantStemBlock
extends AbstractPlantPartBlock
implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_25;
    public static final int MAX_AGE = 25;
    private final double growthChance;

    protected AbstractPlantStemBlock(AbstractBlock.Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater, double growthChance) {
        super(settings, growthDirection, outlineShape, tickWater);
        this.growthChance = growthChance;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    protected abstract MapCodec<? extends AbstractPlantStemBlock> getCodec();

    @Override
    public BlockState getRandomGrowthState(WorldAccess world) {
        return (BlockState)this.getDefaultState().with(AGE, world.getRandom().nextInt(25));
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < 25;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos lv;
        if (state.get(AGE) < 25 && random.nextDouble() < this.growthChance && this.chooseStemState(world.getBlockState(lv = pos.offset(this.growthDirection)))) {
            world.setBlockState(lv, this.age(state, world.random));
        }
    }

    protected BlockState age(BlockState state, Random random) {
        return (BlockState)state.cycle(AGE);
    }

    public BlockState withMaxAge(BlockState state) {
        return (BlockState)state.with(AGE, 25);
    }

    public boolean hasMaxAge(BlockState state) {
        return state.get(AGE) == 25;
    }

    protected BlockState copyState(BlockState from, BlockState to) {
        return to;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == this.growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        if (direction == this.growthDirection && (neighborState.isOf(this) || neighborState.isOf(this.getPlant()))) {
            return this.copyState(state, this.getPlant().getDefaultState());
        }
        if (this.tickWater) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return this.chooseStemState(world.getBlockState(pos.offset(this.growthDirection)));
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockPos lv = pos.offset(this.growthDirection);
        int i = Math.min(state.get(AGE) + 1, 25);
        int j = this.getGrowthLength(random);
        for (int k = 0; k < j && this.chooseStemState(world.getBlockState(lv)); ++k) {
            world.setBlockState(lv, (BlockState)state.with(AGE, i));
            lv = lv.offset(this.growthDirection);
            i = Math.min(i + 1, 25);
        }
    }

    protected abstract int getGrowthLength(Random var1);

    protected abstract boolean chooseStemState(BlockState var1);

    @Override
    protected AbstractPlantStemBlock getStem() {
        return this;
    }
}

