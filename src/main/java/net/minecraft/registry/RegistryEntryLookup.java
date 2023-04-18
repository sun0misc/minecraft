package net.minecraft.registry;

import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

public interface RegistryEntryLookup {
   Optional getOptional(RegistryKey key);

   default RegistryEntry.Reference getOrThrow(RegistryKey key) {
      return (RegistryEntry.Reference)this.getOptional(key).orElseThrow(() -> {
         return new IllegalStateException("Missing element " + key);
      });
   }

   Optional getOptional(TagKey tag);

   default RegistryEntryList.Named getOrThrow(TagKey tag) {
      return (RegistryEntryList.Named)this.getOptional(tag).orElseThrow(() -> {
         return new IllegalStateException("Missing tag " + tag);
      });
   }

   public interface RegistryLookup {
      Optional getOptional(RegistryKey registryRef);

      default RegistryEntryLookup getOrThrow(RegistryKey registryRef) {
         return (RegistryEntryLookup)this.getOptional(registryRef).orElseThrow(() -> {
            return new IllegalStateException("Registry " + registryRef.getValue() + " not found");
         });
      }
   }
}
