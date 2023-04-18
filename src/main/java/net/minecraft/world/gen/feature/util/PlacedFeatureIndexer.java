package net.minecraft.world.gen.feature.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.TopologicalSorts;
import net.minecraft.util.Util;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class PlacedFeatureIndexer {
   public static List collectIndexedFeatures(List biomes, Function biomesToPlacedFeaturesList, boolean listInvolvedBiomesOnFailure) {
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      MutableInt mutableInt = new MutableInt(0);
      Comparator comparator = Comparator.comparingInt(IndexedFeature::step).thenComparingInt(IndexedFeature::featureIndex);
      Map map = new TreeMap(comparator);
      int i = 0;
      Iterator var8 = biomes.iterator();

      ArrayList list2;
      int j;

      record IndexedFeature(int featureIndex, int step, PlacedFeature feature) {
         IndexedFeature(int i, int j, PlacedFeature arg) {
            this.featureIndex = i;
            this.step = j;
            this.feature = arg;
         }

         public int featureIndex() {
            return this.featureIndex;
         }

         public int step() {
            return this.step;
         }

         public PlacedFeature feature() {
            return this.feature;
         }
      }

      while(var8.hasNext()) {
         Object object = var8.next();
         list2 = Lists.newArrayList();
         List list3 = (List)biomesToPlacedFeaturesList.apply(object);
         i = Math.max(i, list3.size());

         for(j = 0; j < list3.size(); ++j) {
            Iterator var13 = ((RegistryEntryList)list3.get(j)).iterator();

            while(var13.hasNext()) {
               RegistryEntry lv = (RegistryEntry)var13.next();
               PlacedFeature lv2 = (PlacedFeature)lv.value();
               list2.add(new IndexedFeature(object2IntMap.computeIfAbsent(lv2, (feature) -> {
                  return mutableInt.getAndIncrement();
               }), j, lv2));
            }
         }

         for(j = 0; j < list2.size(); ++j) {
            Set set = (Set)map.computeIfAbsent((IndexedFeature)list2.get(j), (feature) -> {
               return new TreeSet(comparator);
            });
            if (j < list2.size() - 1) {
               set.add((IndexedFeature)list2.get(j + 1));
            }
         }
      }

      Set set2 = new TreeSet(comparator);
      Set set3 = new TreeSet(comparator);
      list2 = Lists.newArrayList();
      Iterator var21 = map.keySet().iterator();

      while(var21.hasNext()) {
         IndexedFeature lv3 = (IndexedFeature)var21.next();
         if (!set3.isEmpty()) {
            throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
         }

         if (!set2.contains(lv3)) {
            Objects.requireNonNull(list2);
            if (TopologicalSorts.sort(map, set2, set3, list2::add, lv3)) {
               if (!listInvolvedBiomesOnFailure) {
                  throw new IllegalStateException("Feature order cycle found");
               }

               List list4 = new ArrayList(biomes);

               int k;
               do {
                  k = list4.size();
                  ListIterator listIterator = list4.listIterator();

                  while(listIterator.hasNext()) {
                     Object object2 = listIterator.next();
                     listIterator.remove();

                     try {
                        collectIndexedFeatures(list4, biomesToPlacedFeaturesList, false);
                     } catch (IllegalStateException var18) {
                        continue;
                     }

                     listIterator.add(object2);
                  }
               } while(k != list4.size());

               throw new IllegalStateException("Feature order cycle found, involved sources: " + list4);
            }
         }
      }

      Collections.reverse(list2);
      ImmutableList.Builder builder = ImmutableList.builder();

      for(j = 0; j < i; ++j) {
         List list5 = (List)list2.stream().filter((feature) -> {
            return feature.step() == j;
         }).map(IndexedFeature::feature).collect(Collectors.toList());
         builder.add(new IndexedFeatures(list5));
      }

      return builder.build();
   }

   public static record IndexedFeatures(List features, ToIntFunction indexMapping) {
      IndexedFeatures(List features) {
         this(features, Util.lastIndexGetter(features, (size) -> {
            return new Object2IntOpenCustomHashMap(size, Util.identityHashStrategy());
         }));
      }

      public IndexedFeatures(List list, ToIntFunction toIntFunction) {
         this.features = list;
         this.indexMapping = toIntFunction;
      }

      public List features() {
         return this.features;
      }

      public ToIntFunction indexMapping() {
         return this.indexMapping;
      }
   }
}
