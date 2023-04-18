package net.minecraft.client.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String FONTS_JSON = "fonts.json";
   public static final Identifier MISSING_STORAGE_ID = new Identifier("minecraft", "missing");
   static final ResourceFinder FINDER = ResourceFinder.json("font");
   private final FontStorage missingStorage;
   final Map fontStorages = Maps.newHashMap();
   final TextureManager textureManager;
   private Map idOverrides = ImmutableMap.of();
   private final ResourceReloader resourceReloadListener = new SinglePreparationResourceReloader() {
      protected Map prepare(ResourceManager arg, Profiler arg2) {
         arg2.startTick();
         Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
         Map map = Maps.newHashMap();
         Iterator var5 = FontManager.FINDER.findAllResources(arg).entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            Identifier lv = (Identifier)entry.getKey();
            Identifier lv2 = FontManager.FINDER.toResourceId(lv);
            List list = (List)map.computeIfAbsent(lv2, (id) -> {
               return Lists.newArrayList(new Font[]{new BlankFont()});
            });
            Objects.requireNonNull(lv2);
            arg2.push(lv2::toString);

            for(Iterator var10 = ((List)entry.getValue()).iterator(); var10.hasNext(); arg2.pop()) {
               Resource lv3 = (Resource)var10.next();
               arg2.push(lv3.getResourcePackName());

               try {
                  Reader reader = lv3.getReader();

                  try {
                     try {
                        arg2.push("reading");
                        JsonArray jsonArray = JsonHelper.getArray((JsonObject)JsonHelper.deserialize(gson, (Reader)reader, (Class)JsonObject.class), "providers");
                        arg2.swap("parsing");

                        for(int i = jsonArray.size() - 1; i >= 0; --i) {
                           JsonObject jsonObject = JsonHelper.asObject(jsonArray.get(i), "providers[" + i + "]");
                           String string = JsonHelper.getString(jsonObject, "type");
                           FontType lv4 = FontType.byId(string);

                           try {
                              arg2.push(string);
                              Font lv5 = lv4.createLoader(jsonObject).load(arg);
                              if (lv5 != null) {
                                 list.add(lv5);
                              }
                           } finally {
                              arg2.pop();
                           }
                        }
                     } finally {
                        arg2.pop();
                     }
                  } catch (Throwable var34) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var31) {
                           var34.addSuppressed(var31);
                        }
                     }

                     throw var34;
                  }

                  if (reader != null) {
                     reader.close();
                  }
               } catch (Exception var35) {
                  FontManager.LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", new Object[]{lv2, "fonts.json", lv3.getResourcePackName(), var35});
               }
            }

            arg2.push("caching");
            IntSet intSet = new IntOpenHashSet();
            Iterator var37 = list.iterator();

            while(var37.hasNext()) {
               Font lv6 = (Font)var37.next();
               intSet.addAll(lv6.getProvidedGlyphs());
            }

            intSet.forEach((codePoint) -> {
               if (codePoint != 32) {
                  Iterator var2 = Lists.reverse(list).iterator();

                  while(var2.hasNext()) {
                     Font lv = (Font)var2.next();
                     if (lv.getGlyph(codePoint) != null) {
                        break;
                     }
                  }

               }
            });
            arg2.pop();
            arg2.pop();
         }

         arg2.endTick();
         return map;
      }

      protected void apply(Map map, ResourceManager arg, Profiler arg2) {
         arg2.startTick();
         arg2.push("closing");
         FontManager.this.fontStorages.values().forEach(FontStorage::close);
         FontManager.this.fontStorages.clear();
         arg2.swap("reloading");
         map.forEach((id, fonts) -> {
            FontStorage lv = new FontStorage(FontManager.this.textureManager, id);
            lv.setFonts(Lists.reverse(fonts));
            FontManager.this.fontStorages.put(id, lv);
         });
         arg2.pop();
         arg2.endTick();
      }

      public String getName() {
         return "FontManager";
      }

      // $FF: synthetic method
      protected Object prepare(ResourceManager manager, Profiler profiler) {
         return this.prepare(manager, profiler);
      }
   };

   public FontManager(TextureManager manager) {
      this.textureManager = manager;
      this.missingStorage = (FontStorage)Util.make(new FontStorage(manager, MISSING_STORAGE_ID), (fontStorage) -> {
         fontStorage.setFonts(Lists.newArrayList(new Font[]{new BlankFont()}));
      });
   }

   public void setIdOverrides(Map idOverrides) {
      this.idOverrides = idOverrides;
   }

   public TextRenderer createTextRenderer() {
      return new TextRenderer((id) -> {
         return (FontStorage)this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(id, id), this.missingStorage);
      }, false);
   }

   public TextRenderer createAdvanceValidatingTextRenderer() {
      return new TextRenderer((id) -> {
         return (FontStorage)this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(id, id), this.missingStorage);
      }, true);
   }

   public ResourceReloader getResourceReloadListener() {
      return this.resourceReloadListener;
   }

   public void close() {
      this.fontStorages.values().forEach(FontStorage::close);
      this.missingStorage.close();
   }
}
