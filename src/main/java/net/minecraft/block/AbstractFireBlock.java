/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.dimension.NetherPortal;

public abstract class AbstractFireBlock
extends Block {
    private static final int SET_ON_FIRE_SECONDS = 8;
    private final float damage;
    protected static final float BASE_SOUND_VOLUME = 1.0f;
    protected static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public AbstractFireBlock(AbstractBlock.Settings settings, float damage) {
        super(settings);
        this.damage = damage;
    }

    protected abstract MapCodec<? extends AbstractFireBlock> getCodec();

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return AbstractFireBlock.getState(ctx.getWorld(), ctx.getBlockPos());
    }

    public static BlockState getState(BlockView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (SoulFireBlock.isSoulBase(lv2)) {
            return Blocks.SOUL_FIRE.getDefaultState();
        }
        return ((FireBlock)Blocks.FIRE).getStateForPosition(world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BASE_SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        block12: {
            double f;
            double e;
            double d;
            int i;
            block11: {
                BlockPos lv;
                BlockState lv2;
                if (random.nextInt(24) == 0) {
                    world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f, false);
                }
                if (!this.isFlammable(lv2 = world.getBlockState(lv = pos.down())) && !lv2.isSideSolidFullSquare(world, lv, Direction.UP)) break block11;
                for (int i2 = 0; i2 < 3; ++i2) {
                    double d2 = (double)pos.getX() + random.nextDouble();
                    double e2 = (double)pos.getY() + random.nextDouble() * 0.5 + 0.5;
                    double f2 = (double)pos.getZ() + random.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, d2, e2, f2, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (this.isFlammable(world.getBlockState(pos.west()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble() * (double)0.1f;
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.east()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)(pos.getX() + 1) - random.nextDouble() * (double)0.1f;
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.north()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble();
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble() * (double)0.1f;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.south()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble();
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)(pos.getZ() + 1) - random.nextDouble() * (double)0.1f;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (!this.isFlammable(world.getBlockState(pos.up()))) break block12;
            for (i = 0; i < 2; ++i) {
                d = (double)pos.getX() + random.nextDouble();
                e = (double)(pos.getY() + 1) - random.nextDouble() * (double)0.1f;
                f = (double)pos.getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    protected abstract boolean isFlammable(BlockState var1);

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.isFireImmune()) {
            entity.setFireTicks(entity.getFireTicks() + 1);
            if (entity.getFireTicks() == 0) {
                entity.setOnFireFor(8.0f);
            }
        }
        entity.damage(world.getDamageSources().inFire(), this.damage);
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        Optional<NetherPortal> optional;
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (AbstractFireBlock.isOverworldOrNether(world) && (optional = NetherPortal.getNewPortal(world, pos, Direction.Axis.X)).isPresent()) {
            optional.get().createPortal();
            return;
        }
        if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, false);
        }
    }

    private static boolean isOverworldOrNether(World world) {
        return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
    }

    @Override
    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
        }
        return super.onBreak(world, pos, state, player);
    }

    public static boolean canPlaceAt(World world, BlockPos pos, Direction direction) {
        BlockState lv = world.getBlockState(pos);
        if (!lv.isAir()) {
            return false;
        }
        return AbstractFireBlock.getState(world, pos).canPlaceAt(world, pos) || AbstractFireBlock.shouldLightPortalAt(world, pos, direction);
    }

    private static boolean shouldLightPortalAt(World world, BlockPos pos, Direction direction) {
        if (!AbstractFireBlock.isOverworldOrNether(world)) {
            return false;
        }
        BlockPos.Mutable lv = pos.mutableCopy();
        boolean bl = false;
        for (Direction lv2 : Direction.values()) {
            if (!world.getBlockState(lv.set(pos).move(lv2)).isOf(Blocks.OBSIDIAN)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        Direction.Axis lv3 = direction.getAxis().isHorizontal() ? direction.rotateYCounterclockwise().getAxis() : Direction.Type.HORIZONTAL.randomAxis(world.random);
        return NetherPortal.getNewPortal(world, pos, lv3).isPresent();
    }
}

