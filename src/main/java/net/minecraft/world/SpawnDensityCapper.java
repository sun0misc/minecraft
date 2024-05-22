/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;

public class SpawnDensityCapper {
    private final Long2ObjectMap<List<ServerPlayerEntity>> chunkPosToMobSpawnablePlayers = new Long2ObjectOpenHashMap<List<ServerPlayerEntity>>();
    private final Map<ServerPlayerEntity, DensityCap> playersToDensityCap = Maps.newHashMap();
    private final ServerChunkLoadingManager chunkLoadingManager;

    public SpawnDensityCapper(ServerChunkLoadingManager chunkLoadingManager) {
        this.chunkLoadingManager = chunkLoadingManager;
    }

    private List<ServerPlayerEntity> getMobSpawnablePlayers(ChunkPos chunkPos) {
        return this.chunkPosToMobSpawnablePlayers.computeIfAbsent(chunkPos.toLong(), pos -> this.chunkLoadingManager.getPlayersWatchingChunk(chunkPos));
    }

    public void increaseDensity(ChunkPos chunkPos, SpawnGroup spawnGroup) {
        for (ServerPlayerEntity lv : this.getMobSpawnablePlayers(chunkPos)) {
            this.playersToDensityCap.computeIfAbsent(lv, player -> new DensityCap()).increaseDensity(spawnGroup);
        }
    }

    public boolean canSpawn(SpawnGroup spawnGroup, ChunkPos chunkPos) {
        for (ServerPlayerEntity lv : this.getMobSpawnablePlayers(chunkPos)) {
            DensityCap lv2 = this.playersToDensityCap.get(lv);
            if (lv2 != null && !lv2.canSpawn(spawnGroup)) continue;
            return true;
        }
        return false;
    }

    static class DensityCap {
        private final Object2IntMap<SpawnGroup> spawnGroupsToDensity = new Object2IntOpenHashMap<SpawnGroup>(SpawnGroup.values().length);

        DensityCap() {
        }

        public void increaseDensity(SpawnGroup spawnGroup) {
            this.spawnGroupsToDensity.computeInt(spawnGroup, (group, density) -> density == null ? 1 : density + 1);
        }

        public boolean canSpawn(SpawnGroup spawnGroup) {
            return this.spawnGroupsToDensity.getOrDefault((Object)spawnGroup, 0) < spawnGroup.getCapacity();
        }
    }
}

