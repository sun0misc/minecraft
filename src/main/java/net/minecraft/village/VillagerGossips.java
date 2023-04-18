package net.minecraft.village;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Uuids;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;

public class VillagerGossips {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int field_30236 = 2;
   private final Map entityReputation = Maps.newHashMap();

   @Debug
   public Map getEntityReputationAssociatedGossips() {
      Map map = Maps.newHashMap();
      this.entityReputation.keySet().forEach((uuid) -> {
         Reputation lv = (Reputation)this.entityReputation.get(uuid);
         map.put(uuid, lv.associatedGossip);
      });
      return map;
   }

   public void decay() {
      Iterator iterator = this.entityReputation.values().iterator();

      while(iterator.hasNext()) {
         Reputation lv = (Reputation)iterator.next();
         lv.decay();
         if (lv.isObsolete()) {
            iterator.remove();
         }
      }

   }

   private Stream entries() {
      return this.entityReputation.entrySet().stream().flatMap((entry) -> {
         return ((Reputation)entry.getValue()).entriesFor((UUID)entry.getKey());
      });
   }

   private Collection pickGossips(Random random, int count) {
      List list = this.entries().toList();
      if (list.isEmpty()) {
         return Collections.emptyList();
      } else {
         int[] is = new int[list.size()];
         int j = 0;

         for(int k = 0; k < list.size(); ++k) {
            GossipEntry lv = (GossipEntry)list.get(k);
            j += Math.abs(lv.getValue());
            is[k] = j - 1;
         }

         Set set = Sets.newIdentityHashSet();

         for(int l = 0; l < count; ++l) {
            int m = random.nextInt(j);
            int n = Arrays.binarySearch(is, m);
            set.add((GossipEntry)list.get(n < 0 ? -n - 1 : n));
         }

         return set;
      }
   }

   private Reputation getReputationFor(UUID target) {
      return (Reputation)this.entityReputation.computeIfAbsent(target, (uuid) -> {
         return new Reputation();
      });
   }

   public void shareGossipFrom(VillagerGossips from, Random random, int count) {
      Collection collection = from.pickGossips(random, count);
      collection.forEach((gossip) -> {
         int i = gossip.value - gossip.type.shareDecrement;
         if (i >= 2) {
            this.getReputationFor(gossip.target).associatedGossip.mergeInt(gossip.type, i, VillagerGossips::max);
         }

      });
   }

   public int getReputationFor(UUID target, Predicate gossipTypeFilter) {
      Reputation lv = (Reputation)this.entityReputation.get(target);
      return lv != null ? lv.getValueFor(gossipTypeFilter) : 0;
   }

   public long getReputationCount(VillageGossipType type, DoublePredicate predicate) {
      return this.entityReputation.values().stream().filter((reputation) -> {
         return predicate.test((double)(reputation.associatedGossip.getOrDefault(type, 0) * type.multiplier));
      }).count();
   }

   public void startGossip(UUID target, VillageGossipType type, int value) {
      Reputation lv = this.getReputationFor(target);
      lv.associatedGossip.mergeInt(type, value, (left, right) -> {
         return this.mergeReputation(type, left, right);
      });
      lv.clamp(type);
      if (lv.isObsolete()) {
         this.entityReputation.remove(target);
      }

   }

   public void removeGossip(UUID target, VillageGossipType type, int value) {
      this.startGossip(target, type, -value);
   }

   public void remove(UUID target, VillageGossipType type) {
      Reputation lv = (Reputation)this.entityReputation.get(target);
      if (lv != null) {
         lv.remove(type);
         if (lv.isObsolete()) {
            this.entityReputation.remove(target);
         }
      }

   }

   public void remove(VillageGossipType type) {
      Iterator iterator = this.entityReputation.values().iterator();

      while(iterator.hasNext()) {
         Reputation lv = (Reputation)iterator.next();
         lv.remove(type);
         if (lv.isObsolete()) {
            iterator.remove();
         }
      }

   }

   public Object serialize(DynamicOps ops) {
      Optional var10000 = VillagerGossips.GossipEntry.LIST_CODEC.encodeStart(ops, this.entries().toList()).resultOrPartial((error) -> {
         LOGGER.warn("Failed to serialize gossips: {}", error);
      });
      Objects.requireNonNull(ops);
      return var10000.orElseGet(ops::emptyList);
   }

   public void deserialize(Dynamic dynamic) {
      VillagerGossips.GossipEntry.LIST_CODEC.decode(dynamic).resultOrPartial((error) -> {
         LOGGER.warn("Failed to deserialize gossips: {}", error);
      }).stream().flatMap((pair) -> {
         return ((List)pair.getFirst()).stream();
      }).forEach((entry) -> {
         this.getReputationFor(entry.target).associatedGossip.put(entry.type, entry.value);
      });
   }

   private static int max(int left, int right) {
      return Math.max(left, right);
   }

   private int mergeReputation(VillageGossipType type, int left, int right) {
      int k = left + right;
      return k > type.maxValue ? Math.max(type.maxValue, left) : k;
   }

   private static class Reputation {
      final Object2IntMap associatedGossip = new Object2IntOpenHashMap();

      Reputation() {
      }

      public int getValueFor(Predicate gossipTypeFilter) {
         return this.associatedGossip.object2IntEntrySet().stream().filter((entry) -> {
            return gossipTypeFilter.test((VillageGossipType)entry.getKey());
         }).mapToInt((entry) -> {
            return entry.getIntValue() * ((VillageGossipType)entry.getKey()).multiplier;
         }).sum();
      }

      public Stream entriesFor(UUID target) {
         return this.associatedGossip.object2IntEntrySet().stream().map((entry) -> {
            return new GossipEntry(target, (VillageGossipType)entry.getKey(), entry.getIntValue());
         });
      }

      public void decay() {
         ObjectIterator objectIterator = this.associatedGossip.object2IntEntrySet().iterator();

         while(objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            int i = entry.getIntValue() - ((VillageGossipType)entry.getKey()).decay;
            if (i < 2) {
               objectIterator.remove();
            } else {
               entry.setValue(i);
            }
         }

      }

      public boolean isObsolete() {
         return this.associatedGossip.isEmpty();
      }

      public void clamp(VillageGossipType gossipType) {
         int i = this.associatedGossip.getInt(gossipType);
         if (i > gossipType.maxValue) {
            this.associatedGossip.put(gossipType, gossipType.maxValue);
         }

         if (i < 2) {
            this.remove(gossipType);
         }

      }

      public void remove(VillageGossipType gossipType) {
         this.associatedGossip.removeInt(gossipType);
      }
   }

   private static record GossipEntry(UUID target, VillageGossipType type, int value) {
      final UUID target;
      final VillageGossipType type;
      final int value;
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Uuids.INT_STREAM_CODEC.fieldOf("Target").forGetter(GossipEntry::target), VillageGossipType.CODEC.fieldOf("Type").forGetter(GossipEntry::type), Codecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipEntry::value)).apply(instance, GossipEntry::new);
      });
      public static final Codec LIST_CODEC;

      GossipEntry(UUID target, VillageGossipType type, int value) {
         this.target = target;
         this.type = type;
         this.value = value;
      }

      public int getValue() {
         return this.value * this.type.multiplier;
      }

      public UUID target() {
         return this.target;
      }

      public VillageGossipType type() {
         return this.type;
      }

      public int value() {
         return this.value;
      }

      static {
         LIST_CODEC = CODEC.listOf();
      }
   }
}
