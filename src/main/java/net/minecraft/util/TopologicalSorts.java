/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class TopologicalSorts {
    private TopologicalSorts() {
    }

    public static <T> boolean sort(Map<T, Set<T>> successors, Set<T> visited, Set<T> visiting, Consumer<T> reversedOrderConsumer, T now) {
        if (visited.contains(now)) {
            return false;
        }
        if (visiting.contains(now)) {
            return true;
        }
        visiting.add(now);
        for (Object object2 : (Set)successors.getOrDefault(now, ImmutableSet.of())) {
            if (!TopologicalSorts.sort(successors, visited, visiting, reversedOrderConsumer, object2)) continue;
            return true;
        }
        visiting.remove(now);
        visited.add(now);
        reversedOrderConsumer.accept(now);
        return false;
    }
}

