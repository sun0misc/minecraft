package net.minecraft.client.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class AtlasLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceFinder FINDER = new ResourceFinder("atlases", ".json");
   private final List sources;

   private AtlasLoader(List sources) {
      this.sources = sources;
   }

   public List loadSources(ResourceManager resourceManager) {
      final Map map = new HashMap();
      AtlasSource.SpriteRegions lv = new AtlasSource.SpriteRegions() {
         public void add(Identifier arg, AtlasSource.SpriteRegion region) {
            AtlasSource.SpriteRegion lv = (AtlasSource.SpriteRegion)map.put(arg, region);
            if (lv != null) {
               lv.close();
            }

         }

         public void removeIf(Predicate predicate) {
            Iterator iterator = map.entrySet().iterator();

            while(iterator.hasNext()) {
               Map.Entry entry = (Map.Entry)iterator.next();
               if (predicate.test((Identifier)entry.getKey())) {
                  ((AtlasSource.SpriteRegion)entry.getValue()).close();
                  iterator.remove();
               }
            }

         }
      };
      this.sources.forEach((source) -> {
         source.load(resourceManager, lv);
      });
      ImmutableList.Builder builder = ImmutableList.builder();
      builder.add(MissingSprite::createSpriteContents);
      builder.addAll(map.values());
      return builder.build();
   }

   public static AtlasLoader of(ResourceManager resourceManager, Identifier id) {
      Identifier lv = FINDER.toResourcePath(id);
      List list = new ArrayList();
      Iterator var4 = resourceManager.getAllResources(lv).iterator();

      while(var4.hasNext()) {
         Resource lv2 = (Resource)var4.next();

         try {
            BufferedReader bufferedReader = lv2.getReader();

            try {
               Dynamic dynamic = new Dynamic(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader));
               DataResult var10001 = AtlasSourceManager.LIST_CODEC.parse(dynamic);
               Logger var10003 = LOGGER;
               Objects.requireNonNull(var10003);
               list.addAll((Collection)var10001.getOrThrow(false, var10003::error));
            } catch (Throwable var10) {
               if (bufferedReader != null) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (bufferedReader != null) {
               bufferedReader.close();
            }
         } catch (Exception var11) {
            LOGGER.warn("Failed to parse atlas definition {} in pack {}", new Object[]{lv, lv2.getResourcePackName(), var11});
         }
      }

      return new AtlasLoader(list);
   }
}
