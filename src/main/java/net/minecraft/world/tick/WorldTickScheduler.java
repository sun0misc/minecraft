package net.minecraft.world.tick;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;

public class WorldTickScheduler implements QueryableTickScheduler {
   private static final Comparator COMPARATOR = (a, b) -> {
      return OrderedTick.BASIC_COMPARATOR.compare(a.peekNextTick(), b.peekNextTick());
   };
   private final LongPredicate tickingFutureReadyPredicate;
   private final Supplier profilerGetter;
   private final Long2ObjectMap chunkTickSchedulers = new Long2ObjectOpenHashMap();
   private final Long2LongMap nextTriggerTickByChunkPos = (Long2LongMap)Util.make(new Long2LongOpenHashMap(), (map) -> {
      map.defaultReturnValue(Long.MAX_VALUE);
   });
   private final Queue tickableChunkTickSchedulers;
   private final Queue tickableTicks;
   private final List tickedTicks;
   private final Set copiedTickableTicksList;
   private final BiConsumer queuedTickConsumer;

   public WorldTickScheduler(LongPredicate tickingFutureReadyPredicate, Supplier profilerGetter) {
      this.tickableChunkTickSchedulers = new PriorityQueue(COMPARATOR);
      this.tickableTicks = new ArrayDeque();
      this.tickedTicks = new ArrayList();
      this.copiedTickableTicksList = new ObjectOpenCustomHashSet(OrderedTick.HASH_STRATEGY);
      this.queuedTickConsumer = (chunkTickScheduler, tick) -> {
         if (tick.equals(chunkTickScheduler.peekNextTick())) {
            this.schedule(tick);
         }

      };
      this.tickingFutureReadyPredicate = tickingFutureReadyPredicate;
      this.profilerGetter = profilerGetter;
   }

   public void addChunkTickScheduler(ChunkPos pos, ChunkTickScheduler scheduler) {
      long l = pos.toLong();
      this.chunkTickSchedulers.put(l, scheduler);
      OrderedTick lv = scheduler.peekNextTick();
      if (lv != null) {
         this.nextTriggerTickByChunkPos.put(l, lv.triggerTick());
      }

      scheduler.setTickConsumer(this.queuedTickConsumer);
   }

   public void removeChunkTickScheduler(ChunkPos pos) {
      long l = pos.toLong();
      ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.remove(l);
      this.nextTriggerTickByChunkPos.remove(l);
      if (lv != null) {
         lv.setTickConsumer((BiConsumer)null);
      }

   }

   public void scheduleTick(OrderedTick orderedTick) {
      long l = ChunkPos.toLong(orderedTick.pos());
      ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(l);
      if (lv == null) {
         Util.throwOrPause(new IllegalStateException("Trying to schedule tick in not loaded position " + orderedTick.pos()));
      } else {
         lv.scheduleTick(orderedTick);
      }
   }

   public void tick(long time, int maxTicks, BiConsumer ticker) {
      Profiler lv = (Profiler)this.profilerGetter.get();
      lv.push("collect");
      this.collectTickableTicks(time, maxTicks, lv);
      lv.swap("run");
      lv.visit("ticksToRun", this.tickableTicks.size());
      this.tick(ticker);
      lv.swap("cleanup");
      this.clear();
      lv.pop();
   }

   private void collectTickableTicks(long time, int maxTicks, Profiler profiler) {
      this.collectTickableChunkTickSchedulers(time);
      profiler.visit("containersToTick", this.tickableChunkTickSchedulers.size());
      this.addTickableTicks(time, maxTicks);
      this.delayAllTicks();
   }

   private void collectTickableChunkTickSchedulers(long time) {
      ObjectIterator objectIterator = Long2LongMaps.fastIterator(this.nextTriggerTickByChunkPos);

      while(objectIterator.hasNext()) {
         Long2LongMap.Entry entry = (Long2LongMap.Entry)objectIterator.next();
         long m = entry.getLongKey();
         long n = entry.getLongValue();
         if (n <= time) {
            ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(m);
            if (lv == null) {
               objectIterator.remove();
            } else {
               OrderedTick lv2 = lv.peekNextTick();
               if (lv2 == null) {
                  objectIterator.remove();
               } else if (lv2.triggerTick() > time) {
                  entry.setValue(lv2.triggerTick());
               } else if (this.tickingFutureReadyPredicate.test(m)) {
                  objectIterator.remove();
                  this.tickableChunkTickSchedulers.add(lv);
               }
            }
         }
      }

   }

   private void addTickableTicks(long time, int maxTicks) {
      ChunkTickScheduler lv;
      while(this.isTickableTicksCountUnder(maxTicks) && (lv = (ChunkTickScheduler)this.tickableChunkTickSchedulers.poll()) != null) {
         OrderedTick lv2 = lv.pollNextTick();
         this.addTickableTick(lv2);
         this.addTickableTicks(this.tickableChunkTickSchedulers, lv, time, maxTicks);
         OrderedTick lv3 = lv.peekNextTick();
         if (lv3 != null) {
            if (lv3.triggerTick() <= time && this.isTickableTicksCountUnder(maxTicks)) {
               this.tickableChunkTickSchedulers.add(lv);
            } else {
               this.schedule(lv3);
            }
         }
      }

   }

   private void delayAllTicks() {
      Iterator var1 = this.tickableChunkTickSchedulers.iterator();

      while(var1.hasNext()) {
         ChunkTickScheduler lv = (ChunkTickScheduler)var1.next();
         this.schedule(lv.peekNextTick());
      }

   }

