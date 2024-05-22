/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class Pool<E extends Weighted> {
    private final int totalWeight;
    private final ImmutableList<E> entries;

    Pool(List<? extends E> entries) {
        this.entries = ImmutableList.copyOf(entries);
        this.totalWeight = Weighting.getWeightSum(entries);
    }

    public static <E extends Weighted> Pool<E> empty() {
        return new Pool(ImmutableList.of());
    }

    @SafeVarargs
    public static <E extends Weighted> Pool<E> of(E ... entries) {
        return new Pool<E>(ImmutableList.copyOf(entries));
    }

    public static <E extends Weighted> Pool<E> of(List<E> entries) {
        return new Pool<E>(entries);
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<E> getOrEmpty(Random random) {
        if (this.totalWeight == 0) {
            return Optional.empty();
        }
        int i = random.nextInt(this.totalWeight);
        return Weighting.getAt(this.entries, i);
    }

    public List<E> getEntries() {
        return this.entries;
    }

    public static <E extends Weighted> Codec<Pool<E>> createCodec(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(Pool::of, Pool::getEntries);
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pool lv = (Pool)o;
        return this.totalWeight == lv.totalWeight && Objects.equals(this.entries, lv.entries);
    }

    public int hashCode() {
        return Objects.hash(this.totalWeight, this.entries);
    }
}

