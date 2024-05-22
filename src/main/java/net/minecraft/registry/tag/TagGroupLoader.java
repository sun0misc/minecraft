/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.registry.tag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TagGroupLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Function<Identifier, Optional<? extends T>> registryGetter;
    private final String dataType;

    public TagGroupLoader(Function<Identifier, Optional<? extends T>> registryGetter, String dataType) {
        this.registryGetter = registryGetter;
        this.dataType = dataType;
    }

    public Map<Identifier, List<TrackedEntry>> loadTags(ResourceManager resourceManager) {
        HashMap<Identifier, List<TrackedEntry>> map = Maps.newHashMap();
        ResourceFinder lv = ResourceFinder.json(this.dataType);
        for (Map.Entry<Identifier, List<Resource>> entry2 : lv.findAllResources(resourceManager).entrySet()) {
            Identifier lv2 = entry2.getKey();
            Identifier lv3 = lv.toResourceId(lv2);
            for (Resource lv4 : entry2.getValue()) {
                try {
                    BufferedReader reader = lv4.getReader();
                    try {
                        JsonElement jsonElement = JsonParser.parseReader(reader);
                        List list = map.computeIfAbsent(lv3, id -> new ArrayList());
                        TagFile lv5 = (TagFile)TagFile.CODEC.parse(new Dynamic<JsonElement>(JsonOps.INSTANCE, jsonElement)).getOrThrow();
                        if (lv5.replace()) {
                            list.clear();
                        }
                        String string = lv4.getPackId();
                        lv5.entries().forEach(entry -> list.add(new TrackedEntry((TagEntry)entry, string)));
                    } finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                } catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", lv3, lv2, lv4.getPackId(), exception);
                }
            }
        }
        return map;
    }

    private Either<Collection<TrackedEntry>, Collection<T>> resolveAll(TagEntry.ValueGetter<T> valueGetter, List<TrackedEntry> entries) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        ArrayList<TrackedEntry> list2 = new ArrayList<TrackedEntry>();
        for (TrackedEntry lv : entries) {
            if (lv.entry().resolve(valueGetter, builder::add)) continue;
            list2.add(lv);
        }
        return list2.isEmpty() ? Either.right(builder.build()) : Either.left(list2);
    }

    public Map<Identifier, Collection<T>> buildGroup(Map<Identifier, List<TrackedEntry>> tags) {
        final HashMap map2 = Maps.newHashMap();
        TagEntry.ValueGetter lv = new TagEntry.ValueGetter<T>(){

            @Override
            @Nullable
            public T direct(Identifier id) {
                return TagGroupLoader.this.registryGetter.apply(id).orElse(null);
            }

            @Override
            @Nullable
            public Collection<T> tag(Identifier id) {
                return (Collection)map2.get(id);
            }
        };
        DependencyTracker<Identifier, TagDependencies> lv2 = new DependencyTracker<Identifier, TagDependencies>();
        tags.forEach((id, entries) -> lv2.add((Identifier)id, new TagDependencies((List<TrackedEntry>)entries)));
        lv2.traverse((id, dependencies) -> this.resolveAll(lv, dependencies.entries).ifLeft(missingReferences -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", id, (Object)missingReferences.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(resolvedEntries -> map2.put((Identifier)id, (Collection)resolvedEntries)));
        return map2;
    }

    public Map<Identifier, Collection<T>> load(ResourceManager manager) {
        return this.buildGroup(this.loadTags(manager));
    }

    public record TrackedEntry(TagEntry entry, String source) {
        @Override
        public String toString() {
            return String.valueOf(this.entry) + " (from " + this.source + ")";
        }
    }

    record TagDependencies(List<TrackedEntry> entries) implements DependencyTracker.Dependencies<Identifier>
    {
        @Override
        public void forDependencies(Consumer<Identifier> callback) {
            this.entries.forEach(entry -> entry.entry.forEachRequiredTagId(callback));
        }

        @Override
        public void forOptionalDependencies(Consumer<Identifier> callback) {
            this.entries.forEach(entry -> entry.entry.forEachOptionalTagId(callback));
        }
    }
}

