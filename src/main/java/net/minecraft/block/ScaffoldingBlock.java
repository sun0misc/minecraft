/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.FallingBlockEntity;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ScaffoldingBlock
extends Block
implements Waterloggable {
    public static final MapCodec<ScaffoldingBlock> CODEC = ScaffoldingBlock.createCodec(ScaffoldingBlock::new);
    private static final int field_31238 = 1;
    private static final VoxelShape NORMAL_OUTLINE_SHAPE;
    private static final VoxelShape BOTTOM_OUTLINE_SHAPE;
    private static final VoxelShape COLLISION_SHAPE;
    private static final VoxelShape OUTLINE_SHAPE;
    public static final int MAX_DISTANCE = 7;
    public static final IntProperty DISTANCE;
    public static final BooleanProperty WATERLOGGED;
    public static final BooleanProperty BOTTOM;

    public MapCodec<ScaffoldingBlock> getCodec() {
        return CODEC;
    }

    protected ScaffoldingBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(WATERLOGGED, false)).with(BOTTOM, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, WATERLOGGED, BOTTOM);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!context.isHolding(state.getBlock().asItem())) {
            return state.get(BOTTOM) != false ? BOTTOM_OUTLINE_SHAPE : NORMAL_OUTLINE_SHAPE;
        }
        return VoxelShapes.fullCube();
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return context.getStack().isOf(this.asItem());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv = ctx.getBlockPos();
        World lv2 = ctx.getWorld();
        int i = ScaffoldingBlock.calculateDistance(lv2, lv);
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, lv2.getFluidState(lv).getFluid() == Fluids.WATER)).with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(lv2, lv, i));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i = ScaffoldingBlock.calculateDistance(world, pos);
        BlockState lv = (BlockState)((BlockState)state.with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(world, pos, i));
        if (lv.get(DISTANCE) == 7) {
            if (state.get(DISTANCE) == 7) {
                FallingBlockEntity.spawnFromBlock(world, pos, lv);
            } else {
                world.breakBlock(pos, true);
            }
        } else if (state != lv) {
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return ScaffoldingBlock.calculateDistance(world, pos) < 7;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!context.isAbove(VoxelShapes.fullCube(), pos, true) || context.isDescending()) {
            if (state.get(DISTANCE) != 0 && state.get(BOTTOM).booleanValue() && context.isAbove(OUTLINE_SHAPE, pos, true)) {
                return COLLISION_SHAPE;
            }
            return VoxelShapes.empty();
        }
        return NORMAL_OUTLINE_SHAPE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    private boolean shouldBeBottom(BlockView world, BlockPos pos, int distance) {
        return distance > 0 && !world.getBlockState(pos.down()).isOf(this);
    }

    public static int calculateDistance(BlockView world, BlockPos pos) {
        Direction lv3;
        BlockState lv4;
        BlockPos.Mutable lv = pos.mutableCopy().move(Direction.DOWN);
        BlockState lv2 = world.getBlockState(lv);
        int i = 7;
        if (lv2.isOf(Blocks.SCAFFOLDING)) {
            i = lv2.get(DISTANCE);
        } else if (lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
            return 0;
        }
        Iterator<Direction> iterator = Direction.Type.HORIZONTAL.iterator();
        while (iterator.hasNext() && (!(lv4 = world.getBlockState(lv.set((Vec3i)pos, lv3 = iterator.next()))).isOf(Blocks.SCAFFOLDING) || (i = Math.min(i, lv4.get(DISTANCE) + 1)) != 1)) {
        }
        return i;
    }

    static {
        COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        OUTLINE_SHAPE = VoxelShapes.fullCube().offset(0.0, -1.0, 0.0);
        DISTANCE = Properties.DISTANCE_0_7;
        WATERLOGGED = Properties.WATERLOGGED;
        BOTTOM = Properties.BOTTOM;
        VoxelShape lv = Block.createCuboidShape(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
        VoxelShape lv2 = Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 16.0, 2.0);
        VoxelShape lv3 = Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 16.0, 2.0);
        VoxelShape lv4 = Block.createCuboidShape(0.0, 0.0, 14.0, 2.0, 16.0, 16.0);
        VoxelShape lv5 = Block.createCuboidShape(14.0, 0.0, 14.0, 16.0, 16.0, 16.0);
        NORMAL_OUTLINE_SHAPE = VoxelShapes.union(lv, lv2, lv3, lv4, lv5);
        VoxelShape lv6 = Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 2.0, 16.0);
        VoxelShape lv7 = Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        VoxelShape lv8 = Block.createCuboidShape(0.0, 0.0, 14.0, 16.0, 2.0, 16.0);
        VoxelShape lv9 = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 2.0);
        BOTTOM_OUTLINE_SHAPE = VoxelShapes.union(COLLISION_SHAPE, NORMAL_OUTLINE_SHAPE, lv7, lv6, lv9, lv8);
    }
}