   private void schedule(OrderedTick tick) {
      this.nextTriggerTickByChunkPos.put(ChunkPos.toLong(tick.pos()), tick.triggerTick());
   }

   private void addTickableTicks(Queue tickableChunkTickSchedulers, ChunkTickScheduler chunkTickScheduler, long tick, int maxTicks) {
      if (this.isTickableTicksCountUnder(maxTicks)) {
         ChunkTickScheduler lv = (ChunkTickScheduler)tickableChunkTickSchedulers.peek();
         OrderedTick lv2 = lv != null ? lv.peekNextTick() : null;

         while(this.isTickableTicksCountUnder(maxTicks)) {
            OrderedTick lv3 = chunkTickScheduler.peekNextTick();
            if (lv3 == null || lv3.triggerTick() > tick || lv2 != null && OrderedTick.BASIC_COMPARATOR.compare(lv3, lv2) > 0) {
               break;
            }

            chunkTickScheduler.pollNextTick();
            this.addTickableTick(lv3);
         }

      }
   }

   private void addTickableTick(OrderedTick tick) {
      this.tickableTicks.add(tick);
   }

   private boolean isTickableTicksCountUnder(int maxTicks) {
      return this.tickableTicks.size() < maxTicks;
   }

   private void tick(BiConsumer ticker) {
      while(!this.tickableTicks.isEmpty()) {
         OrderedTick lv = (OrderedTick)this.tickableTicks.poll();
         if (!this.copiedTickableTicksList.isEmpty()) {
            this.copiedTickableTicksList.remove(lv);
         }

         this.tickedTicks.add(lv);
         ticker.accept(lv.pos(), lv.type());
      }

   }

   private void clear() {
      this.tickableTicks.clear();
      this.tickableChunkTickSchedulers.clear();
      this.tickedTicks.clear();
      this.copiedTickableTicksList.clear();
   }

   public boolean isQueued(BlockPos pos, Object type) {
      ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(ChunkPos.toLong(pos));
      return lv != null && lv.isQueued(pos, type);
   }

   public boolean isTicking(BlockPos pos, Object type) {
      this.copyTickableTicksList();
      return this.copiedTickableTicksList.contains(OrderedTick.create(type, pos));
   }

   private void copyTickableTicksList() {
      if (this.copiedTickableTicksList.isEmpty() && !this.tickableTicks.isEmpty()) {
         this.copiedTickableTicksList.addAll(this.tickableTicks);
      }

   }

   private void visitChunks(BlockBox box, ChunkVisitor visitor) {
      int i = ChunkSectionPos.getSectionCoord((double)box.getMinX());
      int j = ChunkSectionPos.getSectionCoord((double)box.getMinZ());
      int k = ChunkSectionPos.getSectionCoord((double)box.getMaxX());
      int l = ChunkSectionPos.getSectionCoord((double)box.getMaxZ());

      for(int m = i; m <= k; ++m) {
         for(int n = j; n <= l; ++n) {
            long o = ChunkPos.toLong(m, n);
            ChunkTickScheduler lv = (ChunkTickScheduler)this.chunkTickSchedulers.get(o);
            if (lv != null) {
               visitor.accept(o, lv);
            }
         }
      }

   }

   public void clearNextTicks(BlockBox box) {
      Predicate predicate = (tick) -> {
         return box.contains(tick.pos());
      };
      this.visitChunks(box, (chunkPos, chunkTickScheduler) -> {
         OrderedTick lv = chunkTickScheduler.peekNextTick();
         chunkTickScheduler.removeTicksIf(predicate);
         OrderedTick lv2 = chunkTickScheduler.peekNextTick();
         if (lv2 != lv) {
            if (lv2 != null) {
               this.schedule(lv2);
            } else {
               this.nextTriggerTickByChunkPos.remove(chunkPos);
            }
         }

      });
      this.tickedTicks.removeIf(predicate);
      this.tickableTicks.removeIf(predicate);
   }

   public void scheduleTicks(BlockBox box, Vec3i offset) {
      this.scheduleTicks(this, box, offset);
   }

   public void scheduleTicks(WorldTickScheduler scheduler, BlockBox box, Vec3i offset) {
      List list = new ArrayList();
      Predicate predicate = (tick) -> {
         return box.contains(tick.pos());
      };
      Stream var10000 = scheduler.tickedTicks.stream().filter(predicate);
      Objects.requireNonNull(list);
      var10000.forEach(list::add);
      var10000 = scheduler.tickableTicks.stream().filter(predicate);
      Objects.requireNonNull(list);
      var10000.forEach(list::add);
      scheduler.visitChunks(box, (chunkPos, chunkTickScheduler) -> {
         Stream var10000 = chunkTickScheduler.getQueueAsStream().filter(predicate);
         Objects.requireNonNull(list);
         var10000.forEach(list::add);
      });
      LongSummaryStatistics longSummaryStatistics = list.stream().mapToLong(OrderedTick::subTickOrder).summaryStatistics();
      long l = longSummaryStatistics.getMin();
      long m = longSummaryStatistics.getMax();
      list.forEach((tick) -> {
         this.scheduleTick(new OrderedTick(tick.type(), tick.pos().add(offset), tick.triggerTick(), tick.priority(), tick.subTickOrder() - l + m + 1L));
      });
   }

   public int getTickCount() {
      return this.chunkTickSchedulers.values().stream().mapToInt(TickScheduler::getTickCount).sum();
   }

   @FunctionalInterface
   interface ChunkVisitor {
      void accept(long chunkPos, ChunkTickScheduler chunkTickScheduler);
   }
}
