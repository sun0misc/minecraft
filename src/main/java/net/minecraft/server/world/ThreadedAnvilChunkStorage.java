package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.GameRules;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {
   private static final byte PROTO_CHUNK = -1;
   private static final byte UNMARKED_CHUNK = 0;
   private static final byte LEVEL_CHUNK = 1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_29674 = 200;
   private static final int field_36291 = 20;
   private static final int field_36384 = 10000;
   private static final int field_29675 = 3;
   public static final int field_29669 = 33;
   public static final int MAX_LEVEL = 33 + ChunkStatus.getMaxDistanceFromFull();
   public static final int field_29670 = 31;
   private final Long2ObjectLinkedOpenHashMap currentChunkHolders = new Long2ObjectLinkedOpenHashMap();
   private volatile Long2ObjectLinkedOpenHashMap chunkHolders;
   private final Long2ObjectLinkedOpenHashMap chunksToUnload;
   private final LongSet loadedChunks;
   final ServerWorld world;
   private final ServerLightingProvider lightingProvider;
   private final ThreadExecutor mainThreadExecutor;
   private ChunkGenerator chunkGenerator;
   private final NoiseConfig noiseConfig;
   private final StructurePlacementCalculator structurePlacementCalculator;
   private final Supplier persistentStateManagerFactory;
   private final PointOfInterestStorage pointOfInterestStorage;
   final LongSet unloadedChunks;
   private boolean chunkHolderListDirty;
   private final ChunkTaskPrioritySystem chunkTaskPrioritySystem;
   private final MessageListener worldGenExecutor;
   private final MessageListener mainExecutor;
   private final WorldGenerationProgressListener worldGenerationProgressListener;
   private final ChunkStatusChangeListener chunkStatusChangeListener;
   private final TicketManager ticketManager;
   private final AtomicInteger totalChunksLoadedCount;
   private final StructureTemplateManager structureTemplateManager;
   private final String saveDir;
   private final PlayerChunkWatchingManager playerChunkWatchingManager;
   private final Int2ObjectMap entityTrackers;
   private final Long2ByteMap chunkToType;
   private final Long2LongMap chunkToNextSaveTimeMs;
   private final Queue unloadTaskQueue;
   int watchDistance;

   public ThreadedAnvilChunkStorage(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, int viewDistance, boolean dsync) {
      super(session.getWorldDirectory(world.getRegistryKey()).resolve("region"), dataFixer, dsync);
      this.chunkHolders = this.currentChunkHolders.clone();
      this.chunksToUnload = new Long2ObjectLinkedOpenHashMap();
      this.loadedChunks = new LongOpenHashSet();
      this.unloadedChunks = new LongOpenHashSet();
      this.totalChunksLoadedCount = new AtomicInteger();
      this.playerChunkWatchingManager = new PlayerChunkWatchingManager();
      this.entityTrackers = new Int2ObjectOpenHashMap();
      this.chunkToType = new Long2ByteOpenHashMap();
      this.chunkToNextSaveTimeMs = new Long2LongOpenHashMap();
      this.unloadTaskQueue = Queues.newConcurrentLinkedQueue();
      this.structureTemplateManager = structureTemplateManager;
      Path path = session.getWorldDirectory(world.getRegistryKey());
      this.saveDir = path.getFileName().toString();
      this.world = world;
      this.chunkGenerator = chunkGenerator;
      DynamicRegistryManager lv = world.getRegistryManager();
      long l = world.getSeed();
      if (chunkGenerator instanceof NoiseChunkGenerator lv2) {
         this.noiseConfig = NoiseConfig.create((ChunkGeneratorSettings)((ChunkGeneratorSettings)lv2.getSettings().value()), (RegistryEntryLookup)lv.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
      } else {
         this.noiseConfig = NoiseConfig.create((ChunkGeneratorSettings)ChunkGeneratorSettings.createMissingSettings(), (RegistryEntryLookup)lv.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
      }

      this.structurePlacementCalculator = chunkGenerator.createStructurePlacementCalculator(lv.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), this.noiseConfig, l);
      this.mainThreadExecutor = mainThreadExecutor;
      TaskExecutor lv3 = TaskExecutor.create(executor, "worldgen");
      Objects.requireNonNull(mainThreadExecutor);
      MessageListener lv4 = MessageListener.create("main", mainThreadExecutor::send);
      this.worldGenerationProgressListener = worldGenerationProgressListener;
      this.chunkStatusChangeListener = chunkStatusChangeListener;
      TaskExecutor lv5 = TaskExecutor.create(executor, "light");
      this.chunkTaskPrioritySystem = new ChunkTaskPrioritySystem(ImmutableList.of(lv3, lv4, lv5), executor, Integer.MAX_VALUE);
      this.worldGenExecutor = this.chunkTaskPrioritySystem.createExecutor(lv3, false);
      this.mainExecutor = this.chunkTaskPrioritySystem.createExecutor(lv4, false);
      this.lightingProvider = new ServerLightingProvider(chunkProvider, this, this.world.getDimension().hasSkyLight(), lv5, this.chunkTaskPrioritySystem.createExecutor(lv5, false));
      this.ticketManager = new TicketManager(executor, mainThreadExecutor);
      this.persistentStateManagerFactory = persistentStateManagerFactory;
      this.pointOfInterestStorage = new PointOfInterestStorage(path.resolve("poi"), dataFixer, dsync, lv, world);
      this.setViewDistance(viewDistance);
   }

   protected ChunkGenerator getChunkGenerator() {
      return this.chunkGenerator;
   }

   protected StructurePlacementCalculator getStructurePlacementCalculator() {
      return this.structurePlacementCalculator;
   }

   protected NoiseConfig getNoiseConfig() {
      return this.noiseConfig;
   }

   public void verifyChunkGenerator() {
      DataResult dataResult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.chunkGenerator);
      DataResult dataResult2 = dataResult.flatMap((json) -> {
         return ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, json);
      });
      dataResult2.result().ifPresent((chunkGenerator) -> {
         this.chunkGenerator = chunkGenerator;
      });
   }

   private static double getSquaredDistance(ChunkPos pos, Entity entity) {
      double d = (double)ChunkSectionPos.getOffsetPos(pos.x, 8);
      double e = (double)ChunkSectionPos.getOffsetPos(pos.z, 8);
      double f = d - entity.getX();
      double g = e - entity.getZ();
      return f * f + g * g;
   }

   public static boolean isWithinDistance(int x1, int z1, int x2, int z2, int distance) {
      int n = Math.max(0, Math.abs(x1 - x2) - 1);
      int o = Math.max(0, Math.abs(z1 - z2) - 1);
      long p = (long)Math.max(0, Math.max(n, o) - 1);
      long q = (long)Math.min(n, o);
      long r = q * q + p * p;
      int s = distance - 1;
      int t = s * s;
      return r <= (long)t;
   }

   private static boolean isOnDistanceEdge(int x1, int z1, int x2, int z2, int distance) {
      if (!isWithinDistance(x1, z1, x2, z2, distance)) {
         return false;
      } else if (!isWithinDistance(x1 + 1, z1, x2, z2, distance)) {
         return true;
      } else if (!isWithinDistance(x1, z1 + 1, x2, z2, distance)) {
         return true;
      } else if (!isWithinDistance(x1 - 1, z1, x2, z2, distance)) {
         return true;
      } else {
         return !isWithinDistance(x1, z1 - 1, x2, z2, distance);
      }
   }

   protected ServerLightingProvider getLightingProvider() {
      return this.lightingProvider;
   }

   @Nullable
   protected ChunkHolder getCurrentChunkHolder(long pos) {
      return (ChunkHolder)this.currentChunkHolders.get(pos);
   }

   @Nullable
   protected ChunkHolder getChunkHolder(long pos) {
      return (ChunkHolder)this.chunkHolders.get(pos);
   }

   protected IntSupplier getCompletedLevelSupplier(long pos) {
      return () -> {
         ChunkHolder lv = this.getChunkHolder(pos);
         return lv == null ? LevelPrioritizedQueue.LEVEL_COUNT - 1 : Math.min(lv.getCompletedLevel(), LevelPrioritizedQueue.LEVEL_COUNT - 1);
      };
   }

   public String getChunkLoadingDebugInfo(ChunkPos chunkPos) {
      ChunkHolder lv = this.getChunkHolder(chunkPos.toLong());
      if (lv == null) {
         return "null";
      } else {
         String string = lv.getLevel() + "\n";
         ChunkStatus lv2 = lv.getCurrentStatus();
         Chunk lv3 = lv.getCurrentChunk();
         if (lv2 != null) {
            string = string + "St: §" + lv2.getIndex() + lv2 + "§r\n";
         }

         if (lv3 != null) {
            string = string + "Ch: §" + lv3.getStatus().getIndex() + lv3.getStatus() + "§r\n";
         }

         ChunkHolder.LevelType lv4 = lv.getLevelType();
         string = string + "§" + lv4.ordinal() + lv4;
         return string + "§r";
      }
   }

   private CompletableFuture getRegion(ChunkPos centerChunk, int margin, IntFunction distanceToStatus) {
      List list = new ArrayList();
      List list2 = new ArrayList();
      int j = centerChunk.x;
      int k = centerChunk.z;

      for(int l = -margin; l <= margin; ++l) {
         for(int m = -margin; m <= margin; ++m) {
            int n = Math.max(Math.abs(m), Math.abs(l));
            final ChunkPos lv = new ChunkPos(j + m, k + l);
            long o = lv.toLong();
            ChunkHolder lv2 = this.getCurrentChunkHolder(o);
            if (lv2 == null) {
               return CompletableFuture.completedFuture(Either.right(new ChunkHolder.Unloaded() {
                  public String toString() {
                     return "Unloaded " + lv;
                  }
               }));
            }

            ChunkStatus lv3 = (ChunkStatus)distanceToStatus.apply(n);
            CompletableFuture completableFuture = lv2.getChunkAt(lv3, this);
            list2.add(lv2);
            list.add(completableFuture);
         }
      }

      CompletableFuture completableFuture2 = Util.combineSafe(list);
      CompletableFuture completableFuture3 = completableFuture2.thenApply((chunks) -> {
         List list2 = Lists.newArrayList();
         final int l = 0;

         for(Iterator var7 = chunks.iterator(); var7.hasNext(); ++l) {
            final Either either = (Either)var7.next();
            if (either == null) {
               throw this.crash(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
            }

            Optional optional = either.left();
            if (!optional.isPresent()) {
               return Either.right(new ChunkHolder.Unloaded() {
                  public String toString() {
                     ChunkPos var10000 = new ChunkPos(i + l % (j * 2 + 1), k + l / (j * 2 + 1));
                     return "Unloaded " + var10000 + " " + either.right().get();
                  }
               });
            }

            list2.add((Chunk)optional.get());
         }

         return Either.left(list2);
      });
      Iterator var17 = list2.iterator();

      while(var17.hasNext()) {
         ChunkHolder lv4 = (ChunkHolder)var17.next();
         lv4.combineSavingFuture("getChunkRangeFuture " + centerChunk + " " + margin, completableFuture3);
      }

      return completableFuture3;
   }

   public CrashException crash(IllegalStateException exception, String details) {
      StringBuilder stringBuilder = new StringBuilder();
      Consumer consumer = (chunkHolder) -> {
         chunkHolder.collectFuturesByStatus().forEach((pair) -> {
            ChunkStatus lv = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
               stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(lv).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }

         });
      };
      stringBuilder.append("Updating:").append(System.lineSeparator());
      this.currentChunkHolders.values().forEach(consumer);
      stringBuilder.append("Visible:").append(System.lineSeparator());
      this.chunkHolders.values().forEach(consumer);
      CrashReport lv = CrashReport.create(exception, "Chunk loading");
      CrashReportSection lv2 = lv.addElement("Chunk loading");
      lv2.add("Details", (Object)details);
      lv2.add("Futures", (Object)stringBuilder);
      return new CrashException(lv);
   }

   public CompletableFuture makeChunkEntitiesTickable(ChunkPos pos) {
      return this.getRegion(pos, 2, (distance) -> {
         return ChunkStatus.FULL;
      }).thenApplyAsync((either) -> {
         return either.mapLeft((chunks) -> {
            return (WorldChunk)chunks.get(chunks.size() / 2);
         });
      }, this.mainThreadExecutor);
   }

   @Nullable
   ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
      if (j > MAX_LEVEL && level > MAX_LEVEL) {
         return holder;
      } else {
         if (holder != null) {
            holder.setLevel(level);
         }

         if (holder != null) {
            if (level > MAX_LEVEL) {
               this.unloadedChunks.add(pos);
            } else {
               this.unloadedChunks.remove(pos);
            }
         }

         if (level <= MAX_LEVEL && holder == null) {
            holder = (ChunkHolder)this.chunksToUnload.remove(pos);
            if (holder != null) {
               holder.setLevel(level);
            } else {
               holder = new ChunkHolder(new ChunkPos(pos), level, this.world, this.lightingProvider, this.chunkTaskPrioritySystem, this);
            }

            this.currentChunkHolders.put(pos, holder);
            this.chunkHolderListDirty = true;
         }

         return holder;
      }
   }

   public void close() throws IOException {
      try {
         this.chunkTaskPrioritySystem.close();
         this.pointOfInterestStorage.close();
      } finally {
         super.close();
      }

   }

   protected void save(boolean flush) {
      if (flush) {
         List list = (List)this.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).collect(Collectors.toList());
         MutableBoolean mutableBoolean = new MutableBoolean();

         do {
            mutableBoolean.setFalse();
            list.stream().map((chunkHolder) -> {
               CompletableFuture completableFuture;
               do {
                  completableFuture = chunkHolder.getSavingFuture();
                  ThreadExecutor var10000 = this.mainThreadExecutor;
                  Objects.requireNonNull(completableFuture);
                  var10000.runTasks(completableFuture::isDone);
               } while(completableFuture != chunkHolder.getSavingFuture());

               return (Chunk)completableFuture.join();
            }).filter((chunk) -> {
               return chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk;
            }).filter(this::save).forEach((chunk) -> {
               mutableBoolean.setTrue();
            });
         } while(mutableBoolean.isTrue());

         this.unloadChunks(() -> {
            return true;
         });
         this.completeAll();
      } else {
         this.chunkHolders.values().forEach(this::save);
      }

   }

   protected void tick(BooleanSupplier shouldKeepTicking) {
      Profiler lv = this.world.getProfiler();
      lv.push("poi");
      this.pointOfInterestStorage.tick(shouldKeepTicking);
      lv.swap("chunk_unload");
      if (!this.world.isSavingDisabled()) {
         this.unloadChunks(shouldKeepTicking);
      }

      lv.pop();
   }

   public boolean shouldDelayShutdown() {
      return this.lightingProvider.hasUpdates() || !this.chunksToUnload.isEmpty() || !this.currentChunkHolders.isEmpty() || this.pointOfInterestStorage.hasUnsavedElements() || !this.unloadedChunks.isEmpty() || !this.unloadTaskQueue.isEmpty() || this.chunkTaskPrioritySystem.shouldDelayShutdown() || this.ticketManager.shouldDelayShutdown();
   }

   private void unloadChunks(BooleanSupplier shouldKeepTicking) {
      LongIterator longIterator = this.unloadedChunks.iterator();

      for(int i = 0; longIterator.hasNext() && (shouldKeepTicking.getAsBoolean() || i < 200 || this.unloadedChunks.size() > 2000); longIterator.remove()) {
         long l = longIterator.nextLong();
         ChunkHolder lv = (ChunkHolder)this.currentChunkHolders.remove(l);
         if (lv != null) {
            this.chunksToUnload.put(l, lv);
            this.chunkHolderListDirty = true;
            ++i;
            this.tryUnloadChunk(l, lv);
         }
      }

      int j = Math.max(0, this.unloadTaskQueue.size() - 2000);

      Runnable runnable;
      while((shouldKeepTicking.getAsBoolean() || j > 0) && (runnable = (Runnable)this.unloadTaskQueue.poll()) != null) {
         --j;
         runnable.run();
      }

      int k = 0;
      ObjectIterator objectIterator = this.chunkHolders.values().iterator();

      while(k < 20 && shouldKeepTicking.getAsBoolean() && objectIterator.hasNext()) {
         if (this.save((ChunkHolder)objectIterator.next())) {
            ++k;
         }
      }

   }

   private void tryUnloadChunk(long pos, ChunkHolder holder) {
      CompletableFuture completableFuture = holder.getSavingFuture();
      Consumer var10001 = (chunk) -> {
         CompletableFuture completableFuture2 = holder.getSavingFuture();
         if (completableFuture2 != completableFuture) {
            this.tryUnloadChunk(pos, holder);
         } else {
            if (this.chunksToUnload.remove(pos, holder) && chunk != null) {
               if (chunk instanceof WorldChunk) {
                  ((WorldChunk)chunk).setLoadedToWorld(false);
               }

               this.save(chunk);
               if (this.loadedChunks.remove(pos) && chunk instanceof WorldChunk) {
                  WorldChunk lv = (WorldChunk)chunk;
                  this.world.unloadEntities(lv);
               }

               this.lightingProvider.updateChunkStatus(chunk.getPos());
               this.lightingProvider.tick();
               this.worldGenerationProgressListener.setChunkStatus(chunk.getPos(), (ChunkStatus)null);
               this.chunkToNextSaveTimeMs.remove(chunk.getPos().toLong());
            }

         }
      };
      Queue var10002 = this.unloadTaskQueue;
      Objects.requireNonNull(var10002);
      completableFuture.thenAcceptAsync(var10001, var10002::add).whenComplete((void_, throwable) -> {
         if (throwable != null) {
            LOGGER.error("Failed to save chunk {}", holder.getPos(), throwable);
         }

      });
   }

   protected boolean updateHolderMap() {
      if (!this.chunkHolderListDirty) {
         return false;
      } else {
         this.chunkHolders = this.currentChunkHolders.clone();
         this.chunkHolderListDirty = false;
         return true;
      }
   }

   public CompletableFuture getChunk(ChunkHolder holder, ChunkStatus requiredStatus) {
      ChunkPos lv = holder.getPos();
      if (requiredStatus == ChunkStatus.EMPTY) {
         return this.loadChunk(lv);
      } else {
         if (requiredStatus == ChunkStatus.LIGHT) {
            this.ticketManager.addTicketWithLevel(ChunkTicketType.LIGHT, lv, 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.LIGHT), lv);
         }

         Optional optional = ((Either)holder.getChunkAt(requiredStatus.getPrevious(), this).getNow(ChunkHolder.UNLOADED_CHUNK)).left();
         if (optional.isPresent() && ((Chunk)optional.get()).getStatus().isAtLeast(requiredStatus)) {
            CompletableFuture completableFuture = requiredStatus.runLoadTask(this.world, this.structureTemplateManager, this.lightingProvider, (chunk) -> {
               return this.convertToFullChunk(holder);
            }, (Chunk)optional.get());
            this.worldGenerationProgressListener.setChunkStatus(lv, requiredStatus);
            return completableFuture;
         } else {
            return this.upgradeChunk(holder, requiredStatus);
         }
      }
   }

   private CompletableFuture loadChunk(ChunkPos pos) {
      return this.getUpdatedChunkNbt(pos).thenApply((nbt) -> {
         return nbt.filter((nbt2) -> {
            boolean bl = containsStatus(nbt2);
            if (!bl) {
               LOGGER.error("Chunk file at {} is missing level data, skipping", pos);
            }

            return bl;
         });
      }).thenApplyAsync((nbt) -> {
         this.world.getProfiler().visit("chunkLoad");
         if (nbt.isPresent()) {
            Chunk lv = ChunkSerializer.deserialize(this.world, this.pointOfInterestStorage, pos, (NbtCompound)nbt.get());
            this.mark(pos, lv.getStatus().getChunkType());
            return Either.left(lv);
         } else {
            return Either.left(this.getProtoChunk(pos));
         }
      }, this.mainThreadExecutor).exceptionallyAsync((throwable) -> {
         return this.recoverFromException(throwable, pos);
      }, this.mainThreadExecutor);
   }

   private static boolean containsStatus(NbtCompound nbt) {
      return nbt.contains("Status", NbtElement.STRING_TYPE);
   }

   private Either recoverFromException(Throwable throwable, ChunkPos chunkPos) {
      if (throwable instanceof CrashException lv) {
         Throwable throwable2 = lv.getCause();
         if (!(throwable2 instanceof IOException)) {
            this.markAsProtoChunk(chunkPos);
            throw lv;
         }

         LOGGER.error("Couldn't load chunk {}", chunkPos, throwable2);
      } else if (throwable instanceof IOException) {
         LOGGER.error("Couldn't load chunk {}", chunkPos, throwable);
      }

      return Either.left(this.getProtoChunk(chunkPos));
   }

   private Chunk getProtoChunk(ChunkPos chunkPos) {
      this.markAsProtoChunk(chunkPos);
      return new ProtoChunk(chunkPos, UpgradeData.NO_UPGRADE_DATA, this.world, this.world.getRegistryManager().get(RegistryKeys.BIOME), (BlendingData)null);
   }

   private void markAsProtoChunk(ChunkPos pos) {
      this.chunkToType.put(pos.toLong(), (byte)-1);
   }

   private byte mark(ChunkPos pos, ChunkStatus.ChunkType type) {
      return this.chunkToType.put(pos.toLong(), (byte)(type == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
   }

   private CompletableFuture upgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus) {
      ChunkPos lv = holder.getPos();
      CompletableFuture completableFuture = this.getRegion(lv, requiredStatus.getTaskMargin(), (distance) -> {
         return this.getRequiredStatusForGeneration(requiredStatus, distance);
      });
      this.world.getProfiler().visit(() -> {
         return "chunkGenerate " + requiredStatus.getId();
      });
      Executor executor = (task) -> {
         this.worldGenExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task));
      };
      return completableFuture.thenComposeAsync((either) -> {
         return (CompletionStage)either.map((chunks) -> {
            try {
               CompletableFuture completableFuture = requiredStatus.runGenerationTask(executor, this.world, this.chunkGenerator, this.structureTemplateManager, this.lightingProvider, (chunk) -> {
                  return this.convertToFullChunk(holder);
               }, chunks, false);
               this.worldGenerationProgressListener.setChunkStatus(lv, requiredStatus);
               return completableFuture;
            } catch (Exception var9) {
               var9.getStackTrace();
               CrashReport lvx = CrashReport.create(var9, "Exception generating new chunk");
               CrashReportSection lv2 = lvx.addElement("Chunk to be generated");
               lv2.add("Location", (Object)String.format(Locale.ROOT, "%d,%d", lv.x, lv.z));
               lv2.add("Position hash", (Object)ChunkPos.toLong(lv.x, lv.z));
               lv2.add("Generator", (Object)this.chunkGenerator);
               this.mainThreadExecutor.execute(() -> {
                  throw new CrashException(lvx);
               });
               throw new CrashException(lvx);
            }
         }, (unloaded) -> {
            this.releaseLightTicket(lv);
            return CompletableFuture.completedFuture(Either.right(unloaded));
         });
      }, executor);
   }

   protected void releaseLightTicket(ChunkPos pos) {
      this.mainThreadExecutor.send(Util.debugRunnable(() -> {
         this.ticketManager.removeTicketWithLevel(ChunkTicketType.LIGHT, pos, 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.LIGHT), pos);
      }, () -> {
         return "release light ticket " + pos;
      }));
   }

   private ChunkStatus getRequiredStatusForGeneration(ChunkStatus centerChunkTargetStatus, int distance) {
      ChunkStatus lv;
      if (distance == 0) {
         lv = centerChunkTargetStatus.getPrevious();
      } else {
         lv = ChunkStatus.byDistanceFromFull(ChunkStatus.getDistanceFromFull(centerChunkTargetStatus) + distance);
      }

      return lv;
   }

   private static void addEntitiesFromNbt(ServerWorld world, List nbt) {
      if (!nbt.isEmpty()) {
         world.addEntities(EntityType.streamFromNbt(nbt, world));
      }

   }

   private CompletableFuture convertToFullChunk(ChunkHolder chunkHolder) {
      CompletableFuture completableFuture = chunkHolder.getFutureFor(ChunkStatus.FULL.getPrevious());
      return completableFuture.thenApplyAsync((either) -> {
         ChunkStatus lv = ChunkHolder.getTargetStatusForLevel(chunkHolder.getLevel());
         return !lv.isAtLeast(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft((protoChunk) -> {
            ChunkPos lv = chunkHolder.getPos();
            ProtoChunk lv2 = (ProtoChunk)protoChunk;
            WorldChunk lv3;
            if (lv2 instanceof ReadOnlyChunk) {
               lv3 = ((ReadOnlyChunk)lv2).getWrappedChunk();
            } else {
               lv3 = new WorldChunk(this.world, lv2, (chunk) -> {
                  addEntitiesFromNbt(this.world, lv2.getEntities());
               });
               chunkHolder.setCompletedChunk(new ReadOnlyChunk(lv3, false));
            }

            lv3.setLevelTypeProvider(() -> {
               return ChunkHolder.getLevelType(chunkHolder.getLevel());
            });
            lv3.loadEntities();
            if (this.loadedChunks.add(lv.toLong())) {
               lv3.setLoadedToWorld(true);
               lv3.updateAllBlockEntities();
               lv3.addChunkTickSchedulers(this.world);
            }

            return lv3;
         });
      }, (task) -> {
         MessageListener var10000 = this.mainExecutor;
         long var10002 = chunkHolder.getPos().toLong();
         Objects.requireNonNull(chunkHolder);
         var10000.send(ChunkTaskPrioritySystem.createMessage(task, var10002, chunkHolder::getLevel));
      });
   }

   public CompletableFuture makeChunkTickable(ChunkHolder holder) {
      ChunkPos lv = holder.getPos();
      CompletableFuture completableFuture = this.getRegion(lv, 1, (i) -> {
         return ChunkStatus.FULL;
      });
      CompletableFuture completableFuture2 = completableFuture.thenApplyAsync((either) -> {
         return either.mapLeft((list) -> {
            return (WorldChunk)list.get(list.size() / 2);
         });
      }, (task) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task));
      }).thenApplyAsync((either) -> {
         return either.ifLeft((chunk) -> {
            chunk.runPostProcessing();
            this.world.disableTickSchedulers(chunk);
         });
      }, this.mainThreadExecutor);
      completableFuture2.thenAcceptAsync((either) -> {
         either.ifLeft((chunk) -> {
            this.totalChunksLoadedCount.getAndIncrement();
            MutableObject mutableObject = new MutableObject();
            this.getPlayersWatchingChunk(lv, false).forEach((player) -> {
               this.sendChunkDataPackets(player, mutableObject, chunk);
            });
         });
      }, (task) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task));
      });
      return completableFuture2;
   }

   public CompletableFuture makeChunkAccessible(ChunkHolder holder) {
      return this.getRegion(holder.getPos(), 1, ChunkStatus::byDistanceFromFull).thenApplyAsync((either) -> {
         return either.mapLeft((chunks) -> {
            WorldChunk lv = (WorldChunk)chunks.get(chunks.size() / 2);
            return lv;
         });
      }, (task) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task));
      });
   }

   public int getTotalChunksLoadedCount() {
      return this.totalChunksLoadedCount.get();
   }

   private boolean save(ChunkHolder chunkHolder) {
      if (!chunkHolder.isAccessible()) {
         return false;
      } else {
         Chunk lv = (Chunk)chunkHolder.getSavingFuture().getNow((Object)null);
         if (!(lv instanceof ReadOnlyChunk) && !(lv instanceof WorldChunk)) {
            return false;
         } else {
            long l = lv.getPos().toLong();
            long m = this.chunkToNextSaveTimeMs.getOrDefault(l, -1L);
            long n = System.currentTimeMillis();
            if (n < m) {
               return false;
            } else {
               boolean bl = this.save(lv);
               chunkHolder.updateAccessibleStatus();
               if (bl) {
                  this.chunkToNextSaveTimeMs.put(l, n + 10000L);
               }

               return bl;
            }
         }
      }
   }

   private boolean save(Chunk chunk) {
      this.pointOfInterestStorage.saveChunk(chunk.getPos());
      if (!chunk.needsSaving()) {
         return false;
      } else {
         chunk.setNeedsSaving(false);
         ChunkPos lv = chunk.getPos();

         try {
            ChunkStatus lv2 = chunk.getStatus();
            if (lv2.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
               if (this.isLevelChunk(lv)) {
                  return false;
               }

               if (lv2 == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                  return false;
               }
            }

            this.world.getProfiler().visit("chunkSave");
            NbtCompound lv3 = ChunkSerializer.serialize(this.world, chunk);
            this.setNbt(lv, lv3);
            this.mark(lv, lv2.getChunkType());
            return true;
         } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", new Object[]{lv.x, lv.z, var5});
            return false;
         }
      }
   }

   private boolean isLevelChunk(ChunkPos pos) {
      byte b = this.chunkToType.get(pos.toLong());
      if (b != 0) {
         return b == 1;
      } else {
         NbtCompound lv;
         try {
            lv = (NbtCompound)((Optional)this.getUpdatedChunkNbt(pos).join()).orElse((Object)null);
            if (lv == null) {
               this.markAsProtoChunk(pos);
               return false;
            }
         } catch (Exception var5) {
            LOGGER.error("Failed to read chunk {}", pos, var5);
            this.markAsProtoChunk(pos);
            return false;
         }

         ChunkStatus.ChunkType lv2 = ChunkSerializer.getChunkType(lv);
         return this.mark(pos, lv2) == 1;
      }
   }

   protected void setViewDistance(int watchDistance) {
      int j = MathHelper.clamp(watchDistance + 1, 3, 33);
      if (j != this.watchDistance) {
         int k = this.watchDistance;
         this.watchDistance = j;
         this.ticketManager.setWatchDistance(this.watchDistance + 1);
         ObjectIterator var4 = this.currentChunkHolders.values().iterator();

         while(var4.hasNext()) {
            ChunkHolder lv = (ChunkHolder)var4.next();
            ChunkPos lv2 = lv.getPos();
            MutableObject mutableObject = new MutableObject();
            this.getPlayersWatchingChunk(lv2, false).forEach((player) -> {
               ChunkSectionPos lv = player.getWatchedSection();
               boolean bl = isWithinDistance(lv2.x, lv2.z, lv.getSectionX(), lv.getSectionZ(), k);
               boolean bl2 = isWithinDistance(lv2.x, lv2.z, lv.getSectionX(), lv.getSectionZ(), this.watchDistance);
               this.sendWatchPackets(player, lv2, mutableObject, bl, bl2);
            });
         }
      }

   }

   protected void sendWatchPackets(ServerPlayerEntity player, ChunkPos pos, MutableObject packet, boolean oldWithinViewDistance, boolean newWithinViewDistance) {
      if (player.world == this.world) {
         if (newWithinViewDistance && !oldWithinViewDistance) {
            ChunkHolder lv = this.getChunkHolder(pos.toLong());
            if (lv != null) {
               WorldChunk lv2 = lv.getWorldChunk();
               if (lv2 != null) {
                  this.sendChunkDataPackets(player, packet, lv2);
               }

               DebugInfoSender.sendChunkWatchingChange(this.world, pos);
            }
         }

         if (!newWithinViewDistance && oldWithinViewDistance) {
            player.sendUnloadChunkPacket(pos);
         }

      }
   }

   public int getLoadedChunkCount() {
      return this.chunkHolders.size();
   }

   public ChunkTicketManager getTicketManager() {
      return this.ticketManager;
   }

   protected Iterable entryIterator() {
      return Iterables.unmodifiableIterable(this.chunkHolders.values());
   }

   void dump(Writer writer) throws IOException {
      CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").startBody(writer);
      SimulationDistanceLevelPropagator lv2 = this.ticketManager.getSimulationDistanceTracker();
      ObjectBidirectionalIterator var4 = this.chunkHolders.long2ObjectEntrySet().iterator();

      while(var4.hasNext()) {
         Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)var4.next();
         long l = entry.getLongKey();
         ChunkPos lv3 = new ChunkPos(l);
         ChunkHolder lv4 = (ChunkHolder)entry.getValue();
         Optional optional = Optional.ofNullable(lv4.getCurrentChunk());
         Optional optional2 = optional.flatMap((chunk) -> {
            return chunk instanceof WorldChunk ? Optional.of((WorldChunk)chunk) : Optional.empty();
         });
         lv.printRow(lv3.x, lv3.z, lv4.getLevel(), optional.isPresent(), optional.map(Chunk::getStatus).orElse((Object)null), optional2.map(WorldChunk::getLevelType).orElse((Object)null), getFutureStatus(lv4.getAccessibleFuture()), getFutureStatus(lv4.getTickingFuture()), getFutureStatus(lv4.getEntityTickingFuture()), this.ticketManager.getTicket(l), this.shouldTick(lv3), optional2.map((chunk) -> {
            return chunk.getBlockEntities().size();
         }).orElse(0), lv2.getTickingTicket(l), lv2.getLevel(l), optional2.map((chunk) -> {
            return chunk.getBlockTickScheduler().getTickCount();
         }).orElse(0), optional2.map((chunk) -> {
            return chunk.getFluidTickScheduler().getTickCount();
         }).orElse(0));
      }

   }

   private static String getFutureStatus(CompletableFuture future) {
      try {
         Either either = (Either)future.getNow((Object)null);
         return either != null ? (String)either.map((chunk) -> {
            return "done";
         }, (unloaded) -> {
            return "unloaded";
         }) : "not completed";
      } catch (CompletionException var2) {
         return "failed " + var2.getCause().getMessage();
      } catch (CancellationException var3) {
         return "cancelled";
      }
   }

   private CompletableFuture getUpdatedChunkNbt(ChunkPos chunkPos) {
      return this.getNbt(chunkPos).thenApplyAsync((nbt) -> {
         return nbt.map(this::updateChunkNbt);
      }, Util.getMainWorkerExecutor());
   }

   private NbtCompound updateChunkNbt(NbtCompound nbt) {
      return this.updateChunkNbt(this.world.getRegistryKey(), this.persistentStateManagerFactory, nbt, this.chunkGenerator.getCodecKey());
   }

   boolean shouldTick(ChunkPos pos) {
      long l = pos.toLong();
      if (!this.ticketManager.shouldTick(l)) {
         return false;
      } else {
         Iterator var4 = this.playerChunkWatchingManager.getPlayersWatchingChunk(l).iterator();

         ServerPlayerEntity lv;
         do {
            if (!var4.hasNext()) {
               return false;
            }

            lv = (ServerPlayerEntity)var4.next();
         } while(!this.canTickChunk(lv, pos));

         return true;
      }
   }

   public List getPlayersWatchingChunk(ChunkPos pos) {
      long l = pos.toLong();
      if (!this.ticketManager.shouldTick(l)) {
         return List.of();
      } else {
         ImmutableList.Builder builder = ImmutableList.builder();
         Iterator var5 = this.playerChunkWatchingManager.getPlayersWatchingChunk(l).iterator();

         while(var5.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var5.next();
            if (this.canTickChunk(lv, pos)) {
               builder.add(lv);
            }
         }

         return builder.build();
      }
   }

   private boolean canTickChunk(ServerPlayerEntity player, ChunkPos pos) {
      if (player.isSpectator()) {
         return false;
      } else {
         double d = getSquaredDistance(pos, player);
         return d < 16384.0;
      }
   }

   private boolean doesNotGenerateChunks(ServerPlayerEntity player) {
      return player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS);
   }

   void handlePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added) {
      boolean bl2 = this.doesNotGenerateChunks(player);
      boolean bl3 = this.playerChunkWatchingManager.isWatchInactive(player);
      int i = ChunkSectionPos.getSectionCoord(player.getBlockX());
      int j = ChunkSectionPos.getSectionCoord(player.getBlockZ());
      if (added) {
         this.playerChunkWatchingManager.add(ChunkPos.toLong(i, j), player, bl2);
         this.updateWatchedSection(player);
         if (!bl2) {
            this.ticketManager.handleChunkEnter(ChunkSectionPos.from((EntityLike)player), player);
         }
      } else {
         ChunkSectionPos lv = player.getWatchedSection();
         this.playerChunkWatchingManager.remove(lv.toChunkPos().toLong(), player);
         if (!bl3) {
            this.ticketManager.handleChunkLeave(lv, player);
         }
      }

      for(int k = i - this.watchDistance - 1; k <= i + this.watchDistance + 1; ++k) {
         for(int l = j - this.watchDistance - 1; l <= j + this.watchDistance + 1; ++l) {
            if (isWithinDistance(k, l, i, j, this.watchDistance)) {
               ChunkPos lv2 = new ChunkPos(k, l);
               this.sendWatchPackets(player, lv2, new MutableObject(), !added, added);
            }
         }
      }

   }

   private ChunkSectionPos updateWatchedSection(ServerPlayerEntity player) {
      ChunkSectionPos lv = ChunkSectionPos.from((EntityLike)player);
      player.setWatchedSection(lv);
      player.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(lv.getSectionX(), lv.getSectionZ()));
      return lv;
   }

   public void updatePosition(ServerPlayerEntity player) {
      ObjectIterator var2 = this.entityTrackers.values().iterator();

      while(var2.hasNext()) {
         EntityTracker lv = (EntityTracker)var2.next();
         if (lv.entity == player) {
            lv.updateTrackedStatus(this.world.getPlayers());
         } else {
            lv.updateTrackedStatus(player);
         }
      }

      int i = ChunkSectionPos.getSectionCoord(player.getBlockX());
      int j = ChunkSectionPos.getSectionCoord(player.getBlockZ());
      ChunkSectionPos lv2 = player.getWatchedSection();
      ChunkSectionPos lv3 = ChunkSectionPos.from((EntityLike)player);
      long l = lv2.toChunkPos().toLong();
      long m = lv3.toChunkPos().toLong();
      boolean bl = this.playerChunkWatchingManager.isWatchDisabled(player);
      boolean bl2 = this.doesNotGenerateChunks(player);
      boolean bl3 = lv2.asLong() != lv3.asLong();
      if (bl3 || bl != bl2) {
         this.updateWatchedSection(player);
         if (!bl) {
            this.ticketManager.handleChunkLeave(lv2, player);
         }

         if (!bl2) {
            this.ticketManager.handleChunkEnter(lv3, player);
         }

         if (!bl && bl2) {
            this.playerChunkWatchingManager.disableWatch(player);
         }

         if (bl && !bl2) {
            this.playerChunkWatchingManager.enableWatch(player);
         }

         if (l != m) {
            this.playerChunkWatchingManager.movePlayer(l, m, player);
         }
      }

      int k = lv2.getSectionX();
      int n = lv2.getSectionZ();
      int o;
      int p;
      if (Math.abs(k - i) <= this.watchDistance * 2 && Math.abs(n - j) <= this.watchDistance * 2) {
         o = Math.min(i, k) - this.watchDistance - 1;
         p = Math.min(j, n) - this.watchDistance - 1;
         int q = Math.max(i, k) + this.watchDistance + 1;
         int r = Math.max(j, n) + this.watchDistance + 1;

         for(int s = o; s <= q; ++s) {
            for(int t = p; t <= r; ++t) {
               boolean bl4 = isWithinDistance(s, t, k, n, this.watchDistance);
               boolean bl5 = isWithinDistance(s, t, i, j, this.watchDistance);
               this.sendWatchPackets(player, new ChunkPos(s, t), new MutableObject(), bl4, bl5);
            }
         }
      } else {
         boolean bl6;
         boolean bl7;
         for(o = k - this.watchDistance - 1; o <= k + this.watchDistance + 1; ++o) {
            for(p = n - this.watchDistance - 1; p <= n + this.watchDistance + 1; ++p) {
               if (isWithinDistance(o, p, k, n, this.watchDistance)) {
                  bl6 = true;
                  bl7 = false;
                  this.sendWatchPackets(player, new ChunkPos(o, p), new MutableObject(), true, false);
               }
            }
         }

         for(o = i - this.watchDistance - 1; o <= i + this.watchDistance + 1; ++o) {
            for(p = j - this.watchDistance - 1; p <= j + this.watchDistance + 1; ++p) {
               if (isWithinDistance(o, p, i, j, this.watchDistance)) {
                  bl6 = false;
                  bl7 = true;
                  this.sendWatchPackets(player, new ChunkPos(o, p), new MutableObject(), false, true);
               }
            }
         }
      }

   }

   public List getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {
      Set set = this.playerChunkWatchingManager.getPlayersWatchingChunk(chunkPos.toLong());
      ImmutableList.Builder builder = ImmutableList.builder();
      Iterator var5 = set.iterator();

      while(true) {
         ServerPlayerEntity lv;
         ChunkSectionPos lv2;
         do {
            if (!var5.hasNext()) {
               return builder.build();
            }

            lv = (ServerPlayerEntity)var5.next();
            lv2 = lv.getWatchedSection();
         } while((!onlyOnWatchDistanceEdge || !isOnDistanceEdge(chunkPos.x, chunkPos.z, lv2.getSectionX(), lv2.getSectionZ(), this.watchDistance)) && (onlyOnWatchDistanceEdge || !isWithinDistance(chunkPos.x, chunkPos.z, lv2.getSectionX(), lv2.getSectionZ(), this.watchDistance)));

         builder.add(lv);
      }
   }

   protected void loadEntity(Entity entity) {
      if (!(entity instanceof EnderDragonPart)) {
         EntityType lv = entity.getType();
         int i = lv.getMaxTrackDistance() * 16;
         if (i != 0) {
            int j = lv.getTrackTickInterval();
            if (this.entityTrackers.containsKey(entity.getId())) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Entity is already tracked!"));
            } else {
               EntityTracker lv2 = new EntityTracker(entity, i, j, lv.alwaysUpdateVelocity());
               this.entityTrackers.put(entity.getId(), lv2);
               lv2.updateTrackedStatus(this.world.getPlayers());
               if (entity instanceof ServerPlayerEntity) {
                  ServerPlayerEntity lv3 = (ServerPlayerEntity)entity;
                  this.handlePlayerAddedOrRemoved(lv3, true);
                  ObjectIterator var7 = this.entityTrackers.values().iterator();

                  while(var7.hasNext()) {
                     EntityTracker lv4 = (EntityTracker)var7.next();
                     if (lv4.entity != lv3) {
                        lv4.updateTrackedStatus(lv3);
                     }
                  }
               }

            }
         }
      }
   }

   protected void unloadEntity(Entity entity) {
      if (entity instanceof ServerPlayerEntity lv) {
         this.handlePlayerAddedOrRemoved(lv, false);
         ObjectIterator var3 = this.entityTrackers.values().iterator();

         while(var3.hasNext()) {
            EntityTracker lv2 = (EntityTracker)var3.next();
            lv2.stopTracking(lv);
         }
      }

      EntityTracker lv3 = (EntityTracker)this.entityTrackers.remove(entity.getId());
      if (lv3 != null) {
         lv3.stopTracking();
      }

   }

   protected void tickEntityMovement() {
      List list = Lists.newArrayList();
      List list2 = this.world.getPlayers();
      ObjectIterator var3 = this.entityTrackers.values().iterator();

      EntityTracker lv;
      while(var3.hasNext()) {
         lv = (EntityTracker)var3.next();
         ChunkSectionPos lv2 = lv.trackedSection;
         ChunkSectionPos lv3 = ChunkSectionPos.from((EntityLike)lv.entity);
         boolean bl = !Objects.equals(lv2, lv3);
         if (bl) {
            lv.updateTrackedStatus(list2);
            Entity lv4 = lv.entity;
            if (lv4 instanceof ServerPlayerEntity) {
               list.add((ServerPlayerEntity)lv4);
            }

            lv.trackedSection = lv3;
         }

         if (bl || this.ticketManager.shouldTickEntities(lv3.toChunkPos().toLong())) {
            lv.entry.tick();
         }
      }

      if (!list.isEmpty()) {
         var3 = this.entityTrackers.values().iterator();

         while(var3.hasNext()) {
            lv = (EntityTracker)var3.next();
            lv.updateTrackedStatus((List)list);
         }
      }

   }

   public void sendToOtherNearbyPlayers(Entity entity, Packet packet) {
      EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
      if (lv != null) {
         lv.sendToOtherNearbyPlayers(packet);
      }

   }

   protected void sendToNearbyPlayers(Entity entity, Packet packet) {
      EntityTracker lv = (EntityTracker)this.entityTrackers.get(entity.getId());
      if (lv != null) {
         lv.sendToNearbyPlayers(packet);
      }

   }

   public void sendChunkBiomePackets(List chunks) {
      Map map = new HashMap();
      Iterator var3 = chunks.iterator();

      while(var3.hasNext()) {
         Chunk lv = (Chunk)var3.next();
         ChunkPos lv2 = lv.getPos();
         WorldChunk lv4;
         if (lv instanceof WorldChunk lv3) {
            lv4 = lv3;
         } else {
            lv4 = this.world.getChunk(lv2.x, lv2.z);
         }

         Iterator var9 = this.getPlayersWatchingChunk(lv2, false).iterator();

         while(var9.hasNext()) {
            ServerPlayerEntity lv5 = (ServerPlayerEntity)var9.next();
            ((List)map.computeIfAbsent(lv5, (player) -> {
               return new ArrayList();
            })).add(lv4);
         }
      }

      map.forEach((player, chunksx) -> {
         player.networkHandler.sendPacket(ChunkBiomeDataS2CPacket.create(chunksx));
      });
   }

   private void sendChunkDataPackets(ServerPlayerEntity player, MutableObject cachedDataPacket, WorldChunk chunk) {
      if (cachedDataPacket.getValue() == null) {
         cachedDataPacket.setValue(new ChunkDataS2CPacket(chunk, this.lightingProvider, (BitSet)null, (BitSet)null, true));
      }

      player.sendChunkPacket(chunk.getPos(), (Packet)cachedDataPacket.getValue());
      DebugInfoSender.sendChunkWatchingChange(this.world, chunk.getPos());
      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      ObjectIterator var6 = this.entityTrackers.values().iterator();

      while(var6.hasNext()) {
         EntityTracker lv = (EntityTracker)var6.next();
         Entity lv2 = lv.entity;
         if (lv2 != player && lv2.getChunkPos().equals(chunk.getPos())) {
            lv.updateTrackedStatus(player);
            if (lv2 instanceof MobEntity && ((MobEntity)lv2).getHoldingEntity() != null) {
               list.add(lv2);
            }

            if (!lv2.getPassengerList().isEmpty()) {
               list2.add(lv2);
            }
         }
      }

      Iterator var9;
      Entity lv3;
      if (!list.isEmpty()) {
         var9 = list.iterator();

         while(var9.hasNext()) {
            lv3 = (Entity)var9.next();
            player.networkHandler.sendPacket(new EntityAttachS2CPacket(lv3, ((MobEntity)lv3).getHoldingEntity()));
         }
      }

      if (!list2.isEmpty()) {
         var9 = list2.iterator();

         while(var9.hasNext()) {
            lv3 = (Entity)var9.next();
            player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(lv3));
         }
      }

   }

   protected PointOfInterestStorage getPointOfInterestStorage() {
      return this.pointOfInterestStorage;
   }

   public String getSaveDir() {
      return this.saveDir;
   }

   void onChunkStatusChange(ChunkPos chunkPos, ChunkHolder.LevelType levelType) {
      this.chunkStatusChangeListener.onChunkStatusChange(chunkPos, levelType);
   }

   private class TicketManager extends ChunkTicketManager {
      protected TicketManager(Executor workerExecutor, Executor mainThreadExecutor) {
         super(workerExecutor, mainThreadExecutor);
      }

      protected boolean isUnloaded(long pos) {
         return ThreadedAnvilChunkStorage.this.unloadedChunks.contains(pos);
      }

      @Nullable
      protected ChunkHolder getChunkHolder(long pos) {
         return ThreadedAnvilChunkStorage.this.getCurrentChunkHolder(pos);
      }

      @Nullable
      protected ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j) {
         return ThreadedAnvilChunkStorage.this.setLevel(pos, level, holder, j);
      }
   }

   private class EntityTracker {
      final EntityTrackerEntry entry;
      final Entity entity;
      private final int maxDistance;
      ChunkSectionPos trackedSection;
      private final Set listeners = Sets.newIdentityHashSet();

      public EntityTracker(Entity entity, int maxDistance, int tickInterval, boolean alwaysUpdateVelocity) {
         this.entry = new EntityTrackerEntry(ThreadedAnvilChunkStorage.this.world, entity, tickInterval, alwaysUpdateVelocity, this::sendToOtherNearbyPlayers);
         this.entity = entity;
         this.maxDistance = maxDistance;
         this.trackedSection = ChunkSectionPos.from((EntityLike)entity);
      }

      public boolean equals(Object o) {
         if (o instanceof EntityTracker) {
            return ((EntityTracker)o).entity.getId() == this.entity.getId();
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.entity.getId();
      }

      public void sendToOtherNearbyPlayers(Packet packet) {
         Iterator var2 = this.listeners.iterator();

         while(var2.hasNext()) {
            EntityTrackingListener lv = (EntityTrackingListener)var2.next();
            lv.sendPacket(packet);
         }

      }

      public void sendToNearbyPlayers(Packet packet) {
         this.sendToOtherNearbyPlayers(packet);
         if (this.entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
         }

      }

      public void stopTracking() {
         Iterator var1 = this.listeners.iterator();

         while(var1.hasNext()) {
            EntityTrackingListener lv = (EntityTrackingListener)var1.next();
            this.entry.stopTracking(lv.getPlayer());
         }

      }

      public void stopTracking(ServerPlayerEntity player) {
         if (this.listeners.remove(player.networkHandler)) {
            this.entry.stopTracking(player);
         }

      }

      public void updateTrackedStatus(ServerPlayerEntity player) {
         if (player != this.entity) {
            Vec3d lv = player.getPos().subtract(this.entity.getPos());
            double d = (double)Math.min(this.getMaxTrackDistance(), (ThreadedAnvilChunkStorage.this.watchDistance - 1) * 16);
            double e = lv.x * lv.x + lv.z * lv.z;
            double f = d * d;
            boolean bl = e <= f && this.entity.canBeSpectated(player);
            if (bl) {
               if (this.listeners.add(player.networkHandler)) {
                  this.entry.startTracking(player);
               }
            } else if (this.listeners.remove(player.networkHandler)) {
               this.entry.stopTracking(player);
            }

         }
      }

      private int adjustTrackingDistance(int initialDistance) {
         return ThreadedAnvilChunkStorage.this.world.getServer().adjustTrackingDistance(initialDistance);
      }

      private int getMaxTrackDistance() {
         int i = this.maxDistance;
         Iterator var2 = this.entity.getPassengersDeep().iterator();

         while(var2.hasNext()) {
            Entity lv = (Entity)var2.next();
            int j = lv.getType().getMaxTrackDistance() * 16;
            if (j > i) {
               i = j;
            }
         }

         return this.adjustTrackingDistance(i);
      }

      public void updateTrackedStatus(List players) {
         Iterator var2 = players.iterator();

         while(var2.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var2.next();
            this.updateTrackedStatus(lv);
         }

      }
   }
}
