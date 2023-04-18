package net.minecraft.client.resource;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.fs.ResourceFileSystem;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ResourceIndex {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Splitter SEPARATOR_SPLITTER = Splitter.on('/');

   public static Path buildFileSystem(Path assetsDir, String indexName) {
      Path path2 = assetsDir.resolve("objects");
      ResourceFileSystem.Builder lv = ResourceFileSystem.builder();
      Path path3 = assetsDir.resolve("indexes/" + indexName + ".json");

      try {
         BufferedReader bufferedReader = Files.newBufferedReader(path3, StandardCharsets.UTF_8);

         try {
            JsonObject jsonObject = JsonHelper.deserialize((Reader)bufferedReader);
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "objects", (JsonObject)null);
            if (jsonObject2 != null) {
               Iterator var8 = jsonObject2.entrySet().iterator();

               while(var8.hasNext()) {
                  Map.Entry entry = (Map.Entry)var8.next();
                  JsonObject jsonObject3 = (JsonObject)entry.getValue();
                  String string2 = (String)entry.getKey();
                  List list = SEPARATOR_SPLITTER.splitToList(string2);
                  String string3 = JsonHelper.getString(jsonObject3, "hash");
                  String var10001 = string3.substring(0, 2);
                  Path path4 = path2.resolve(var10001 + "/" + string3);
                  lv.withFile(list, path4);
               }
            }
         } catch (Throwable var16) {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }
      } catch (JsonParseException var17) {
         LOGGER.error("Unable to parse resource index file: {}", path3);
      } catch (IOException var18) {
         LOGGER.error("Can't open the resource index file: {}", path3);
      }

      return lv.build("index-" + indexName).getPath("/");
   }
}
