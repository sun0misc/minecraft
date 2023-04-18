package net.minecraft.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.entry.RegistryEntry;

public interface MutableRegistry extends Registry {
   RegistryEntry set(int rawId, RegistryKey key, Object value, Lifecycle lifecycle);

   RegistryEntry.Reference add(RegistryKey key, Object entry, Lifecycle lifecycle);

   boolean isEmpty();

   RegistryEntryLookup createMutableEntryLookup();
}
