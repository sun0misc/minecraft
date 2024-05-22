/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TntBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class FireBlock
extends AbstractFireBlock {
    public static final MapCodec<FireBlock> CODEC = FireBlock.createCodec(FireBlock::new);
    public static final int field_31093 = 15;
    public static final IntProperty AGE = Properties.AGE_15;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    public static final BooleanProperty UP = ConnectingBlock.UP;
    private static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private final Map<BlockState, VoxelShape> shapesByState;
    private static final int field_31085 = 60;
    private static final int field_31086 = 30;
    private static final int field_31087 = 15;
    private static final int field_31088 = 5;
    private static final int field_31089 = 100;
    private static final int field_31090 = 60;
    private static final int field_31091 = 20;
    private static final int field_31092 = 5;
    private final Object2IntMap<Block> burnChances = new Object2IntOpenHashMap<Block>();
    private final Object2IntMap<Block> spreadChances = new Object2IntOpenHashMap<Block>();

    public MapCodec<FireBlock> getCodec() {
        return CODEC;
    }

    public FireBlock(AbstractBlock.Settings arg) {
        super(arg, 1.0f);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(UP, false));
        this.shapesByState = ImmutableMap.copyOf(this.stateManager.getStates().stream().filter(state -> state.get(AGE) == 0).collect(Collectors.toMap(Function.identity(), FireBlock::getShapeForState)));
    }

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape lv = VoxelShapes.empty();
        if (state.get(UP).booleanValue()) {
            lv = UP_SHAPE;
        }
        if (state.get(NORTH).booleanValue()) {
            lv = VoxelShapes.union(lv, NORTH_SHAPE);
        }
        if (state.get(SOUTH).booleanValue()) {
            lv = VoxelShapes.union(lv, SOUTH_SHAPE);
        }
        if (state.get(EAST).booleanValue()) {
            lv = VoxelShapes.union(lv, EAST_SHAPE);
        }
        if (state.get(WEST).booleanValue()) {
            lv = VoxelShapes.union(lv, WEST_SHAPE);
        }
        return lv.isEmpty() ? BASE_SHAPE : lv;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (this.canPlaceAt(state, world, pos)) {
            return this.getStateWithAge(world, pos, state.get(AGE));
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapesByState.get(state.with(AGE, 0));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getStateForPosition(ctx.getWorld(), ctx.getBlockPos());
    }

    protected BlockState getStateForPosition(BlockView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (this.isFlammable(lv2) || lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
            return this.getDefaultState();
        }
        BlockState lv3 = this.getDefaultState();
        for (Direction lv4 : Direction.values()) {
            BooleanProperty lv5 = DIRECTION_PROPERTIES.get(lv4);
            if (lv5 == null) continue;
            lv3 = (BlockState)lv3.with(lv5, this.isFlammable(world.getBlockState(pos.offset(lv4))));
        }
        return lv3;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.down();
        return world.getBlockState(lv).isSideSolidFullSquare(world, lv, Direction.UP) || this.areBlocksAroundFlammable(world, pos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl2;
        world.scheduleBlockTick(pos, this, FireBlock.getFireTickDelay(world.random));
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }
        if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, false);
        }
        BlockState lv = world.getBlockState(pos.down());
        boolean bl = lv.isIn(world.getDimension().infiniburn());
        int i = state.get(AGE);
        if (!bl && world.isRaining() && this.isRainingAround(world, pos) && random.nextFloat() < 0.2f + (float)i * 0.03f) {
            world.removeBlock(pos, false);
            return;
        }
        int j = Math.min(15, i + random.nextInt(3) / 2);
        if (i != j) {
            state = (BlockState)state.with(AGE, j);
            world.setBlockState(pos, state, Block.NO_REDRAW);
        }
        if (!bl) {
            if (!this.areBlocksAroundFlammable(world, pos)) {
                BlockPos lv2 = pos.down();
                if (!world.getBlockState(lv2).isSideSolidFullSquare(world, lv2, Direction.UP) || i > 3) {
                    world.removeBlock(pos, false);
                }
                return;
            }
            if (i == 15 && random.nextInt(4) == 0 && !this.isFlammable(world.getBlockState(pos.down()))) {
                world.removeBlock(pos, false);
                return;
            }
        }
        int k = (bl2 = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT)) ? -50 : 0;
        this.trySpreadingFire(world, pos.east(), 300 + k, random, i);
        this.trySpreadingFire(world, pos.west(), 300 + k, random, i);
        this.trySpreadingFire(world, pos.down(), 250 + k, random, i);
        this.trySpreadingFire(world, pos.up(), 250 + k, random, i);
        this.trySpreadingFire(world, pos.north(), 300 + k, random, i);
        this.trySpreadingFire(world, pos.south(), 300 + k, random, i);
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int l = -1; l <= 1; ++l) {
            for (int m = -1; m <= 1; ++m) {
                for (int n = -1; n <= 4; ++n) {
                    if (l == 0 && n == 0 && m == 0) continue;
                    int o = 100;
                    if (n > 1) {
                        o += (n - 1) * 100;
                    }
                    lv3.set(pos, l, n, m);
                    int p = this.getBurnChance(world, lv3);
                    if (p <= 0) continue;
                    int q = (p + 40 + world.getDifficulty().getId() * 7) / (i + 30);
                    if (bl2) {
                        q /= 2;
                    }
                    if (q <= 0 || random.nextInt(o) > q || world.isRaining() && this.isRainingAround(world, lv3)) continue;
                    int r = Math.min(15, i + random.nextInt(5) / 4);
                    world.setBlockState(lv3, this.getStateWithAge(world, lv3, r), Block.NOTIFY_ALL);
                }
            }
        }
    }

    protected boolean isRainingAround(World world, BlockPos pos) {
        return world.hasRain(pos) || world.hasRain(pos.west()) || world.hasRain(pos.east()) || world.hasRain(pos.north()) || world.hasRain(pos.south());
    }

    private int getSpreadChance(BlockState state) {
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED).booleanValue()) {
            return 0;
        }
        return this.spreadChances.getInt(state.getBlock());
    }

    private int getBurnChance(BlockState state) {
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED).booleanValue()) {
            return 0;
        }
        return this.burnChances.getInt(state.getBlock());
    }

    private void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge) {
        int k = this.getSpreadChance(world.getBlockState(pos));
        if (random.nextInt(spreadFactor) < k) {
            BlockState lv = world.getBlockState(pos);
            if (random.nextInt(currentAge + 10) < 5 && !world.hasRain(pos)) {
                int l = Math.min(currentAge + random.nextInt(5) / 4, 15);
                world.setBlockState(pos, this.getStateWithAge(world, pos, l), Block.NOTIFY_ALL);
            } else {
                world.removeBlock(pos, false);
            }
            Block lv2 = lv.getBlock();
            if (lv2 instanceof TntBlock) {
                TntBlock.primeTnt(world, pos);
            }
        }
    }

    private BlockState getStateWithAge(WorldAccess world, BlockPos pos, int age) {
        BlockState lv = FireBlock.getState(world, pos);
        if (lv.isOf(Blocks.FIRE)) {
            return (BlockState)lv.with(AGE, age);
        }
        return lv;
    }

    private boolean areBlocksAroundFlammable(BlockView world, BlockPos pos) {
        for (Direction lv : Direction.values()) {
            if (!this.isFlammable(world.getBlockState(pos.offset(lv)))) continue;
            return true;
        }
        return false;
    }

    private int getBurnChance(WorldView world, BlockPos pos) {
        if (!world.isAir(pos)) {
            return 0;
        }
        int i = 0;
        for (Direction lv : Direction.values()) {
            BlockState lv2 = world.getBlockState(pos.offset(lv));
            i = Math.max(this.getBurnChance(lv2), i);
        }
        return i;
    }

    @Override
    protected boolean isFlammable(BlockState state) {
        return this.getBurnChance(state) > 0;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, FireBlock.getFireTickDelay(world.random));
    }

    private static int getFireTickDelay(Random random) {
        return 30 + random.nextInt(10);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    public void registerFlammableBlock(Block block, int burnChance, int spreadChance) {
        this.burnChances.put(block, burnChance);
        this.spreadChances.put(block, spreadChance);
    }

    public static void registerDefaultFlammables() {
        FireBlock lv = (FireBlock)Blocks.FIRE;
        lv.registerFlammableBlock(Blocks.OAK_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.SPRUCE_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.BIRCH_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.JUNGLE_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.ACACIA_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.CHERRY_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.DARK_OAK_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.MANGROVE_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_PLANKS, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_MOSAIC, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.SPRUCE_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.BIRCH_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.JUNGLE_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.ACACIA_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.CHERRY_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.DARK_OAK_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.MANGROVE_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_MOSAIC_SLAB, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.BIRCH_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.ACACIA_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.CHERRY_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.MANGROVE_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_FENCE_GATE, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.SPRUCE_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.BIRCH_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.JUNGLE_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.ACACIA_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.CHERRY_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.DARK_OAK_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.MANGROVE_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_FENCE, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.BIRCH_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.SPRUCE_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.JUNGLE_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.ACACIA_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.CHERRY_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.DARK_OAK_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.MANGROVE_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.BAMBOO_MOSAIC_STAIRS, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.SPRUCE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.BIRCH_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.JUNGLE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.ACACIA_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.CHERRY_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.DARK_OAK_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.MANGROVE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.BAMBOO_BLOCK, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_OAK_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_CHERRY_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_MANGROVE_LOG, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_BAMBOO_BLOCK, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_OAK_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_CHERRY_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.STRIPPED_MANGROVE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.OAK_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.SPRUCE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.BIRCH_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.JUNGLE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.ACACIA_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.CHERRY_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.DARK_OAK_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.MANGROVE_WOOD, 5, 5);
        lv.registerFlammableBlock(Blocks.MANGROVE_ROOTS, 5, 20);
        lv.registerFlammableBlock(Blocks.OAK_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.SPRUCE_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.BIRCH_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.JUNGLE_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.ACACIA_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.CHERRY_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.DARK_OAK_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.MANGROVE_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.BOOKSHELF, 30, 20);
        lv.registerFlammableBlock(Blocks.TNT, 15, 100);
        lv.registerFlammableBlock(Blocks.SHORT_GRASS, 60, 100);
        lv.registerFlammableBlock(Blocks.FERN, 60, 100);
        lv.registerFlammableBlock(Blocks.DEAD_BUSH, 60, 100);
        lv.registerFlammableBlock(Blocks.SUNFLOWER, 60, 100);
        lv.registerFlammableBlock(Blocks.LILAC, 60, 100);
        lv.registerFlammableBlock(Blocks.ROSE_BUSH, 60, 100);
        lv.registerFlammableBlock(Blocks.PEONY, 60, 100);
        lv.registerFlammableBlock(Blocks.TALL_GRASS, 60, 100);
        lv.registerFlammableBlock(Blocks.LARGE_FERN, 60, 100);
        lv.registerFlammableBlock(Blocks.DANDELION, 60, 100);
        lv.registerFlammableBlock(Blocks.POPPY, 60, 100);
        lv.registerFlammableBlock(Blocks.BLUE_ORCHID, 60, 100);
        lv.registerFlammableBlock(Blocks.ALLIUM, 60, 100);
        lv.registerFlammableBlock(Blocks.AZURE_BLUET, 60, 100);
        lv.registerFlammableBlock(Blocks.RED_TULIP, 60, 100);
        lv.registerFlammableBlock(Blocks.ORANGE_TULIP, 60, 100);
        lv.registerFlammableBlock(Blocks.WHITE_TULIP, 60, 100);
        lv.registerFlammableBlock(Blocks.PINK_TULIP, 60, 100);
        lv.registerFlammableBlock(Blocks.OXEYE_DAISY, 60, 100);
        lv.registerFlammableBlock(Blocks.CORNFLOWER, 60, 100);
        lv.registerFlammableBlock(Blocks.LILY_OF_THE_VALLEY, 60, 100);
        lv.registerFlammableBlock(Blocks.TORCHFLOWER, 60, 100);
        lv.registerFlammableBlock(Blocks.PITCHER_PLANT, 60, 100);
        lv.registerFlammableBlock(Blocks.WITHER_ROSE, 60, 100);
        lv.registerFlammableBlock(Blocks.PINK_PETALS, 60, 100);
        lv.registerFlammableBlock(Blocks.WHITE_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.ORANGE_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.MAGENTA_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.LIGHT_BLUE_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.YELLOW_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.LIME_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.PINK_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.GRAY_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.LIGHT_GRAY_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.CYAN_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.PURPLE_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.BLUE_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.BROWN_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.GREEN_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.RED_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.BLACK_WOOL, 30, 60);
        lv.registerFlammableBlock(Blocks.VINE, 15, 100);
        lv.registerFlammableBlock(Blocks.COAL_BLOCK, 5, 5);
        lv.registerFlammableBlock(Blocks.HAY_BLOCK, 60, 20);
        lv.registerFlammableBlock(Blocks.TARGET, 15, 20);
        lv.registerFlammableBlock(Blocks.WHITE_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.ORANGE_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.MAGENTA_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.LIGHT_BLUE_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.YELLOW_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.LIME_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.PINK_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.GRAY_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.LIGHT_GRAY_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.CYAN_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.PURPLE_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.BLUE_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.BROWN_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.GREEN_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.RED_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.BLACK_CARPET, 60, 20);
        lv.registerFlammableBlock(Blocks.DRIED_KELP_BLOCK, 30, 60);
        lv.registerFlammableBlock(Blocks.BAMBOO, 60, 60);
        lv.registerFlammableBlock(Blocks.SCAFFOLDING, 60, 60);
        lv.registerFlammableBlock(Blocks.LECTERN, 30, 20);
        lv.registerFlammableBlock(Blocks.COMPOSTER, 5, 20);
        lv.registerFlammableBlock(Blocks.SWEET_BERRY_BUSH, 60, 100);
        lv.registerFlammableBlock(Blocks.BEEHIVE, 5, 20);
        lv.registerFlammableBlock(Blocks.BEE_NEST, 30, 20);
        lv.registerFlammableBlock(Blocks.AZALEA_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
        lv.registerFlammableBlock(Blocks.CAVE_VINES, 15, 60);
        lv.registerFlammableBlock(Blocks.CAVE_VINES_PLANT, 15, 60);
        lv.registerFlammableBlock(Blocks.SPORE_BLOSSOM, 60, 100);
        lv.registerFlammableBlock(Blocks.AZALEA, 30, 60);
        lv.registerFlammableBlock(Blocks.FLOWERING_AZALEA, 30, 60);
        lv.registerFlammableBlock(Blocks.BIG_DRIPLEAF, 60, 100);
        lv.registerFlammableBlock(Blocks.BIG_DRIPLEAF_STEM, 60, 100);
        lv.registerFlammableBlock(Blocks.SMALL_DRIPLEAF, 60, 100);
        lv.registerFlammableBlock(Blocks.HANGING_ROOTS, 30, 60);
        lv.registerFlammableBlock(Blocks.GLOW_LICHEN, 15, 100);
    }
}

