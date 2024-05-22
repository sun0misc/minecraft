/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ConduitBlockEntity
extends BlockEntity {
    private static final int field_31333 = 2;
    private static final int field_31334 = 13;
    private static final float field_31335 = -0.0375f;
    private static final int field_31336 = 16;
    private static final int MIN_BLOCKS_TO_ACTIVATE = 42;
    private static final int field_31338 = 8;
    private static final Block[] ACTIVATING_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int ticks;
    private float ticksActive;
    private boolean active;
    private boolean eyeOpen;
    private final List<BlockPos> activatingBlocks = Lists.newArrayList();
    @Nullable
    private LivingEntity targetEntity;
    @Nullable
    private UUID targetUuid;
    private long nextAmbientSoundTime;

    public ConduitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CONDUIT, pos, state);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.targetUuid = nbt.containsUuid("Target") ? nbt.getUuid("Target") : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (this.targetEntity != null) {
            nbt.putUuid("Target", this.targetEntity.getUuid());
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createComponentlessNbt(registryLookup);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ConduitBlockEntity blockEntity) {
        ++blockEntity.ticks;
        long l = world.getTime();
        List<BlockPos> list = blockEntity.activatingBlocks;
        if (l % 40L == 0L) {
            blockEntity.active = ConduitBlockEntity.updateActivatingBlocks(world, pos, list);
            ConduitBlockEntity.openEye(blockEntity, list);
        }
        ConduitBlockEntity.updateTargetEntity(world, pos, blockEntity);
        ConduitBlockEntity.spawnNautilusParticles(world, pos, list, blockEntity.targetEntity, blockEntity.ticks);
        if (blockEntity.isActive()) {
            blockEntity.ticksActive += 1.0f;
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ConduitBlockEntity blockEntity) {
        ++blockEntity.ticks;
        long l = world.getTime();
        List<BlockPos> list = blockEntity.activatingBlocks;
        if (l % 40L == 0L) {
            boolean bl = ConduitBlockEntity.updateActivatingBlocks(world, pos, list);
            if (bl != blockEntity.active) {
                SoundEvent lv = bl ? SoundEvents.BLOCK_CONDUIT_ACTIVATE : SoundEvents.BLOCK_CONDUIT_DEACTIVATE;
                world.playSound(null, pos, lv, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            blockEntity.active = bl;
            ConduitBlockEntity.openEye(blockEntity, list);
            if (bl) {
                ConduitBlockEntity.givePlayersEffects(world, pos, list);
                ConduitBlockEntity.attackHostileEntity(world, pos, state, list, blockEntity);
            }
        }
        if (blockEntity.isActive()) {
            if (l % 80L == 0L) {
                world.playSound(null, pos, SoundEvents.BLOCK_CONDUIT_AMBIENT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            if (l > blockEntity.nextAmbientSoundTime) {
                blockEntity.nextAmbientSoundTime = l + 60L + (long)world.getRandom().nextInt(40);
                world.playSound(null, pos, SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private static void openEye(ConduitBlockEntity blockEntity, List<BlockPos> activatingBlocks) {
        blockEntity.setEyeOpen(activatingBlocks.size() >= 42);
    }

    private static boolean updateActivatingBlocks(World world, BlockPos pos, List<BlockPos> activatingBlocks) {
        int k;
        int j;
        int i;
        activatingBlocks.clear();
        for (i = -1; i <= 1; ++i) {
            for (j = -1; j <= 1; ++j) {
                for (k = -1; k <= 1; ++k) {
                    BlockPos lv = pos.add(i, j, k);
                    if (world.isWater(lv)) continue;
                    return false;
                }
            }
        }
        for (i = -2; i <= 2; ++i) {
            for (j = -2; j <= 2; ++j) {
                for (k = -2; k <= 2; ++k) {
                    int l = Math.abs(i);
                    int m = Math.abs(j);
                    int n = Math.abs(k);
                    if (l <= 1 && m <= 1 && n <= 1 || (i != 0 || m != 2 && n != 2) && (j != 0 || l != 2 && n != 2) && (k != 0 || l != 2 && m != 2)) continue;
                    BlockPos lv2 = pos.add(i, j, k);
                    BlockState lv3 = world.getBlockState(lv2);
                    for (Block lv4 : ACTIVATING_BLOCKS) {
                        if (!lv3.isOf(lv4)) continue;
                        activatingBlocks.add(lv2);
                    }
                }
            }
        }
        return activatingBlocks.size() >= 16;
    }

    private static void givePlayersEffects(World world, BlockPos pos, List<BlockPos> activatingBlocks) {
        int m;
        int l;
        int i = activatingBlocks.size();
        int j = i / 7 * 16;
        int k = pos.getX();
        Box lv = new Box(k, l = pos.getY(), m = pos.getZ(), k + 1, l + 1, m + 1).expand(j).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list2 = world.getNonSpectatingEntities(PlayerEntity.class, lv);
        if (list2.isEmpty()) {
            return;
        }
        for (PlayerEntity lv2 : list2) {
            if (!pos.isWithinDistance(lv2.getBlockPos(), (double)j) || !lv2.isTouchingWaterOrRain()) continue;
            lv2.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 260, 0, true, true));
        }
    }

    private static void attackHostileEntity(World world, BlockPos pos, BlockState state, List<BlockPos> activatingBlocks, ConduitBlockEntity blockEntity) {
        LivingEntity lv = blockEntity.targetEntity;
        int i = activatingBlocks.size();
        if (i < 42) {
            blockEntity.targetEntity = null;
        } else if (blockEntity.targetEntity == null && blockEntity.targetUuid != null) {
            blockEntity.targetEntity = ConduitBlockEntity.findTargetEntity(world, pos, blockEntity.targetUuid);
            blockEntity.targetUuid = null;
        } else if (blockEntity.targetEntity == null) {
            List<LivingEntity> list2 = world.getEntitiesByClass(LivingEntity.class, ConduitBlockEntity.getAttackZone(pos), entity -> entity instanceof Monster && entity.isTouchingWaterOrRain());
            if (!list2.isEmpty()) {
                blockEntity.targetEntity = list2.get(world.random.nextInt(list2.size()));
            }
        } else if (!blockEntity.targetEntity.isAlive() || !pos.isWithinDistance(blockEntity.targetEntity.getBlockPos(), 8.0)) {
            blockEntity.targetEntity = null;
        }
        if (blockEntity.targetEntity != null) {
            world.playSound(null, blockEntity.targetEntity.getX(), blockEntity.targetEntity.getY(), blockEntity.targetEntity.getZ(), SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0f, 1.0f);
            blockEntity.targetEntity.damage(world.getDamageSources().magic(), 4.0f);
        }
        if (lv != blockEntity.targetEntity) {
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    private static void updateTargetEntity(World world, BlockPos pos, ConduitBlockEntity blockEntity) {
        if (blockEntity.targetUuid == null) {
            blockEntity.targetEntity = null;
        } else if (blockEntity.targetEntity == null || !blockEntity.targetEntity.getUuid().equals(blockEntity.targetUuid)) {
            blockEntity.targetEntity = ConduitBlockEntity.findTargetEntity(world, pos, blockEntity.targetUuid);
            if (blockEntity.targetEntity == null) {
                blockEntity.targetUuid = null;
            }
        }
    }

    private static Box getAttackZone(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        return new Box(i, j, k, i + 1, j + 1, k + 1).expand(8.0);
    }

    @Nullable
    private static LivingEntity findTargetEntity(World world, BlockPos pos, UUID uuid) {
        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, ConduitBlockEntity.getAttackZone(pos), entity -> entity.getUuid().equals(uuid));
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    private static void spawnNautilusParticles(World world, BlockPos pos, List<BlockPos> activatingBlocks, @Nullable Entity entity, int ticks) {
        float f;
        Random lv = world.random;
        double d = MathHelper.sin((float)(ticks + 35) * 0.1f) / 2.0f + 0.5f;
        d = (d * d + d) * (double)0.3f;
        Vec3d lv2 = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.5 + d, (double)pos.getZ() + 0.5);
        for (BlockPos lv3 : activatingBlocks) {
            if (lv.nextInt(50) != 0) continue;
            BlockPos lv4 = lv3.subtract(pos);
            f = -0.5f + lv.nextFloat() + (float)lv4.getX();
            float g = -2.0f + lv.nextFloat() + (float)lv4.getY();
            float h = -0.5f + lv.nextFloat() + (float)lv4.getZ();
            world.addParticle(ParticleTypes.NAUTILUS, lv2.x, lv2.y, lv2.z, f, g, h);
        }
        if (entity != null) {
            Vec3d lv5 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
            float j = (-0.5f + lv.nextFloat()) * (3.0f + entity.getWidth());
            float k = -1.0f + lv.nextFloat() * entity.getHeight();
            f = (-0.5f + lv.nextFloat()) * (3.0f + entity.getWidth());
            Vec3d lv6 = new Vec3d(j, k, f);
            world.addParticle(ParticleTypes.NAUTILUS, lv5.x, lv5.y, lv5.z, lv6.x, lv6.y, lv6.z);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isEyeOpen() {
        return this.eyeOpen;
    }

    private void setEyeOpen(boolean eyeOpen) {
        this.eyeOpen = eyeOpen;
    }

    public float getRotation(float tickDelta) {
        return (this.ticksActive + tickDelta) * -0.0375f;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

