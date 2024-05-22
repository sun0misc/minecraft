/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class TargetBlock
extends Block {
    public static final MapCodec<TargetBlock> CODEC = TargetBlock.createCodec(TargetBlock::new);
    private static final IntProperty POWER = Properties.POWER;
    private static final int RECOVERABLE_POWER_DELAY = 20;
    private static final int REGULAR_POWER_DELAY = 8;

    public MapCodec<TargetBlock> getCodec() {
        return CODEC;
    }

    public TargetBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWER, 0));
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        int i = TargetBlock.trigger(world, state, hit, projectile);
        Entity lv = projectile.getOwner();
        if (lv instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
            lv2.incrementStat(Stats.TARGET_HIT);
            Criteria.TARGET_HIT.trigger(lv2, projectile, hit.getPos(), i);
        }
    }

    private static int trigger(WorldAccess world, BlockState state, BlockHitResult hitResult, Entity entity) {
        int j;
        int i = TargetBlock.calculatePower(hitResult, hitResult.getPos());
        int n = j = entity instanceof PersistentProjectileEntity ? 20 : 8;
        if (!world.getBlockTickScheduler().isQueued(hitResult.getBlockPos(), state.getBlock())) {
            TargetBlock.setPower(world, state, i, hitResult.getBlockPos(), j);
        }
        return i;
    }

    private static int calculatePower(BlockHitResult hitResult, Vec3d pos) {
        Direction lv = hitResult.getSide();
        double d = Math.abs(MathHelper.fractionalPart(pos.x) - 0.5);
        double e = Math.abs(MathHelper.fractionalPart(pos.y) - 0.5);
        double f = Math.abs(MathHelper.fractionalPart(pos.z) - 0.5);
        Direction.Axis lv2 = lv.getAxis();
        double g = lv2 == Direction.Axis.Y ? Math.max(d, f) : (lv2 == Direction.Axis.Z ? Math.max(d, e) : Math.max(e, f));
        return Math.max(1, MathHelper.ceil(15.0 * MathHelper.clamp((0.5 - g) / 0.5, 0.0, 1.0)));
    }

    private static void setPower(WorldAccess world, BlockState state, int power, BlockPos pos, int delay) {
        world.setBlockState(pos, (BlockState)state.with(POWER, power), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, state.getBlock(), delay);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWER) != 0) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.isClient() || state.isOf(oldState.getBlock())) {
            return;
        }
        if (state.get(POWER) > 0 && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        }
    }
}

