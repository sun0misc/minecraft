package net.minecraft.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.util.Util;

public class CombinedDynamicRegistries {
   private final List types;
   private final List registryManagers;
   private final DynamicRegistryManager.Immutable combinedRegistryManager;

   public CombinedDynamicRegistries(List types) {
      this(types, (List)Util.make(() -> {
         DynamicRegistryManager.Immutable[] lvs = new DynamicRegistryManager.Immutable[types.size()];
         Arrays.fill(lvs, DynamicRegistryManager.EMPTY);
         return Arrays.asList(lvs);
      }));
   }

   private CombinedDynamicRegistries(List types, List registryManagers) {
      this.types = List.copyOf(types);
      this.registryManagers = List.copyOf(registryManagers);
      this.combinedRegistryManager = (new DynamicRegistryManager.ImmutableImpl(toRegistryMap(registryManagers.stream()))).toImmutable();
   }

   private int getIndex(Object type) {
      int i = this.types.indexOf(type);
      if (i == -1) {
         throw new IllegalStateException("Can't find " + type + " inside " + this.types);
      } else {
         return i;
      }
   }

   public DynamicRegistryManager.Immutable get(Object index) {
      int i = this.getIndex(index);
      return (DynamicRegistryManager.Immutable)this.registryManagers.get(i);
   }

   public DynamicRegistryManager.Immutable getPrecedingRegistryManagers(Object type) {
      int i = this.getIndex(type);
      return this.subset(0, i);
   }

   public DynamicRegistryManager.Immutable getSucceedingRegistryManagers(Object type) {
      int i = this.getIndex(type);
      return this.subset(i, this.registryManagers.size());
   }

   private DynamicRegistryManager.Immutable subset(int startIndex, int endIndex) {
      return (new DynamicRegistryManager.ImmutableImpl(toRegistryMap(this.registryManagers.subList(startIndex, endIndex).stream()))).toImmutable();
   }

   public CombinedDynamicRegistries with(Object type, DynamicRegistryManager.Immutable... registryManagers) {
      return this.with(type, Arrays.asList(registryManagers));
   }

   public CombinedDynamicRegistries with(Object type, List registryManagers) {
      int i = this.getIndex(type);
      if (registryManagers.size() > this.registryManagers.size() - i) {
         throw new IllegalStateException("Too many values to replace");
      } else {
         List list2 = new ArrayList();

         for(int j = 0; j < i; ++j) {
            list2.add((DynamicRegistryManager.Immutable)this.registryManagers.get(j));
         }

         list2.addAll(registryManagers);

         while(list2.size() < this.registryManagers.size()) {
            list2.add(DynamicRegistryManager.EMPTY);
         }

         return new CombinedDynamicRegistries(this.types, list2);
      }
   }

   public DynamicRegistryManager.Immutable getCombinedRegistryManager() {
      return this.combinedRegistryManager;
   }

   private static Map toRegistryMap(Stream registryManagers) {
      Map map = new HashMap();
      registryManagers.forEach((registryManager) -> {
         registryManager.streamAllRegistries().forEach((entry) -> {
            if (map.put(entry.key(), entry.value()) != null) {
               throw new IllegalStateException("Duplicated registry " + entry.key());
            }
         });
      });
      return map;
   }
}
