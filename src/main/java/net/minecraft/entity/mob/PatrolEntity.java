/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public abstract class PatrolEntity
extends HostileEntity {
    @Nullable
    private BlockPos patrolTarget;
    private boolean patrolLeader;
    private boolean patrolling;

    protected PatrolEntity(EntityType<? extends PatrolEntity> arg, World arg2) {
        super((EntityType<? extends HostileEntity>)arg, arg2);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(4, new PatrolGoal<PatrolEntity>(this, 0.7, 0.595));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.patrolTarget != null) {
            nbt.put("patrol_target", NbtHelper.fromBlockPos(this.patrolTarget));
        }
        nbt.putBoolean("PatrolLeader", this.patrolLeader);
        nbt.putBoolean("Patrolling", this.patrolling);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtHelper.toBlockPos(nbt, "patrol_target").ifPresent(patrolTarget -> {
            this.patrolTarget = patrolTarget;
        });
        this.patrolLeader = nbt.getBoolean("PatrolLeader");
        this.patrolling = nbt.getBoolean("Patrolling");
    }

    public boolean canLead() {
        return true;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (spawnReason != SpawnReason.PATROL && spawnReason != SpawnReason.EVENT && spawnReason != SpawnReason.STRUCTURE && world.getRandom().nextFloat() < 0.06f && this.canLead()) {
            this.patrolLeader = true;
        }
        if (this.isPatrolLeader()) {
            this.equipStack(EquipmentSlot.HEAD, Raid.getOminousBanner(this.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)));
            this.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0f);
        }
        if (spawnReason == SpawnReason.PATROL) {
            this.patrolling = true;
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public static boolean canSpawn(EntityType<? extends PatrolEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 8) {
            return false;
        }
        return PatrolEntity.canSpawnIgnoreLightLevel(type, world, spawnReason, pos, random);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.patrolling || distanceSquared > 16384.0;
    }

    public void setPatrolTarget(BlockPos targetPos) {
        this.patrolTarget = targetPos;
        this.patrolling = true;
    }

    public BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean patrolLeader) {
        this.patrolLeader = patrolLeader;
        this.patrolling = true;
    }

    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    public boolean hasNoRaid() {
        return true;
    }

    public void setRandomPatrolTarget() {
        this.patrolTarget = this.getBlockPos().add(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    protected boolean isRaidCenterSet() {
        return this.patrolling;
    }

    protected void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
    }

    public static class PatrolGoal<T extends PatrolEntity>
    extends Goal {
        private static final int field_30474 = 200;
        private final T entity;
        private final double leaderSpeed;
        private final double followSpeed;
        private long nextPatrolSearchTime;

        public PatrolGoal(T entity, double leaderSpeed, double followSpeed) {
            this.entity = entity;
            this.leaderSpeed = leaderSpeed;
            this.followSpeed = followSpeed;
            this.nextPatrolSearchTime = -1L;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            boolean bl = ((Entity)this.entity).getWorld().getTime() < this.nextPatrolSearchTime;
            return ((PatrolEntity)this.entity).isRaidCenterSet() && ((MobEntity)this.entity).getTarget() == null && !((Entity)this.entity).hasControllingPassenger() && ((PatrolEntity)this.entity).hasPatrolTarget() && !bl;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
            boolean bl = ((PatrolEntity)this.entity).isPatrolLeader();
            EntityNavigation lv = ((MobEntity)this.entity).getNavigation();
            if (lv.isIdle()) {
                List<PatrolEntity> list = this.findPatrolTargets();
                if (((PatrolEntity)this.entity).isRaidCenterSet() && list.isEmpty()) {
                    ((PatrolEntity)this.entity).setPatrolling(false);
                } else if (!bl || !((PatrolEntity)this.entity).getPatrolTarget().isWithinDistance(((Entity)this.entity).getPos(), 10.0)) {
                    Vec3d lv2 = Vec3d.ofBottomCenter(((PatrolEntity)this.entity).getPatrolTarget());
                    Vec3d lv3 = ((Entity)this.entity).getPos();
                    Vec3d lv4 = lv3.subtract(lv2);
                    lv2 = lv4.rotateY(90.0f).multiply(0.4).add(lv2);
                    Vec3d lv5 = lv2.subtract(lv3).normalize().multiply(10.0).add(lv3);
                    BlockPos lv6 = BlockPos.ofFloored(lv5);
                    lv6 = ((Entity)this.entity).getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv6);
                    if (!lv.startMovingTo(lv6.getX(), lv6.getY(), lv6.getZ(), bl ? this.followSpeed : this.leaderSpeed)) {
                        this.wander();
                        this.nextPatrolSearchTime = ((Entity)this.entity).getWorld().getTime() + 200L;
                    } else if (bl) {
                        for (PatrolEntity lv7 : list) {
                            lv7.setPatrolTarget(lv6);
                        }
                    }
                } else {
                    ((PatrolEntity)this.entity).setRandomPatrolTarget();
                }
            }
        }

        private List<PatrolEntity> findPatrolTargets() {
            return ((Entity)this.entity).getWorld().getEntitiesByClass(PatrolEntity.class, ((Entity)this.entity).getBoundingBox().expand(16.0), arg -> arg.hasNoRaid() && !arg.isPartOf((Entity)this.entity));
        }

        private boolean wander() {
            Random lv = ((Entity)this.entity).getRandom();
            BlockPos lv2 = ((Entity)this.entity).getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ((Entity)this.entity).getBlockPos().add(-8 + lv.nextInt(16), 0, -8 + lv.nextInt(16)));
            return ((MobEntity)this.entity).getNavigation().startMovingTo(lv2.getX(), lv2.getY(), lv2.getZ(), this.leaderSpeed);
        }
    }
}

