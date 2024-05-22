/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BigDripleafStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Tilt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BigDripleafBlock
extends HorizontalFacingBlock
implements Fertilizable,
Waterloggable {
    public static final MapCodec<BigDripleafBlock> CODEC = BigDripleafBlock.createCodec(BigDripleafBlock::new);
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final EnumProperty<Tilt> TILT = Properties.TILT;
    private static final int field_31015 = -1;
    private static final Object2IntMap<Tilt> NEXT_TILT_DELAYS = Util.make(new Object2IntArrayMap(), delays -> {
        delays.defaultReturnValue(-1);
        delays.put(Tilt.UNSTABLE, 10);
        delays.put(Tilt.PARTIAL, 10);
        delays.put(Tilt.FULL, 100);
    });
    private static final int field_31016 = 5;
    private static final int field_31017 = 6;
    private static final int field_31018 = 11;
    private static final int field_31019 = 13;
    private static final Map<Tilt, VoxelShape> SHAPES_FOR_TILT = ImmutableMap.of(Tilt.NONE, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 15.0, 16.0), Tilt.UNSTABLE, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 15.0, 16.0), Tilt.PARTIAL, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 13.0, 16.0), Tilt.FULL, VoxelShapes.empty());
    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, VoxelShape> SHAPES_FOR_DIRECTION = ImmutableMap.of(Direction.NORTH, VoxelShapes.combine(BigDripleafStemBlock.NORTH_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.SOUTH, VoxelShapes.combine(BigDripleafStemBlock.SOUTH_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.EAST, VoxelShapes.combine(BigDripleafStemBlock.EAST_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.WEST, VoxelShapes.combine(BigDripleafStemBlock.WEST_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST));
    private final Map<BlockState, VoxelShape> shapes;

    public MapCodec<BigDripleafBlock> getCodec() {
        return CODEC;
    }

    protected BigDripleafBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false)).with(FACING, Direction.NORTH)).with(TILT, Tilt.NONE));
        this.shapes = this.getShapesForStates(BigDripleafBlock::getShapeForState);
    }

    private static VoxelShape getShapeForState(BlockState state) {
        return VoxelShapes.union(SHAPES_FOR_TILT.get(state.get(TILT)), SHAPES_FOR_DIRECTION.get(state.get(FACING)));
    }

    public static void grow(WorldAccess world, Random random, BlockPos pos, Direction direction) {
        int j;
        int i = MathHelper.nextInt(random, 2, 5);
        BlockPos.Mutable lv = pos.mutableCopy();
        for (j = 0; j < i && BigDripleafBlock.canGrowInto(world, lv, world.getBlockState(lv)); ++j) {
            lv.move(Direction.UP);
        }
        int k = pos.getY() + j - 1;
        lv.setY(pos.getY());
        while (lv.getY() < k) {
            BigDripleafStemBlock.placeStemAt(world, lv, world.getFluidState(lv), direction);
            lv.move(Direction.UP);
        }
        BigDripleafBlock.placeDripleafAt(world, lv, world.getFluidState(lv), direction);
    }

    private static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.SMALL_DRIPLEAF);
    }

    protected static boolean canGrowInto(HeightLimitView world, BlockPos pos, BlockState state) {
        return !world.isOutOfHeightLimit(pos) && BigDripleafBlock.canGrowInto(state);
    }

    protected static boolean placeDripleafAt(WorldAccess world, BlockPos pos, FluidState fluidState, Direction direction) {
        BlockState lv = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF.getDefaultState().with(WATERLOGGED, fluidState.isEqualAndStill(Fluids.WATER))).with(FACING, direction);
        return world.setBlockState(pos, lv, Block.NOTIFY_ALL);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        this.changeTilt(state, world, hit.getBlockPos(), Tilt.FULL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        return lv2.isOf(this) || lv2.isOf(Blocks.BIG_DRIPLEAF_STEM) || lv2.isIn(BlockTags.BIG_DRIPLEAF_PLACEABLE);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == Direction.UP && neighborState.isOf(this)) {
            return Blocks.BIG_DRIPLEAF_STEM.getStateWithProperties(state);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        BlockState lv = world.getBlockState(pos.up());
        return BigDripleafBlock.canGrowInto(lv);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockState lv2;
        BlockPos lv = pos.up();
        if (BigDripleafBlock.canGrowInto(world, lv, lv2 = world.getBlockState(lv))) {
            Direction lv3 = state.get(FACING);
            BigDripleafStemBlock.placeStemAt(world, pos, state.getFluidState(), lv3);
            BigDripleafBlock.placeDripleafAt(world, lv, lv2.getFluidState(), lv3);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) {
            return;
        }
        if (state.get(TILT) == Tilt.NONE && BigDripleafBlock.isEntityAbove(pos, entity) && !world.isReceivingRedstonePower(pos)) {
            this.changeTilt(state, world, pos, Tilt.UNSTABLE, null);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isReceivingRedstonePower(pos)) {
            BigDripleafBlock.resetTilt(state, world, pos);
            return;
        }
        Tilt lv = state.get(TILT);
        if (lv == Tilt.UNSTABLE) {
            this.changeTilt(state, world, pos, Tilt.PARTIAL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
        } else if (lv == Tilt.PARTIAL) {
            this.changeTilt(state, world, pos, Tilt.FULL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
        } else if (lv == Tilt.FULL) {
            BigDripleafBlock.resetTilt(state, world, pos);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            BigDripleafBlock.resetTilt(state, world, pos);
        }
    }

    private static void playTiltSound(World world, BlockPos pos, SoundEvent soundEvent) {
        float f = MathHelper.nextBetween(world.random, 0.8f, 1.2f);
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, f);
    }

    private static boolean isEntityAbove(BlockPos pos, Entity entity) {
        return entity.isOnGround() && entity.getPos().y > (double)((float)pos.getY() + 0.6875f);
    }

    private void changeTilt(BlockState state, World world, BlockPos pos, Tilt tilt, @Nullable SoundEvent sound) {
        int i;
        BigDripleafBlock.changeTilt(state, world, pos, tilt);
        if (sound != null) {
            BigDripleafBlock.playTiltSound(world, pos, sound);
        }
        if ((i = NEXT_TILT_DELAYS.getInt(tilt)) != -1) {
            world.scheduleBlockTick(pos, this, i);
        }
    }

    private static void resetTilt(BlockState state, World world, BlockPos pos) {
        BigDripleafBlock.changeTilt(state, world, pos, Tilt.NONE);
        if (state.get(TILT) != Tilt.NONE) {
            BigDripleafBlock.playTiltSound(world, pos, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_UP);
        }
    }

    private static void changeTilt(BlockState state, World world, BlockPos pos, Tilt tilt) {
        Tilt lv = state.get(TILT);
        world.setBlockState(pos, (BlockState)state.with(TILT, tilt), Block.NOTIFY_LISTENERS);
        if (tilt.isStable() && tilt != lv) {
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_FOR_TILT.get(state.get(TILT));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapes.get(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = lv.isOf(Blocks.BIG_DRIPLEAF) || lv.isOf(Blocks.BIG_DRIPLEAF_STEM);
        return (BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, lv2.isEqualAndStill(Fluids.WATER))).with(FACING, bl ? lv.get(FACING) : ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, TILT);
    }
}

