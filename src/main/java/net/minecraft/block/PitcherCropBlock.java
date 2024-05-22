/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PitcherCropBlock
extends TallPlantBlock
implements Fertilizable {
    public static final MapCodec<PitcherCropBlock> CODEC = PitcherCropBlock.createCodec(PitcherCropBlock::new);
    public static final IntProperty AGE = Properties.AGE_4;
    public static final int field_43240 = 4;
    private static final int field_43241 = 3;
    private static final int field_43391 = 1;
    private static final VoxelShape GROWN_UPPER_OUTLINE_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
    private static final VoxelShape GROWN_LOWER_OUTLINE_SHAPE = Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape AGE_0_SHAPE = Block.createCuboidShape(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
    private static final VoxelShape LOWER_COLLISION_SHAPE = Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
    private static final VoxelShape[] UPPER_OUTLINE_SHAPES = new VoxelShape[]{Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), GROWN_UPPER_OUTLINE_SHAPE};
    private static final VoxelShape[] LOWER_OUTLINE_SHAPES = new VoxelShape[]{AGE_0_SHAPE, Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), GROWN_LOWER_OUTLINE_SHAPE, GROWN_LOWER_OUTLINE_SHAPE, GROWN_LOWER_OUTLINE_SHAPE};

    public MapCodec<PitcherCropBlock> getCodec() {
        return CODEC;
    }

    public PitcherCropBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? UPPER_OUTLINE_SHAPES[Math.min(Math.abs(4 - (state.get(AGE) + 1)), UPPER_OUTLINE_SHAPES.length - 1)] : LOWER_OUTLINE_SHAPES[state.get(AGE)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(AGE) == 0) {
            return AGE_0_SHAPE;
        }
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_COLLISION_SHAPE;
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (PitcherCropBlock.isDoubleTallAtAge(state.get(AGE))) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        return state.canPlaceAt(world, pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (PitcherCropBlock.isLowerHalf(state) && !PitcherCropBlock.canPlaceAt(world, pos)) {
            return false;
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.appendProperties(builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof RavagerEntity && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            world.breakBlock(pos, true, entity);
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER && !this.isFullyGrown(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl;
        float f = CropBlock.getAvailableMoisture(this, world, pos);
        boolean bl2 = bl = random.nextInt((int)(25.0f / f) + 1) == 0;
        if (bl) {
            this.tryGrow(world, state, pos, 1);
        }
    }

    private void tryGrow(ServerWorld world, BlockState state, BlockPos pos, int amount) {
        int j = Math.min(state.get(AGE) + amount, 4);
        if (!this.canGrow((WorldView)world, pos, state, j)) {
            return;
        }
        BlockState lv = (BlockState)state.with(AGE, j);
        world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
        if (PitcherCropBlock.isDoubleTallAtAge(j)) {
            world.setBlockState(pos.up(), (BlockState)lv.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
        }
    }

    private static boolean canGrowAt(WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.isAir() || lv.isOf(Blocks.PITCHER_CROP);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return CropBlock.hasEnoughLightAt(world, pos);
    }

    private static boolean isLowerHalf(BlockState state) {
        return state.isOf(Blocks.PITCHER_CROP) && state.get(HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isDoubleTallAtAge(int age) {
        return age >= 3;
    }

    private boolean canGrow(WorldView world, BlockPos pos, BlockState state, int age) {
        return !this.isFullyGrown(state) && PitcherCropBlock.canPlaceAt(world, pos) && (!PitcherCropBlock.isDoubleTallAtAge(age) || PitcherCropBlock.canGrowAt(world, pos.up()));
    }

    private boolean isFullyGrown(BlockState state) {
        return state.get(AGE) >= 4;
    }

    @Nullable
    private LowerHalfContext getLowerHalfContext(WorldView world, BlockPos pos, BlockState state) {
        if (PitcherCropBlock.isLowerHalf(state)) {
            return new LowerHalfContext(pos, state);
        }
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (PitcherCropBlock.isLowerHalf(lv2)) {
            return new LowerHalfContext(lv, lv2);
        }
        return null;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        LowerHalfContext lv = this.getLowerHalfContext(world, pos, state);
        if (lv == null) {
            return false;
        }
        return this.canGrow(world, lv.pos, lv.state, lv.state.get(AGE) + 1);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        LowerHalfContext lv = this.getLowerHalfContext(world, pos, state);
        if (lv == null) {
            return;
        }
        this.tryGrow(world, lv.state, lv.pos, 1);
    }

    record LowerHalfContext(BlockPos pos, BlockState state) {
    }
}

