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
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CampfireBlock
extends BlockWithEntity
implements Waterloggable {
    public static final MapCodec<CampfireBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("spawn_particles")).forGetter(block -> block.emitsParticles), ((MapCodec)Codec.intRange(0, 1000).fieldOf("fire_damage")).forGetter(block -> block.fireDamage), CampfireBlock.createSettingsCodec()).apply((Applicative<CampfireBlock, ?>)instance, CampfireBlock::new));
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty SIGNAL_FIRE = Properties.SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SMOKEY_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final int field_31049 = 5;
    private final boolean emitsParticles;
    private final int fireDamage;

    public MapCodec<CampfireBlock> getCodec() {
        return CODEC;
    }

    public CampfireBlock(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
        super(settings);
        this.emitsParticles = emitsParticles;
        this.fireDamage = fireDamage;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack lv3;
        CampfireBlockEntity lv2;
        Optional<RecipeEntry<CampfireCookingRecipe>> optional;
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof CampfireBlockEntity && (optional = (lv2 = (CampfireBlockEntity)lv).getRecipeFor(lv3 = player.getStackInHand(hand))).isPresent()) {
            if (!world.isClient && lv2.addItem(player, lv3, optional.get().value().getCookingTime())) {
                player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
                return ItemActionResult.SUCCESS;
            }
            return ItemActionResult.CONSUME;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (state.get(LIT).booleanValue() && entity instanceof LivingEntity) {
            entity.damage(world.getDamageSources().campfire(), this.fireDamage);
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof CampfireBlockEntity) {
            ItemScatterer.spawn(world, pos, ((CampfireBlockEntity)lv).getItemsBeingCooked());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv2;
        World lv = ctx.getWorld();
        boolean bl = lv.getFluidState(lv2 = ctx.getBlockPos()).getFluid() == Fluids.WATER;
        return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, bl)).with(SIGNAL_FIRE, this.isSignalFireBaseBlock(lv.getBlockState(lv2.down())))).with(LIT, !bl)).with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == Direction.DOWN) {
            return (BlockState)state.with(SIGNAL_FIRE, this.isSignalFireBaseBlock(neighborState));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    private boolean isSignalFireBaseBlock(BlockState state) {
        return state.isOf(Blocks.HAY_BLOCK);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(LIT).booleanValue()) {
            return;
        }
        if (random.nextInt(10) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
        if (this.emitsParticles && random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, random.nextFloat() / 2.0f, 5.0E-5, random.nextFloat() / 2.0f);
            }
        }
    }

    public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity lv;
        if (world.isClient()) {
            for (int i = 0; i < 20; ++i) {
                CampfireBlock.spawnSmokeParticle((World)world, pos, state.get(SIGNAL_FIRE), true);
            }
        }
        if ((lv = world.getBlockEntity(pos)) instanceof CampfireBlockEntity) {
            ((CampfireBlockEntity)lv).spawnItemsBeingCooked();
        }
        world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(Properties.WATERLOGGED).booleanValue() && fluidState.getFluid() == Fluids.WATER) {
            boolean bl = state.get(LIT);
            if (bl) {
                if (!world.isClient()) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                CampfireBlock.extinguish(null, world, pos, state);
            }
            world.setBlockState(pos, (BlockState)((BlockState)state.with(WATERLOGGED, true)).with(LIT, false), Block.NOTIFY_ALL);
            world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        }
        return false;
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos lv = hit.getBlockPos();
        if (!world.isClient && projectile.isOnFire() && projectile.canModifyAt(world, lv) && !state.get(LIT).booleanValue() && !state.get(WATERLOGGED).booleanValue()) {
            world.setBlockState(lv, (BlockState)state.with(Properties.LIT, true), Block.NOTIFY_ALL_AND_REDRAW);
        }
    }

    public static void spawnSmokeParticle(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke) {
        Random lv = world.getRandom();
        SimpleParticleType lv2 = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        world.addImportantParticle(lv2, true, (double)pos.getX() + 0.5 + lv.nextDouble() / 3.0 * (double)(lv.nextBoolean() ? 1 : -1), (double)pos.getY() + lv.nextDouble() + lv.nextDouble(), (double)pos.getZ() + 0.5 + lv.nextDouble() / 3.0 * (double)(lv.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
        if (lotsOfSmoke) {
            world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.5 + lv.nextDouble() / 4.0 * (double)(lv.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4, (double)pos.getZ() + 0.5 + lv.nextDouble() / 4.0 * (double)(lv.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
        }
    }

    public static boolean isLitCampfireInRange(World world, BlockPos pos) {
        for (int i = 1; i <= 5; ++i) {
            BlockPos lv = pos.down(i);
            BlockState lv2 = world.getBlockState(lv);
            if (CampfireBlock.isLitCampfire(lv2)) {
                return true;
            }
            boolean bl = VoxelShapes.matchesAnywhere(SMOKEY_SHAPE, lv2.getCollisionShape(world, pos, ShapeContext.absent()), BooleanBiFunction.AND);
            if (!bl) continue;
            BlockState lv3 = world.getBlockState(lv.down());
            return CampfireBlock.isLitCampfire(lv3);
        }
        return false;
    }

    public static boolean isLitCampfire(BlockState state) {
        return state.contains(LIT) && state.isIn(BlockTags.CAMPFIRES) && state.get(LIT) != false;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
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
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CampfireBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            if (state.get(LIT).booleanValue()) {
                return CampfireBlock.validateTicker(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::clientTick);
            }
        } else {
            if (state.get(LIT).booleanValue()) {
                return CampfireBlock.validateTicker(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::litServerTick);
            }
            return CampfireBlock.validateTicker(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::unlitServerTick);
        }
        return null;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(BlockTags.CAMPFIRES, statex -> statex.contains(WATERLOGGED) && statex.contains(LIT)) && state.get(WATERLOGGED) == false && state.get(LIT) == false;
    }
}

