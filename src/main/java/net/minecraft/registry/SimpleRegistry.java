package net.minecraft.registry;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SimpleRegistry implements MutableRegistry {
   private static final Logger LOGGER = LogUtils.getLogger();
   final RegistryKey key;
   private final ObjectList rawIdToEntry;
   private final Object2IntMap entryToRawId;
   private final Map idToEntry;
   private final Map keyToEntry;
   private final Map valueToEntry;
   private final Map entryToLifecycle;
   private Lifecycle lifecycle;
   private volatile Map tagToEntryList;
   private boolean frozen;
   @Nullable
   private Map intrusiveValueToEntry;
   @Nullable
   private List cachedEntries;
   private int nextId;
   private final RegistryWrapper.Impl wrapper;

   public SimpleRegistry(RegistryKey key, Lifecycle lifecycle) {
      this(key, lifecycle, false);
   }

   public SimpleRegistry(RegistryKey key, Lifecycle lifecycle, boolean intrusive) {
      this.rawIdToEntry = new ObjectArrayList(256);
      this.entryToRawId = (Object2IntMap)Util.make(new Object2IntOpenCustomHashMap(Util.identityHashStrategy()), (map) -> {
         map.defaultReturnValue(-1);
      });
      this.idToEntry = new HashMap();
      this.keyToEntry = new HashMap();
      this.valueToEntry = new IdentityHashMap();
      this.entryToLifecycle = new IdentityHashMap();
      this.tagToEntryList = new IdentityHashMap();
      this.wrapper = new RegistryWrapper.Impl() {
         public RegistryKey getRegistryKey() {
            return SimpleRegistry.this.key;
         }

         public Lifecycle getLifecycle() {
            return SimpleRegistry.this.getLifecycle();
         }

         public Optional getOptional(RegistryKey key) {
            return SimpleRegistry.this.getEntry(key);
         }

         public Stream streamEntries() {
            return SimpleRegistry.this.streamEntries();
         }

         public Optional getOptional(TagKey tag) {
            return SimpleRegistry.this.getEntryList(tag);
         }

         public Stream streamTags() {
            return SimpleRegistry.this.streamTagsAndEntries().map(Pair::getSecond);
         }
      };
      Bootstrap.ensureBootstrapped(() -> {
         return "registry " + key;
      });
      this.key = key;
      this.lifecycle = lifecycle;
      if (intrusive) {
         this.intrusiveValueToEntry = new IdentityHashMap();
      }

   }

   public RegistryKey getKey() {
      return this.key;
   }

   public String toString() {
      return "Registry[" + this.key + " (" + this.lifecycle + ")]";
   }

   private List getEntries() {
      if (this.cachedEntries == null) {
         this.cachedEntries = this.rawIdToEntry.stream().filter(Objects::nonNull).toList();
      }

      return this.cachedEntries;
   }

   private void assertNotFrozen() {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen");
      }
   }

   private void assertNotFrozen(RegistryKey key) {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen (trying to add key " + key + ")");
      }
   }

   public RegistryEntry.Reference set(int i, RegistryKey arg, Object object, Lifecycle lifecycle) {
      this.assertNotFrozen(arg);
      Validate.notNull(arg);
      Validate.notNull(object);
      if (this.idToEntry.containsKey(arg.getValue())) {
         Util.throwOrPause(new IllegalStateException("Adding duplicate key '" + arg + "' to registry"));
      }

      if (this.valueToEntry.containsKey(object)) {
         Util.throwOrPause(new IllegalStateException("Adding duplicate value '" + object + "' to registry"));
      }

      RegistryEntry.Reference lv;
      if (this.intrusiveValueToEntry != null) {
         lv = (RegistryEntry.Reference)this.intrusiveValueToEntry.remove(object);
         if (lv == null) {
            throw new AssertionError("Missing intrusive holder for " + arg + ":" + object);
         }

         lv.setRegistryKey(arg);
      } else {
         lv = (RegistryEntry.Reference)this.keyToEntry.computeIfAbsent(arg, (key) -> {
            return RegistryEntry.Reference.standAlone(this.getEntryOwner(), key);
         });
      }

      this.keyToEntry.put(arg, lv);
      this.idToEntry.put(arg.getValue(), lv);
      this.valueToEntry.put(object, lv);
      this.rawIdToEntry.size(Math.max(this.rawIdToEntry.size(), i + 1));
      this.rawIdToEntry.set(i, lv);
      this.entryToRawId.put(object, i);
      if (this.nextId <= i) {
         this.nextId = i + 1;
      }

      this.entryToLifecycle.put(object, lifecycle);
      this.lifecycle = this.lifecycle.add(lifecycle);
      this.cachedEntries = null;
      return lv;
   }

   public RegistryEntry.Reference add(RegistryKey key, Object entry, Lifecycle lifecycle) {
      return this.set(this.nextId, key, entry, lifecycle);
   }

   @Nullable
   public Identifier getId(Object value) {
      RegistryEntry.Reference lv = (RegistryEntry.Reference)this.valueToEntry.get(value);
      return lv != null ? lv.registryKey().getValue() : null;
   }

   public Optional getKey(Object entry) {
      return Optional.ofNullable((RegistryEntry.Reference)this.valueToEntry.get(entry)).map(RegistryEntry.Reference::registryKey);
   }

   public int getRawId(@Nullable Object value) {
      return this.entryToRawId.getInt(value);
   }

   @Nullable
   public Object get(@Nullable RegistryKey key) {
      return getValue((RegistryEntry.Reference)this.keyToEntry.get(key));
   }

   @Nullable
   public Object get(int index) {
      return index >= 0 && index < this.rawIdToEntry.size() ? getValue((RegistryEntry.Reference)this.rawIdToEntry.get(index)) : null;
   }

   public Optional getEntry(int rawId) {
      return rawId >= 0 && rawId < this.rawIdToEntry.size() ? Optional.ofNullable((RegistryEntry.Reference)this.rawIdToEntry.get(rawId)) : Optional.empty();
   }

   public Optional getEntry(RegistryKey key) {
      return Optional.ofNullable((RegistryEntry.Reference)this.keyToEntry.get(key));
   }

   public RegistryEntry getEntry(Object value) {
      RegistryEntry.Reference lv = (RegistryEntry.Reference)this.valueToEntry.get(value);
      return (RegistryEntry)(lv != null ? lv : RegistryEntry.of(value));
   }

   RegistryEntry.Reference getOrCreateEntry(RegistryKey key) {
      return (RegistryEntry.Reference)this.keyToEntry.computeIfAbsent(key, (key2) -> {
         if (this.intrusiveValueToEntry != null) {
            throw new IllegalStateException("This registry can't create new holders without value");
         } else {
            this.assertNotFrozen(key2);
            return RegistryEntry.Reference.standAlone(this.getEntryOwner(), key2);
         }
      });
   }

   public int size() {
      return this.keyToEntry.size();
   }

   public Lifecycle getEntryLifecycle(Object entry) {
      return (Lifecycle)this.entryToLifecycle.get(entry);
   }

   public Lifecycle getLifecycle() {
      return this.lifecycle;
   }

   public Iterator iterator() {
      return Iterators.transform(this.getEntries().iterator(), RegistryEntry::value);
   }

   @Nullable
   public Object get(@Nullable Identifier id) {
      RegistryEntry.Reference lv = (RegistryEntry.Reference)this.idToEntry.get(id);
      return getValue(lv);
   }

   @Nullable
   private static Object getValue(@Nullable RegistryEntry.Reference entry) {
      return entry != null ? entry.value() : null;
   }

   public Set getIds() {
      return Collections.unmodifiableSet(this.idToEntry.keySet());
   }

   public Set getKeys() {
      return Collections.unmodifiableSet(this.keyToEntry.keySet());
   }

   public Set getEntrySet() {
      return Collections.unmodifiableSet(Maps.transformValues(this.keyToEntry, RegistryEntry::value).entrySet());
   }

   public Stream streamEntries() {
      return this.getEntries().stream();
   }

   public Stream streamTagsAndEntries() {
      return this.tagToEntryList.entrySet().stream().map((entry) -> {
         return Pair.of((TagKey)entry.getKey(), (RegistryEntryList.Named)entry.getValue());
      });
   }

   public RegistryEntryList.Named getOrCreateEntryList(TagKey tag) {
      RegistryEntryList.Named lv = (RegistryEntryList.Named)this.tagToEntryList.get(tag);
      if (lv == null) {
         lv = this.createNamedEntryList(tag);
         Map map = new IdentityHashMap(this.tagToEntryList);
         map.put(tag, lv);
         this.tagToEntryList = map;
      }

      return lv;
   }

   private RegistryEntryList.Named createNamedEntryList(TagKey tag) {
      return new RegistryEntryList.Named(this.getEntryOwner(), tag);
   }

   public Stream streamTags() {
      return this.tagToEntryList.keySet().stream();
   }

   public boolean isEmpty() {
      return this.keyToEntry.isEmpty();
   }

   public Optional getRandom(Random random) {
      return Util.getRandomOrEmpty(this.getEntries(), random);
   }

   public boolean containsId(Identifier id) {
      return this.idToEntry.containsKey(id);
   }

   public boolean contains(RegistryKey key) {
      return this.keyToEntry.containsKey(key);
   }

   public Registry freeze() {
      if (this.frozen) {
         return this;
      } else {
         this.frozen = true;
         this.valueToEntry.forEach((value, entry) -> {
            entry.setValue(value);
         });
         List list = this.keyToEntry.entrySet().stream().filter((entry) -> {
            return !((RegistryEntry.Reference)entry.getValue()).hasKeyAndValue();
         }).map((entry) -> {
            return ((RegistryKey)entry.getKey()).getValue();
         }).sorted().toList();
         if (!list.isEmpty()) {
            RegistryKey var10002 = this.getKey();
            throw new IllegalStateException("Unbound values in registry " + var10002 + ": " + list);
         } else {
            if (this.intrusiveValueToEntry != null) {
               if (!this.intrusiveValueToEntry.isEmpty()) {
                  throw new IllegalStateException("Some intrusive holders were not registered: " + this.intrusiveValueToEntry.values());
               }

               this.intrusiveValueToEntry = null;
            }

            return this;
         }
      }
   }

   public RegistryEntry.Reference createEntry(Object value) {
      if (this.intrusiveValueToEntry == null) {
         throw new IllegalStateException("This registry can't create intrusive holders");
      } else {
         this.assertNotFrozen();
         return (RegistryEntry.Reference)this.intrusiveValueToEntry.computeIfAbsent(value, (valuex) -> {
            return RegistryEntry.Reference.intrusive(this.getReadOnlyWrapper(), valuex);
         });
      }
   }

   public Optional getEntryList(TagKey tag) {
      return Optional.ofNullable((RegistryEntryList.Named)this.tagToEntryList.get(tag));
   }

   public void populateTags(Map tagEntries) {
      Map map2 = new IdentityHashMap();
      this.keyToEntry.values().forEach((entry) -> {
         map2.put(entry, new ArrayList());
      });
      tagEntries.forEach((tag, entries) -> {
         Iterator var4 = entries.iterator();

         while(var4.hasNext()) {
            RegistryEntry lv = (RegistryEntry)var4.next();
            if (!lv.ownerEquals(this.getReadOnlyWrapper())) {
               throw new IllegalStateException("Can't create named set " + tag + " containing value " + lv + " from outside registry " + this);
            }

            if (!(lv instanceof RegistryEntry.Reference)) {
               throw new IllegalStateException("Found direct holder " + lv + " value in tag " + tag);
            }

            RegistryEntry.Reference lv2 = (RegistryEntry.Reference)lv;
            ((List)map2.get(lv2)).add(tag);
         }

      });
      Set set = Sets.difference(this.tagToEntryList.keySet(), tagEntries.keySet());
      if (!set.isEmpty()) {
         LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.getKey(), set.stream().map((tag) -> {
            return tag.id().toString();
         }).sorted().collect(Collectors.joining(", ")));
      }

      Map map3 = new IdentityHashMap(this.tagToEntryList);
      tagEntries.forEach((tag, entries) -> {
         ((RegistryEntryList.Named)map3.computeIfAbsent(tag, this::createNamedEntryList)).copyOf(entries);
      });
      map2.forEach(RegistryEntry.Reference::setTags);
      this.tagToEntryList = map3;
   }

   public void clearTags() {
      this.tagToEntryList.values().forEach((entryList) -> {
         entryList.copyOf(List.of());
      });
      this.keyToEntry.values().forEach((entry) -> {
         entry.setTags(Set.of());
      });
   }

   public RegistryEntryLookup createMutableEntryLookup() {
      this.assertNotFrozen();
      return new RegistryEntryLookup() {
         public Optional getOptional(RegistryKey key) {
            return Optional.of(this.getOrThrow(key));
         }

         public RegistryEntry.Reference getOrThrow(RegistryKey key) {
            return SimpleRegistry.this.getOrCreateEntry(key);
         }

         public Optional getOptional(TagKey tag) {
            return Optional.of(this.getOrThrow(tag));
         }

         public RegistryEntryList.Named getOrThrow(TagKey tag) {
            return SimpleRegistry.this.getOrCreateEntryList(tag);
         }
      };
   }

   public RegistryEntryOwner getEntryOwner() {
      return this.wrapper;
   }

   public RegistryWrapper.Impl getReadOnlyWrapper() {
      return this.wrapper;
   }

   // $FF: synthetic method
   public RegistryEntry set(int rawId, RegistryKey key, Object value, Lifecycle lifecycle) {
      return this.set(rawId, key, value, lifecycle);
   }
}
