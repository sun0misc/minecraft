/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.tick;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.SerializableTickScheduler;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.Nullable;

public class ChunkTickScheduler<T>
implements SerializableTickScheduler<T>,
BasicTickScheduler<T> {
    private final Queue<OrderedTick<T>> tickQueue = new PriorityQueue(OrderedTick.TRIGGER_TICK_COMPARATOR);
    @Nullable
    private List<Tick<T>> ticks;
    private final Set<OrderedTick<?>> queuedTicks = new ObjectOpenCustomHashSet(OrderedTick.HASH_STRATEGY);
    @Nullable
    private BiConsumer<ChunkTickScheduler<T>, OrderedTick<T>> tickConsumer;

    public ChunkTickScheduler() {
    }

    public ChunkTickScheduler(List<Tick<T>> ticks) {
        this.ticks = ticks;
        for (Tick<T> lv : ticks) {
            this.queuedTicks.add(OrderedTick.create(lv.type(), lv.pos()));
        }
    }

    public void setTickConsumer(@Nullable BiConsumer<ChunkTickScheduler<T>, OrderedTick<T>> tickConsumer) {
        this.tickConsumer = tickConsumer;
    }

    @Nullable
    public OrderedTick<T> peekNextTick() {
        return this.tickQueue.peek();
    }

    @Nullable
    public OrderedTick<T> pollNextTick() {
        OrderedTick<T> lv = this.tickQueue.poll();
        if (lv != null) {
            this.queuedTicks.remove(lv);
        }
        return lv;
    }

    @Override
    public void scheduleTick(OrderedTick<T> orderedTick) {
        if (this.queuedTicks.add(orderedTick)) {
            this.queueTick(orderedTick);
        }
    }

    private void queueTick(OrderedTick<T> orderedTick) {
        this.tickQueue.add(orderedTick);
        if (this.tickConsumer != null) {
            this.tickConsumer.accept(this, orderedTick);
        }
    }

    @Override
    public boolean isQueued(BlockPos pos, T type) {
        return this.queuedTicks.contains(OrderedTick.create(type, pos));
    }

    public void removeTicksIf(Predicate<OrderedTick<T>> predicate) {
        Iterator iterator = this.tickQueue.iterator();
        while (iterator.hasNext()) {
            OrderedTick lv = (OrderedTick)iterator.next();
            if (!predicate.test(lv)) continue;
            iterator.remove();
            this.queuedTicks.remove(lv);
        }
    }

    public Stream<OrderedTick<T>> getQueueAsStream() {
        return this.tickQueue.stream();
    }

    @Override
    public int getTickCount() {
        return this.tickQueue.size() + (this.ticks != null ? this.ticks.size() : 0);
    }

    @Override
    public NbtList toNbt(long l, Function<T, String> function) {
        NbtList lv = new NbtList();
        if (this.ticks != null) {
            for (Tick<Object> tick : this.ticks) {
                lv.add(tick.toNbt(function));
            }
        }
        for (OrderedTick orderedTick : this.tickQueue) {
            lv.add(Tick.orderedTickToNbt(orderedTick, function, l));
        }
        return lv;
    }

    public void disable(long time) {
        if (this.ticks != null) {
            int i = -this.ticks.size();
            for (Tick<T> lv : this.ticks) {
                this.queueTick(lv.createOrderedTick(time, i++));
            }
        }
        this.ticks = null;
    }

    public static <T> ChunkTickScheduler<T> create(NbtList tickQueue, Function<String, Optional<T>> nameToTypeFunction, ChunkPos pos) {
        ImmutableList.Builder builder = ImmutableList.builder();
        Tick.tick(tickQueue, nameToTypeFunction, pos, builder::add);
        return new ChunkTickScheduler<T>(builder.build());
    }

    @Override
    public /* synthetic */ NbtElement toNbt(long time, Function typeToNameFunction) {
        return this.toNbt(time, typeToNameFunction);
    }
}

