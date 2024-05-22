/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.Nullable;

public class IdList<T>
implements IndexedIterable<T> {
    private int nextId;
    private final Reference2IntMap<T> idMap;
    private final List<T> list;

    public IdList() {
        this(512);
    }

    public IdList(int initialSize) {
        this.list = Lists.newArrayListWithExpectedSize(initialSize);
        this.idMap = new Reference2IntOpenHashMap<T>(initialSize);
        this.idMap.defaultReturnValue(-1);
    }

    public void set(T value, int id) {
        this.idMap.put(value, id);
        while (this.list.size() <= id) {
            this.list.add(null);
        }
        this.list.set(id, value);
        if (this.nextId <= id) {
            this.nextId = id + 1;
        }
    }

    public void add(T value) {
        this.set(value, this.nextId);
    }

    @Override
    public int getRawId(T value) {
        return this.idMap.getInt(value);
    }

    @Override
    @Nullable
    public final T get(int index) {
        if (index >= 0 && index < this.list.size()) {
            return this.list.get(index);
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.list.iterator(), Objects::nonNull);
    }

    public boolean containsKey(int index) {
        return this.get(index) != null;
    }

    @Override
    public int size() {
        return this.idMap.size();
    }
}

