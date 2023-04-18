package net.minecraft.resource;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resource.metadata.ResourceFilter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LifecycledResourceManagerImpl implements LifecycledResourceManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map subManagers;
   private final List packs;

   public LifecycledResourceManagerImpl(ResourceType type, List packs) {
      this.packs = List.copyOf(packs);
      Map map = new HashMap();
      List list2 = packs.stream().flatMap((pack) -> {
         return pack.getNamespaces(type).stream();
      }).distinct().toList();
      Iterator var5 = packs.iterator();

      label57:
      while(var5.hasNext()) {
         ResourcePack lv = (ResourcePack)var5.next();
         ResourceFilter lv2 = this.parseResourceFilter(lv);
         Set set = lv.getNamespaces(type);
         Predicate predicate = lv2 != null ? (id) -> {
            return lv2.isPathBlocked(id.getPath());
         } : null;
         Iterator var10 = list2.iterator();

         while(true) {
            while(true) {
               String string;
               boolean bl;
               boolean bl2;
               do {
                  if (!var10.hasNext()) {
                     continue label57;
                  }

                  string = (String)var10.next();
                  bl = set.contains(string);
                  bl2 = lv2 != null && lv2.isNamespaceBlocked(string);
               } while(!bl && !bl2);

               NamespaceResourceManager lv3 = (NamespaceResourceManager)map.get(string);
               if (lv3 == null) {
                  lv3 = new NamespaceResourceManager(type, string);
                  map.put(string, lv3);
               }

               if (bl && bl2) {
                  lv3.addPack(lv, predicate);
               } else if (bl) {
                  lv3.addPack(lv);
               } else {
                  lv3.addPack(lv.getName(), predicate);
               }
            }
         }
      }

      this.subManagers = map;
   }

   @Nullable
   private ResourceFilter parseResourceFilter(ResourcePack pack) {
      try {
         return (ResourceFilter)pack.parseMetadata(ResourceFilter.SERIALIZER);
      } catch (IOException var3) {
         LOGGER.error("Failed to get filter section from pack {}", pack.getName());
         return null;
      }
   }

   public Set getAllNamespaces() {
      return this.subManagers.keySet();
   }

   public Optional getResource(Identifier id) {
      ResourceManager lv = (ResourceManager)this.subManagers.get(id.getNamespace());
      return lv != null ? lv.getResource(id) : Optional.empty();
   }

   public List getAllResources(Identifier id) {
      ResourceManager lv = (ResourceManager)this.subManagers.get(id.getNamespace());
      return lv != null ? lv.getAllResources(id) : List.of();
   }

   public Map findResources(String startingPath, Predicate allowedPathPredicate) {
      validateStartingPath(startingPath);
      Map map = new TreeMap();
      Iterator var4 = this.subManagers.values().iterator();

      while(var4.hasNext()) {
         NamespaceResourceManager lv = (NamespaceResourceManager)var4.next();
         map.putAll(lv.findResources(startingPath, allowedPathPredicate));
      }

      return map;
   }

   public Map findAllResources(String startingPath, Predicate allowedPathPredicate) {
      validateStartingPath(startingPath);
      Map map = new TreeMap();
      Iterator var4 = this.subManagers.values().iterator();

      while(var4.hasNext()) {
         NamespaceResourceManager lv = (NamespaceResourceManager)var4.next();
         map.putAll(lv.findAllResources(startingPath, allowedPathPredicate));
      }

      return map;
   }

   private static void validateStartingPath(String startingPath) {
      if (startingPath.endsWith("/")) {
         throw new IllegalArgumentException("Trailing slash in path " + startingPath);
      }
   }

   public Stream streamResourcePacks() {
      return this.packs.stream();
   }

   public void close() {
      this.packs.forEach(ResourcePack::close);
   }
}
