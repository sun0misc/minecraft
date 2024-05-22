/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MushroomBlock
extends Block {
    public static final MapCodec<MushroomBlock> CODEC = MushroomBlock.createCodec(MushroomBlock::new);
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    public static final BooleanProperty UP = ConnectingBlock.UP;
    public static final BooleanProperty DOWN = ConnectingBlock.DOWN;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;

    public MapCodec<MushroomBlock> getCodec() {
        return CODEC;
    }

    public MushroomBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, true)).with(EAST, true)).with(SOUTH, true)).with(WEST, true)).with(UP, true)).with(DOWN, true));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(DOWN, !lv.getBlockState(lv2.down()).isOf(this))).with(UP, !lv.getBlockState(lv2.up()).isOf(this))).with(NORTH, !lv.getBlockState(lv2.north()).isOf(this))).with(EAST, !lv.getBlockState(lv2.east()).isOf(this))).with(SOUTH, !lv.getBlockState(lv2.south()).isOf(this))).with(WEST, !lv.getBlockState(lv2.west()).isOf(this));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (neighborState.isOf(this)) {
            return (BlockState)state.with(FACING_PROPERTIES.get(direction), false);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.with(FACING_PROPERTIES.get(rotation.rotate(Direction.NORTH)), state.get(NORTH))).with(FACING_PROPERTIES.get(rotation.rotate(Direction.SOUTH)), state.get(SOUTH))).with(FACING_PROPERTIES.get(rotation.rotate(Direction.EAST)), state.get(EAST))).with(FACING_PROPERTIES.get(rotation.rotate(Direction.WEST)), state.get(WEST))).with(FACING_PROPERTIES.get(rotation.rotate(Direction.UP)), state.get(UP))).with(FACING_PROPERTIES.get(rotation.rotate(Direction.DOWN)), state.get(DOWN));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.with(FACING_PROPERTIES.get(mirror.apply(Direction.NORTH)), state.get(NORTH))).with(FACING_PROPERTIES.get(mirror.apply(Direction.SOUTH)), state.get(SOUTH))).with(FACING_PROPERTIES.get(mirror.apply(Direction.EAST)), state.get(EAST))).with(FACING_PROPERTIES.get(mirror.apply(Direction.WEST)), state.get(WEST))).with(FACING_PROPERTIES.get(mirror.apply(Direction.UP)), state.get(UP))).with(FACING_PROPERTIES.get(mirror.apply(Direction.DOWN)), state.get(DOWN));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }
}

