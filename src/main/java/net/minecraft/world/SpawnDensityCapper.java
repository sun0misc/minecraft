package net.minecraft.world;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;

public class SpawnDensityCapper {
   private final Long2ObjectMap chunkPosToMobSpawnablePlayers = new Long2ObjectOpenHashMap();
   private final Map playersToDensityCap = Maps.newHashMap();
   private final ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

   public SpawnDensityCapper(ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
      this.threadedAnvilChunkStorage = threadedAnvilChunkStorage;
   }

   private List getMobSpawnablePlayers(ChunkPos chunkPos) {
      return (List)this.chunkPosToMobSpawnablePlayers.computeIfAbsent(chunkPos.toLong(), (pos) -> {
         return this.threadedAnvilChunkStorage.getPlayersWatchingChunk(chunkPos);
      });
   }

   public void increaseDensity(ChunkPos chunkPos, SpawnGroup spawnGroup) {
      Iterator var3 = this.getMobSpawnablePlayers(chunkPos).iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         ((DensityCap)this.playersToDensityCap.computeIfAbsent(lv, (player) -> {
            return new DensityCap();
         })).increaseDensity(spawnGroup);
      }

   }

   public boolean canSpawn(SpawnGroup spawnGroup, ChunkPos chunkPos) {
      Iterator var3 = this.getMobSpawnablePlayers(chunkPos).iterator();

      DensityCap lv2;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         lv2 = (DensityCap)this.playersToDensityCap.get(lv);
      } while(lv2 != null && !lv2.canSpawn(spawnGroup));

      return true;
   }

   private static class DensityCap {
      private final Object2IntMap spawnGroupsToDensity = new Object2IntOpenHashMap(SpawnGroup.values().length);

      DensityCap() {
      }

      public void increaseDensity(SpawnGroup spawnGroup) {
         this.spawnGroupsToDensity.computeInt(spawnGroup, (group, density) -> {
            return density == null ? 1 : density + 1;
         });
      }

      public boolean canSpawn(SpawnGroup spawnGroup) {
         return this.spawnGroupsToDensity.getOrDefault(spawnGroup, 0) < spawnGroup.getCapacity();
      }
   }
}
