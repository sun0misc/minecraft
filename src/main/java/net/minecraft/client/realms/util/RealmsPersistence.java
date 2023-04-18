package net.minecraft.client.realms.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPersistence {
   private static final String FILE_NAME = "realms_persistence.json";
   private static final CheckedGson CHECKED_GSON = new CheckedGson();
   private static final Logger LOGGER = LogUtils.getLogger();

   public RealmsPersistenceData load() {
      return readFile();
   }

   public void save(RealmsPersistenceData data) {
      writeFile(data);
   }

   public static RealmsPersistenceData readFile() {
      File file = getFile();

      try {
         String string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
         RealmsPersistenceData lv = (RealmsPersistenceData)CHECKED_GSON.fromJson(string, RealmsPersistenceData.class);
         if (lv != null) {
            return lv;
         }
      } catch (FileNotFoundException var3) {
      } catch (Exception var4) {
         LOGGER.warn("Failed to read Realms storage {}", file, var4);
      }

      return new RealmsPersistenceData();
   }

   public static void writeFile(RealmsPersistenceData data) {
      File file = getFile();

      try {
         FileUtils.writeStringToFile(file, CHECKED_GSON.toJson((RealmsSerializable)data), StandardCharsets.UTF_8);
      } catch (IOException var3) {
      }

   }

   private static File getFile() {
      return new File(MinecraftClient.getInstance().runDirectory, "realms_persistence.json");
   }

   @Environment(EnvType.CLIENT)
   public static class RealmsPersistenceData implements RealmsSerializable {
      @SerializedName("newsLink")
      public String newsLink;
      @SerializedName("hasUnreadNews")
      public boolean hasUnreadNews;
   }
}
