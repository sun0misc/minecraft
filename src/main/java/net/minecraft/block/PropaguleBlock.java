/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PropaguleBlock
extends SaplingBlock
implements Waterloggable {
    public static final MapCodec<PropaguleBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)SaplingGenerator.CODEC.fieldOf("tree")).forGetter(block -> block.generator), PropaguleBlock.createSettingsCodec()).apply((Applicative<PropaguleBlock, ?>)instance, PropaguleBlock::new));
    public static final IntProperty AGE = Properties.AGE_4;
    public static final int field_37589 = 4;
    private static final VoxelShape[] SHAPES = new VoxelShape[]{Block.createCuboidShape(7.0, 13.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 10.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 7.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 3.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)};
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty HANGING = Properties.HANGING;

    public MapCodec<PropaguleBlock> getCodec() {
        return CODEC;
    }

    public PropaguleBlock(SaplingGenerator arg, AbstractBlock.Settings arg2) {
        super(arg, arg2);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(STAGE, 0)).with(AGE, 0)).with(WATERLOGGED, false)).with(HANGING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return super.canPlantOnTop(floor, world, pos) || floor.isOf(Blocks.CLAY);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = lv.getFluid() == Fluids.WATER;
        return (BlockState)((BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl)).with(AGE, 4);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d lv = state.getModelOffset(world, pos);
        VoxelShape lv2 = state.get(HANGING) == false ? SHAPES[4] : SHAPES[state.get(AGE)];
        return lv2.offset(lv.x, lv.y, lv.z);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (PropaguleBlock.isHanging(state)) {
            return world.getBlockState(pos.up()).isOf(Blocks.MANGROVE_LEAVES);
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!PropaguleBlock.isHanging(state)) {
            if (random.nextInt(7) == 0) {
                this.generate(world, pos, state, random);
            }
            return;
        }
        if (!PropaguleBlock.isFullyGrown(state)) {
            world.setBlockState(pos, (BlockState)state.cycle(AGE), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return !PropaguleBlock.isHanging(state) || !PropaguleBlock.isFullyGrown(state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return PropaguleBlock.isHanging(state) ? !PropaguleBlock.isFullyGrown(state) : super.canGrow(world, random, pos, state);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        if (PropaguleBlock.isHanging(state) && !PropaguleBlock.isFullyGrown(state)) {
            world.setBlockState(pos, (BlockState)state.cycle(AGE), Block.NOTIFY_LISTENERS);
        } else {
            super.grow(world, random, pos, state);
        }
    }

    private static boolean isHanging(BlockState state) {
        return state.get(HANGING);
    }

    private static boolean isFullyGrown(BlockState state) {
        return state.get(AGE) == 4;
    }

    public static BlockState getDefaultHangingState() {
        return PropaguleBlock.getHangingState(0);
    }

    public static BlockState getHangingState(int age) {
        return (BlockState)((BlockState)Blocks.MANGROVE_PROPAGULE.getDefaultState().with(HANGING, true)).with(AGE, age);
    }
}

