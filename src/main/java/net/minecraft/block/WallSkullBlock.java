/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SkullBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WallSkullBlock
extends AbstractSkullBlock {
    public static final MapCodec<WallSkullBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)SkullBlock.SkullType.CODEC.fieldOf("kind")).forGetter(AbstractSkullBlock::getSkullType), WallSkullBlock.createSettingsCodec()).apply((Applicative<WallSkullBlock, ?>)instance, WallSkullBlock::new));
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private static final Map<Direction, VoxelShape> FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(4.0, 4.0, 8.0, 12.0, 12.0, 16.0), Direction.SOUTH, Block.createCuboidShape(4.0, 4.0, 0.0, 12.0, 12.0, 8.0), Direction.EAST, Block.createCuboidShape(0.0, 4.0, 4.0, 8.0, 12.0, 12.0), Direction.WEST, Block.createCuboidShape(8.0, 4.0, 4.0, 16.0, 12.0, 12.0)));

    public MapCodec<? extends WallSkullBlock> getCodec() {
        return CODEC;
    }

    protected WallSkullBlock(SkullBlock.SkullType arg, AbstractBlock.Settings arg2) {
        super(arg, arg2);
        this.setDefaultState((BlockState)this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public String getTranslationKey() {
        return this.asItem().getTranslationKey();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return FACING_TO_SHAPE.get(state.get(FACING));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction[] lvs;
        BlockState lv = super.getPlacementState(ctx);
        World lv2 = ctx.getWorld();
        BlockPos lv3 = ctx.getBlockPos();
        for (Direction lv4 : lvs = ctx.getPlacementDirections()) {
            if (!lv4.getAxis().isHorizontal()) continue;
            Direction lv5 = lv4.getOpposite();
            lv = (BlockState)lv.with(FACING, lv5);
            if (lv2.getBlockState(lv3.offset(lv4)).canReplace(ctx)) continue;
            return lv;
        }
        return null;
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
        super.appendProperties(builder);
        builder.add(FACING);
    }
}

