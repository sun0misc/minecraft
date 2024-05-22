/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class ButtonBlock
extends WallMountedBlock {
    public static final MapCodec<ButtonBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(block -> block.blockSetType), ((MapCodec)Codec.intRange(1, 1024).fieldOf("ticks_to_stay_pressed")).forGetter(block -> block.pressTicks), ButtonBlock.createSettingsCodec()).apply((Applicative<ButtonBlock, ?>)instance, ButtonBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final int field_31040 = 1;
    private static final int field_31041 = 2;
    protected static final int field_31042 = 2;
    protected static final int field_31043 = 3;
    protected static final VoxelShape CEILING_X_SHAPE = Block.createCuboidShape(6.0, 14.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape CEILING_Z_SHAPE = Block.createCuboidShape(5.0, 14.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape FLOOR_X_SHAPE = Block.createCuboidShape(6.0, 0.0, 5.0, 10.0, 2.0, 11.0);
    protected static final VoxelShape FLOOR_Z_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 11.0, 2.0, 10.0);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0, 6.0, 14.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0, 6.0, 0.0, 11.0, 10.0, 2.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 6.0, 5.0, 2.0, 10.0, 11.0);
    protected static final VoxelShape CEILING_X_PRESSED_SHAPE = Block.createCuboidShape(6.0, 15.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape CEILING_Z_PRESSED_SHAPE = Block.createCuboidShape(5.0, 15.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape FLOOR_X_PRESSED_SHAPE = Block.createCuboidShape(6.0, 0.0, 5.0, 10.0, 1.0, 11.0);
    protected static final VoxelShape FLOOR_Z_PRESSED_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 11.0, 1.0, 10.0);
    protected static final VoxelShape NORTH_PRESSED_SHAPE = Block.createCuboidShape(5.0, 6.0, 15.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_PRESSED_SHAPE = Block.createCuboidShape(5.0, 6.0, 0.0, 11.0, 10.0, 1.0);
    protected static final VoxelShape WEST_PRESSED_SHAPE = Block.createCuboidShape(15.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_PRESSED_SHAPE = Block.createCuboidShape(0.0, 6.0, 5.0, 1.0, 10.0, 11.0);
    private final BlockSetType blockSetType;
    private final int pressTicks;

    public MapCodec<ButtonBlock> getCodec() {
        return CODEC;
    }

    protected ButtonBlock(BlockSetType blockSetType, int pressTicks, AbstractBlock.Settings settings) {
        super(settings.sounds(blockSetType.soundType()));
        this.blockSetType = blockSetType;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(FACE, BlockFace.WALL));
        this.pressTicks = pressTicks;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction lv = state.get(FACING);
        boolean bl = state.get(POWERED);
        switch ((BlockFace)state.get(FACE)) {
            case FLOOR: {
                if (lv.getAxis() == Direction.Axis.X) {
                    return bl ? FLOOR_X_PRESSED_SHAPE : FLOOR_X_SHAPE;
                }
                return bl ? FLOOR_Z_PRESSED_SHAPE : FLOOR_Z_SHAPE;
            }
            case WALL: {
                return switch (lv) {
                    default -> throw new MatchException(null, null);
                    case Direction.EAST -> {
                        if (bl) {
                            yield EAST_PRESSED_SHAPE;
                        }
                        yield EAST_SHAPE;
                    }
                    case Direction.WEST -> {
                        if (bl) {
                            yield WEST_PRESSED_SHAPE;
                        }
                        yield WEST_SHAPE;
                    }
                    case Direction.SOUTH -> {
                        if (bl) {
                            yield SOUTH_PRESSED_SHAPE;
                        }
                        yield SOUTH_SHAPE;
                    }
                    case Direction.NORTH, Direction.UP, Direction.DOWN -> bl ? NORTH_PRESSED_SHAPE : NORTH_SHAPE;
                };
            }
        }
        if (lv.getAxis() == Direction.Axis.X) {
            return bl ? CEILING_X_PRESSED_SHAPE : CEILING_X_SHAPE;
        }
        return bl ? CEILING_Z_PRESSED_SHAPE : CEILING_Z_SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (state.get(POWERED).booleanValue()) {
            return ActionResult.CONSUME;
        }
        this.powerOn(state, world, pos, player);
        return ActionResult.success(world.isClient);
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks() && !state.get(POWERED).booleanValue()) {
            this.powerOn(state, world, pos, null);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    public void powerOn(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        world.setBlockState(pos, (BlockState)state.with(POWERED, true), Block.NOTIFY_ALL);
        this.updateNeighbors(state, world, pos);
        world.scheduleBlockTick(pos, this, this.pressTicks);
        this.playClickSound(player, world, pos, true);
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_ACTIVATE, pos);
    }

    protected void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, boolean powered) {
        world.playSound(powered ? player : null, pos, this.getClickSound(powered), SoundCategory.BLOCKS);
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ? this.blockSetType.buttonClickOn() : this.blockSetType.buttonClickOff();
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) {
            return;
        }
        if (state.get(POWERED).booleanValue()) {
            this.updateNeighbors(state, world, pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(POWERED).booleanValue() && ButtonBlock.getDirection(state) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(POWERED).booleanValue()) {
            return;
        }
        this.tryPowerWithProjectiles(state, world, pos);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient || !this.blockSetType.canButtonBeActivatedByArrows() || state.get(POWERED).booleanValue()) {
            return;
        }
        this.tryPowerWithProjectiles(state, world, pos);
    }

    protected void tryPowerWithProjectiles(BlockState state, World world, BlockPos pos) {
        boolean bl2;
        PersistentProjectileEntity lv = this.blockSetType.canButtonBeActivatedByArrows() ? (PersistentProjectileEntity)world.getNonSpectatingEntities(PersistentProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos)).stream().findFirst().orElse(null) : null;
        boolean bl = lv != null;
        if (bl != (bl2 = state.get(POWERED).booleanValue())) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, bl), Block.NOTIFY_ALL);
            this.updateNeighbors(state, world, pos);
            this.playClickSound(null, world, pos, bl);
            world.emitGameEvent((Entity)lv, bl ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        }
        if (bl) {
            world.scheduleBlockTick(new BlockPos(pos), this, this.pressTicks);
        }
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, this);
        world.updateNeighborsAlways(pos.offset(ButtonBlock.getDirection(state).getOpposite()), this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, FACE);
    }
}

