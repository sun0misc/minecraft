package net.minecraft.registry;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;

public interface RegistryWrapper extends RegistryEntryLookup {
   Stream streamEntries();

   default Stream streamKeys() {
      return this.streamEntries().map(RegistryEntry.Reference::registryKey);
   }

   Stream streamTags();

   default Stream streamTagKeys() {
      return this.streamTags().map(RegistryEntryList.Named::getTag);
   }

   default RegistryWrapper filter(final Predicate filter) {
      return new Delegating(this) {
         public Optional getOptional(RegistryKey key) {
            return this.baseWrapper.getOptional(key).filter((entry) -> {
               return filter.test(entry.value());
            });
         }

         public Stream streamEntries() {
            return this.baseWrapper.streamEntries().filter((entry) -> {
               return filter.test(entry.value());
            });
         }
      };
   }

   public interface WrapperLookup {
      Optional getOptionalWrapper(RegistryKey registryRef);

      default Impl getWrapperOrThrow(RegistryKey registryRef) {
         return (Impl)this.getOptionalWrapper(registryRef).orElseThrow(() -> {
            return new IllegalStateException("Registry " + registryRef.getValue() + " not found");
         });
      }

      default RegistryEntryLookup.RegistryLookup createRegistryLookup() {
         return new RegistryEntryLookup.RegistryLookup() {
            public Optional getOptional(RegistryKey registryRef) {
               return WrapperLookup.this.getOptionalWrapper(registryRef).map((lookup) -> {
                  return lookup;
               });
            }
         };
      }

      static WrapperLookup of(Stream wrappers) {
         final Map map = (Map)wrappers.collect(Collectors.toUnmodifiableMap(Impl::getRegistryKey, (wrapper) -> {
            return wrapper;
         }));
         return new WrapperLookup() {
            public Optional getOptionalWrapper(RegistryKey registryRef) {
               return Optional.ofNullable((Impl)map.get(registryRef));
            }
         };
      }
   }

   public static class Delegating implements RegistryWrapper {
      protected final RegistryWrapper baseWrapper;

      public Delegating(RegistryWrapper baseWrapper) {
         this.baseWrapper = baseWrapper;
      }

      public Optional getOptional(RegistryKey key) {
         return this.baseWrapper.getOptional(key);
      }

      public Stream streamEntries() {
         return this.baseWrapper.streamEntries();
      }

      public Optional getOptional(TagKey tag) {
         return this.baseWrapper.getOptional(tag);
      }

      public Stream streamTags() {
         return this.baseWrapper.streamTags();
      }
   }

   public interface Impl extends RegistryWrapper, RegistryEntryOwner {
      RegistryKey getRegistryKey();

      Lifecycle getLifecycle();

      default RegistryWrapper withFeatureFilter(FeatureSet enabledFeatures) {
         return (RegistryWrapper)(ToggleableFeature.FEATURE_ENABLED_REGISTRY_KEYS.contains(this.getRegistryKey()) ? this.filter((feature) -> {
            return ((ToggleableFeature)feature).isEnabled(enabledFeatures);
         }) : this);
      }

      public abstract static class Delegating implements Impl {
         protected abstract Impl getBase();

         public RegistryKey getRegistryKey() {
            return this.getBase().getRegistryKey();
         }

         public Lifecycle getLifecycle() {
            return this.getBase().getLifecycle();
         }

         public Optional getOptional(RegistryKey key) {
            return this.getBase().getOptional(key);
         }

         public Stream streamEntries() {
            return this.getBase().streamEntries();
         }

         public Optional getOptional(TagKey tag) {
            return this.getBase().getOptional(tag);
         }

         public Stream streamTags() {
            return this.getBase().streamTags();
         }
      }
   }
}
