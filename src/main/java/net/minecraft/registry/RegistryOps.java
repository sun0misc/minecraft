package net.minecraft.registry;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

public class RegistryOps extends ForwardingDynamicOps {
   private final RegistryInfoGetter registryInfoGetter;

   private static RegistryInfoGetter caching(final RegistryInfoGetter registryInfoGetter) {
      return new RegistryInfoGetter() {
         private final Map registryRefToInfo = new HashMap();

         public Optional getRegistryInfo(RegistryKey registryRef) {
            Map var10000 = this.registryRefToInfo;
            RegistryInfoGetter var10002 = registryInfoGetter;
            Objects.requireNonNull(var10002);
            return (Optional)var10000.computeIfAbsent(registryRef, var10002::getRegistryInfo);
         }
      };
   }

   public static RegistryOps of(DynamicOps delegate, final RegistryWrapper.WrapperLookup wrapperLookup) {
      return of(delegate, caching(new RegistryInfoGetter() {
         public Optional getRegistryInfo(RegistryKey registryRef) {
            return wrapperLookup.getOptionalWrapper(registryRef).map((wrapper) -> {
               return new RegistryInfo(wrapper, wrapper, wrapper.getLifecycle());
            });
         }
      }));
   }

   public static RegistryOps of(DynamicOps delegate, RegistryInfoGetter registryInfoGetter) {
      return new RegistryOps(delegate, registryInfoGetter);
   }

   private RegistryOps(DynamicOps delegate, RegistryInfoGetter registryInfoGetter) {
      super(delegate);
      this.registryInfoGetter = registryInfoGetter;
   }

   public Optional getOwner(RegistryKey registryRef) {
      return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::owner);
   }

   public Optional getEntryLookup(RegistryKey registryRef) {
      return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::entryLookup);
   }

   public static RecordCodecBuilder getEntryLookupCodec(RegistryKey registryRef) {
      return Codecs.createContextRetrievalCodec((ops) -> {
         if (ops instanceof RegistryOps lv) {
            return (DataResult)lv.registryInfoGetter.getRegistryInfo(registryRef).map((info) -> {
               return DataResult.success(info.entryLookup(), info.elementsLifecycle());
            }).orElseGet(() -> {
               return DataResult.error(() -> {
                  return "Unknown registry: " + registryRef;
               });
            });
         } else {
            return DataResult.error(() -> {
               return "Not a registry ops";
            });
         }
      }).forGetter((object) -> {
         return null;
      });
   }

   public static RecordCodecBuilder getEntryCodec(RegistryKey key) {
      RegistryKey lv = RegistryKey.ofRegistry(key.getRegistry());
      return Codecs.createContextRetrievalCodec((ops) -> {
         if (ops instanceof RegistryOps lvx) {
            return (DataResult)lvx.registryInfoGetter.getRegistryInfo(lv).flatMap((info) -> {
               return info.entryLookup().getOptional(key);
            }).map(DataResult::success).orElseGet(() -> {
               return DataResult.error(() -> {
                  return "Can't find value: " + key;
               });
            });
         } else {
            return DataResult.error(() -> {
               return "Not a registry ops";
            });
         }
      }).forGetter((object) -> {
         return null;
      });
   }

   public interface RegistryInfoGetter {
      Optional getRegistryInfo(RegistryKey registryRef);
   }

   public static record RegistryInfo(RegistryEntryOwner owner, RegistryEntryLookup entryLookup, Lifecycle elementsLifecycle) {
      public RegistryInfo(RegistryEntryOwner arg, RegistryEntryLookup arg2, Lifecycle lifecycle) {
         this.owner = arg;
         this.entryLookup = arg2;
         this.elementsLifecycle = lifecycle;
      }

      public RegistryEntryOwner owner() {
         return this.owner;
      }

      public RegistryEntryLookup entryLookup() {
         return this.entryLookup;
      }

      public Lifecycle elementsLifecycle() {
         return this.elementsLifecycle;
      }
   }
}
