package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

public interface DynamicRegistryManager extends RegistryWrapper.WrapperLookup {
   Logger LOGGER = LogUtils.getLogger();
   Immutable EMPTY = (new ImmutableImpl(Map.of())).toImmutable();

   Optional getOptional(RegistryKey key);

   default Optional getOptionalWrapper(RegistryKey registryRef) {
      return this.getOptional(registryRef).map(Registry::getReadOnlyWrapper);
   }

   default Registry get(RegistryKey key) {
      return (Registry)this.getOptional(key).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + key);
      });
   }

   Stream streamAllRegistries();

   static Immutable of(final Registry registries) {
      return new Immutable() {
         public Optional getOptional(RegistryKey key) {
            Registry lv = registries;
            return lv.getOrEmpty(key);
         }

         public Stream streamAllRegistries() {
            return registries.getEntrySet().stream().map(Entry::of);
         }

         public Immutable toImmutable() {
            return this;
         }
      };
   }

   default Immutable toImmutable() {
      class Immutablized extends ImmutableImpl implements Immutable {
         protected Immutablized(Stream entryStream) {
            super(entryStream);
         }
      }

      return new Immutablized(this.streamAllRegistries().map(Entry::freeze));
   }

   default Lifecycle getRegistryLifecycle() {
      return (Lifecycle)this.streamAllRegistries().map((entry) -> {
         return entry.value.getLifecycle();
      }).reduce(Lifecycle.stable(), Lifecycle::add);
   }

   public static record Entry(RegistryKey key, Registry value) {
      final Registry value;

      public Entry(RegistryKey arg, Registry arg2) {
         this.key = arg;
         this.value = arg2;
      }

      private static Entry of(Map.Entry entry) {
         return of((RegistryKey)entry.getKey(), (Registry)entry.getValue());
      }

      private static Entry of(RegistryKey key, Registry value) {
         return new Entry(key, value);
      }

      private Entry freeze() {
         return new Entry(this.key, this.value.freeze());
      }

      public RegistryKey key() {
         return this.key;
      }

      public Registry value() {
         return this.value;
      }
   }

   public static class ImmutableImpl implements DynamicRegistryManager {
      private final Map registries;

      public ImmutableImpl(List registries) {
         this.registries = (Map)registries.stream().collect(Collectors.toUnmodifiableMap(Registry::getKey, (registry) -> {
            return registry;
         }));
      }

      public ImmutableImpl(Map registries) {
         this.registries = Map.copyOf(registries);
      }

      public ImmutableImpl(Stream entryStream) {
         this.registries = (Map)entryStream.collect(ImmutableMap.toImmutableMap(Entry::key, Entry::value));
      }

      public Optional getOptional(RegistryKey key) {
         return Optional.ofNullable((Registry)this.registries.get(key)).map((registry) -> {
            return registry;
         });
      }

      public Stream streamAllRegistries() {
         return this.registries.entrySet().stream().map(Entry::of);
      }
   }

   public interface Immutable extends DynamicRegistryManager {
   }
}
