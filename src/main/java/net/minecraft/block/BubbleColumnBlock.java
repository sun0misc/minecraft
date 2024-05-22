/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BubbleColumnBlock
extends Block
implements FluidDrainable {
    public static final MapCodec<BubbleColumnBlock> CODEC = BubbleColumnBlock.createCodec(BubbleColumnBlock::new);
    public static final BooleanProperty DRAG = Properties.DRAG;
    private static final int SCHEDULED_TICK_DELAY = 5;

    public MapCodec<BubbleColumnBlock> getCodec() {
        return CODEC;
    }

    public BubbleColumnBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DRAG, true));
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockState lv = world.getBlockState(pos.up());
        if (lv.isAir()) {
            entity.onBubbleColumnSurfaceCollision(state.get(DRAG));
            if (!world.isClient) {
                ServerWorld lv2 = (ServerWorld)world;
                for (int i = 0; i < 2; ++i) {
                    lv2.spawnParticles(ParticleTypes.SPLASH, (double)pos.getX() + world.random.nextDouble(), pos.getY() + 1, (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                    lv2.spawnParticles(ParticleTypes.BUBBLE, (double)pos.getX() + world.random.nextDouble(), pos.getY() + 1, (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.01, 0.0, 0.2);
                }
            }
        } else {
            entity.onBubbleColumnCollision(state.get(DRAG));
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BubbleColumnBlock.update(world, pos, state, world.getBlockState(pos.down()));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getStill(false);
    }

    public static void update(WorldAccess world, BlockPos pos, BlockState state) {
        BubbleColumnBlock.update(world, pos, world.getBlockState(pos), state);
    }

    public static void update(WorldAccess world, BlockPos pos, BlockState water, BlockState bubbleSource) {
        if (!BubbleColumnBlock.isStillWater(water)) {
            return;
        }
        BlockState lv = BubbleColumnBlock.getBubbleState(bubbleSource);
        world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
        BlockPos.Mutable lv2 = pos.mutableCopy().move(Direction.UP);
        while (BubbleColumnBlock.isStillWater(world.getBlockState(lv2))) {
            if (!world.setBlockState(lv2, lv, Block.NOTIFY_LISTENERS)) {
                return;
            }
            lv2.move(Direction.UP);
        }
    }

    private static boolean isStillWater(BlockState state) {
        return state.isOf(Blocks.BUBBLE_COLUMN) || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() >= 8 && state.getFluidState().isStill();
    }

    private static BlockState getBubbleState(BlockState state) {
        if (state.isOf(Blocks.BUBBLE_COLUMN)) {
            return state;
        }
        if (state.isOf(Blocks.SOUL_SAND)) {
            return (BlockState)Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, false);
        }
        if (state.isOf(Blocks.MAGMA_BLOCK)) {
            return (BlockState)Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, true);
        }
        return Blocks.WATER.getDefaultState();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d = pos.getX();
        double e = pos.getY();
        double f = pos.getZ();
        if (state.get(DRAG).booleanValue()) {
            world.addImportantParticle(ParticleTypes.CURRENT_DOWN, d + 0.5, e + 0.8, f, 0.0, 0.0, 0.0);
            if (random.nextInt(200) == 0) {
                world.playSound(d, e, f, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f, false);
            }
        } else {
            world.addImportantParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + 0.5, e, f + 0.5, 0.0, 0.04, 0.0);
            world.addImportantParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + (double)random.nextFloat(), e + (double)random.nextFloat(), f + (double)random.nextFloat(), 0.0, 0.04, 0.0);
            if (random.nextInt(200) == 0) {
                world.playSound(d, e, f, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        if (!state.canPlaceAt(world, pos) || direction == Direction.DOWN || direction == Direction.UP && !neighborState.isOf(Blocks.BUBBLE_COLUMN) && BubbleColumnBlock.isStillWater(neighborState)) {
            world.scheduleBlockTick(pos, this, 5);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.down());
        return lv.isOf(Blocks.BUBBLE_COLUMN) || lv.isOf(Blocks.MAGMA_BLOCK) || lv.isOf(Blocks.SOUL_SAND);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DRAG);
    }

    @Override
    public ItemStack tryDrainFluid(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Fluids.WATER.getBucketFillSound();
    }
}

