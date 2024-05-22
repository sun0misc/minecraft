/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class HoneyBlock
extends TranslucentBlock {
    public static final MapCodec<HoneyBlock> CODEC = HoneyBlock.createCodec(HoneyBlock::new);
    private static final double field_31101 = 0.13;
    private static final double field_31102 = 0.08;
    private static final double field_31103 = 0.05;
    private static final int TICKS_PER_SECOND = 20;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

    public MapCodec<HoneyBlock> getCodec() {
        return CODEC;
    }

    public HoneyBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    private static boolean hasHoneyBlockEffects(Entity entity) {
        return entity instanceof LivingEntity || entity instanceof AbstractMinecartEntity || entity instanceof TntEntity || entity instanceof BoatEntity;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.playSound(SoundEvents.BLOCK_HONEY_BLOCK_SLIDE, 1.0f, 1.0f);
        if (!world.isClient) {
            world.sendEntityStatus(entity, EntityStatuses.DRIP_RICH_HONEY);
        }
        if (entity.handleFallDamage(fallDistance, 0.2f, world.getDamageSources().fall())) {
            entity.playSound(this.soundGroup.getFallSound(), this.soundGroup.getVolume() * 0.5f, this.soundGroup.getPitch() * 0.75f);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.isSliding(pos, entity)) {
            this.triggerAdvancement(entity, pos);
            this.updateSlidingVelocity(entity);
            this.addCollisionEffects(world, entity);
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    private boolean isSliding(BlockPos pos, Entity entity) {
        if (entity.isOnGround()) {
            return false;
        }
        if (entity.getY() > (double)pos.getY() + 0.9375 - 1.0E-7) {
            return false;
        }
        if (entity.getVelocity().y >= -0.08) {
            return false;
        }
        double d = Math.abs((double)pos.getX() + 0.5 - entity.getX());
        double e = Math.abs((double)pos.getZ() + 0.5 - entity.getZ());
        double f = 0.4375 + (double)(entity.getWidth() / 2.0f);
        return d + 1.0E-7 > f || e + 1.0E-7 > f;
    }

    private void triggerAdvancement(Entity entity, BlockPos pos) {
        if (entity instanceof ServerPlayerEntity && entity.getWorld().getTime() % 20L == 0L) {
            Criteria.SLIDE_DOWN_BLOCK.trigger((ServerPlayerEntity)entity, entity.getWorld().getBlockState(pos));
        }
    }

    private void updateSlidingVelocity(Entity entity) {
        Vec3d lv = entity.getVelocity();
        if (lv.y < -0.13) {
            double d = -0.05 / lv.y;
            entity.setVelocity(new Vec3d(lv.x * d, -0.05, lv.z * d));
        } else {
            entity.setVelocity(new Vec3d(lv.x, -0.05, lv.z));
        }
        entity.onLanding();
    }

    private void addCollisionEffects(World world, Entity entity) {
        if (HoneyBlock.hasHoneyBlockEffects(entity)) {
            if (world.random.nextInt(5) == 0) {
                entity.playSound(SoundEvents.BLOCK_HONEY_BLOCK_SLIDE, 1.0f, 1.0f);
            }
            if (!world.isClient && world.random.nextInt(5) == 0) {
                world.sendEntityStatus(entity, EntityStatuses.DRIP_HONEY);
            }
        }
    }

    public static void addRegularParticles(Entity entity) {
        HoneyBlock.addParticles(entity, 5);
    }

    public static void addRichParticles(Entity entity) {
        HoneyBlock.addParticles(entity, 10);
    }

    private static void addParticles(Entity entity, int count) {
        if (!entity.getWorld().isClient) {
            return;
        }
        BlockState lv = Blocks.HONEY_BLOCK.getDefaultState();
        for (int j = 0; j < count; ++j) {
            entity.getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, lv), entity.getX(), entity.getY(), entity.getZ(), 0.0, 0.0, 0.0);
        }
    }
}

