package net.minecraft.registry.tag;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public class TagPacketSerializer {
   public static Map serializeTags(CombinedDynamicRegistries dynamicRegistryManager) {
      return (Map)SerializableRegistries.streamRegistryManagerEntries(dynamicRegistryManager).map((registry) -> {
         return Pair.of(registry.key(), serializeTags(registry.value()));
      }).filter((pair) -> {
         return !((Serialized)pair.getSecond()).isEmpty();
      }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   private static Serialized serializeTags(Registry registry) {
      Map map = new HashMap();
      registry.streamTagsAndEntries().forEach((pair) -> {
         RegistryEntryList lv = (RegistryEntryList)pair.getSecond();
         IntList intList = new IntArrayList(lv.size());
         Iterator var5 = lv.iterator();

         while(var5.hasNext()) {
            RegistryEntry lv2 = (RegistryEntry)var5.next();
            if (lv2.getType() != RegistryEntry.Type.REFERENCE) {
               throw new IllegalStateException("Can't serialize unregistered value " + lv2);
            }

            intList.add(registry.getRawId(lv2.value()));
         }

         map.put(((TagKey)pair.getFirst()).id(), intList);
      });
      return new Serialized(map);
   }

   public static void loadTags(RegistryKey registryKey, Registry registry, Serialized serialized, Loader loader) {
      serialized.contents.forEach((tagId, rawIds) -> {
         TagKey lv = TagKey.of(registryKey, tagId);
         IntStream var10000 = rawIds.intStream();
         Objects.requireNonNull(registry);
         List list = (List)var10000.mapToObj(registry::getEntry).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
         loader.accept(lv, list);
      });
   }

   public static final class Serialized {
      final Map contents;

      Serialized(Map contents) {
         this.contents = contents;
      }

      public void writeBuf(PacketByteBuf buf) {
         buf.writeMap(this.contents, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntList);
      }

      public static Serialized fromBuf(PacketByteBuf buf) {
         return new Serialized(buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readIntList));
      }

      public boolean isEmpty() {
         return this.contents.isEmpty();
      }
   }

   @FunctionalInterface
   public interface Loader {
      void accept(TagKey tag, List entries);
   }
}
