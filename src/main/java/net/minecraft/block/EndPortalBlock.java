/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.class_9797;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class EndPortalBlock
extends BlockWithEntity
implements class_9797 {
    public static final MapCodec<EndPortalBlock> CODEC = EndPortalBlock.createCodec(EndPortalBlock::new);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

    public MapCodec<EndPortalBlock> getCodec() {
        return CODEC;
    }

    protected EndPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndPortalBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.canUsePortals()) return;
        if (!VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) return;
        if (!world.isClient && world.getRegistryKey() == World.END && entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            if (!lv.seenCredits) {
                lv.detachForDimensionChange();
                return;
            }
        }
        entity.method_60697(this, pos);
    }

    @Override
    public TeleportTarget method_60770(ServerWorld arg, Entity arg2, BlockPos arg3) {
        RegistryKey<World> lv = arg.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
        ServerWorld lv2 = arg.getServer().getWorld(lv);
        boolean bl = lv == World.END;
        BlockPos lv3 = bl ? ServerWorld.END_SPAWN_POS : lv2.getSpawnPos();
        Vec3d lv4 = new Vec3d((double)lv3.getX() + 0.5, lv3.getY(), (double)lv3.getZ() + 0.5);
        if (bl) {
            this.method_60771(lv2, BlockPos.ofFloored(lv4).down());
        } else {
            if (arg2 instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv5 = (ServerPlayerEntity)arg2;
                return lv5.getRespawnTarget(false);
            }
            int i = lv2.getWorldChunk(lv3).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv3.getX(), lv3.getZ()) + 1;
            lv4 = new Vec3d(lv4.x, i, lv4.z);
        }
        return new TeleportTarget(lv2, lv4, arg2.getVelocity(), arg2.getYaw(), arg2.getPitch());
    }

    private void method_60771(ServerWorld arg, BlockPos arg2) {
        BlockPos.Mutable lv = arg2.mutableCopy();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    BlockState lv2 = k == -1 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState();
                    arg.setBlockState(lv.set(arg2).move(j, k, i), lv2);
                }
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() + 0.8;
        double f = (double)pos.getZ() + random.nextDouble();
        world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }
}

