/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CandleCakeBlock
extends AbstractCandleBlock {
    public static final MapCodec<CandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("candle")).forGetter(block -> block.candle), CandleCakeBlock.createSettingsCodec()).apply((Applicative<CandleCakeBlock, ?>)instance, CandleCakeBlock::new));
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    protected static final float field_31052 = 1.0f;
    protected static final VoxelShape CAKE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
    protected static final VoxelShape CANDLE_SHAPE = Block.createCuboidShape(7.0, 8.0, 7.0, 9.0, 14.0, 9.0);
    protected static final VoxelShape SHAPE = VoxelShapes.union(CAKE_SHAPE, CANDLE_SHAPE);
    private static final Map<CandleBlock, CandleCakeBlock> CANDLES_TO_CANDLE_CAKES = Maps.newHashMap();
    private static final Iterable<Vec3d> PARTICLE_OFFSETS = ImmutableList.of(new Vec3d(0.5, 1.0, 0.5));
    private final CandleBlock candle;

    public MapCodec<CandleCakeBlock> getCodec() {
        return CODEC;
    }

    protected CandleCakeBlock(Block candle, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, false));
        if (!(candle instanceof CandleBlock)) {
            throw new IllegalArgumentException("Expected block to be of " + String.valueOf(CandleBlock.class) + " was " + String.valueOf(candle.getClass()));
        }
        CandleBlock lv = (CandleBlock)candle;
        CANDLES_TO_CANDLE_CAKES.put(lv, this);
        this.candle = lv;
    }

    @Override
    protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return PARTICLE_OFFSETS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE)) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (CandleCakeBlock.isHittingCandle(hit) && stack.isEmpty() && state.get(LIT).booleanValue()) {
            CandleCakeBlock.extinguish(player, state, world, pos);
            return ItemActionResult.success(world.isClient);
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ActionResult lv = CakeBlock.tryEat(world, pos, Blocks.CAKE.getDefaultState(), player);
        if (lv.isAccepted()) {
            CandleCakeBlock.dropStacks(state, world, pos);
        }
        return lv;
    }

    private static boolean isHittingCandle(BlockHitResult hitResult) {
        return hitResult.getPos().y - (double)hitResult.getBlockPos().getY() > 0.5;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.CAKE);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSolid();
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return CakeBlock.DEFAULT_COMPARATOR_OUTPUT;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public static BlockState getCandleCakeFromCandle(CandleBlock candle) {
        return CANDLES_TO_CANDLE_CAKES.get(candle).getDefaultState();
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(BlockTags.CANDLE_CAKES, statex -> statex.contains(LIT) && state.get(LIT) == false);
    }
}

