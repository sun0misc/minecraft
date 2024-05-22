/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZombieSiegeManager
implements SpecialSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean spawned;
    private State state = State.SIEGE_DONE;
    private int remaining;
    private int countdown;
    private int startX;
    private int startY;
    private int startZ;

    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        if (world.isDay() || !spawnMonsters) {
            this.state = State.SIEGE_DONE;
            this.spawned = false;
            return 0;
        }
        float f = world.getSkyAngle(0.0f);
        if ((double)f == 0.5) {
            State state = this.state = world.random.nextInt(10) == 0 ? State.SIEGE_TONIGHT : State.SIEGE_DONE;
        }
        if (this.state == State.SIEGE_DONE) {
            return 0;
        }
        if (!this.spawned) {
            if (this.spawn(world)) {
                this.spawned = true;
            } else {
                return 0;
            }
        }
        if (this.countdown > 0) {
            --this.countdown;
            return 0;
        }
        this.countdown = 2;
        if (this.remaining > 0) {
            this.trySpawnZombie(world);
            --this.remaining;
        } else {
            this.state = State.SIEGE_DONE;
        }
        return 1;
    }

    private boolean spawn(ServerWorld world) {
        for (PlayerEntity playerEntity : world.getPlayers()) {
            BlockPos lv2;
            if (playerEntity.isSpectator() || !world.isNearOccupiedPointOfInterest(lv2 = playerEntity.getBlockPos()) || world.getBiome(lv2).isIn(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) continue;
            for (int i = 0; i < 10; ++i) {
                float f = world.random.nextFloat() * ((float)Math.PI * 2);
                this.startX = lv2.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0f);
                this.startY = lv2.getY();
                this.startZ = lv2.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0f);
                if (this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ)) == null) continue;
                this.countdown = 0;
                this.remaining = 20;
                break;
            }
            return true;
        }
        return false;
    }

    private void trySpawnZombie(ServerWorld world) {
        ZombieEntity lv2;
        Vec3d lv = this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ));
        if (lv == null) {
            return;
        }
        try {
            lv2 = new ZombieEntity(world);
            lv2.initialize(world, world.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.EVENT, null);
        } catch (Exception exception) {
            LOGGER.warn("Failed to create zombie for village siege at {}", (Object)lv, (Object)exception);
            return;
        }
        lv2.refreshPositionAndAngles(lv.x, lv.y, lv.z, world.random.nextFloat() * 360.0f, 0.0f);
        world.spawnEntityAndPassengers(lv2);
    }

    @Nullable
    private Vec3d getSpawnVector(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 10; ++i) {
            int k;
            int l;
            int j = pos.getX() + world.random.nextInt(16) - 8;
            BlockPos lv = new BlockPos(j, l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k = pos.getZ() + world.random.nextInt(16) - 8), k);
            if (!world.isNearOccupiedPointOfInterest(lv) || !HostileEntity.canSpawnInDark(EntityType.ZOMBIE, world, SpawnReason.EVENT, lv, world.random)) continue;
            return Vec3d.ofBottomCenter(lv);
        }
        return null;
    }

    static enum State {
        SIEGE_CAN_ACTIVATE,
        SIEGE_TONIGHT,
        SIEGE_DONE;

    }
}

