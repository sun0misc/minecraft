/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.timer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.timer.TimerCallback;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.slf4j.Logger;

public class Timer<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CALLBACK_KEY = "Callback";
    private static final String NAME_KEY = "Name";
    private static final String TRIGGER_TIME_KEY = "TriggerTime";
    private final TimerCallbackSerializer<T> callback;
    private final Queue<Event<T>> events = new PriorityQueue<Event<T>>(Timer.createEventComparator());
    private UnsignedLong eventCounter = UnsignedLong.ZERO;
    private final Table<String, Long, Event<T>> eventsByName = HashBasedTable.create();

    private static <T> Comparator<Event<T>> createEventComparator() {
        return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.id);
    }

    public Timer(TimerCallbackSerializer<T> timerCallbackSerializer, Stream<? extends Dynamic<?>> nbts) {
        this(timerCallbackSerializer);
        this.events.clear();
        this.eventsByName.clear();
        this.eventCounter = UnsignedLong.ZERO;
        nbts.forEach(nbt -> {
            NbtElement lv = nbt.convert(NbtOps.INSTANCE).getValue();
            if (lv instanceof NbtCompound) {
                NbtCompound lv2 = (NbtCompound)lv;
                this.addEvent(lv2);
            } else {
                LOGGER.warn("Invalid format of events: {}", (Object)lv);
            }
        });
    }

    public Timer(TimerCallbackSerializer<T> timerCallbackSerializer) {
        this.callback = timerCallbackSerializer;
    }

    public void processEvents(T server, long time) {
        Event<T> lv;
        while ((lv = this.events.peek()) != null && lv.triggerTime <= time) {
            this.events.remove();
            this.eventsByName.remove(lv.name, time);
            lv.callback.call(server, this, time);
        }
    }

    public void setEvent(String name, long triggerTime, TimerCallback<T> callback) {
        if (this.eventsByName.contains(name, triggerTime)) {
            return;
        }
        this.eventCounter = this.eventCounter.plus(UnsignedLong.ONE);
        Event<T> lv = new Event<T>(triggerTime, this.eventCounter, name, callback);
        this.eventsByName.put(name, triggerTime, lv);
        this.events.add(lv);
    }

    public int remove(String name) {
        Collection<Event<Event>> collection = this.eventsByName.row(name).values();
        collection.forEach(this.events::remove);
        int i = collection.size();
        collection.clear();
        return i;
    }

    public Set<String> getEventNames() {
        return Collections.unmodifiableSet(this.eventsByName.rowKeySet());
    }

    private void addEvent(NbtCompound nbt) {
        NbtCompound lv = nbt.getCompound(CALLBACK_KEY);
        TimerCallback<T> lv2 = this.callback.deserialize(lv);
        if (lv2 != null) {
            String string = nbt.getString(NAME_KEY);
            long l = nbt.getLong(TRIGGER_TIME_KEY);
            this.setEvent(string, l, lv2);
        }
    }

    private NbtCompound serialize(Event<T> event) {
        NbtCompound lv = new NbtCompound();
        lv.putString(NAME_KEY, event.name);
        lv.putLong(TRIGGER_TIME_KEY, event.triggerTime);
        lv.put(CALLBACK_KEY, this.callback.serialize(event.callback));
        return lv;
    }

    public NbtList toNbt() {
        NbtList lv = new NbtList();
        this.events.stream().sorted(Timer.createEventComparator()).map(this::serialize).forEach(lv::add);
        return lv;
    }

    public static class Event<T> {
        public final long triggerTime;
        public final UnsignedLong id;
        public final String name;
        public final TimerCallback<T> callback;

        Event(long triggerTime, UnsignedLong id, String name, TimerCallback<T> callback) {
            this.triggerTime = triggerTime;
            this.id = id;
            this.name = name;
            this.callback = callback;
        }
    }
}

