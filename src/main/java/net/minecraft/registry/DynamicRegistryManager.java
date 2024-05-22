/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public interface DynamicRegistryManager
extends RegistryWrapper.WrapperLookup {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Immutable EMPTY = new ImmutableImpl(Map.of()).toImmutable();

    public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> var1);

    @Override
    default public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
        return this.getOptional(registryRef).map(Registry::getReadOnlyWrapper);
    }

    default public <E> Registry<E> get(RegistryKey<? extends Registry<? extends E>> key) {
        return this.getOptional(key).orElseThrow(() -> new IllegalStateException("Missing registry: " + String.valueOf(key)));
    }

    public Stream<Entry<?>> streamAllRegistries();

    @Override
    default public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
        return this.streamAllRegistries().map(Entry::key);
    }

    public static Immutable of(final Registry<? extends Registry<?>> registries) {
        return new Immutable(){

            public <T> Optional<Registry<T>> getOptional(RegistryKey<? extends Registry<? extends T>> key) {
                Registry lv = registries;
                return lv.getOrEmpty(key);
            }

            @Override
            public Stream<Entry<?>> streamAllRegistries() {
                return registries.getEntrySet().stream().map(Entry::of);
            }

            @Override
            public Immutable toImmutable() {
                return this;
            }
        };
    }

    default public Immutable toImmutable() {
        class Immutablized
        extends ImmutableImpl
        implements Immutable {
            protected Immutablized(DynamicRegistryManager arg, Stream<Entry<?>> entryStream) {
                super(entryStream);
            }
        }
        return new Immutablized(this, this.streamAllRegistries().map(Entry::freeze));
    }

    default public Lifecycle getRegistryLifecycle() {
        return this.streamAllRegistries().map(entry -> entry.value.getLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
    }

    public record Entry<T>(RegistryKey<? extends Registry<T>> key, Registry<T> value) {
        private static <T, R extends Registry<? extends T>> Entry<T> of(Map.Entry<? extends RegistryKey<? extends Registry<?>>, R> entry) {
            return Entry.of(entry.getKey(), (Registry)entry.getValue());
        }

        private static <T> Entry<T> of(RegistryKey<? extends Registry<?>> key, Registry<?> value) {
            return new Entry(key, value);
        }

        private Entry<T> freeze() {
            return new Entry<T>(this.key, this.value.freeze());
        }
    }

    public static class ImmutableImpl
    implements DynamicRegistryManager {
        private final Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableImpl(List<? extends Registry<?>> registries) {
            this.registries = registries.stream().collect(Collectors.toUnmodifiableMap(Registry::getKey, registry -> registry));
        }

        public ImmutableImpl(Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> registries) {
            this.registries = Map.copyOf(registries);
        }

        public ImmutableImpl(Stream<Entry<?>> entryStream) {
            this.registries = entryStream.collect(ImmutableMap.toImmutableMap(Entry::key, Entry::value));
        }

        @Override
        public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> key) {
            return Optional.ofNullable(this.registries.get(key)).map(registry -> registry);
        }

        @Override
        public Stream<Entry<?>> streamAllRegistries() {
            return this.registries.entrySet().stream().map(Entry::of);
        }
    }

    public static interface Immutable
    extends DynamicRegistryManager {
    }
}

