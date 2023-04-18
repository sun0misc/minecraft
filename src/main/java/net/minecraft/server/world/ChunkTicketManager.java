package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ChunkTicketManager {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_29764 = 2;
   static final int NEARBY_PLAYER_TICKET_LEVEL;
   private static final int field_29765 = 4;
   private static final int field_34884 = 32;
   private static final int field_34885 = 33;
   final Long2ObjectMap playersByChunkPos = new Long2ObjectOpenHashMap();
   final Long2ObjectOpenHashMap ticketsByPosition = new Long2ObjectOpenHashMap();
   private final TicketDistanceLevelPropagator distanceFromTicketTracker = new TicketDistanceLevelPropagator();
   private final DistanceFromNearestPlayerTracker distanceFromNearestPlayerTracker = new DistanceFromNearestPlayerTracker(8);
   private final SimulationDistanceLevelPropagator simulationDistanceTracker = new SimulationDistanceLevelPropagator();
   private final NearbyChunkTicketUpdater nearbyChunkTicketUpdater = new NearbyChunkTicketUpdater(33);
   final Set chunkHolders = Sets.newHashSet();
   final ChunkTaskPrioritySystem levelUpdateListener;
   final MessageListener playerTicketThrottler;
   final MessageListener playerTicketThrottlerUnblocker;
   final LongSet chunkPositions = new LongOpenHashSet();
   final Executor mainThreadExecutor;
   private long age;
   private int simulationDistance = 10;

   protected ChunkTicketManager(Executor workerExecutor, Executor mainThreadExecutor) {
      Objects.requireNonNull(mainThreadExecutor);
      MessageListener lv = MessageListener.create("player ticket throttler", mainThreadExecutor::execute);
      ChunkTaskPrioritySystem lv2 = new ChunkTaskPrioritySystem(ImmutableList.of(lv), workerExecutor, 4);
      this.levelUpdateListener = lv2;
      this.playerTicketThrottler = lv2.createExecutor(lv, true);
      this.playerTicketThrottlerUnblocker = lv2.createUnblockingExecutor(lv);
      this.mainThreadExecutor = mainThreadExecutor;
   }

   protected void purge() {
      ++this.age;
      ObjectIterator objectIterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();

      while(objectIterator.hasNext()) {
         Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
         Iterator iterator = ((SortedArraySet)entry.getValue()).iterator();
         boolean bl = false;

         while(iterator.hasNext()) {
            ChunkTicket lv = (ChunkTicket)iterator.next();
            if (lv.isExpired(this.age)) {
               iterator.remove();
               bl = true;
               this.simulationDistanceTracker.remove(entry.getLongKey(), lv);
            }
         }

         if (bl) {
            this.distanceFromTicketTracker.updateLevel(entry.getLongKey(), getLevel((SortedArraySet)entry.getValue()), false);
         }

         if (((SortedArraySet)entry.getValue()).isEmpty()) {
            objectIterator.remove();
         }
      }

   }

   private static int getLevel(SortedArraySet tickets) {
      return !tickets.isEmpty() ? ((ChunkTicket)tickets.first()).getLevel() : ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
   }

   protected abstract boolean isUnloaded(long pos);

   @Nullable
   protected abstract ChunkHolder getChunkHolder(long pos);

   @Nullable
   protected abstract ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int j);

   public boolean tick(ThreadedAnvilChunkStorage chunkStorage) {
      this.distanceFromNearestPlayerTracker.updateLevels();
      this.simulationDistanceTracker.updateLevels();
      this.nearbyChunkTicketUpdater.updateLevels();
      int i = Integer.MAX_VALUE - this.distanceFromTicketTracker.update(Integer.MAX_VALUE);
      boolean bl = i != 0;
      if (bl) {
      }

      if (!this.chunkHolders.isEmpty()) {
         this.chunkHolders.forEach((holder) -> {
            holder.tick(chunkStorage, this.mainThreadExecutor);
         });
         this.chunkHolders.clear();
         return true;
      } else {
         if (!this.chunkPositions.isEmpty()) {
            LongIterator longIterator = this.chunkPositions.iterator();

            while(longIterator.hasNext()) {
               long l = longIterator.nextLong();
               if (this.getTicketSet(l).stream().anyMatch((ticket) -> {
                  return ticket.getType() == ChunkTicketType.PLAYER;
               })) {
                  ChunkHolder lv = chunkStorage.getCurrentChunkHolder(l);
                  if (lv == null) {
                     throw new IllegalStateException();
                  }

                  CompletableFuture completableFuture = lv.getEntityTickingFuture();
                  completableFuture.thenAccept((either) -> {
                     this.mainThreadExecutor.execute(() -> {
                        this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> {
                        }, l, false));
                     });
                  });
               }
            }

            this.chunkPositions.clear();
         }

         return bl;
      }
   }

   void addTicket(long position, ChunkTicket ticket) {
      SortedArraySet lv = this.getTicketSet(position);
      int i = getLevel(lv);
      ChunkTicket lv2 = (ChunkTicket)lv.addAndGet(ticket);
      lv2.setTickCreated(this.age);
      if (ticket.getLevel() < i) {
         this.distanceFromTicketTracker.updateLevel(position, ticket.getLevel(), true);
      }

   }

   void removeTicket(long pos, ChunkTicket ticket) {
      SortedArraySet lv = this.getTicketSet(pos);
      if (lv.remove(ticket)) {
      }

      if (lv.isEmpty()) {
         this.ticketsByPosition.remove(pos);
      }

      this.distanceFromTicketTracker.updateLevel(pos, getLevel(lv), false);
   }

   public void addTicketWithLevel(ChunkTicketType type, ChunkPos pos, int level, Object argument) {
      this.addTicket(pos.toLong(), new ChunkTicket(type, level, argument));
   }

   public void removeTicketWithLevel(ChunkTicketType type, ChunkPos pos, int level, Object argument) {
      ChunkTicket lv = new ChunkTicket(type, level, argument);
      this.removeTicket(pos.toLong(), lv);
   }

   public void addTicket(ChunkTicketType type, ChunkPos pos, int radius, Object argument) {
      ChunkTicket lv = new ChunkTicket(type, 33 - radius, argument);
      long l = pos.toLong();
      this.addTicket(l, lv);
      this.simulationDistanceTracker.add(l, lv);
   }

   public void removeTicket(ChunkTicketType type, ChunkPos pos, int radius, Object argument) {
      ChunkTicket lv = new ChunkTicket(type, 33 - radius, argument);
      long l = pos.toLong();
      this.removeTicket(l, lv);
      this.simulationDistanceTracker.remove(l, lv);
   }

   private SortedArraySet getTicketSet(long position) {
      return (SortedArraySet)this.ticketsByPosition.computeIfAbsent(position, (pos) -> {
         return SortedArraySet.create(4);
      });
   }

   protected void setChunkForced(ChunkPos pos, boolean forced) {
      ChunkTicket lv = new ChunkTicket(ChunkTicketType.FORCED, 31, pos);
      long l = pos.toLong();
      if (forced) {
         this.addTicket(l, lv);
         this.simulationDistanceTracker.add(l, lv);
      } else {
         this.removeTicket(l, lv);
         this.simulationDistanceTracker.remove(l, lv);
      }

   }

   public void handleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player) {
      ChunkPos lv = pos.toChunkPos();
      long l = lv.toLong();
      ((ObjectSet)this.playersByChunkPos.computeIfAbsent(l, (sectionPos) -> {
         return new ObjectOpenHashSet();
      })).add(player);
      this.distanceFromNearestPlayerTracker.updateLevel(l, 0, true);
      this.nearbyChunkTicketUpdater.updateLevel(l, 0, true);
      this.simulationDistanceTracker.add(ChunkTicketType.PLAYER, lv, this.getPlayerSimulationLevel(), lv);
   }

   public void handleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player) {
      ChunkPos lv = pos.toChunkPos();
      long l = lv.toLong();
      ObjectSet objectSet = (ObjectSet)this.playersByChunkPos.get(l);
      objectSet.remove(player);
      if (objectSet.isEmpty()) {
         this.playersByChunkPos.remove(l);
         this.distanceFromNearestPlayerTracker.updateLevel(l, Integer.MAX_VALUE, false);
         this.nearbyChunkTicketUpdater.updateLevel(l, Integer.MAX_VALUE, false);
         this.simulationDistanceTracker.remove(ChunkTicketType.PLAYER, lv, this.getPlayerSimulationLevel(), lv);
      }

   }

   private int getPlayerSimulationLevel() {
      return Math.max(0, 31 - this.simulationDistance);
   }

   public boolean shouldTickEntities(long chunkPos) {
      return this.simulationDistanceTracker.getLevel(chunkPos) < 32;
   }

   public boolean shouldTickBlocks(long chunkPos) {
      return this.simulationDistanceTracker.getLevel(chunkPos) < 33;
   }

   protected String getTicket(long pos) {
      SortedArraySet lv = (SortedArraySet)this.ticketsByPosition.get(pos);
      return lv != null && !lv.isEmpty() ? ((ChunkTicket)lv.first()).toString() : "no_ticket";
   }

   protected void setWatchDistance(int viewDistance) {
      this.nearbyChunkTicketUpdater.setWatchDistance(viewDistance);
   }

   public void setSimulationDistance(int simulationDistance) {
      if (simulationDistance != this.simulationDistance) {
         this.simulationDistance = simulationDistance;
         this.simulationDistanceTracker.updatePlayerTickets(this.getPlayerSimulationLevel());
      }

   }

   public int getTickedChunkCount() {
      this.distanceFromNearestPlayerTracker.updateLevels();
      return this.distanceFromNearestPlayerTracker.distanceFromNearestPlayer.size();
   }

   public boolean shouldTick(long chunkPos) {
      this.distanceFromNearestPlayerTracker.updateLevels();
      return this.distanceFromNearestPlayerTracker.distanceFromNearestPlayer.containsKey(chunkPos);
   }

   public String toDumpString() {
      return this.levelUpdateListener.getDebugString();
   }

   private void dump(String path) {
      try {
         FileOutputStream fileOutputStream = new FileOutputStream(new File(path));

         try {
            ObjectIterator var3 = this.ticketsByPosition.long2ObjectEntrySet().iterator();

            while(var3.hasNext()) {
               Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)var3.next();
               ChunkPos lv = new ChunkPos(entry.getLongKey());
               Iterator var6 = ((SortedArraySet)entry.getValue()).iterator();

               while(var6.hasNext()) {
                  ChunkTicket lv2 = (ChunkTicket)var6.next();
                  int var10001 = lv.x;
                  fileOutputStream.write(("" + var10001 + "\t" + lv.z + "\t" + lv2.getType() + "\t" + lv2.getLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
               }
            }
         } catch (Throwable var9) {
            try {
               fileOutputStream.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         fileOutputStream.close();
      } catch (IOException var10) {
         LOGGER.error("Failed to dump tickets to {}", path, var10);
      }

   }

   @VisibleForTesting
   SimulationDistanceLevelPropagator getSimulationDistanceTracker() {
      return this.simulationDistanceTracker;
   }

   public void removePersistentTickets() {
      ImmutableSet immutableSet = ImmutableSet.of(ChunkTicketType.UNKNOWN, ChunkTicketType.POST_TELEPORT, ChunkTicketType.LIGHT);
      ObjectIterator objectIterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();

      while(objectIterator.hasNext()) {
         Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
         Iterator iterator = ((SortedArraySet)entry.getValue()).iterator();
         boolean bl = false;

         while(iterator.hasNext()) {
            ChunkTicket lv = (ChunkTicket)iterator.next();
            if (!immutableSet.contains(lv.getType())) {
               iterator.remove();
               bl = true;
               this.simulationDistanceTracker.remove(entry.getLongKey(), lv);
            }
         }

         if (bl) {
            this.distanceFromTicketTracker.updateLevel(entry.getLongKey(), getLevel((SortedArraySet)entry.getValue()), false);
         }

         if (((SortedArraySet)entry.getValue()).isEmpty()) {
            objectIterator.remove();
         }
      }

   }

   public boolean shouldDelayShutdown() {
      return !this.ticketsByPosition.isEmpty();
   }

   static {
      NEARBY_PLAYER_TICKET_LEVEL = 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.FULL) - 2;
   }

   class TicketDistanceLevelPropagator extends ChunkPosDistanceLevelPropagator {
      public TicketDistanceLevelPropagator() {
         super(ThreadedAnvilChunkStorage.MAX_LEVEL + 2, 16, 256);
      }

      protected int getInitialLevel(long id) {
         SortedArraySet lv = (SortedArraySet)ChunkTicketManager.this.ticketsByPosition.get(id);
         if (lv == null) {
            return Integer.MAX_VALUE;
         } else {
            return lv.isEmpty() ? Integer.MAX_VALUE : ((ChunkTicket)lv.first()).getLevel();
         }
      }

      protected int getLevel(long id) {
         if (!ChunkTicketManager.this.isUnloaded(id)) {
            ChunkHolder lv = ChunkTicketManager.this.getChunkHolder(id);
            if (lv != null) {
               return lv.getLevel();
            }
         }

         return ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
      }

      protected void setLevel(long id, int level) {
         ChunkHolder lv = ChunkTicketManager.this.getChunkHolder(id);
         int j = lv == null ? ThreadedAnvilChunkStorage.MAX_LEVEL + 1 : lv.getLevel();
         if (j != level) {
            lv = ChunkTicketManager.this.setLevel(id, level, lv, j);
            if (lv != null) {
               ChunkTicketManager.this.chunkHolders.add(lv);
            }

         }
      }

      public int update(int distance) {
         return this.applyPendingUpdates(distance);
      }
   }

   private class DistanceFromNearestPlayerTracker extends ChunkPosDistanceLevelPropagator {
      protected final Long2ByteMap distanceFromNearestPlayer = new Long2ByteOpenHashMap();
      protected final int maxDistance;

      protected DistanceFromNearestPlayerTracker(int maxDistance) {
         super(maxDistance + 2, 16, 256);
         this.maxDistance = maxDistance;
         this.distanceFromNearestPlayer.defaultReturnValue((byte)(maxDistance + 2));
      }

      protected int getLevel(long id) {
         return this.distanceFromNearestPlayer.get(id);
      }

      protected void setLevel(long id, int level) {
         byte b;
         if (level > this.maxDistance) {
            b = this.distanceFromNearestPlayer.remove(id);
         } else {
            b = this.distanceFromNearestPlayer.put(id, (byte)level);
         }

         this.onDistanceChange(id, b, level);
      }

      protected void onDistanceChange(long pos, int oldDistance, int distance) {
      }

      protected int getInitialLevel(long id) {
         return this.isPlayerInChunk(id) ? 0 : Integer.MAX_VALUE;
      }

      private boolean isPlayerInChunk(long chunkPos) {
         ObjectSet objectSet = (ObjectSet)ChunkTicketManager.this.playersByChunkPos.get(chunkPos);
         return objectSet != null && !objectSet.isEmpty();
      }

      public void updateLevels() {
         this.applyPendingUpdates(Integer.MAX_VALUE);
      }

      private void dump(String path) {
         try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));

            try {
               ObjectIterator var3 = this.distanceFromNearestPlayer.long2ByteEntrySet().iterator();

               while(var3.hasNext()) {
                  Long2ByteMap.Entry entry = (Long2ByteMap.Entry)var3.next();
                  ChunkPos lv = new ChunkPos(entry.getLongKey());
                  String string2 = Byte.toString(entry.getByteValue());
                  fileOutputStream.write((lv.x + "\t" + lv.z + "\t" + string2 + "\n").getBytes(StandardCharsets.UTF_8));
               }
            } catch (Throwable var8) {
               try {
                  fileOutputStream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            fileOutputStream.close();
         } catch (IOException var9) {
            ChunkTicketManager.LOGGER.error("Failed to dump chunks to {}", path, var9);
         }

      }
   }

   class NearbyChunkTicketUpdater extends DistanceFromNearestPlayerTracker {
      private int watchDistance = 0;
      private final Long2IntMap distances = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
      private final LongSet positionsAffected = new LongOpenHashSet();

      protected NearbyChunkTicketUpdater(int i) {
         super(i);
         this.distances.defaultReturnValue(i + 2);
      }

      protected void onDistanceChange(long pos, int oldDistance, int distance) {
         this.positionsAffected.add(pos);
      }

      public void setWatchDistance(int watchDistance) {
         ObjectIterator var2 = this.distanceFromNearestPlayer.long2ByteEntrySet().iterator();

         while(var2.hasNext()) {
            Long2ByteMap.Entry entry = (Long2ByteMap.Entry)var2.next();
            byte b = entry.getByteValue();
            long l = entry.getLongKey();
            this.updateTicket(l, b, this.isWithinViewDistance(b), b <= watchDistance - 2);
         }

         this.watchDistance = watchDistance;
      }

      private void updateTicket(long pos, int distance, boolean oldWithinViewDistance, boolean withinViewDistance) {
         if (oldWithinViewDistance != withinViewDistance) {
            ChunkTicket lv = new ChunkTicket(ChunkTicketType.PLAYER, ChunkTicketManager.NEARBY_PLAYER_TICKET_LEVEL, new ChunkPos(pos));
            if (withinViewDistance) {
               ChunkTicketManager.this.playerTicketThrottler.send(ChunkTaskPrioritySystem.createMessage(() -> {
                  ChunkTicketManager.this.mainThreadExecutor.execute(() -> {
                     if (this.isWithinViewDistance(this.getLevel(pos))) {
                        ChunkTicketManager.this.addTicket(pos, lv);
                        ChunkTicketManager.this.chunkPositions.add(pos);
                     } else {
                        ChunkTicketManager.this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> {
                        }, pos, false));
                     }

                  });
               }, pos, () -> {
                  return distance;
               }));
            } else {
               ChunkTicketManager.this.playerTicketThrottlerUnblocker.send(ChunkTaskPrioritySystem.createUnblockingMessage(() -> {
                  ChunkTicketManager.this.mainThreadExecutor.execute(() -> {
                     ChunkTicketManager.this.removeTicket(pos, lv);
                  });
               }, pos, true));
            }
         }

      }

      public void updateLevels() {
         super.updateLevels();
         if (!this.positionsAffected.isEmpty()) {
            LongIterator longIterator = this.positionsAffected.iterator();

            while(longIterator.hasNext()) {
               long l = longIterator.nextLong();
               int i = this.distances.get(l);
               int j = this.getLevel(l);
               if (i != j) {
                  ChunkTicketManager.this.levelUpdateListener.updateLevel(new ChunkPos(l), () -> {
                     return this.distances.get(l);
                  }, j, (level) -> {
                     if (level >= this.distances.defaultReturnValue()) {
                        this.distances.remove(l);
                     } else {
                        this.distances.put(l, level);
                     }

                  });
                  this.updateTicket(l, j, this.isWithinViewDistance(i), this.isWithinViewDistance(j));
               }
            }

            this.positionsAffected.clear();
         }

      }

      private boolean isWithinViewDistance(int distance) {
         return distance <= this.watchDistance - 2;
      }
   }
}
