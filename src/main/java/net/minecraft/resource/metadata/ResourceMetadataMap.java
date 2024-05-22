/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.metadata;

import java.util.Map;
import net.minecraft.resource.metadata.ResourceMetadataReader;

public class ResourceMetadataMap {
    private static final ResourceMetadataMap EMPTY = new ResourceMetadataMap(Map.of());
    private final Map<ResourceMetadataReader<?>, ?> values;

    private ResourceMetadataMap(Map<ResourceMetadataReader<?>, ?> values) {
        this.values = values;
    }

    public <T> T get(ResourceMetadataReader<T> reader) {
        return (T)this.values.get(reader);
    }

    public static ResourceMetadataMap of() {
        return EMPTY;
    }

    public static <T> ResourceMetadataMap of(ResourceMetadataReader<T> reader, T value) {
        return new ResourceMetadataMap(Map.of(reader, value));
    }

    public static <T1, T2> ResourceMetadataMap of(ResourceMetadataReader<T1> reader, T1 value, ResourceMetadataReader<T2> reader2, T2 value2) {
        return new ResourceMetadataMap(Map.of(reader, value, reader2, value2));
    }
}

