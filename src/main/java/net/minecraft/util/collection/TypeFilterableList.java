/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Util;

public class TypeFilterableList<T>
extends AbstractCollection<T> {
    private final Map<Class<?>, List<T>> elementsByType = Maps.newHashMap();
    private final Class<T> elementType;
    private final List<T> allElements = Lists.newArrayList();

    public TypeFilterableList(Class<T> elementType) {
        this.elementType = elementType;
        this.elementsByType.put(elementType, this.allElements);
    }

    @Override
    public boolean add(T e) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.elementsByType.entrySet()) {
            if (!entry.getKey().isInstance(e)) continue;
            bl |= entry.getValue().add(e);
        }
        return bl;
    }

    @Override
    public boolean remove(Object o) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.elementsByType.entrySet()) {
            if (!entry.getKey().isInstance(o)) continue;
            List<T> list = entry.getValue();
            bl |= list.remove(o);
        }
        return bl;
    }

    @Override
    public boolean contains(Object o) {
        return this.getAllOfType(o.getClass()).contains(o);
    }

    public <S> Collection<S> getAllOfType(Class<S> type) {
        if (!this.elementType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Don't know how to search for " + String.valueOf(type));
        }
        List list = this.elementsByType.computeIfAbsent(type, typeClass -> this.allElements.stream().filter(typeClass::isInstance).collect(Util.toArrayList()));
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Iterator<T> iterator() {
        if (this.allElements.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Iterators.unmodifiableIterator(this.allElements.iterator());
    }

    public List<T> copy() {
        return ImmutableList.copyOf(this.allElements);
    }

    @Override
    public int size() {
        return this.allElements.size();
    }
}

