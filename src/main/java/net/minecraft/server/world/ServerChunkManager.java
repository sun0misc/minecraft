package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.NbtScannable;
import org.jetbrains.annotations.Nullable;

public class ServerChunkManager extends ChunkManager {
   private static final List CHUNK_STATUSES = ChunkStatus.createOrderedList();
   private final ChunkTicketManager ticketManager;
   final ServerWorld world;
   final Thread serverThread;
   final ServerLightingProvider lightingProvider;
   private final MainThreadExecutor mainThreadExecutor;
   public final ThreadedAnvilChunkStorage threadedAnvilChunkStorage;
   private final PersistentStateManager persistentStateManager;
   private long lastMobSpawningTime;
   private boolean spawnMonsters = true;
   private boolean spawnAnimals = true;
   private static final int CACHE_SIZE = 4;
   private final long[] chunkPosCache = new long[4];
   private final ChunkStatus[] chunkStatusCache = new ChunkStatus[4];
   private final Chunk[] chunkCache = new Chunk[4];
   @Nullable
   @Debug
   private SpawnHelper.Info spawnInfo;

   public ServerChunkManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor workerExecutor, ChunkGenerator chunkGenerator, int viewDistance, int simulationDistance, boolean dsync, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory) {
      this.world = world;
      this.mainThreadExecutor = new MainThreadExecutor(world);
      this.serverThread = Thread.currentThread();
      File file = session.getWorldDirectory(world.getRegistryKey()).resolve("data").toFile();
      file.mkdirs();
      this.persistentStateManager = new PersistentStateManager(file, dataFixer);
      this.threadedAnvilChunkStorage = new ThreadedAnvilChunkStorage(world, session, dataFixer, structureTemplateManager, workerExecutor, this.mainThreadExecutor, this, chunkGenerator, worldGenerationProgressListener, chunkStatusChangeListener, persistentStateManagerFactory, viewDistance, dsync);
      this.lightingProvider = this.threadedAnvilChunkStorage.getLightingProvider();
      this.ticketManager = this.threadedAnvilChunkStorage.getTicketManager();
      this.ticketManager.setSimulationDistance(simulationDistance);
      this.initChunkCaches();
   }

   public ServerLightingProvider getLightingProvider() {
      return this.lightingProvider;
   }

   @Nullable
   private ChunkHolder getChunkHolder(long pos) {
      return this.threadedAnvilChunkStorage.getChunkHolder(pos);
   }

   public int getTotalChunksLoadedCount() {
      return this.threadedAnvilChunkStorage.getTotalChunksLoadedCount();
   }

   private void putInCache(long pos, Chunk chunk, ChunkStatus status) {
      for(int i = 3; i > 0; --i) {
         this.chunkPosCache[i] = this.chunkPosCache[i - 1];
         this.chunkStatusCache[i] = this.chunkStatusCache[i - 1];
         this.chunkCache[i] = this.chunkCache[i - 1];
      }

      this.chunkPosCache[0] = pos;
      this.chunkStatusCache[0] = status;
      this.chunkCache[0] = chunk;
   }

   @Nullable
   public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
      if (Thread.currentThread() != this.serverThread) {
         return (Chunk)CompletableFuture.supplyAsync(() -> {
            return this.getChunk(x, z, leastStatus, create);
         }, this.mainThreadExecutor).join();
      } else {
         Profiler lv = this.world.getProfiler();
         lv.visit("getChunk");
         long l = ChunkPos.toLong(x, z);

         Chunk lv2;
         for(int k = 0; k < 4; ++k) {
            if (l == this.chunkPosCache[k] && leastStatus == this.chunkStatusCache[k]) {
               lv2 = this.chunkCache[k];
               if (lv2 != null || !create) {
                  return lv2;
               }
            }
         }

         lv.visit("getChunkCacheMiss");
         CompletableFuture completableFuture = this.getChunkFuture(x, z, leastStatus, create);
         MainThreadExecutor var10000 = this.mainThreadExecutor;
         Objects.requireNonNull(completableFuture);
         var10000.runTasks(completableFuture::isDone);
         lv2 = (Chunk)((Either)completableFuture.join()).map((chunk) -> {
            return chunk;
         }, (unloaded) -> {
            if (create) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Chunk not there when requested: " + unloaded));
            } else {
               return null;
            }
         });
         this.putInCache(l, lv2, leastStatus);
         return lv2;
      }
   }

   @Nullable
   public WorldChunk getWorldChunk(int chunkX, int chunkZ) {
      if (Thread.currentThread() != this.serverThread) {
         return null;
      } else {
         this.world.getProfiler().visit("getChunkNow");
         long l = ChunkPos.toLong(chunkX, chunkZ);

         for(int k = 0; k < 4; ++k) {
            if (l == this.chunkPosCache[k] && this.chunkStatusCache[k] == ChunkStatus.FULL) {
               Chunk lv = this.chunkCache[k];
               return lv instanceof WorldChunk ? (WorldChunk)lv : null;
            }
         }

         ChunkHolder lv2 = this.getChunkHolder(l);
         if (lv2 == null) {
            return null;
         } else {
            Either either = (Either)lv2.getValidFutureFor(ChunkStatus.FULL).getNow((Object)null);
            if (either == null) {
               return null;
            } else {
               Chunk lv3 = (Chunk)either.left().orElse((Object)null);
               if (lv3 != null) {
                  this.putInCache(l, lv3, ChunkStatus.FULL);
                  if (lv3 instanceof WorldChunk) {
                     return (WorldChunk)lv3;
                  }
               }

               return null;
            }
         }
      }
   }

   private void initChunkCaches() {
      Arrays.fill(this.chunkPosCache, ChunkPos.MARKER);
      Arrays.fill(this.chunkStatusCache, (Object)null);
      Arrays.fill(this.chunkCache, (Object)null);
   }

   public CompletableFuture getChunkFutureSyncOnMainThread(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      boolean bl2 = Thread.currentThread() == this.serverThread;
      CompletableFuture completableFuture;
      if (bl2) {
         completableFuture = this.getChunkFuture(chunkX, chunkZ, leastStatus, create);
         MainThreadExecutor var10000 = this.mainThreadExecutor;
         Objects.requireNonNull(completableFuture);
         var10000.runTasks(completableFuture::isDone);
      } else {
         completableFuture = CompletableFuture.supplyAsync(() -> {
            return this.getChunkFuture(chunkX, chunkZ, leastStatus, create);
         }, this.mainThreadExecutor).thenCompose((completableFuturex) -> {
            return completableFuturex;
         });
      }

      return completableFuture;
   }

   private CompletableFuture getChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      ChunkPos lv = new ChunkPos(chunkX, chunkZ);
      long l = lv.toLong();
      int k = 33 + ChunkStatus.getDistanceFromFull(leastStatus);
      ChunkHolder lv2 = this.getChunkHolder(l);
      if (create) {
         this.ticketManager.addTicketWithLevel(ChunkTicketType.UNKNOWN, lv, k, lv);
         if (this.isMissingForLevel(lv2, k)) {
            Profiler lv3 = this.world.getProfiler();
            lv3.push("chunkLoad");
            this.tick();
            lv2 = this.getChunkHolder(l);
            lv3.pop();
            if (this.isMissingForLevel(lv2, k)) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("No chunk holder after ticket has been added"));
            }
         }
      }

      return this.isMissingForLevel(lv2, k) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : lv2.getChunkAt(leastStatus, this.threadedAnvilChunkStorage);
   }

   private boolean isMissingForLevel(@Nullable ChunkHolder holder, int maxLevel) {
      return holder == null || holder.getLevel() > maxLevel;
   }

   public boolean isChunkLoaded(int x, int z) {
      ChunkHolder lv = this.getChunkHolder((new ChunkPos(x, z)).toLong());
      int k = 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.FULL);
      return !this.isMissingForLevel(lv, k);
   }

   public BlockView getChunk(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);
      ChunkHolder lv = this.getChunkHolder(l);
      if (lv == null) {
         return null;
      } else {
         int k = CHUNK_STATUSES.size() - 1;

         while(true) {
            ChunkStatus lv2 = (ChunkStatus)CHUNK_STATUSES.get(k);
            Optional optional = ((Either)lv.getFutureFor(lv2).getNow(ChunkHolder.UNLOADED_CHUNK)).left();
            if (optional.isPresent()) {
               return (BlockView)optional.get();
            }

            if (lv2 == ChunkStatus.LIGHT.getPrevious()) {
               return null;
            }

            --k;
         }
      }
   }

   public World getWorld() {
      return this.world;
   }

   public boolean executeQueuedTasks() {
      return this.mainThreadExecutor.runTask();
   }

   boolean tick() {
      boolean bl = this.ticketManager.tick(this.threadedAnvilChunkStorage);
      boolean bl2 = this.threadedAnvilChunkStorage.updateHolderMap();
      if (!bl && !bl2) {
         return false;
      } else {
         this.initChunkCaches();
         return true;
      }
   }

   public boolean isTickingFutureReady(long pos) {
      ChunkHolder lv = this.getChunkHolder(pos);
      if (lv == null) {
         return false;
      } else if (!this.world.shouldTickBlocksInChunk(pos)) {
         return false;
      } else {
         Either either = (Either)lv.getTickingFuture().getNow((Object)null);
         return either != null && either.left().isPresent();
      }
   }

   public void save(boolean flush) {
      this.tick();
      this.threadedAnvilChunkStorage.save(flush);
   }

   public void close() throws IOException {
      this.save(true);
      this.lightingProvider.close();
      this.threadedAnvilChunkStorage.close();
   }

   public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {
      this.world.getProfiler().push("purge");
      this.ticketManager.purge();
      this.tick();
      this.world.getProfiler().swap("chunks");
      if (tickChunks) {
         this.tickChunks();
      }

      this.world.getProfiler().swap("unload");
      this.threadedAnvilChunkStorage.tick(shouldKeepTicking);
      this.world.getProfiler().pop();
      this.initChunkCaches();
   }

   private void tickChunks() {
      long l = this.world.getTime();
      long m = l - this.lastMobSpawningTime;
      this.lastMobSpawningTime = l;
      boolean bl = this.world.isDebugWorld();
      if (bl) {
         this.threadedAnvilChunkStorage.tickEntityMovement();
      } else {
         WorldProperties lv = this.world.getLevelProperties();
         Profiler lv2 = this.world.getProfiler();
         lv2.push("pollingChunks");
         int i = this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
         boolean bl2 = lv.getTime() % 400L == 0L;
         lv2.push("naturalSpawnCount");
         int j = this.ticketManager.getTickedChunkCount();
         SpawnHelper.Info lv3 = SpawnHelper.setupSpawn(j, this.world.iterateEntities(), this::ifChunkLoaded, new SpawnDensityCapper(this.threadedAnvilChunkStorage));
         this.spawnInfo = lv3;
         lv2.swap("filteringLoadedChunks");
         List list = Lists.newArrayListWithCapacity(j);
         Iterator var13 = this.threadedAnvilChunkStorage.entryIterator().iterator();

         while(var13.hasNext()) {
            ChunkHolder lv4 = (ChunkHolder)var13.next();
            WorldChunk lv5 = lv4.getWorldChunk();
            if (lv5 != null) {
               list.add(new ChunkWithHolder(lv5, lv4));
            }
         }

         lv2.swap("spawnAndTick");
         boolean bl3 = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
         Collections.shuffle(list);
         Iterator var19 = list.iterator();

         while(true) {
            WorldChunk lv7;
            ChunkPos lv8;
            do {
               do {
                  if (!var19.hasNext()) {
                     lv2.swap("customSpawners");
                     if (bl3) {
                        this.world.tickSpawners(this.spawnMonsters, this.spawnAnimals);
                     }

                     lv2.swap("broadcast");
                     list.forEach((chunk) -> {
                        chunk.holder.flushUpdates(chunk.chunk);
                     });
                     lv2.pop();
                     lv2.pop();
                     this.threadedAnvilChunkStorage.tickEntityMovement();
                     return;
                  }

                  ChunkWithHolder lv6 = (ChunkWithHolder)var19.next();
                  lv7 = lv6.chunk;
                  lv8 = lv7.getPos();
               } while(!this.world.shouldTick(lv8));
            } while(!this.threadedAnvilChunkStorage.shouldTick(lv8));

            lv7.increaseInhabitedTime(m);
            if (bl3 && (this.spawnMonsters || this.spawnAnimals) && this.world.getWorldBorder().contains(lv8)) {
               SpawnHelper.spawn(this.world, lv7, lv3, this.spawnAnimals, this.spawnMonsters, bl2);
            }

            if (this.world.shouldTickBlocksInChunk(lv8.toLong())) {
               this.world.tickChunk(lv7, i);
            }
         }
      }
   }

   private void ifChunkLoaded(long pos, Consumer chunkConsumer) {
      ChunkHolder lv = this.getChunkHolder(pos);
      if (lv != null) {
         ((Either)lv.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK)).left().ifPresent(chunkConsumer);
      }

   }

   public String getDebugString() {
      return Integer.toString(this.getLoadedChunkCount());
   }

   @VisibleForTesting
   public int getPendingTasks() {
      return this.mainThreadExecutor.getTaskCount();
   }

   public ChunkGenerator getChunkGenerator() {
      return this.threadedAnvilChunkStorage.getChunkGenerator();
   }

   public StructurePlacementCalculator getStructurePlacementCalculator() {
      return this.threadedAnvilChunkStorage.getStructurePlacementCalculator();
   }

   public NoiseConfig getNoiseConfig() {
      return this.threadedAnvilChunkStorage.getNoiseConfig();
   }

   public int getLoadedChunkCount() {
      return this.threadedAnvilChunkStorage.getLoadedChunkCount();
   }

   public void markForUpdate(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      ChunkHolder lv = this.getChunkHolder(ChunkPos.toLong(i, j));
      if (lv != null) {
         lv.markForBlockUpdate(pos);
      }

   }

   public void onLightUpdate(LightType type, ChunkSectionPos pos) {
      this.mainThreadExecutor.execute(() -> {
         ChunkHolder lv = this.getChunkHolder(pos.toChunkPos().toLong());
         if (lv != null) {
            lv.markForLightUpdate(type, pos.getSectionY());
         }

      });
   }

   public void addTicket(ChunkTicketType ticketType, ChunkPos pos, int radius, Object argument) {
      this.ticketManager.addTicket(ticketType, pos, radius, argument);
   }

   public void removeTicket(ChunkTicketType ticketType, ChunkPos pos, int radius, Object argument) {
      this.ticketManager.removeTicket(ticketType, pos, radius, argument);
   }

   public void setChunkForced(ChunkPos pos, boolean forced) {
      this.ticketManager.setChunkForced(pos, forced);
   }

   public void updatePosition(ServerPlayerEntity player) {
      if (!player.isRemoved()) {
         this.threadedAnvilChunkStorage.updatePosition(player);
      }

   }

   public void unloadEntity(Entity entity) {
      this.threadedAnvilChunkStorage.unloadEntity(entity);
   }

   public void loadEntity(Entity entity) {
      this.threadedAnvilChunkStorage.loadEntity(entity);
   }

   public void sendToNearbyPlayers(Entity entity, Packet packet) {
      this.threadedAnvilChunkStorage.sendToNearbyPlayers(entity, packet);
   }

   public void sendToOtherNearbyPlayers(Entity entity, Packet packet) {
      this.threadedAnvilChunkStorage.sendToOtherNearbyPlayers(entity, packet);
   }

   public void applyViewDistance(int watchDistance) {
      this.threadedAnvilChunkStorage.setViewDistance(watchDistance);
   }

   public void applySimulationDistance(int simulationDistance) {
      this.ticketManager.setSimulationDistance(simulationDistance);
   }

   public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
      this.spawnMonsters = spawnMonsters;
      this.spawnAnimals = spawnAnimals;
   }

   public String getChunkLoadingDebugInfo(ChunkPos pos) {
      return this.threadedAnvilChunkStorage.getChunkLoadingDebugInfo(pos);
   }

   public PersistentStateManager getPersistentStateManager() {
      return this.persistentStateManager;
   }

   public PointOfInterestStorage getPointOfInterestStorage() {
      return this.threadedAnvilChunkStorage.getPointOfInterestStorage();
   }

   public NbtScannable getChunkIoWorker() {
      return this.threadedAnvilChunkStorage.getWorker();
   }

   @Nullable
   @Debug
   public SpawnHelper.Info getSpawnInfo() {
      return this.spawnInfo;
   }

   public void removePersistentTickets() {
      this.ticketManager.removePersistentTickets();
   }

   // $FF: synthetic method
   public LightingProvider getLightingProvider() {
      return this.getLightingProvider();
   }

   // $FF: synthetic method
   public BlockView getWorld() {
      return this.getWorld();
   }

   final class MainThreadExecutor extends ThreadExecutor {
      MainThreadExecutor(World world) {
         super("Chunk source main thread executor for " + world.getRegistryKey().getValue());
      }

      protected Runnable createTask(Runnable runnable) {
         return runnable;
      }

      protected boolean canExecute(Runnable task) {
         return true;
      }

      protected boolean shouldExecuteAsync() {
         return true;
      }

      protected Thread getThread() {
         return ServerChunkManager.this.serverThread;
      }

      protected void executeTask(Runnable task) {
         ServerChunkManager.this.world.getProfiler().visit("runTask");
         super.executeTask(task);
      }

      protected boolean runTask() {
         if (ServerChunkManager.this.tick()) {
            return true;
         } else {
            ServerChunkManager.this.lightingProvider.tick();
            return super.runTask();
         }
      }
   }

   static record ChunkWithHolder(WorldChunk chunk, ChunkHolder holder) {
      final WorldChunk chunk;
      final ChunkHolder holder;

      ChunkWithHolder(WorldChunk arg, ChunkHolder arg2) {
         this.chunk = arg;
         this.holder = arg2;
      }

      public WorldChunk chunk() {
         return this.chunk;
      }

      public ChunkHolder holder() {
         return this.holder;
      }
   }
}
