/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.tag;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

public abstract class ValueLookupTagProvider<T>
extends TagProvider<T> {
    private final Function<T, RegistryKey<T>> valueToKey;

    public ValueLookupTagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture, Function<T, RegistryKey<T>> valueToKey) {
        super(output, registryRef, registryLookupFuture);
        this.valueToKey = valueToKey;
    }

    public ValueLookupTagProvider(DataOutput output, RegistryKey<? extends Registry<T>> registryRef, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture, CompletableFuture<TagProvider.TagLookup<T>> parentTagLookupFuture, Function<T, RegistryKey<T>> valueToKey) {
        super(output, registryRef, registryLookupFuture, parentTagLookupFuture);
        this.valueToKey = valueToKey;
    }

    @Override
    protected ObjectBuilder<T> getOrCreateTagBuilder(TagKey<T> arg) {
        TagBuilder lv = this.getTagBuilder(arg);
        return new ObjectBuilder<T>(lv, this.valueToKey);
    }

    @Override
    protected /* synthetic */ TagProvider.ProvidedTagBuilder getOrCreateTagBuilder(TagKey tag) {
        return this.getOrCreateTagBuilder(tag);
    }

    protected static class ObjectBuilder<T>
    extends TagProvider.ProvidedTagBuilder<T> {
        private final Function<T, RegistryKey<T>> valueToKey;

        ObjectBuilder(TagBuilder builder, Function<T, RegistryKey<T>> valueToKey) {
            super(builder);
            this.valueToKey = valueToKey;
        }

        @Override
        public ObjectBuilder<T> addTag(TagKey<T> arg) {
            super.addTag(arg);
            return this;
        }

        public final ObjectBuilder<T> add(T value) {
            ((TagProvider.ProvidedTagBuilder)this).add(this.valueToKey.apply(value));
            return this;
        }

        @SafeVarargs
        public final ObjectBuilder<T> add(T ... values) {
            Stream.of(values).map(this.valueToKey).forEach(this::add);
            return this;
        }

        @Override
        public /* synthetic */ TagProvider.ProvidedTagBuilder addTag(TagKey identifiedTag) {
            return this.addTag(identifiedTag);
        }
    }
}

