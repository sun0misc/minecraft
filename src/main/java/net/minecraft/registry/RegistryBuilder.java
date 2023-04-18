package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;

public class RegistryBuilder {
   private final List registries = new ArrayList();

   static RegistryEntryLookup toLookup(final RegistryWrapper.Impl wrapper) {
      return new EntryListCreatingLookup(wrapper) {
         public Optional getOptional(RegistryKey key) {
            return wrapper.getOptional(key);
         }
      };
   }

   public RegistryBuilder addRegistry(RegistryKey registryRef, Lifecycle lifecycle, BootstrapFunction bootstrapFunction) {
      this.registries.add(new RegistryInfo(registryRef, lifecycle, bootstrapFunction));
      return this;
   }

   public RegistryBuilder addRegistry(RegistryKey registryRef, BootstrapFunction bootstrapFunction) {
      return this.addRegistry(registryRef, Lifecycle.stable(), bootstrapFunction);
   }

   private Registries createBootstrappedRegistries(DynamicRegistryManager registryManager) {
      Registries lv = RegistryBuilder.Registries.of(registryManager, this.registries.stream().map(RegistryInfo::key));
      this.registries.forEach((registry) -> {
         registry.runBootstrap(lv);
      });
      return lv;
   }

   public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager baseRegistryManager) {
      Registries lv = this.createBootstrappedRegistries(baseRegistryManager);
      Stream stream = baseRegistryManager.streamAllRegistries().map((entry) -> {
         return entry.value().getReadOnlyWrapper();
      });
      Stream stream2 = this.registries.stream().map((info) -> {
         return info.init(lv).toWrapper();
      });
      Objects.requireNonNull(lv);
      RegistryWrapper.WrapperLookup lv2 = RegistryWrapper.WrapperLookup.of(Stream.concat(stream, stream2.peek(lv::addOwner)));
      lv.validateReferences();
      lv.throwErrors();
      return lv2;
   }

   public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager baseRegistryManager, RegistryWrapper.WrapperLookup wrapperLookup) {
      Registries lv = this.createBootstrappedRegistries(baseRegistryManager);
      Map map = new HashMap();
      lv.streamRegistries().forEach((registry) -> {
         map.put(registry.key, registry);
      });
      this.registries.stream().map((info) -> {
         return info.init(lv);
      }).forEach((registry) -> {
         map.put(registry.key, registry);
      });
      Stream stream = baseRegistryManager.streamAllRegistries().map((entry) -> {
         return entry.value().getReadOnlyWrapper();
      });
      Stream var10001 = map.values().stream().map(InitializedRegistry::toWrapper);
      Objects.requireNonNull(lv);
      RegistryWrapper.WrapperLookup lv2 = RegistryWrapper.WrapperLookup.of(Stream.concat(stream, var10001.peek(lv::addOwner)));
      lv.setReferenceEntryValues(wrapperLookup);
      lv.validateReferences();
      lv.throwErrors();
      return lv2;
   }

   static record RegistryInfo(RegistryKey key, Lifecycle lifecycle, BootstrapFunction bootstrap) {
      RegistryInfo(RegistryKey arg, Lifecycle lifecycle, BootstrapFunction arg2) {
         this.key = arg;
         this.lifecycle = lifecycle;
         this.bootstrap = arg2;
      }

      void runBootstrap(Registries registries) {
         this.bootstrap.run(registries.createRegisterable());
      }

      public InitializedRegistry init(Registries registries) {
         Map map = new HashMap();
         Iterator iterator = registries.registeredValues.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            RegistryKey lv = (RegistryKey)entry.getKey();
            if (lv.isOf(this.key)) {
               RegisteredValue lv3 = (RegisteredValue)entry.getValue();
               RegistryEntry.Reference lv4 = (RegistryEntry.Reference)registries.lookup.keysToEntries.remove(lv);
               map.put(lv, new EntryAssociatedValue(lv3, Optional.ofNullable(lv4)));
               iterator.remove();
            }
         }

         return new InitializedRegistry(this.key, this.lifecycle, map);
      }

      public RegistryKey key() {
         return this.key;
      }

      public Lifecycle lifecycle() {
         return this.lifecycle;
      }

      public BootstrapFunction bootstrap() {
         return this.bootstrap;
      }
   }

   @FunctionalInterface
   public interface BootstrapFunction {
      void run(Registerable registerable);
   }

   static record Registries(AnyOwner owner, StandAloneEntryCreatingLookup lookup, Map registries, Map registeredValues, List errors) {
      final StandAloneEntryCreatingLookup lookup;
      final Map registries;
      final Map registeredValues;
      final List errors;

      private Registries(AnyOwner arg, StandAloneEntryCreatingLookup arg2, Map map, Map map2, List list) {
         this.owner = arg;
         this.lookup = arg2;
         this.registries = map;
         this.registeredValues = map2;
         this.errors = list;
      }

      public static Registries of(DynamicRegistryManager dynamicRegistryManager, Stream registryRefs) {
         AnyOwner lv = new AnyOwner();
         List list = new ArrayList();
         StandAloneEntryCreatingLookup lv2 = new StandAloneEntryCreatingLookup(lv);
         ImmutableMap.Builder builder = ImmutableMap.builder();
         dynamicRegistryManager.streamAllRegistries().forEach((entry) -> {
            builder.put(entry.key().getValue(), RegistryBuilder.toLookup(entry.value().getReadOnlyWrapper()));
         });
         registryRefs.forEach((registryRef) -> {
            builder.put(registryRef.getValue(), lv2);
         });
         return new Registries(lv, lv2, builder.build(), new HashMap(), list);
      }

      public Registerable createRegisterable() {
         return new Registerable() {
            public RegistryEntry.Reference register(RegistryKey key, Object value, Lifecycle lifecycle) {
               RegisteredValue lv = (RegisteredValue)Registries.this.registeredValues.put(key, new RegisteredValue(value, lifecycle));
               if (lv != null) {
                  Registries.this.errors.add(new IllegalStateException("Duplicate registration for " + key + ", new=" + value + ", old=" + lv.value));
               }

               return Registries.this.lookup.getOrCreate(key);
            }

            public RegistryEntryLookup getRegistryLookup(RegistryKey registryRef) {
               return (RegistryEntryLookup)Registries.this.registries.getOrDefault(registryRef.getValue(), Registries.this.lookup);
            }
         };
      }

      public void validateReferences() {
         Iterator var1 = this.lookup.keysToEntries.keySet().iterator();

         while(var1.hasNext()) {
            RegistryKey lv = (RegistryKey)var1.next();
            this.errors.add(new IllegalStateException("Unreferenced key: " + lv));
         }

         this.registeredValues.forEach((key, value) -> {
            this.errors.add(new IllegalStateException("Orpaned value " + value.value + " for key " + key));
         });
      }

      public void throwErrors() {
         if (!this.errors.isEmpty()) {
            IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
            Iterator var2 = this.errors.iterator();

            while(var2.hasNext()) {
               RuntimeException runtimeException = (RuntimeException)var2.next();
               illegalStateException.addSuppressed(runtimeException);
            }

            throw illegalStateException;
         }
      }

      public void addOwner(RegistryEntryOwner owner) {
         this.owner.addOwner(owner);
      }

      public void setReferenceEntryValues(RegistryWrapper.WrapperLookup lookup) {
         Map map = new HashMap();
         Iterator iterator = this.lookup.keysToEntries.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            RegistryKey lv = (RegistryKey)entry.getKey();
            RegistryEntry.Reference lv2 = (RegistryEntry.Reference)entry.getValue();
            ((Optional)map.computeIfAbsent(lv.getRegistry(), (registryId) -> {
               return lookup.getOptionalWrapper(RegistryKey.ofRegistry(registryId));
            })).flatMap((entryLookup) -> {
               return entryLookup.getOptional(lv);
            }).ifPresent((entryx) -> {
               lv2.setValue(entryx.value());
               iterator.remove();
            });
         }

      }

      public Stream streamRegistries() {
         return this.lookup.keysToEntries.keySet().stream().map(RegistryKey::getRegistry).distinct().map((registry) -> {
            return new InitializedRegistry(RegistryKey.ofRegistry(registry), Lifecycle.stable(), Map.of());
         });
      }

      public AnyOwner owner() {
         return this.owner;
      }

      public StandAloneEntryCreatingLookup lookup() {
         return this.lookup;
      }

      public Map registries() {
         return this.registries;
      }

      public Map registeredValues() {
         return this.registeredValues;
      }

      public List errors() {
         return this.errors;
      }
   }

   private static record InitializedRegistry(RegistryKey key, Lifecycle lifecycle, Map values) {
      final RegistryKey key;
      final Lifecycle lifecycle;
      final Map values;

      InitializedRegistry(RegistryKey arg, Lifecycle lifecycle, Map map) {
         this.key = arg;
         this.lifecycle = lifecycle;
         this.values = map;
      }

      public RegistryWrapper.Impl toWrapper() {
         return new RegistryWrapper.Impl() {
            private final Map keysToEntries;

            {
               this.keysToEntries = (Map)InitializedRegistry.this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, (entry) -> {
                  EntryAssociatedValue lv = (EntryAssociatedValue)entry.getValue();
                  RegistryEntry.Reference lv2 = (RegistryEntry.Reference)lv.entry().orElseGet(() -> {
                     return RegistryEntry.Reference.standAlone(this, (RegistryKey)entry.getKey());
                  });
                  lv2.setValue(lv.value().value());
                  return lv2;
               }));
            }

            public RegistryKey getRegistryKey() {
               return InitializedRegistry.this.key;
            }

            public Lifecycle getLifecycle() {
               return InitializedRegistry.this.lifecycle;
            }

            public Optional getOptional(RegistryKey key) {
               return Optional.ofNullable((RegistryEntry.Reference)this.keysToEntries.get(key));
            }

            public Stream streamEntries() {
               return this.keysToEntries.values().stream();
            }

            public Optional getOptional(TagKey tag) {
               return Optional.empty();
            }

            public Stream streamTags() {
               return Stream.empty();
            }
         };
      }

      public RegistryKey key() {
         return this.key;
      }

      public Lifecycle lifecycle() {
         return this.lifecycle;
      }

      public Map values() {
         return this.values;
      }
   }

   private static record EntryAssociatedValue(RegisteredValue value, Optional entry) {
      EntryAssociatedValue(RegisteredValue arg, Optional optional) {
         this.value = arg;
         this.entry = optional;
      }

      public RegisteredValue value() {
         return this.value;
      }

      public Optional entry() {
         return this.entry;
      }
   }

   private static record RegisteredValue(Object value, Lifecycle lifecycle) {
      final Object value;

      RegisteredValue(Object object, Lifecycle lifecycle) {
         this.value = object;
         this.lifecycle = lifecycle;
      }

      public Object value() {
         return this.value;
      }

      public Lifecycle lifecycle() {
         return this.lifecycle;
      }
   }

   private static class StandAloneEntryCreatingLookup extends EntryListCreatingLookup {
      final Map keysToEntries = new HashMap();

      public StandAloneEntryCreatingLookup(RegistryEntryOwner arg) {
         super(arg);
      }

      public Optional getOptional(RegistryKey key) {
         return Optional.of(this.getOrCreate(key));
      }

      RegistryEntry.Reference getOrCreate(RegistryKey key) {
         return (RegistryEntry.Reference)this.keysToEntries.computeIfAbsent(key, (key2) -> {
            return RegistryEntry.Reference.standAlone(this.entryOwner, key2);
         });
      }
   }

   static class AnyOwner implements RegistryEntryOwner {
      private final Set owners = Sets.newIdentityHashSet();

      public boolean ownerEquals(RegistryEntryOwner other) {
         return this.owners.contains(other);
      }

      public void addOwner(RegistryEntryOwner owner) {
         this.owners.add(owner);
      }
   }

   private abstract static class EntryListCreatingLookup implements RegistryEntryLookup {
      protected final RegistryEntryOwner entryOwner;

      protected EntryListCreatingLookup(RegistryEntryOwner entryOwner) {
         this.entryOwner = entryOwner;
      }

      public Optional getOptional(TagKey tag) {
         return Optional.of(RegistryEntryList.of(this.entryOwner, tag));
      }
   }
}
