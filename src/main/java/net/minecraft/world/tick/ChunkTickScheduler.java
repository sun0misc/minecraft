package net.minecraft.world.tick;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class ChunkTickScheduler implements SerializableTickScheduler, BasicTickScheduler {
   private final Queue tickQueue;
   @Nullable
   private List ticks;
   private final Set queuedTicks;
   @Nullable
   private BiConsumer tickConsumer;

   public ChunkTickScheduler() {
      this.tickQueue = new PriorityQueue(OrderedTick.TRIGGER_TICK_COMPARATOR);
      this.queuedTicks = new ObjectOpenCustomHashSet(OrderedTick.HASH_STRATEGY);
   }

   public ChunkTickScheduler(List ticks) {
      this.tickQueue = new PriorityQueue(OrderedTick.TRIGGER_TICK_COMPARATOR);
      this.queuedTicks = new ObjectOpenCustomHashSet(OrderedTick.HASH_STRATEGY);
      this.ticks = ticks;
      Iterator var2 = ticks.iterator();

      while(var2.hasNext()) {
         Tick lv = (Tick)var2.next();
         this.queuedTicks.add(OrderedTick.create(lv.type(), lv.pos()));
      }

   }

   public void setTickConsumer(@Nullable BiConsumer tickConsumer) {
      this.tickConsumer = tickConsumer;
   }

   @Nullable
   public OrderedTick peekNextTick() {
      return (OrderedTick)this.tickQueue.peek();
   }

   @Nullable
   public OrderedTick pollNextTick() {
      OrderedTick lv = (OrderedTick)this.tickQueue.poll();
      if (lv != null) {
         this.queuedTicks.remove(lv);
      }

      return lv;
   }

   public void scheduleTick(OrderedTick orderedTick) {
      if (this.queuedTicks.add(orderedTick)) {
         this.queueTick(orderedTick);
      }

   }

   private void queueTick(OrderedTick orderedTick) {
      this.tickQueue.add(orderedTick);
      if (this.tickConsumer != null) {
         this.tickConsumer.accept(this, orderedTick);
      }

   }

   public boolean isQueued(BlockPos pos, Object type) {
      return this.queuedTicks.contains(OrderedTick.create(type, pos));
   }

   public void removeTicksIf(Predicate predicate) {
      Iterator iterator = this.tickQueue.iterator();

      while(iterator.hasNext()) {
         OrderedTick lv = (OrderedTick)iterator.next();
         if (predicate.test(lv)) {
            iterator.remove();
            this.queuedTicks.remove(lv);
         }
      }

   }

   public Stream getQueueAsStream() {
      return this.tickQueue.stream();
   }

   public int getTickCount() {
      return this.tickQueue.size() + (this.ticks != null ? this.ticks.size() : 0);
   }

   public NbtList toNbt(long l, Function function) {
      NbtList lv = new NbtList();
      Iterator var5;
      if (this.ticks != null) {
         var5 = this.ticks.iterator();

         while(var5.hasNext()) {
            Tick lv2 = (Tick)var5.next();
            lv.add(lv2.toNbt(function));
         }
      }

      var5 = this.tickQueue.iterator();

      while(var5.hasNext()) {
         OrderedTick lv3 = (OrderedTick)var5.next();
         lv.add(Tick.orderedTickToNbt(lv3, function, l));
      }

      return lv;
   }

   public void disable(long time) {
      if (this.ticks != null) {
         int i = -this.ticks.size();
         Iterator var4 = this.ticks.iterator();

         while(var4.hasNext()) {
            Tick lv = (Tick)var4.next();
            this.queueTick(lv.createOrderedTick(time, (long)(i++)));
         }
      }

      this.ticks = null;
   }

   public static ChunkTickScheduler create(NbtList tickQueue, Function nameToTypeFunction, ChunkPos pos) {
      ImmutableList.Builder builder = ImmutableList.builder();
      Objects.requireNonNull(builder);
      Tick.tick(tickQueue, nameToTypeFunction, pos, builder::add);
      return new ChunkTickScheduler(builder.build());
   }

   // $FF: synthetic method
   public NbtElement toNbt(long time, Function typeToNameFunction) {
      return this.toNbt(time, typeToNameFunction);
   }
}
