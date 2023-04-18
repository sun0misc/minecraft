package net.minecraft.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.entry.RegistryFixedCodec;

public class RegistryCodecs {
   private static MapCodec managerEntry(RegistryKey registryRef, MapCodec elementCodec) {
      return RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(RegistryKey.createCodec(registryRef).fieldOf("name").forGetter(RegistryManagerEntry::key), Codec.INT.fieldOf("id").forGetter(RegistryManagerEntry::rawId), elementCodec.forGetter(RegistryManagerEntry::value)).apply(instance, RegistryManagerEntry::new);
      });
   }

   public static Codec createRegistryCodec(RegistryKey registryRef, Lifecycle lifecycle, Codec elementCodec) {
      return managerEntry(registryRef, elementCodec.fieldOf("element")).codec().listOf().xmap((entries) -> {
         MutableRegistry lv = new SimpleRegistry(registryRef, lifecycle);
         Iterator var4 = entries.iterator();

         while(var4.hasNext()) {
            RegistryManagerEntry lv2 = (RegistryManagerEntry)var4.next();
            lv.set(lv2.rawId(), lv2.key(), lv2.value(), lifecycle);
         }

         return lv;
      }, (registry) -> {
         ImmutableList.Builder builder = ImmutableList.builder();
         Iterator var2 = registry.iterator();

         while(var2.hasNext()) {
            Object object = var2.next();
            builder.add(new RegistryManagerEntry((RegistryKey)registry.getKey(object).get(), registry.getRawId(object), object));
         }

         return builder.build();
      });
   }

   public static Codec createKeyedRegistryCodec(RegistryKey registryRef, Lifecycle lifecycle, Codec elementCodec) {
      Codec codec2 = Codec.unboundedMap(RegistryKey.createCodec(registryRef), elementCodec);
      return codec2.xmap((entries) -> {
         MutableRegistry lv = new SimpleRegistry(registryRef, lifecycle);
         entries.forEach((key, value) -> {
            lv.add(key, value, lifecycle);
         });
         return lv.freeze();
      }, (registry) -> {
         return ImmutableMap.copyOf(registry.getEntrySet());
      });
   }

   public static Codec entryList(RegistryKey registryRef, Codec elementCodec) {
      return entryList(registryRef, elementCodec, false);
   }

   public static Codec entryList(RegistryKey registryRef, Codec elementCodec, boolean alwaysSerializeAsList) {
      return RegistryEntryListCodec.create(registryRef, RegistryElementCodec.of(registryRef, elementCodec), alwaysSerializeAsList);
   }

   public static Codec entryList(RegistryKey registryRef) {
      return entryList(registryRef, false);
   }

   public static Codec entryList(RegistryKey registryRef, boolean alwaysSerializeAsList) {
      return RegistryEntryListCodec.create(registryRef, RegistryFixedCodec.of(registryRef), alwaysSerializeAsList);
   }

   static record RegistryManagerEntry(RegistryKey key, int rawId, Object value) {
      RegistryManagerEntry(RegistryKey key, int rawId, Object value) {
         this.key = key;
         this.rawId = rawId;
         this.value = value;
      }

      public RegistryKey key() {
         return this.key;
      }

      public int rawId() {
         return this.rawId;
      }

      public Object value() {
         return this.value;
      }
   }
}
