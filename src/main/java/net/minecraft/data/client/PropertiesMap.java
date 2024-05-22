/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.client;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.state.property.Property;

public final class PropertiesMap {
    private static final PropertiesMap EMPTY = new PropertiesMap(ImmutableList.of());
    private static final Comparator<Property.Value<?>> COMPARATOR = Comparator.comparing(value -> value.property().getName());
    private final List<Property.Value<?>> values;

    public PropertiesMap withValue(Property.Value<?> value) {
        return new PropertiesMap((List<Property.Value<?>>)((Object)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().addAll(this.values)).add(value)).build()));
    }

    public PropertiesMap copyOf(PropertiesMap propertiesMap) {
        return new PropertiesMap((List<Property.Value<?>>)((Object)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().addAll(this.values)).addAll(propertiesMap.values)).build()));
    }

    private PropertiesMap(List<Property.Value<?>> values) {
        this.values = values;
    }

    public static PropertiesMap empty() {
        return EMPTY;
    }

    public static PropertiesMap withValues(Property.Value<?> ... values) {
        return new PropertiesMap(ImmutableList.copyOf(values));
    }

    public boolean equals(Object o) {
        return this == o || o instanceof PropertiesMap && this.values.equals(((PropertiesMap)o).values);
    }

    public int hashCode() {
        return this.values.hashCode();
    }

    public String asString() {
        return this.values.stream().sorted(COMPARATOR).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    public String toString() {
        return this.asString();
    }
}

