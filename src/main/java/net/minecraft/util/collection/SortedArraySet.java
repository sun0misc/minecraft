/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

public class SortedArraySet<T>
extends AbstractSet<T> {
    private static final int DEFAULT_CAPACITY = 10;
    private final Comparator<T> comparator;
    T[] elements;
    int size;

    private SortedArraySet(int initialCapacity, Comparator<T> comparator) {
        this.comparator = comparator;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity (" + initialCapacity + ") is negative");
        }
        this.elements = SortedArraySet.cast(new Object[initialCapacity]);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create() {
        return SortedArraySet.create(10);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create(int initialCapacity) {
        return new SortedArraySet(initialCapacity, Comparator.naturalOrder());
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator) {
        return SortedArraySet.create(comparator, 10);
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator, int initialCapacity) {
        return new SortedArraySet<T>(initialCapacity, comparator);
    }

    private static <T> T[] cast(Object[] array) {
        return array;
    }

    private int binarySearch(T object) {
        return Arrays.binarySearch(this.elements, 0, this.size, object, this.comparator);
    }

    private static int insertionPoint(int binarySearchResult) {
        return -binarySearchResult - 1;
    }

    @Override
    public boolean add(T object) {
        int i = this.binarySearch(object);
        if (i >= 0) {
            return false;
        }
        int j = SortedArraySet.insertionPoint(i);
        this.add(object, j);
        return true;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= this.elements.length) {
            return;
        }
        if (this.elements != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            minCapacity = (int)Math.max(Math.min((long)this.elements.length + (long)(this.elements.length >> 1), 0x7FFFFFF7L), (long)minCapacity);
        } else if (minCapacity < 10) {
            minCapacity = 10;
        }
        Object[] objects = new Object[minCapacity];
        System.arraycopy(this.elements, 0, objects, 0, this.size);
        this.elements = SortedArraySet.cast(objects);
    }

    private void add(T object, int index) {
        this.ensureCapacity(this.size + 1);
        if (index != this.size) {
            System.arraycopy(this.elements, index, this.elements, index + 1, this.size - index);
        }
        this.elements[index] = object;
        ++this.size;
    }

    void remove(int index) {
        --this.size;
        if (index != this.size) {
            System.arraycopy(this.elements, index + 1, this.elements, index, this.size - index);
        }
        this.elements[this.size] = null;
    }

    private T get(int index) {
        return this.elements[index];
    }

    public T addAndGet(T object) {
        int i = this.binarySearch(object);
        if (i >= 0) {
            return this.get(i);
        }
        this.add(object, SortedArraySet.insertionPoint(i));
        return object;
    }

    @Override
    public boolean remove(Object object) {
        int i = this.binarySearch(object);
        if (i >= 0) {
            this.remove(i);
            return true;
        }
        return false;
    }

    @Nullable
    public T getIfContains(T object) {
        int i = this.binarySearch(object);
        if (i >= 0) {
            return this.get(i);
        }
        return null;
    }

    public T first() {
        return this.get(0);
    }

    public T last() {
        return this.get(this.size - 1);
    }

    @Override
    public boolean contains(Object object) {
        int i = this.binarySearch(object);
        return i >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new SetIterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.elements, this.size, Object[].class);
    }

    @Override
    public <U> U[] toArray(U[] array) {
        if (array.length < this.size) {
            return Arrays.copyOf(this.elements, this.size, array.getClass());
        }
        System.arraycopy(this.elements, 0, array, 0, this.size);
        if (array.length > this.size) {
            array[this.size] = null;
        }
        return array;
    }

    @Override
    public void clear() {
        Arrays.fill(this.elements, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SortedArraySet) {
            SortedArraySet lv = (SortedArraySet)o;
            if (this.comparator.equals(lv.comparator)) {
                return this.size == lv.size && Arrays.equals(this.elements, lv.elements);
            }
        }
        return super.equals(o);
    }

    class SetIterator
    implements Iterator<T> {
        private int nextIndex;
        private int lastIndex = -1;

        SetIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.nextIndex < SortedArraySet.this.size;
        }

        @Override
        public T next() {
            if (this.nextIndex >= SortedArraySet.this.size) {
                throw new NoSuchElementException();
            }
            this.lastIndex = this.nextIndex++;
            return SortedArraySet.this.elements[this.lastIndex];
        }

        @Override
        public void remove() {
            if (this.lastIndex == -1) {
                throw new IllegalStateException();
            }
            SortedArraySet.this.remove(this.lastIndex);
            --this.nextIndex;
            this.lastIndex = -1;
        }
    }
}

