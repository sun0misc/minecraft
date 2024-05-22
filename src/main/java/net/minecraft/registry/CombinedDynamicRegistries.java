/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;

public class CombinedDynamicRegistries<T> {
    private final List<T> types;
    private final List<DynamicRegistryManager.Immutable> registryManagers;
    private final DynamicRegistryManager.Immutable combinedRegistryManager;

    public CombinedDynamicRegistries(List<T> types) {
        this(types, Util.make(() -> {
            Object[] lvs = new DynamicRegistryManager.Immutable[types.size()];
            Arrays.fill(lvs, DynamicRegistryManager.EMPTY);
            return Arrays.asList(lvs);
        }));
    }

    private CombinedDynamicRegistries(List<T> types, List<DynamicRegistryManager.Immutable> registryManagers) {
        this.types = List.copyOf(types);
        this.registryManagers = List.copyOf(registryManagers);
        this.combinedRegistryManager = new DynamicRegistryManager.ImmutableImpl(CombinedDynamicRegistries.toRegistryMap(registryManagers.stream())).toImmutable();
    }

    private int getIndex(T type) {
        int i = this.types.indexOf(type);
        if (i == -1) {
            throw new IllegalStateException("Can't find " + String.valueOf(type) + " inside " + String.valueOf(this.types));
        }
        return i;
    }

    public DynamicRegistryManager.Immutable get(T index) {
        int i = this.getIndex(index);
        return this.registryManagers.get(i);
    }

    public DynamicRegistryManager.Immutable getPrecedingRegistryManagers(T type) {
        int i = this.getIndex(type);
        return this.subset(0, i);
    }

    public DynamicRegistryManager.Immutable getSucceedingRegistryManagers(T type) {
        int i = this.getIndex(type);
        return this.subset(i, this.registryManagers.size());
    }

    private DynamicRegistryManager.Immutable subset(int startIndex, int endIndex) {
        return new DynamicRegistryManager.ImmutableImpl(CombinedDynamicRegistries.toRegistryMap(this.registryManagers.subList(startIndex, endIndex).stream())).toImmutable();
    }

    public CombinedDynamicRegistries<T> with(T type, DynamicRegistryManager.Immutable ... registryManagers) {
        return this.with(type, Arrays.asList(registryManagers));
    }

    public CombinedDynamicRegistries<T> with(T type, List<DynamicRegistryManager.Immutable> registryManagers) {
        int i = this.getIndex(type);
        if (registryManagers.size() > this.registryManagers.size() - i) {
            throw new IllegalStateException("Too many values to replace");
        }
        ArrayList<DynamicRegistryManager.Immutable> list2 = new ArrayList<DynamicRegistryManager.Immutable>();
        for (int j = 0; j < i; ++j) {
            list2.add(this.registryManagers.get(j));
        }
        list2.addAll(registryManagers);
        while (list2.size() < this.registryManagers.size()) {
            list2.add(DynamicRegistryManager.EMPTY);
        }
        return new CombinedDynamicRegistries<T>(this.types, list2);
    }

    public DynamicRegistryManager.Immutable getCombinedRegistryManager() {
        return this.combinedRegistryManager;
    }

    private static Map<RegistryKey<? extends Registry<?>>, Registry<?>> toRegistryMap(Stream<? extends DynamicRegistryManager> registryManagers) {
        HashMap map = new HashMap();
        registryManagers.forEach(registryManager -> registryManager.streamAllRegistries().forEach(entry -> {
            if (map.put(entry.key(), entry.value()) != null) {
                throw new IllegalStateException("Duplicated registry " + String.valueOf(entry.key()));
            }
        }));
        return map;
    }
}

