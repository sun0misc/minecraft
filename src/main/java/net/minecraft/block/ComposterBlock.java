/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ComposterBlock
extends Block
implements InventoryProvider {
    public static final MapCodec<ComposterBlock> CODEC = ComposterBlock.createCodec(ComposterBlock::new);
    public static final int NUM_LEVELS = 8;
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 7;
    public static final IntProperty LEVEL = Properties.LEVEL_8;
    public static final Object2FloatMap<ItemConvertible> ITEM_TO_LEVEL_INCREASE_CHANCE = new Object2FloatOpenHashMap<ItemConvertible>();
    private static final int field_31074 = 2;
    private static final VoxelShape RAYCAST_SHAPE = VoxelShapes.fullCube();
    private static final VoxelShape[] LEVEL_TO_COLLISION_SHAPE = Util.make(new VoxelShape[9], shapes -> {
        for (int i = 0; i < 8; ++i) {
            shapes[i] = VoxelShapes.combineAndSimplify(RAYCAST_SHAPE, Block.createCuboidShape(2.0, Math.max(2, 1 + i * 2), 2.0, 14.0, 16.0, 14.0), BooleanBiFunction.ONLY_FIRST);
        }
        shapes[8] = shapes[7];
    });

    public MapCodec<ComposterBlock> getCodec() {
        return CODEC;
    }

    public static void registerDefaultCompostableItems() {
        ITEM_TO_LEVEL_INCREASE_CHANCE.defaultReturnValue(-1.0f);
        float f = 0.3f;
        float g = 0.5f;
        float h = 0.65f;
        float i = 0.85f;
        float j = 1.0f;
        ComposterBlock.registerCompostableItem(0.3f, Items.JUNGLE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.OAK_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.SPRUCE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.DARK_OAK_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.ACACIA_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.CHERRY_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.BIRCH_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.AZALEA_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.OAK_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.SPRUCE_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.BIRCH_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.JUNGLE_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.ACACIA_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.CHERRY_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.DARK_OAK_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_PROPAGULE);
        ComposterBlock.registerCompostableItem(0.3f, Items.BEETROOT_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.DRIED_KELP);
        ComposterBlock.registerCompostableItem(0.3f, Items.SHORT_GRASS);
        ComposterBlock.registerCompostableItem(0.3f, Items.KELP);
        ComposterBlock.registerCompostableItem(0.3f, Items.MELON_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.PUMPKIN_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.SEAGRASS);
        ComposterBlock.registerCompostableItem(0.3f, Items.SWEET_BERRIES);
        ComposterBlock.registerCompostableItem(0.3f, Items.GLOW_BERRIES);
        ComposterBlock.registerCompostableItem(0.3f, Items.WHEAT_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.MOSS_CARPET);
        ComposterBlock.registerCompostableItem(0.3f, Items.PINK_PETALS);
        ComposterBlock.registerCompostableItem(0.3f, Items.SMALL_DRIPLEAF);
        ComposterBlock.registerCompostableItem(0.3f, Items.HANGING_ROOTS);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_ROOTS);
        ComposterBlock.registerCompostableItem(0.3f, Items.TORCHFLOWER_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.PITCHER_POD);
        ComposterBlock.registerCompostableItem(0.5f, Items.DRIED_KELP_BLOCK);
        ComposterBlock.registerCompostableItem(0.5f, Items.TALL_GRASS);
        ComposterBlock.registerCompostableItem(0.5f, Items.FLOWERING_AZALEA_LEAVES);
        ComposterBlock.registerCompostableItem(0.5f, Items.CACTUS);
        ComposterBlock.registerCompostableItem(0.5f, Items.SUGAR_CANE);
        ComposterBlock.registerCompostableItem(0.5f, Items.VINE);
        ComposterBlock.registerCompostableItem(0.5f, Items.NETHER_SPROUTS);
        ComposterBlock.registerCompostableItem(0.5f, Items.WEEPING_VINES);
        ComposterBlock.registerCompostableItem(0.5f, Items.TWISTING_VINES);
        ComposterBlock.registerCompostableItem(0.5f, Items.MELON_SLICE);
        ComposterBlock.registerCompostableItem(0.5f, Items.GLOW_LICHEN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SEA_PICKLE);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILY_PAD);
        ComposterBlock.registerCompostableItem(0.65f, Items.PUMPKIN);
        ComposterBlock.registerCompostableItem(0.65f, Items.CARVED_PUMPKIN);
        ComposterBlock.registerCompostableItem(0.65f, Items.MELON);
        ComposterBlock.registerCompostableItem(0.65f, Items.APPLE);
        ComposterBlock.registerCompostableItem(0.65f, Items.BEETROOT);
        ComposterBlock.registerCompostableItem(0.65f, Items.CARROT);
        ComposterBlock.registerCompostableItem(0.65f, Items.COCOA_BEANS);
        ComposterBlock.registerCompostableItem(0.65f, Items.POTATO);
        ComposterBlock.registerCompostableItem(0.65f, Items.WHEAT);
        ComposterBlock.registerCompostableItem(0.65f, Items.BROWN_MUSHROOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.RED_MUSHROOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.MUSHROOM_STEM);
        ComposterBlock.registerCompostableItem(0.65f, Items.CRIMSON_FUNGUS);
        ComposterBlock.registerCompostableItem(0.65f, Items.WARPED_FUNGUS);
        ComposterBlock.registerCompostableItem(0.65f, Items.NETHER_WART);
        ComposterBlock.registerCompostableItem(0.65f, Items.CRIMSON_ROOTS);
        ComposterBlock.registerCompostableItem(0.65f, Items.WARPED_ROOTS);
        ComposterBlock.registerCompostableItem(0.65f, Items.SHROOMLIGHT);
        ComposterBlock.registerCompostableItem(0.65f, Items.DANDELION);
        ComposterBlock.registerCompostableItem(0.65f, Items.POPPY);
        ComposterBlock.registerCompostableItem(0.65f, Items.BLUE_ORCHID);
        ComposterBlock.registerCompostableItem(0.65f, Items.ALLIUM);
        ComposterBlock.registerCompostableItem(0.65f, Items.AZURE_BLUET);
        ComposterBlock.registerCompostableItem(0.65f, Items.RED_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.ORANGE_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.WHITE_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.PINK_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.OXEYE_DAISY);
        ComposterBlock.registerCompostableItem(0.65f, Items.CORNFLOWER);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILY_OF_THE_VALLEY);
        ComposterBlock.registerCompostableItem(0.65f, Items.WITHER_ROSE);
        ComposterBlock.registerCompostableItem(0.65f, Items.FERN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SUNFLOWER);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILAC);
        ComposterBlock.registerCompostableItem(0.65f, Items.ROSE_BUSH);
        ComposterBlock.registerCompostableItem(0.65f, Items.PEONY);
        ComposterBlock.registerCompostableItem(0.65f, Items.LARGE_FERN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SPORE_BLOSSOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.AZALEA);
        ComposterBlock.registerCompostableItem(0.65f, Items.MOSS_BLOCK);
        ComposterBlock.registerCompostableItem(0.65f, Items.BIG_DRIPLEAF);
        ComposterBlock.registerCompostableItem(0.85f, Items.HAY_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.BROWN_MUSHROOM_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.RED_MUSHROOM_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.NETHER_WART_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.WARPED_WART_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.FLOWERING_AZALEA);
        ComposterBlock.registerCompostableItem(0.85f, Items.BREAD);
        ComposterBlock.registerCompostableItem(0.85f, Items.BAKED_POTATO);
        ComposterBlock.registerCompostableItem(0.85f, Items.COOKIE);
        ComposterBlock.registerCompostableItem(0.85f, Items.TORCHFLOWER);
        ComposterBlock.registerCompostableItem(0.85f, Items.PITCHER_PLANT);
        ComposterBlock.registerCompostableItem(1.0f, Items.CAKE);
        ComposterBlock.registerCompostableItem(1.0f, Items.PUMPKIN_PIE);
    }

    private static void registerCompostableItem(float levelIncreaseChance, ItemConvertible item) {
        ITEM_TO_LEVEL_INCREASE_CHANCE.put((ItemConvertible)item.asItem(), levelIncreaseChance);
    }

    public ComposterBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 0));
    }

    public static void playEffects(World world, BlockPos pos, boolean fill) {
        BlockState lv = world.getBlockState(pos);
        world.playSoundAtBlockCenter(pos, fill ? SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS : SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        double d = lv.getOutlineShape(world, pos).getEndingCoord(Direction.Axis.Y, 0.5, 0.5) + 0.03125;
        double e = 0.13125f;
        double f = 0.7375f;
        Random lv2 = world.getRandom();
        for (int i = 0; i < 10; ++i) {
            double g = lv2.nextGaussian() * 0.02;
            double h = lv2.nextGaussian() * 0.02;
            double j = lv2.nextGaussian() * 0.02;
            world.addParticle(ParticleTypes.COMPOSTER, (double)pos.getX() + (double)0.13125f + (double)0.7375f * (double)lv2.nextFloat(), (double)pos.getY() + d + (double)lv2.nextFloat() * (1.0 - d), (double)pos.getZ() + (double)0.13125f + (double)0.7375f * (double)lv2.nextFloat(), g, h, j);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LEVEL_TO_COLLISION_SHAPE[state.get(LEVEL)];
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LEVEL_TO_COLLISION_SHAPE[0];
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(LEVEL) == 7) {
            world.scheduleBlockTick(pos, state.getBlock(), 20);
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int i = state.get(LEVEL);
        if (i < 8 && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(stack.getItem())) {
            if (i < 7 && !world.isClient) {
                BlockState lv = ComposterBlock.addToComposter(player, state, world, pos, stack);
                world.syncWorldEvent(WorldEvents.COMPOSTER_USED, pos, state != lv ? 1 : 0);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                stack.decrementUnlessCreative(1, player);
            }
            return ItemActionResult.success(world.isClient);
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        int i = state.get(LEVEL);
        if (i == 8) {
            ComposterBlock.emptyFullComposter(player, state, world, pos);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    public static BlockState compost(Entity user, BlockState state, ServerWorld world, ItemStack stack, BlockPos pos) {
        int i = state.get(LEVEL);
        if (i < 7 && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(stack.getItem())) {
            BlockState lv = ComposterBlock.addToComposter(user, state, world, pos, stack);
            stack.decrement(1);
            return lv;
        }
        return state;
    }

    public static BlockState emptyFullComposter(Entity user, BlockState state, World world, BlockPos pos) {
        if (!world.isClient) {
            Vec3d lv = Vec3d.add(pos, 0.5, 1.01, 0.5).addRandom(world.random, 0.7f);
            ItemEntity lv2 = new ItemEntity(world, lv.getX(), lv.getY(), lv.getZ(), new ItemStack(Items.BONE_MEAL));
            lv2.setToDefaultPickupDelay();
            world.spawnEntity(lv2);
        }
        BlockState lv3 = ComposterBlock.emptyComposter(user, state, world, pos);
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return lv3;
    }

    static BlockState emptyComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos) {
        BlockState lv = (BlockState)state.with(LEVEL, 0);
        world.setBlockState(pos, lv, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, lv));
        return lv;
    }

    static BlockState addToComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos, ItemStack stack) {
        int i = state.get(LEVEL);
        float f = ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat(stack.getItem());
        if (i == 0 && f > 0.0f || world.getRandom().nextDouble() < (double)f) {
            int j = i + 1;
            BlockState lv = (BlockState)state.with(LEVEL, j);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, lv));
            if (j == 7) {
                world.scheduleBlockTick(pos, state.getBlock(), 20);
            }
            return lv;
        }
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LEVEL) == 7) {
            world.setBlockState(pos, (BlockState)state.cycle(LEVEL), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        int i = state.get(LEVEL);
        if (i == 8) {
            return new FullComposterInventory(state, world, pos, new ItemStack(Items.BONE_MEAL));
        }
        if (i < 7) {
            return new ComposterInventory(state, world, pos);
        }
        return new DummyInventory();
    }

    static class FullComposterInventory
    extends SimpleInventory
    implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public FullComposterInventory(BlockState state, WorldAccess world, BlockPos pos, ItemStack outputItem) {
            super(outputItem);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.DOWN) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return !this.dirty && dir == Direction.DOWN && stack.isOf(Items.BONE_MEAL);
        }

        @Override
        public void markDirty() {
            ComposterBlock.emptyComposter(null, this.state, this.world, this.pos);
            this.dirty = true;
        }
    }

    static class ComposterInventory
    extends SimpleInventory
    implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public ComposterInventory(BlockState state, WorldAccess world, BlockPos pos) {
            super(1);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.UP) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return !this.dirty && dir == Direction.UP && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(stack.getItem());
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public void markDirty() {
            ItemStack lv = this.getStack(0);
            if (!lv.isEmpty()) {
                this.dirty = true;
                BlockState lv2 = ComposterBlock.addToComposter(null, this.state, this.world, this.pos, lv);
                this.world.syncWorldEvent(WorldEvents.COMPOSTER_USED, this.pos, lv2 != this.state ? 1 : 0);
                this.removeStack(0);
            }
        }
    }

    static class DummyInventory
    extends SimpleInventory
    implements SidedInventory {
        public DummyInventory() {
            super(0);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return new int[0];
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }
    }
}

