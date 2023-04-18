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

public final class RegistryElementCodec implements Codec {
   private final RegistryKey registryRef;
   private final Codec elementCodec;
   private final boolean allowInlineDefinitions;

   public static RegistryElementCodec of(RegistryKey registryRef, Codec elementCodec) {
      return of(registryRef, elementCodec, true);
   }

   public static RegistryElementCodec of(RegistryKey registryRef, Codec elementCodec, boolean allowInlineDefinitions) {
      return new RegistryElementCodec(registryRef, elementCodec, allowInlineDefinitions);
   }

   private RegistryElementCodec(RegistryKey registryRef, Codec elementCodec, boolean allowInlineDefinitions) {
      this.registryRef = registryRef;
      this.elementCodec = elementCodec;
      this.allowInlineDefinitions = allowInlineDefinitions;
   }

   public DataResult encode(RegistryEntry arg, DynamicOps dynamicOps, Object object) {
      if (dynamicOps instanceof RegistryOps lv) {
         Optional optional = lv.getOwner(this.registryRef);
         if (optional.isPresent()) {
            if (!arg.ownerEquals((RegistryEntryOwner)optional.get())) {
               return DataResult.error(() -> {
                  return "Element " + arg + " is not valid in current registry set";
               });
            }

            return (DataResult)arg.getKeyOrValue().map((key) -> {
               return Identifier.CODEC.encode(key.getValue(), dynamicOps, object);
            }, (value) -> {
               return this.elementCodec.encode(value, dynamicOps, object);
            });
         }
      }

      return this.elementCodec.encode(arg.value(), dynamicOps, object);
   }

   public DataResult decode(DynamicOps ops, Object input) {
      if (ops instanceof RegistryOps lv) {
         Optional optional = lv.getEntryLookup(this.registryRef);
         if (optional.isEmpty()) {
            return DataResult.error(() -> {
               return "Registry does not exist: " + this.registryRef;
            });
         } else {
            RegistryEntryLookup lv2 = (RegistryEntryLookup)optional.get();
            DataResult dataResult = Identifier.CODEC.decode(ops, input);
            if (dataResult.result().isEmpty()) {
               return !this.allowInlineDefinitions ? DataResult.error(() -> {
                  return "Inline definitions not allowed here";
               }) : this.elementCodec.decode(ops, input).map((pairx) -> {
                  return pairx.mapFirst(RegistryEntry::of);
               });
            } else {
               Pair pair = (Pair)dataResult.result().get();
               RegistryKey lv3 = RegistryKey.of(this.registryRef, (Identifier)pair.getFirst());
               return ((DataResult)lv2.getOptional(lv3).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error(() -> {
                     return "Failed to get element " + lv3;
                  });
               })).map((arg) -> {
                  return Pair.of(arg, pair.getSecond());
               }).setLifecycle(Lifecycle.stable());
            }
         }
      } else {
         return this.elementCodec.decode(ops, input).map((pairx) -> {
            return pairx.mapFirst(RegistryEntry::of);
         });
      }
   }

   public String toString() {
      return "RegistryFileCodec[" + this.registryRef + " " + this.elementCodec + "]";
   }

   // $FF: synthetic method
   public DataResult encode(Object input, DynamicOps ops, Object prefix) {
      return this.encode((RegistryEntry)input, ops, prefix);
   }
}
