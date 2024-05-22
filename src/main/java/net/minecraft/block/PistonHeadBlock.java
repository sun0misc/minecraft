/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PistonHeadBlock
extends FacingBlock {
    public static final MapCodec<PistonHeadBlock> CODEC = PistonHeadBlock.createCodec(PistonHeadBlock::new);
    public static final EnumProperty<PistonType> TYPE = Properties.PISTON_TYPE;
    public static final BooleanProperty SHORT = Properties.SHORT;
    public static final float field_31377 = 4.0f;
    protected static final VoxelShape EAST_HEAD_SHAPE = Block.createCuboidShape(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_HEAD_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_HEAD_SHAPE = Block.createCuboidShape(0.0, 0.0, 12.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape NORTH_HEAD_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 4.0);
    protected static final VoxelShape UP_HEAD_SHAPE = Block.createCuboidShape(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape DOWN_HEAD_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    protected static final float field_31378 = 2.0f;
    protected static final float field_31379 = 6.0f;
    protected static final float field_31380 = 10.0f;
    protected static final VoxelShape UP_ARM_SHAPE = Block.createCuboidShape(6.0, -4.0, 6.0, 10.0, 12.0, 10.0);
    protected static final VoxelShape DOWN_ARM_SHAPE = Block.createCuboidShape(6.0, 4.0, 6.0, 10.0, 20.0, 10.0);
    protected static final VoxelShape SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0, 6.0, -4.0, 10.0, 10.0, 12.0);
    protected static final VoxelShape NORTH_ARM_SHAPE = Block.createCuboidShape(6.0, 6.0, 4.0, 10.0, 10.0, 20.0);
    protected static final VoxelShape EAST_ARM_SHAPE = Block.createCuboidShape(-4.0, 6.0, 6.0, 12.0, 10.0, 10.0);
    protected static final VoxelShape WEST_ARM_SHAPE = Block.createCuboidShape(4.0, 6.0, 6.0, 20.0, 10.0, 10.0);
    protected static final VoxelShape SHORT_UP_ARM_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 12.0, 10.0);
    protected static final VoxelShape SHORT_DOWN_ARM_SHAPE = Block.createCuboidShape(6.0, 4.0, 6.0, 10.0, 16.0, 10.0);
    protected static final VoxelShape SHORT_SOUTH_ARM_SHAPE = Block.createCuboidShape(6.0, 6.0, 0.0, 10.0, 10.0, 12.0);
    protected static final VoxelShape SHORT_NORTH_ARM_SHAPE = Block.createCuboidShape(6.0, 6.0, 4.0, 10.0, 10.0, 16.0);
    protected static final VoxelShape SHORT_EAST_ARM_SHAPE = Block.createCuboidShape(0.0, 6.0, 6.0, 12.0, 10.0, 10.0);
    protected static final VoxelShape SHORT_WEST_ARM_SHAPE = Block.createCuboidShape(4.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape[] SHORT_HEAD_SHAPES = PistonHeadBlock.getHeadShapes(true);
    private static final VoxelShape[] HEAD_SHAPES = PistonHeadBlock.getHeadShapes(false);

    protected MapCodec<PistonHeadBlock> getCodec() {
        return CODEC;
    }

    private static VoxelShape[] getHeadShapes(boolean shortHead) {
        return (VoxelShape[])Arrays.stream(Direction.values()).map(direction -> PistonHeadBlock.getHeadShape(direction, shortHead)).toArray(VoxelShape[]::new);
    }

    private static VoxelShape getHeadShape(Direction direction, boolean shortHead) {
        switch (direction) {
            default: {
                return VoxelShapes.union(DOWN_HEAD_SHAPE, shortHead ? SHORT_DOWN_ARM_SHAPE : DOWN_ARM_SHAPE);
            }
            case UP: {
                return VoxelShapes.union(UP_HEAD_SHAPE, shortHead ? SHORT_UP_ARM_SHAPE : UP_ARM_SHAPE);
            }
            case NORTH: {
                return VoxelShapes.union(NORTH_HEAD_SHAPE, shortHead ? SHORT_NORTH_ARM_SHAPE : NORTH_ARM_SHAPE);
            }
            case SOUTH: {
                return VoxelShapes.union(SOUTH_HEAD_SHAPE, shortHead ? SHORT_SOUTH_ARM_SHAPE : SOUTH_ARM_SHAPE);
            }
            case WEST: {
                return VoxelShapes.union(WEST_HEAD_SHAPE, shortHead ? SHORT_WEST_ARM_SHAPE : WEST_ARM_SHAPE);
            }
            case EAST: 
        }
        return VoxelShapes.union(EAST_HEAD_SHAPE, shortHead ? SHORT_EAST_ARM_SHAPE : EAST_ARM_SHAPE);
    }

    public PistonHeadBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(TYPE, PistonType.DEFAULT)).with(SHORT, false));
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (state.get(SHORT) != false ? SHORT_HEAD_SHAPES : HEAD_SHAPES)[state.get(FACING).ordinal()];
    }

    private boolean isAttached(BlockState headState, BlockState pistonState) {
        Block lv = headState.get(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
        return pistonState.isOf(lv) && pistonState.get(PistonBlock.EXTENDED) != false && pistonState.get(FACING) == headState.get(FACING);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos lv;
        if (!world.isClient && player.getAbilities().creativeMode && this.isAttached(state, world.getBlockState(lv = pos.offset(state.get(FACING).getOpposite())))) {
            world.breakBlock(lv, false);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        super.onStateReplaced(state, world, pos, newState, moved);
        BlockPos lv = pos.offset(state.get(FACING).getOpposite());
        if (this.isAttached(state, world.getBlockState(lv))) {
            world.breakBlock(lv, true);
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        return this.isAttached(state, lv) || lv.isOf(Blocks.MOVING_PISTON) && lv.get(FACING) == state.get(FACING);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (state.canPlaceAt(world, pos)) {
            world.updateNeighbor(pos.offset(state.get(FACING).getOpposite()), sourceBlock, sourcePos);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(state.get(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}

