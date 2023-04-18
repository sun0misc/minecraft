package net.minecraft.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public abstract class JsonDataLoader extends SinglePreparationResourceReloader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Gson gson;
   private final String dataType;

   public JsonDataLoader(Gson gson, String dataType) {
      this.gson = gson;
      this.dataType = dataType;
   }

   protected Map prepare(ResourceManager arg, Profiler arg2) {
      Map map = new HashMap();
      load(arg, this.dataType, this.gson, map);
      return map;
   }

   public static void load(ResourceManager manager, String dataType, Gson gson, Map results) {
      ResourceFinder lv = ResourceFinder.json(dataType);
      Iterator var5 = lv.findResources(manager).entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         Identifier lv2 = (Identifier)entry.getKey();
         Identifier lv3 = lv.toResourceId(lv2);

         try {
            Reader reader = ((Resource)entry.getValue()).getReader();

            try {
               JsonElement jsonElement = (JsonElement)JsonHelper.deserialize(gson, (Reader)reader, (Class)JsonElement.class);
               JsonElement jsonElement2 = (JsonElement)results.put(lv3, jsonElement);
               if (jsonElement2 != null) {
                  throw new IllegalStateException("Duplicate data file ignored with ID " + lv3);
               }
            } catch (Throwable var13) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (IllegalArgumentException | IOException | JsonParseException var14) {
            LOGGER.error("Couldn't parse data file {} from {}", new Object[]{lv3, lv2, var14});
         }
      }

   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.prepare(manager, profiler);
   }
}
