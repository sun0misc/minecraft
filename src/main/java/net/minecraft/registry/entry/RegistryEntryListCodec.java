package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;

public class RegistryEntryListCodec implements Codec {
   private final RegistryKey registry;
   private final Codec entryCodec;
   private final Codec directEntryListCodec;
   private final Codec entryListStorageCodec;

   private static Codec createDirectEntryListCodec(Codec entryCodec, boolean alwaysSerializeAsList) {
      Codec codec2 = Codecs.validate(entryCodec.listOf(), Codecs.createEqualTypeChecker(RegistryEntry::getType));
      return alwaysSerializeAsList ? codec2 : Codec.either(codec2, entryCodec).xmap((either) -> {
         return (List)either.map((entries) -> {
            return entries;
         }, List::of);
      }, (entries) -> {
         return entries.size() == 1 ? Either.right((RegistryEntry)entries.get(0)) : Either.left(entries);
      });
   }

   public static Codec create(RegistryKey registryRef, Codec entryCodec, boolean alwaysSerializeAsList) {
      return new RegistryEntryListCodec(registryRef, entryCodec, alwaysSerializeAsList);
   }

   private RegistryEntryListCodec(RegistryKey registry, Codec entryCodec, boolean alwaysSerializeAsList) {
      this.registry = registry;
      this.entryCodec = entryCodec;
      this.directEntryListCodec = createDirectEntryListCodec(entryCodec, alwaysSerializeAsList);
      this.entryListStorageCodec = Codec.either(TagKey.codec(registry), this.directEntryListCodec);
   }

   public DataResult decode(DynamicOps ops, Object input) {
      if (ops instanceof RegistryOps lv) {
         Optional optional = lv.getEntryLookup(this.registry);
         if (optional.isPresent()) {
            RegistryEntryLookup lv2 = (RegistryEntryLookup)optional.get();
            return this.entryListStorageCodec.decode(ops, input).map((pair) -> {
               return pair.mapFirst((either) -> {
                  Objects.requireNonNull(lv2);
                  return (RegistryEntryList)either.map(lv2::getOrThrow, RegistryEntryList::of);
               });
            });
         }
      }

      return this.decodeDirect(ops, input);
   }

   public DataResult encode(RegistryEntryList arg, DynamicOps dynamicOps, Object object) {
      if (dynamicOps instanceof RegistryOps lv) {
         Optional optional = lv.getOwner(this.registry);
         if (optional.isPresent()) {
            if (!arg.ownerEquals((RegistryEntryOwner)optional.get())) {
               return DataResult.error(() -> {
                  return "HolderSet " + arg + " is not valid in current registry set";
               });
            }

            return this.entryListStorageCodec.encode(arg.getStorage().mapRight(List::copyOf), dynamicOps, object);
         }
      }

      return this.encodeDirect(arg, dynamicOps, object);
   }

   private DataResult decodeDirect(DynamicOps ops, Object input) {
      return this.entryCodec.listOf().decode(ops, input).flatMap((pair) -> {
         List list = new ArrayList();
         Iterator var2 = ((List)pair.getFirst()).iterator();

         while(var2.hasNext()) {
            RegistryEntry lv = (RegistryEntry)var2.next();
            if (!(lv instanceof RegistryEntry.Direct)) {
               return DataResult.error(() -> {
                  return "Can't decode element " + lv + " without registry";
               });
            }

            RegistryEntry.Direct lv2 = (RegistryEntry.Direct)lv;
            list.add(lv2);
         }

         return DataResult.success(new Pair(RegistryEntryList.of((List)list), pair.getSecond()));
      });
   }

   private DataResult encodeDirect(RegistryEntryList entryList, DynamicOps ops, Object prefix) {
      return this.directEntryListCodec.encode(entryList.stream().toList(), ops, prefix);
   }

   // $FF: synthetic method
   public DataResult encode(Object entryList, DynamicOps ops, Object prefix) {
      return this.encode((RegistryEntryList)entryList, ops, prefix);
   }
}
