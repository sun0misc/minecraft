/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookshelfBlock
extends BlockWithEntity {
    public static final MapCodec<ChiseledBookshelfBlock> CODEC = ChiseledBookshelfBlock.createCodec(ChiseledBookshelfBlock::new);
    private static final int MAX_BOOK_COUNT = 6;
    public static final int BOOK_HEIGHT = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(Properties.SLOT_0_OCCUPIED, Properties.SLOT_1_OCCUPIED, Properties.SLOT_2_OCCUPIED, Properties.SLOT_3_OCCUPIED, Properties.SLOT_4_OCCUPIED, Properties.SLOT_5_OCCUPIED);

    public MapCodec<ChiseledBookshelfBlock> getCodec() {
        return CODEC;
    }

    public ChiseledBookshelfBlock(AbstractBlock.Settings arg) {
        super(arg);
        BlockState lv = (BlockState)((BlockState)this.stateManager.getDefaultState()).with(HorizontalFacingBlock.FACING, Direction.NORTH);
        for (BooleanProperty lv2 : SLOT_OCCUPIED_PROPERTIES) {
            lv = (BlockState)lv.with(lv2, false);
        }
        this.setDefaultState(lv);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChiseledBookshelfBlockEntity)) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        ChiseledBookshelfBlockEntity lv = (ChiseledBookshelfBlockEntity)blockEntity;
        if (!stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        OptionalInt optionalInt = this.getSlotForHitPos(hit, state);
        if (optionalInt.isEmpty()) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (((Boolean)state.get(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ChiseledBookshelfBlock.tryAddBook(world, pos, player, lv, stack, optionalInt.getAsInt());
        return ItemActionResult.success(world.isClient);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChiseledBookshelfBlockEntity)) {
            return ActionResult.PASS;
        }
        ChiseledBookshelfBlockEntity lv = (ChiseledBookshelfBlockEntity)blockEntity;
        OptionalInt optionalInt = this.getSlotForHitPos(hit, state);
        if (optionalInt.isEmpty()) {
            return ActionResult.PASS;
        }
        if (!((Boolean)state.get(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return ActionResult.CONSUME;
        }
        ChiseledBookshelfBlock.tryRemoveBook(world, pos, player, lv, optionalInt.getAsInt());
        return ActionResult.success(world.isClient);
    }

    private OptionalInt getSlotForHitPos(BlockHitResult hit, BlockState state) {
        return ChiseledBookshelfBlock.getHitPos(hit, state.get(HorizontalFacingBlock.FACING)).map(hitPos -> {
            int i = hitPos.y >= 0.5f ? 0 : 1;
            int j = ChiseledBookshelfBlock.getColumn(hitPos.x);
            return OptionalInt.of(j + i * 3);
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2f> getHitPos(BlockHitResult hit, Direction facing) {
        Direction lv = hit.getSide();
        if (facing != lv) {
            return Optional.empty();
        }
        BlockPos lv2 = hit.getBlockPos().offset(lv);
        Vec3d lv3 = hit.getPos().subtract(lv2.getX(), lv2.getY(), lv2.getZ());
        double d = lv3.getX();
        double e = lv3.getY();
        double f = lv3.getZ();
        return switch (lv) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> Optional.of(new Vec2f((float)(1.0 - d), (float)e));
            case Direction.SOUTH -> Optional.of(new Vec2f((float)d, (float)e));
            case Direction.WEST -> Optional.of(new Vec2f((float)f, (float)e));
            case Direction.EAST -> Optional.of(new Vec2f((float)(1.0 - f), (float)e));
            case Direction.DOWN, Direction.UP -> Optional.empty();
        };
    }

    private static int getColumn(float x) {
        float g = 0.0625f;
        float h = 0.375f;
        if (x < 0.375f) {
            return 0;
        }
        float i = 0.6875f;
        if (x < 0.6875f) {
            return 1;
        }
        return 2;
    }

    private static void tryAddBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, ItemStack stack, int slot) {
        if (world.isClient) {
            return;
        }
        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        SoundEvent lv = stack.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT;
        blockEntity.setStack(slot, stack.splitUnlessCreative(1, player));
        world.playSound(null, pos, lv, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private static void tryRemoveBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, int slot) {
        if (world.isClient) {
            return;
        }
        ItemStack lv = blockEntity.removeStack(slot, 1);
        SoundEvent lv2 = lv.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP;
        world.playSound(null, pos, lv2, SoundCategory.BLOCKS, 1.0f, 1.0f);
        if (!player.getInventory().insertStack(lv)) {
            player.dropItem(lv, false);
        }
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChiseledBookshelfBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HorizontalFacingBlock.FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(property -> builder.add((Property<?>)property));
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ChiseledBookshelfBlockEntity lv2;
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ChiseledBookshelfBlockEntity && !(lv2 = (ChiseledBookshelfBlockEntity)lv).isEmpty()) {
            for (int i = 0; i < 6; ++i) {
                ItemStack lv3 = lv2.getStack(i);
                if (lv3.isEmpty()) continue;
                ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv3);
            }
            lv2.clear();
            world.updateComparators(pos, this);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(HorizontalFacingBlock.FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(HorizontalFacingBlock.FACING, rotation.rotate(state.get(HorizontalFacingBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(HorizontalFacingBlock.FACING)));
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.isClient()) {
            return 0;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChiseledBookshelfBlockEntity) {
            ChiseledBookshelfBlockEntity lv = (ChiseledBookshelfBlockEntity)blockEntity;
            return lv.getLastInteractedSlot() + 1;
        }
        return 0;
    }
}

