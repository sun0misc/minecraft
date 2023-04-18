package net.minecraft.resource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NamespaceResourceManager implements ResourceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   protected final List packList = Lists.newArrayList();
   private final ResourceType type;
   private final String namespace;

   public NamespaceResourceManager(ResourceType type, String namespace) {
      this.type = type;
      this.namespace = namespace;
   }

   public void addPack(ResourcePack pack) {
      this.addPack(pack.getName(), pack, (Predicate)null);
   }

   public void addPack(ResourcePack pack, Predicate filter) {
      this.addPack(pack.getName(), pack, filter);
   }

   public void addPack(String name, Predicate filter) {
      this.addPack(name, (ResourcePack)null, filter);
   }

   private void addPack(String name, @Nullable ResourcePack underlyingPack, @Nullable Predicate filter) {
      this.packList.add(new FilterablePack(name, underlyingPack, filter));
   }

   public Set getAllNamespaces() {
      return ImmutableSet.of(this.namespace);
   }

   public Optional getResource(Identifier id) {
      for(int i = this.packList.size() - 1; i >= 0; --i) {
         FilterablePack lv = (FilterablePack)this.packList.get(i);
         ResourcePack lv2 = lv.underlying;
         if (lv2 != null) {
            InputSupplier lv3 = lv2.open(this.type, id);
            if (lv3 != null) {
               InputSupplier lv4 = this.createMetadataSupplier(id, i);
               return Optional.of(createResource(lv2, id, lv3, lv4));
            }
         }

         if (lv.isFiltered(id)) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", id, lv.name);
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static Resource createResource(ResourcePack pack, Identifier id, InputSupplier supplier, InputSupplier metadataSupplier) {
      return new Resource(pack, wrapForDebug(id, pack, supplier), metadataSupplier);
   }

   private static InputSupplier wrapForDebug(Identifier id, ResourcePack pack, InputSupplier supplier) {
      return LOGGER.isDebugEnabled() ? () -> {
         return new DebugInputStream((InputStream)supplier.get(), id, pack.getName());
      } : supplier;
   }

   public List getAllResources(Identifier id) {
      Identifier lv = getMetadataPath(id);
      List list = new ArrayList();
      boolean bl = false;
      String string = null;

      for(int i = this.packList.size() - 1; i >= 0; --i) {
         FilterablePack lv2 = (FilterablePack)this.packList.get(i);
         ResourcePack lv3 = lv2.underlying;
         if (lv3 != null) {
            InputSupplier lv4 = lv3.open(this.type, id);
            if (lv4 != null) {
               InputSupplier lv5;
               if (bl) {
                  lv5 = ResourceMetadata.NONE_SUPPLIER;
               } else {
                  lv5 = () -> {
                     InputSupplier lvx = lv3.open(this.type, lv);
                     return lvx != null ? loadMetadata(lvx) : ResourceMetadata.NONE;
                  };
               }

               list.add(new Resource(lv3, lv4, lv5));
            }
         }

         if (lv2.isFiltered(id)) {
            string = lv2.name;
            break;
         }

         if (lv2.isFiltered(lv)) {
            bl = true;
         }
      }

      if (list.isEmpty() && string != null) {
         LOGGER.warn("Resource {} not found, but was filtered by pack {}", id, string);
      }

      return Lists.reverse(list);
   }

   private static boolean isMcmeta(Identifier id) {
      return id.getPath().endsWith(".mcmeta");
   }

   private static Identifier getMetadataFileName(Identifier id) {
      String string = id.getPath().substring(0, id.getPath().length() - ".mcmeta".length());
      return id.withPath(string);
   }

   static Identifier getMetadataPath(Identifier id) {
      return id.withPath(id.getPath() + ".mcmeta");
   }

   public Map findResources(String startingPath, Predicate allowedPathPredicate) {
      Map map = new HashMap();
      Map map2 = new HashMap();
      int i = this.packList.size();

      for(int j = 0; j < i; ++j) {
         FilterablePack lv = (FilterablePack)this.packList.get(j);
         lv.removeFiltered(map.keySet());
         lv.removeFiltered(map2.keySet());
         ResourcePack lv2 = lv.underlying;
         if (lv2 != null) {
            lv2.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
               record Result(ResourcePack pack, InputSupplier supplier, int packIndex) {
                  final ResourcePack pack;
                  final InputSupplier supplier;
                  final int packIndex;

                  Result(ResourcePack arg, InputSupplier arg2, int i) {
                     this.pack = arg;
                     this.supplier = arg2;
                     this.packIndex = i;
                  }

                  public ResourcePack pack() {
                     return this.pack;
                  }

                  public InputSupplier supplier() {
                     return this.supplier;
                  }

                  public int packIndex() {
                     return this.packIndex;
                  }
               }

               if (isMcmeta(id)) {
                  if (allowedPathPredicate.test(getMetadataFileName(id))) {
                     map2.put(id, new Result(lv2, supplier, j));
                  }
               } else if (allowedPathPredicate.test(id)) {
                  map.put(id, new Result(lv2, supplier, j));
               }

            });
         }
      }

      Map map3 = Maps.newTreeMap();
      map.forEach((id, result) -> {
         Identifier lv = getMetadataPath(id);
         Result lv2 = (Result)map2.get(lv);
         InputSupplier lv3;
         if (lv2 != null && lv2.packIndex >= result.packIndex) {
            lv3 = getMetadataSupplier(lv2.supplier);
         } else {
            lv3 = ResourceMetadata.NONE_SUPPLIER;
         }

         map3.put(id, createResource(result.pack, id, result.supplier, lv3));
      });
      return map3;
   }

   private InputSupplier createMetadataSupplier(Identifier id, int index) {
      return () -> {
         Identifier lv = getMetadataPath(id);

         for(int j = this.packList.size() - 1; j >= index; --j) {
            FilterablePack lv2 = (FilterablePack)this.packList.get(j);
            ResourcePack lv3 = lv2.underlying;
            if (lv3 != null) {
               InputSupplier lv4 = lv3.open(this.type, lv);
               if (lv4 != null) {
                  return loadMetadata(lv4);
               }
            }

            if (lv2.isFiltered(lv)) {
               break;
            }
         }

         return ResourceMetadata.NONE;
      };
   }

   private static InputSupplier getMetadataSupplier(InputSupplier supplier) {
      return () -> {
         return loadMetadata(supplier);
      };
   }

   private static ResourceMetadata loadMetadata(InputSupplier supplier) throws IOException {
      InputStream inputStream = (InputStream)supplier.get();

      ResourceMetadata var2;
      try {
         var2 = ResourceMetadata.create(inputStream);
      } catch (Throwable var5) {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (inputStream != null) {
         inputStream.close();
      }

      return var2;
   }

   private static void applyFilter(FilterablePack pack, Map idToEntryList) {
      Iterator var2 = idToEntryList.values().iterator();

      while(var2.hasNext()) {
         EntryList lv = (EntryList)var2.next();
         if (pack.isFiltered(lv.id)) {
            lv.fileSources.clear();
         } else if (pack.isFiltered(lv.metadataId())) {
            lv.metaSources.clear();
         }
      }

   }

   private void findAndAdd(FilterablePack pack, String startingPath, Predicate allowedPathPredicate, Map idToEntryList) {
      ResourcePack lv = pack.underlying;
      if (lv != null) {
         lv.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
            if (isMcmeta(id)) {
               Identifier lvx = getMetadataFileName(id);
               if (!allowedPathPredicate.test(lvx)) {
                  return;
               }

               ((EntryList)idToEntryList.computeIfAbsent(lvx, EntryList::new)).metaSources.put(lv, supplier);
            } else {
               if (!allowedPathPredicate.test(id)) {
                  return;
               }

               ((EntryList)idToEntryList.computeIfAbsent(id, EntryList::new)).fileSources.add(new FileSource(lv, supplier));
            }

         });
      }
   }

   public Map findAllResources(String startingPath, Predicate allowedPathPredicate) {
      Map map = Maps.newHashMap();
      Iterator var4 = this.packList.iterator();

      while(var4.hasNext()) {
         FilterablePack lv = (FilterablePack)var4.next();
         applyFilter(lv, map);
         this.findAndAdd(lv, startingPath, allowedPathPredicate, map);
      }

      TreeMap treeMap = Maps.newTreeMap();
      Iterator var14 = map.values().iterator();

      while(true) {
         EntryList lv2;
         do {
            if (!var14.hasNext()) {
               return treeMap;
            }

            lv2 = (EntryList)var14.next();
         } while(lv2.fileSources.isEmpty());

         List list = new ArrayList();
         Iterator var8 = lv2.fileSources.iterator();

         while(var8.hasNext()) {
            FileSource lv3 = (FileSource)var8.next();
            ResourcePack lv4 = lv3.sourcePack;
            InputSupplier lv5 = (InputSupplier)lv2.metaSources.get(lv4);
            InputSupplier lv6 = lv5 != null ? getMetadataSupplier(lv5) : ResourceMetadata.NONE_SUPPLIER;
            list.add(createResource(lv4, lv2.id, lv3.supplier, lv6));
         }

         treeMap.put(lv2.id, list);
      }
   }

   public Stream streamResourcePacks() {
      return this.packList.stream().map((pack) -> {
         return pack.underlying;
      }).filter(Objects::nonNull);
   }

   static record FilterablePack(String name, @Nullable ResourcePack underlying, @Nullable Predicate filter) {
      final String name;
      @Nullable
      final ResourcePack underlying;

      FilterablePack(String string, @Nullable ResourcePack arg, @Nullable Predicate predicate) {
         this.name = string;
         this.underlying = arg;
         this.filter = predicate;
      }

      public void removeFiltered(Collection ids) {
         if (this.filter != null) {
            ids.removeIf(this.filter);
         }

      }

      public boolean isFiltered(Identifier id) {
         return this.filter != null && this.filter.test(id);
      }

      public String name() {
         return this.name;
      }

      @Nullable
      public ResourcePack underlying() {
         return this.underlying;
      }

      @Nullable
      public Predicate filter() {
         return this.filter;
      }
   }

   static record EntryList(Identifier id, Identifier metadataId, List fileSources, Map metaSources) {
      final Identifier id;
      final List fileSources;
      final Map metaSources;

      EntryList(Identifier id) {
         this(id, NamespaceResourceManager.getMetadataPath(id), new ArrayList(), new Object2ObjectArrayMap());
      }

      private EntryList(Identifier arg, Identifier arg2, List list, Map map) {
         this.id = arg;
         this.metadataId = arg2;
         this.fileSources = list;
         this.metaSources = map;
      }

      public Identifier id() {
         return this.id;
      }

      public Identifier metadataId() {
         return this.metadataId;
      }

      public List fileSources() {
         return this.fileSources;
      }

      public Map metaSources() {
         return this.metaSources;
      }
   }

   static record FileSource(ResourcePack sourcePack, InputSupplier supplier) {
      final ResourcePack sourcePack;
      final InputSupplier supplier;

      FileSource(ResourcePack arg, InputSupplier arg2) {
         this.sourcePack = arg;
         this.supplier = arg2;
      }

      public ResourcePack sourcePack() {
         return this.sourcePack;
      }

      public InputSupplier supplier() {
         return this.supplier;
      }
   }

   static class DebugInputStream extends FilterInputStream {
      private final Supplier leakMessage;
      private boolean closed;

      public DebugInputStream(InputStream parent, Identifier id, String packName) {
         super(parent);
         Exception exception = new Exception("Stacktrace");
         this.leakMessage = () -> {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            return "Leaked resource: '" + id + "' loaded from pack: '" + packName + "'\n" + stringWriter;
         };
      }

      public void close() throws IOException {
         super.close();
         this.closed = true;
      }

      protected void finalize() throws Throwable {
         if (!this.closed) {
            NamespaceResourceManager.LOGGER.warn("{}", this.leakMessage.get());
         }

         super.finalize();
      }
   }
}
