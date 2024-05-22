/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.class_9797;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NetherPortalBlock
extends Block
implements class_9797 {
    public static final MapCodec<NetherPortalBlock> CODEC = NetherPortalBlock.createCodec(NetherPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    private static final Logger field_52060 = LogUtils.getLogger();
    protected static final int field_31196 = 2;
    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public MapCodec<NetherPortalBlock> getCodec() {
        return CODEC;
    }

    public NetherPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.X));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(AXIS)) {
            case Z: {
                return Z_SHAPE;
            }
        }
        return X_SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
            ZombifiedPiglinEntity lv;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ZOMBIFIED_PIGLIN) && (lv = EntityType.ZOMBIFIED_PIGLIN.spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                lv.resetPortalCooldown();
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean bl;
        Direction.Axis lv = direction.getAxis();
        Direction.Axis lv2 = state.get(AXIS);
        boolean bl2 = bl = lv2 != lv && lv.isHorizontal();
        if (bl || neighborState.isOf(this) || new NetherPortal(world, pos, lv2).wasAlreadyValid()) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.canUsePortals()) {
            entity.method_60697(this, pos);
        }
    }

    @Override
    public int method_60772(ServerWorld arg, Entity arg2) {
        if (arg2 instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)arg2;
            return Math.max(1, arg.getGameRules().getInt(lv.getAbilities().invulnerable ? GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }
        return 0;
    }

    @Override
    @Nullable
    public TeleportTarget method_60770(ServerWorld arg, Entity arg2, BlockPos arg3) {
        RegistryKey<World> lv = arg.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
        ServerWorld lv2 = arg.getServer().getWorld(lv);
        boolean bl = lv2.getRegistryKey() == World.NETHER;
        WorldBorder lv3 = lv2.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(arg.getDimension(), lv2.getDimension());
        BlockPos lv4 = lv3.clamp(arg2.getX() * d, arg2.getY(), arg2.getZ() * d);
        return this.method_60773(lv2, arg2, arg3, lv4, bl, lv3);
    }

    @Nullable
    private TeleportTarget method_60773(ServerWorld arg, Entity arg2, BlockPos arg3, BlockPos arg42, boolean bl, WorldBorder arg5) {
        Optional<BlockLocating.Rectangle> optional = arg.getPortalForcer().getPortalRect(arg42, bl, arg5);
        if (optional.isEmpty()) {
            Direction.Axis lv = arg2.getWorld().getBlockState(arg3).getOrEmpty(AXIS).orElse(Direction.Axis.X);
            Optional<BlockLocating.Rectangle> optional2 = arg.getPortalForcer().createPortal(arg42, lv);
            if (optional2.isEmpty()) {
                field_52060.error("Unable to create a portal, likely target out of worldborder");
                return null;
            }
            return NetherPortalBlock.method_60777(arg2, arg3, optional2.get(), arg);
        }
        return optional.map(arg4 -> NetherPortalBlock.method_60777(arg2, arg3, arg4, arg)).orElse(null);
    }

    private static TeleportTarget method_60777(Entity arg, BlockPos arg2, BlockLocating.Rectangle arg32, ServerWorld arg4) {
        Vec3d lv4;
        Direction.Axis lv2;
        BlockState lv = arg.getWorld().getBlockState(arg2);
        if (lv.contains(Properties.HORIZONTAL_AXIS)) {
            lv2 = lv.get(Properties.HORIZONTAL_AXIS);
            BlockLocating.Rectangle lv3 = BlockLocating.getLargestRectangle(arg2, lv2, 21, Direction.Axis.Y, 21, arg3 -> arg.getWorld().getBlockState((BlockPos)arg3) == lv);
            lv4 = arg.positionInPortal(lv2, lv3);
        } else {
            lv2 = Direction.Axis.X;
            lv4 = new Vec3d(0.5, 0.0, 0.0);
        }
        return NetherPortalBlock.method_60774(arg4, arg32, lv2, lv4, arg, arg.getVelocity(), arg.getYaw(), arg.getPitch());
    }

    private static TeleportTarget method_60774(ServerWorld arg, BlockLocating.Rectangle arg2, Direction.Axis arg3, Vec3d arg4, Entity arg5, Vec3d arg6, float f, float g) {
        BlockPos lv = arg2.lowerLeft;
        BlockState lv2 = arg.getBlockState(lv);
        Direction.Axis lv3 = lv2.getOrEmpty(Properties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double d = arg2.width;
        double e = arg2.height;
        EntityDimensions lv4 = arg5.getDimensions(arg5.getPose());
        int i = arg3 == lv3 ? 0 : 90;
        Vec3d lv5 = arg3 == lv3 ? arg6 : new Vec3d(arg6.z, arg6.y, -arg6.x);
        double h = (double)lv4.width() / 2.0 + (d - (double)lv4.width()) * arg4.getX();
        double j = (e - (double)lv4.height()) * arg4.getY();
        double k = 0.5 + arg4.getZ();
        boolean bl = lv3 == Direction.Axis.X;
        Vec3d lv6 = new Vec3d((double)lv.getX() + (bl ? h : k), (double)lv.getY() + j, (double)lv.getZ() + (bl ? k : h));
        Vec3d lv7 = NetherPortal.findOpenPosition(lv6, arg, arg5, lv4);
        return new TeleportTarget(arg, lv7, lv5, f + (float)i, g);
    }

    @Override
    public class_9797.class_9798 method_60778() {
        return class_9797.class_9798.CONFUSION;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5f, random.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - 0.5) * 0.5;
            double h = ((double)random.nextFloat() - 0.5) * 0.5;
            double j = ((double)random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (world.getBlockState(pos.west()).isOf(this) || world.getBlockState(pos.east()).isOf(this)) {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
                j = random.nextFloat() * 2.0f * (float)k;
            } else {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
                g = random.nextFloat() * 2.0f * (float)k;
            }
            world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                switch (state.get(AXIS)) {
                    case X: {
                        return (BlockState)state.with(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return (BlockState)state.with(AXIS, Direction.Axis.X);
                    }
                }
                return state;
            }
        }
        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}

