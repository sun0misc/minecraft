/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.metadata;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resource.metadata.BlockEntry;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public class ResourceFilter {
    private static final Codec<ResourceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.list(BlockEntry.CODEC).fieldOf("block")).forGetter(filter -> filter.blocks)).apply((Applicative<ResourceFilter, ?>)instance, ResourceFilter::new));
    public static final ResourceMetadataSerializer<ResourceFilter> SERIALIZER = ResourceMetadataSerializer.fromCodec("filter", CODEC);
    private final List<BlockEntry> blocks;

    public ResourceFilter(List<BlockEntry> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public boolean isNamespaceBlocked(String namespace) {
        return this.blocks.stream().anyMatch(block -> block.getNamespacePredicate().test(namespace));
    }

    public boolean isPathBlocked(String namespace) {
        return this.blocks.stream().anyMatch(block -> block.getPathPredicate().test(namespace));
    }
}

