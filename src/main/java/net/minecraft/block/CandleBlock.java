/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CandleBlock
extends AbstractCandleBlock
implements Waterloggable {
    public static final MapCodec<CandleBlock> CODEC = CandleBlock.createCodec(CandleBlock::new);
    public static final int field_31050 = 1;
    public static final int MAX_CANDLE_AMOUNT = 4;
    public static final IntProperty CANDLES = Properties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> state.get(LIT) != false ? 3 * state.get(CANDLES) : 0;
    private static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectOpenHashMap int2ObjectMap = new Int2ObjectOpenHashMap();
        int2ObjectMap.defaultReturnValue(ImmutableList.of());
        int2ObjectMap.put(1, ImmutableList.of(new Vec3d(0.5, 0.5, 0.5)));
        int2ObjectMap.put(2, ImmutableList.of(new Vec3d(0.375, 0.44, 0.5), new Vec3d(0.625, 0.5, 0.44)));
        int2ObjectMap.put(3, ImmutableList.of(new Vec3d(0.5, 0.313, 0.625), new Vec3d(0.375, 0.44, 0.5), new Vec3d(0.56, 0.5, 0.44)));
        int2ObjectMap.put(4, ImmutableList.of(new Vec3d(0.44, 0.313, 0.56), new Vec3d(0.625, 0.44, 0.56), new Vec3d(0.375, 0.44, 0.375), new Vec3d(0.56, 0.5, 0.375)));
        return Int2ObjectMaps.unmodifiable(int2ObjectMap);
    });
    private static final VoxelShape ONE_CANDLE_SHAPE = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
    private static final VoxelShape TWO_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
    private static final VoxelShape THREE_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
    private static final VoxelShape FOUR_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);

    public MapCodec<CandleBlock> getCodec() {
        return CODEC;
    }

    public CandleBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CANDLES, 1)).with(LIT, false)).with(WATERLOGGED, false));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty() && player.getAbilities().allowModifyWorld && state.get(LIT).booleanValue()) {
            CandleBlock.extinguish(player, state, world, pos);
            return ItemActionResult.success(world.isClient);
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (!context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(CANDLES) < 4) {
            return true;
        }
        return super.canReplace(state, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (lv.isOf(this)) {
            return (BlockState)lv.cycle(CANDLES);
        }
        FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = lv2.getFluid() == Fluids.WATER;
        return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
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
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(CANDLES)) {
            default: {
                return ONE_CANDLE_SHAPE;
            }
            case 2: {
                return TWO_CANDLES_SHAPE;
            }
            case 3: {
                return THREE_CANDLES_SHAPE;
            }
            case 4: 
        }
        return FOUR_CANDLES_SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CANDLES, LIT, WATERLOGGED);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.get(WATERLOGGED).booleanValue() || fluidState.getFluid() != Fluids.WATER) {
            return false;
        }
        BlockState lv = (BlockState)state.with(WATERLOGGED, true);
        if (state.get(LIT).booleanValue()) {
            CandleBlock.extinguish(null, lv, world, pos);
        } else {
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
        }
        world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
        return true;
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(BlockTags.CANDLES, statex -> statex.contains(LIT) && statex.contains(WATERLOGGED)) && state.get(LIT) == false && state.get(WATERLOGGED) == false;
    }

    @Override
    protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return (Iterable)CANDLES_TO_PARTICLE_OFFSETS.get(state.get(CANDLES));
    }

    @Override
    protected boolean isNotLit(BlockState state) {
        return state.get(WATERLOGGED) == false && super.isNotLit(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }
}

