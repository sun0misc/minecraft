package net.minecraft.registry;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface Registry extends Keyable, IndexedIterable {
   RegistryKey getKey();

   default Codec getCodec() {
      Codec codec = Identifier.CODEC.flatXmap((id) -> {
         return (DataResult)Optional.ofNullable(this.get(id)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               RegistryKey var10000 = this.getKey();
               return "Unknown registry key in " + var10000 + ": " + id;
            });
         });
      }, (value) -> {
         return (DataResult)this.getKey(value).map(RegistryKey::getValue).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               RegistryKey var10000 = this.getKey();
               return "Unknown registry element in " + var10000 + ":" + value;
            });
         });
      });
      Codec codec2 = Codecs.rawIdChecked((value) -> {
         return this.getKey(value).isPresent() ? this.getRawId(value) : -1;
      }, this::get, -1);
      return Codecs.withLifecycle(Codecs.orCompressed(codec, codec2), this::getEntryLifecycle, this::getEntryLifecycle);
   }

   default Codec createEntryCodec() {
      Codec codec = Identifier.CODEC.flatXmap((id) -> {
         return (DataResult)this.getEntry(RegistryKey.of(this.getKey(), id)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               RegistryKey var10000 = this.getKey();
               return "Unknown registry key in " + var10000 + ": " + id;
            });
         });
      }, (entry) -> {
         return (DataResult)entry.getKey().map(RegistryKey::getValue).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               RegistryKey var10000 = this.getKey();
               return "Unknown registry element in " + var10000 + ":" + entry;
            });
         });
      });
      return Codecs.withLifecycle(codec, (entry) -> {
         return this.getEntryLifecycle(entry.value());
      }, (entry) -> {
         return this.getEntryLifecycle(entry.value());
      });
   }

   default Stream keys(DynamicOps ops) {
      return this.getIds().stream().map((id) -> {
         return ops.createString(id.toString());
      });
   }

   @Nullable
   Identifier getId(Object value);

   Optional getKey(Object entry);

   int getRawId(@Nullable Object value);

   @Nullable
   Object get(@Nullable RegistryKey key);

   @Nullable
   Object get(@Nullable Identifier id);

   Lifecycle getEntryLifecycle(Object entry);

   Lifecycle getLifecycle();

   default Optional getOrEmpty(@Nullable Identifier id) {
      return Optional.ofNullable(this.get(id));
   }

   default Optional getOrEmpty(@Nullable RegistryKey key) {
      return Optional.ofNullable(this.get(key));
   }

   default Object getOrThrow(RegistryKey key) {
      Object object = this.get(key);
      if (object == null) {
         RegistryKey var10002 = this.getKey();
         throw new IllegalStateException("Missing key in " + var10002 + ": " + key);
      } else {
         return object;
      }
   }

   Set getIds();

   Set getEntrySet();

   Set getKeys();

   Optional getRandom(Random random);

   default Stream stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   boolean containsId(Identifier id);

   boolean contains(RegistryKey key);

   static Object register(Registry registry, String id, Object entry) {
      return register(registry, new Identifier(id), entry);
   }

   static Object register(Registry registry, Identifier id, Object entry) {
      return register(registry, RegistryKey.of(registry.getKey(), id), entry);
   }

   static Object register(Registry registry, RegistryKey key, Object entry) {
      ((MutableRegistry)registry).add(key, entry, Lifecycle.stable());
      return entry;
   }

   static RegistryEntry.Reference registerReference(Registry registry, RegistryKey key, Object entry) {
      return ((MutableRegistry)registry).add(key, entry, Lifecycle.stable());
   }

   static RegistryEntry.Reference registerReference(Registry registry, Identifier id, Object entry) {
      return registerReference(registry, RegistryKey.of(registry.getKey(), id), entry);
   }

   static Object register(Registry registry, int rawId, String id, Object entry) {
      ((MutableRegistry)registry).set(rawId, RegistryKey.of(registry.getKey(), new Identifier(id)), entry, Lifecycle.stable());
      return entry;
   }

   Registry freeze();

   RegistryEntry.Reference createEntry(Object value);

   Optional getEntry(int rawId);

   Optional getEntry(RegistryKey key);

   RegistryEntry getEntry(Object value);

   default RegistryEntry.Reference entryOf(RegistryKey key) {
      return (RegistryEntry.Reference)this.getEntry(key).orElseThrow(() -> {
         RegistryKey var10002 = this.getKey();
         return new IllegalStateException("Missing key in " + var10002 + ": " + key);
      });
   }

   Stream streamEntries();

   Optional getEntryList(TagKey tag);

   default Iterable iterateEntries(TagKey tag) {
      return (Iterable)DataFixUtils.orElse(this.getEntryList(tag), List.of());
   }

   RegistryEntryList.Named getOrCreateEntryList(TagKey tag);

   Stream streamTagsAndEntries();

   Stream streamTags();

   void clearTags();

   void populateTags(Map tagEntries);

   default IndexedIterable getIndexedEntries() {
      return new IndexedIterable() {
         public int getRawId(RegistryEntry arg) {
            return Registry.this.getRawId(arg.value());
         }

         @Nullable
         public RegistryEntry get(int i) {
            return (RegistryEntry)Registry.this.getEntry(i).orElse((Object)null);
         }

         public int size() {
            return Registry.this.size();
         }

         public Iterator iterator() {
            return Registry.this.streamEntries().map((entry) -> {
               return entry;
            }).iterator();
         }

         // $FF: synthetic method
         @Nullable
         public Object get(int index) {
            return this.get(index);
         }
      };
   }

   RegistryEntryOwner getEntryOwner();

   RegistryWrapper.Impl getReadOnlyWrapper();

   default RegistryWrapper.Impl getTagCreatingWrapper() {
      return new RegistryWrapper.Impl.Delegating() {
         protected RegistryWrapper.Impl getBase() {
            return Registry.this.getReadOnlyWrapper();
         }

         public Optional getOptional(TagKey tag) {
            return Optional.of(this.getOrThrow(tag));
         }

         public RegistryEntryList.Named getOrThrow(TagKey tag) {
            return Registry.this.getOrCreateEntryList(tag);
         }
      };
   }
}
