/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.data.server.tag.ValueLookupTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class ItemTagProvider
extends ValueLookupTagProvider<Item> {
    private final CompletableFuture<TagProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> blockTagsToCopy = new HashMap<TagKey<Block>, TagKey<Item>>();

    public ItemTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture, CompletableFuture<TagProvider.TagLookup<Block>> blockTagLookupFuture) {
        super(output, RegistryKeys.ITEM, registryLookupFuture, (T item) -> item.getRegistryEntry().registryKey());
        this.blockTags = blockTagLookupFuture;
    }

    public ItemTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture, CompletableFuture<TagProvider.TagLookup<Item>> parentTagLookupFuture, CompletableFuture<TagProvider.TagLookup<Block>> blockTagLookupFuture) {
        super(output, RegistryKeys.ITEM, registryLookupFuture, parentTagLookupFuture, item -> item.getRegistryEntry().registryKey());
        this.blockTags = blockTagLookupFuture;
    }

    protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        this.blockTagsToCopy.put(blockTag, itemTag);
    }

    @Override
    protected CompletableFuture<RegistryWrapper.WrapperLookup> getRegistryLookupFuture() {
        return super.getRegistryLookupFuture().thenCombineAsync(this.blockTags, (lookup, blockTags) -> {
            this.blockTagsToCopy.forEach((blockTag, itemTag) -> {
                TagBuilder lv = this.getTagBuilder(itemTag);
                Optional optional = (Optional)blockTags.apply(blockTag);
                ((TagBuilder)optional.orElseThrow(() -> new IllegalStateException("Missing block tag " + String.valueOf(itemTag.id())))).build().forEach(lv::add);
            });
            return lookup;
        });
    }
}

