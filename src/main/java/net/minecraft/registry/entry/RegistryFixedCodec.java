package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;

public final class RegistryFixedCodec implements Codec {
   private final RegistryKey registry;

   public static RegistryFixedCodec of(RegistryKey registry) {
      return new RegistryFixedCodec(registry);
   }

   private RegistryFixedCodec(RegistryKey registry) {
      this.registry = registry;
   }

   public DataResult encode(RegistryEntry arg, DynamicOps dynamicOps, Object object) {
      if (dynamicOps instanceof RegistryOps lv) {
         Optional optional = lv.getOwner(this.registry);
         if (optional.isPresent()) {
            if (!arg.ownerEquals((RegistryEntryOwner)optional.get())) {
               return DataResult.error(() -> {
                  return "Element " + arg + " is not valid in current registry set";
               });
            }

            return (DataResult)arg.getKeyOrValue().map((registryKey) -> {
               return Identifier.CODEC.encode(registryKey.getValue(), dynamicOps, object);
            }, (value) -> {
               return DataResult.error(() -> {
                  return "Elements from registry " + this.registry + " can't be serialized to a value";
               });
            });
         }
      }

      return DataResult.error(() -> {
         return "Can't access registry " + this.registry;
      });
   }

   public DataResult decode(DynamicOps ops, Object input) {
      if (ops instanceof RegistryOps lv) {
         Optional optional = lv.getEntryLookup(this.registry);
         if (optional.isPresent()) {
            return Identifier.CODEC.decode(ops, input).flatMap((pair) -> {
               Identifier lv = (Identifier)pair.getFirst();
               return ((DataResult)((RegistryEntryLookup)optional.get()).getOptional(RegistryKey.of(this.registry, lv)).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error(() -> {
                     return "Failed to get element " + lv;
                  });
               })).map((arg) -> {
                  return Pair.of(arg, pair.getSecond());
               }).setLifecycle(Lifecycle.stable());
            });
         }
      }

      return DataResult.error(() -> {
         return "Can't access registry " + this.registry;
      });
   }

   public String toString() {
      return "RegistryFixedCodec[" + this.registry + "]";
   }

   // $FF: synthetic method
   public DataResult encode(Object entry, DynamicOps ops, Object prefix) {
      return this.encode((RegistryEntry)entry, ops, prefix);
   }
}
