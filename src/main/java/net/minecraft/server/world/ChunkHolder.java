package net.minecraft.server.world;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.AtomicStack;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class ChunkHolder {
   public static final Either UNLOADED_CHUNK;
   public static final CompletableFuture UNLOADED_CHUNK_FUTURE;
   public static final Either UNLOADED_WORLD_CHUNK;
   private static final Either field_36388;
   private static final CompletableFuture UNLOADED_WORLD_CHUNK_FUTURE;
   private static final List CHUNK_STATUSES;
   private static final LevelType[] LEVEL_TYPES;
   private static final int field_29668 = 64;
   private final AtomicReferenceArray futuresByStatus;
   private final HeightLimitView world;
   private volatile CompletableFuture accessibleFuture;
   private volatile CompletableFuture tickingFuture;
   private volatile CompletableFuture entityTickingFuture;
   private CompletableFuture savingFuture;
   @Nullable
   private final AtomicStack actionStack;
   private int lastTickLevel;
   private int level;
   private int completedLevel;
   final ChunkPos pos;
   private boolean pendingBlockUpdates;
   private final ShortSet[] blockUpdatesBySection;
   private final BitSet blockLightUpdateBits;
   private final BitSet skyLightUpdateBits;
   private final LightingProvider lightingProvider;
   private final LevelUpdateListener levelUpdateListener;
   private final PlayersWatchingChunkProvider playersWatchingChunkProvider;
   private boolean accessible;
   private boolean noLightingUpdates;
   private CompletableFuture field_26930;

   public ChunkHolder(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, LevelUpdateListener levelUpdateListener, PlayersWatchingChunkProvider playersWatchingChunkProvider) {
      this.futuresByStatus = new AtomicReferenceArray(CHUNK_STATUSES.size());
      this.accessibleFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.savingFuture = CompletableFuture.completedFuture((Object)null);
      this.actionStack = null;
      this.blockLightUpdateBits = new BitSet();
      this.skyLightUpdateBits = new BitSet();
      this.field_26930 = CompletableFuture.completedFuture((Object)null);
      this.pos = pos;
      this.world = world;
      this.lightingProvider = lightingProvider;
      this.levelUpdateListener = levelUpdateListener;
      this.playersWatchingChunkProvider = playersWatchingChunkProvider;
      this.lastTickLevel = ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
      this.level = this.lastTickLevel;
      this.completedLevel = this.lastTickLevel;
      this.setLevel(level);
      this.blockUpdatesBySection = new ShortSet[world.countVerticalSections()];
   }

   public CompletableFuture getFutureFor(ChunkStatus leastStatus) {
      CompletableFuture completableFuture = (CompletableFuture)this.futuresByStatus.get(leastStatus.getIndex());
      return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
   }

   public CompletableFuture getValidFutureFor(ChunkStatus leastStatus) {
      return getTargetStatusForLevel(this.level).isAtLeast(leastStatus) ? this.getFutureFor(leastStatus) : UNLOADED_CHUNK_FUTURE;
   }

   public CompletableFuture getTickingFuture() {
      return this.tickingFuture;
   }

   public CompletableFuture getEntityTickingFuture() {
      return this.entityTickingFuture;
   }

   public CompletableFuture getAccessibleFuture() {
      return this.accessibleFuture;
   }

   @Nullable
   public WorldChunk getWorldChunk() {
      CompletableFuture completableFuture = this.getTickingFuture();
      Either either = (Either)completableFuture.getNow((Object)null);
      return either == null ? null : (WorldChunk)either.left().orElse((Object)null);
   }

   @Nullable
   public WorldChunk method_41205() {
      CompletableFuture completableFuture = this.getAccessibleFuture();
      Either either = (Either)completableFuture.getNow((Object)null);
      return either == null ? null : (WorldChunk)either.left().orElse((Object)null);
   }

   @Nullable
   public ChunkStatus getCurrentStatus() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus lv = (ChunkStatus)CHUNK_STATUSES.get(i);
         CompletableFuture completableFuture = this.getFutureFor(lv);
         if (((Either)completableFuture.getNow(UNLOADED_CHUNK)).left().isPresent()) {
            return lv;
         }
      }

      return null;
   }

   @Nullable
   public Chunk getCurrentChunk() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus lv = (ChunkStatus)CHUNK_STATUSES.get(i);
         CompletableFuture completableFuture = this.getFutureFor(lv);
         if (!completableFuture.isCompletedExceptionally()) {
            Optional optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
            if (optional.isPresent()) {
               return (Chunk)optional.get();
            }
         }
      }

      return null;
   }

   public CompletableFuture getSavingFuture() {
      return this.savingFuture;
   }

   public void markForBlockUpdate(BlockPos pos) {
      WorldChunk lv = this.getWorldChunk();
      if (lv != null) {
         int i = this.world.getSectionIndex(pos.getY());
         if (this.blockUpdatesBySection[i] == null) {
            this.pendingBlockUpdates = true;
            this.blockUpdatesBySection[i] = new ShortOpenHashSet();
         }

         this.blockUpdatesBySection[i].add(ChunkSectionPos.packLocal(pos));
      }
   }

   public void markForLightUpdate(LightType lightType, int y) {
      Either either = (Either)this.getValidFutureFor(ChunkStatus.FEATURES).getNow((Object)null);
      if (either != null) {
         Chunk lv = (Chunk)either.left().orElse((Object)null);
         if (lv != null) {
            lv.setNeedsSaving(true);
            WorldChunk lv2 = this.getWorldChunk();
            if (lv2 != null) {
               int j = this.lightingProvider.getBottomY();
               int k = this.lightingProvider.getTopY();
               if (y >= j && y <= k) {
                  int l = y - j;
                  if (lightType == LightType.SKY) {
                     this.skyLightUpdateBits.set(l);
                  } else {
                     this.blockLightUpdateBits.set(l);
                  }

               }
            }
         }
      }
   }

   public void flushUpdates(WorldChunk chunk) {
      if (this.pendingBlockUpdates || !this.skyLightUpdateBits.isEmpty() || !this.blockLightUpdateBits.isEmpty()) {
         World lv = chunk.getWorld();
         int i = 0;

         int j;
         for(j = 0; j < this.blockUpdatesBySection.length; ++j) {
            i += this.blockUpdatesBySection[j] != null ? this.blockUpdatesBySection[j].size() : 0;
         }

         this.noLightingUpdates |= i >= 64;
         if (!this.skyLightUpdateBits.isEmpty() || !this.blockLightUpdateBits.isEmpty()) {
            this.sendPacketToPlayersWatching(new LightUpdateS2CPacket(chunk.getPos(), this.lightingProvider, this.skyLightUpdateBits, this.blockLightUpdateBits, true), !this.noLightingUpdates);
            this.skyLightUpdateBits.clear();
            this.blockLightUpdateBits.clear();
         }

         for(j = 0; j < this.blockUpdatesBySection.length; ++j) {
            ShortSet shortSet = this.blockUpdatesBySection[j];
            if (shortSet != null) {
               int k = this.world.sectionIndexToCoord(j);
               ChunkSectionPos lv2 = ChunkSectionPos.from(chunk.getPos(), k);
               if (shortSet.size() == 1) {
                  BlockPos lv3 = lv2.unpackBlockPos(shortSet.iterator().nextShort());
                  BlockState lv4 = lv.getBlockState(lv3);
                  this.sendPacketToPlayersWatching(new BlockUpdateS2CPacket(lv3, lv4), false);
                  this.tryUpdateBlockEntityAt(lv, lv3, lv4);
               } else {
                  ChunkSection lv5 = chunk.getSection(j);
                  ChunkDeltaUpdateS2CPacket lv6 = new ChunkDeltaUpdateS2CPacket(lv2, shortSet, lv5, this.noLightingUpdates);
                  this.sendPacketToPlayersWatching(lv6, false);
                  lv6.visitUpdates((pos, state) -> {
                     this.tryUpdateBlockEntityAt(lv, pos, state);
                  });
               }

               this.blockUpdatesBySection[j] = null;
            }
         }

         this.pendingBlockUpdates = false;
      }
   }

   private void tryUpdateBlockEntityAt(World world, BlockPos pos, BlockState state) {
      if (state.hasBlockEntity()) {
         this.sendBlockEntityUpdatePacket(world, pos);
      }

   }

   private void sendBlockEntityUpdatePacket(World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv != null) {
         Packet lv2 = lv.toUpdatePacket();
         if (lv2 != null) {
            this.sendPacketToPlayersWatching(lv2, false);
         }
      }

   }

   private void sendPacketToPlayersWatching(Packet packet, boolean onlyOnWatchDistanceEdge) {
      this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, onlyOnWatchDistanceEdge).forEach((player) -> {
         player.networkHandler.sendPacket(packet);
      });
   }

   public CompletableFuture getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
      int i = targetStatus.getIndex();
      CompletableFuture completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
      if (completableFuture != null) {
         Either either = (Either)completableFuture.getNow(field_36388);
         if (either == null) {
            String string = "value in future for status: " + targetStatus + " was incorrectly set to null at chunk: " + this.pos;
            throw chunkStorage.crash(new IllegalStateException("null value previously set for chunk status"), string);
         }

         if (either == field_36388 || either.right().isEmpty()) {
            return completableFuture;
         }
      }

      if (getTargetStatusForLevel(this.level).isAtLeast(targetStatus)) {
         CompletableFuture completableFuture2 = chunkStorage.getChunk(this, targetStatus);
         this.combineSavingFuture(completableFuture2, "schedule " + targetStatus);
         this.futuresByStatus.set(i, completableFuture2);
         return completableFuture2;
      } else {
         return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
      }
   }

   protected void combineSavingFuture(String thenDesc, CompletableFuture then) {
      if (this.actionStack != null) {
         this.actionStack.push(new MultithreadAction(Thread.currentThread(), then, thenDesc));
      }

      this.savingFuture = this.savingFuture.thenCombine(then, (arg, object) -> {
         return arg;
      });
   }

   private void combineSavingFuture(CompletableFuture then, String thenDesc) {
      if (this.actionStack != null) {
         this.actionStack.push(new MultithreadAction(Thread.currentThread(), then, thenDesc));
      }

      this.savingFuture = this.savingFuture.thenCombine(then, (arg, either) -> {
         return (Chunk)either.map((argx) -> {
            return argx;
         }, (arg2) -> {
            return arg;
         });
      });
   }

   public LevelType getLevelType() {
      return getLevelType(this.level);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public int getLevel() {
      return this.level;
   }

   public int getCompletedLevel() {
      return this.completedLevel;
   }

   private void setCompletedLevel(int level) {
      this.completedLevel = level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   private void method_31409(ThreadedAnvilChunkStorage arg, CompletableFuture completableFuture, Executor executor, LevelType arg2) {
      this.field_26930.cancel(false);
      CompletableFuture completableFuture2 = new CompletableFuture();
      completableFuture2.thenRunAsync(() -> {
         arg.onChunkStatusChange(this.pos, arg2);
      }, executor);
      this.field_26930 = completableFuture2;
      completableFuture.thenAccept((either) -> {
         either.ifLeft((arg) -> {
            completableFuture2.complete((Object)null);
         });
      });
   }

   private void method_31408(ThreadedAnvilChunkStorage arg, LevelType arg2) {
      this.field_26930.cancel(false);
      arg.onChunkStatusChange(this.pos, arg2);
   }

   protected void tick(ThreadedAnvilChunkStorage chunkStorage, Executor executor) {
      ChunkStatus lv = getTargetStatusForLevel(this.lastTickLevel);
      ChunkStatus lv2 = getTargetStatusForLevel(this.level);
      boolean bl = this.lastTickLevel <= ThreadedAnvilChunkStorage.MAX_LEVEL;
      boolean bl2 = this.level <= ThreadedAnvilChunkStorage.MAX_LEVEL;
      LevelType lv3 = getLevelType(this.lastTickLevel);
      LevelType lv4 = getLevelType(this.level);
      if (bl) {
         Either either = Either.right(new Unloaded() {
            public String toString() {
               return "Unloaded ticket level " + ChunkHolder.this.pos;
            }
         });

         for(int i = bl2 ? lv2.getIndex() + 1 : 0; i <= lv.getIndex(); ++i) {
            CompletableFuture completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
            if (completableFuture == null) {
               this.futuresByStatus.set(i, CompletableFuture.completedFuture(either));
            }
         }
      }

      boolean bl3 = lv3.isAfter(ChunkHolder.LevelType.BORDER);
      boolean bl4 = lv4.isAfter(ChunkHolder.LevelType.BORDER);
      this.accessible |= bl4;
      if (!bl3 && bl4) {
         this.accessibleFuture = chunkStorage.makeChunkAccessible(this);
         this.method_31409(chunkStorage, this.accessibleFuture, executor, ChunkHolder.LevelType.BORDER);
         this.combineSavingFuture(this.accessibleFuture, "full");
      }

      if (bl3 && !bl4) {
         this.accessibleFuture.complete(UNLOADED_WORLD_CHUNK);
         this.accessibleFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      }

      boolean bl5 = lv3.isAfter(ChunkHolder.LevelType.TICKING);
      boolean bl6 = lv4.isAfter(ChunkHolder.LevelType.TICKING);
      if (!bl5 && bl6) {
         this.tickingFuture = chunkStorage.makeChunkTickable(this);
         this.method_31409(chunkStorage, this.tickingFuture, executor, ChunkHolder.LevelType.TICKING);
         this.combineSavingFuture(this.tickingFuture, "ticking");
      }

      if (bl5 && !bl6) {
         this.tickingFuture.complete(UNLOADED_WORLD_CHUNK);
         this.tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      }

      boolean bl7 = lv3.isAfter(ChunkHolder.LevelType.ENTITY_TICKING);
      boolean bl8 = lv4.isAfter(ChunkHolder.LevelType.ENTITY_TICKING);
      if (!bl7 && bl8) {
         if (this.entityTickingFuture != UNLOADED_WORLD_CHUNK_FUTURE) {
            throw (IllegalStateException)Util.throwOrPause(new IllegalStateException());
         }

         this.entityTickingFuture = chunkStorage.makeChunkEntitiesTickable(this.pos);
         this.method_31409(chunkStorage, this.entityTickingFuture, executor, ChunkHolder.LevelType.ENTITY_TICKING);
         this.combineSavingFuture(this.entityTickingFuture, "entity ticking");
      }

      if (bl7 && !bl8) {
         this.entityTickingFuture.complete(UNLOADED_WORLD_CHUNK);
         this.entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      }

      if (!lv4.isAfter(lv3)) {
         this.method_31408(chunkStorage, lv4);
      }

      this.levelUpdateListener.updateLevel(this.pos, this::getCompletedLevel, this.level, this::setCompletedLevel);
      this.lastTickLevel = this.level;
   }

   public static ChunkStatus getTargetStatusForLevel(int level) {
      return level < 33 ? ChunkStatus.FULL : ChunkStatus.byDistanceFromFull(level - 33);
   }

   public static LevelType getLevelType(int distance) {
      return LEVEL_TYPES[MathHelper.clamp(33 - distance + 1, 0, LEVEL_TYPES.length - 1)];
   }

   public boolean isAccessible() {
      return this.accessible;
   }

   public void updateAccessibleStatus() {
      this.accessible = getLevelType(this.level).isAfter(ChunkHolder.LevelType.BORDER);
   }

   public void setCompletedChunk(ReadOnlyChunk chunk) {
      for(int i = 0; i < this.futuresByStatus.length(); ++i) {
         CompletableFuture completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
         if (completableFuture != null) {
            Optional optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
            if (!optional.isEmpty() && optional.get() instanceof ProtoChunk) {
               this.futuresByStatus.set(i, CompletableFuture.completedFuture(Either.left(chunk)));
            }
         }
      }

      this.combineSavingFuture(CompletableFuture.completedFuture(Either.left(chunk.getWrappedChunk())), "replaceProto");
   }

   public List collectFuturesByStatus() {
      List list = new ArrayList();

      for(int i = 0; i < CHUNK_STATUSES.size(); ++i) {
         list.add(Pair.of((ChunkStatus)CHUNK_STATUSES.get(i), (CompletableFuture)this.futuresByStatus.get(i)));
      }

      return list;
   }

   static {
      UNLOADED_CHUNK = Either.right(ChunkHolder.Unloaded.INSTANCE);
      UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
      UNLOADED_WORLD_CHUNK = Either.right(ChunkHolder.Unloaded.INSTANCE);
      field_36388 = Either.right(ChunkHolder.Unloaded.INSTANCE);
      UNLOADED_WORLD_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_WORLD_CHUNK);
      CHUNK_STATUSES = ChunkStatus.createOrderedList();
      LEVEL_TYPES = ChunkHolder.LevelType.values();
   }

   @FunctionalInterface
   public interface LevelUpdateListener {
      void updateLevel(ChunkPos pos, IntSupplier levelGetter, int targetLevel, IntConsumer levelSetter);
   }

   public interface PlayersWatchingChunkProvider {
      List getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);
   }

   static final class MultithreadAction {
      private final Thread thread;
      private final CompletableFuture action;
      private final String actionDesc;

      MultithreadAction(Thread thread, CompletableFuture action, String actionDesc) {
         this.thread = thread;
         this.action = action;
         this.actionDesc = actionDesc;
      }
   }

   public static enum LevelType {
      INACCESSIBLE,
      BORDER,
      TICKING,
      ENTITY_TICKING;

      public boolean isAfter(LevelType levelType) {
         return this.ordinal() >= levelType.ordinal();
      }

      // $FF: synthetic method
      private static LevelType[] method_36576() {
         return new LevelType[]{INACCESSIBLE, BORDER, TICKING, ENTITY_TICKING};
      }
   }

   public interface Unloaded {
      Unloaded INSTANCE = new Unloaded() {
         public String toString() {
            return "UNLOADED";
         }
      };
   }
}
