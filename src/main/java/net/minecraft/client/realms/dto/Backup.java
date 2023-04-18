package net.minecraft.client.realms.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.JsonUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Backup extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String backupId;
   public Date lastModifiedDate;
   public long size;
   private boolean uploadedVersion;
   public Map metadata = Maps.newHashMap();
   public Map changeList = Maps.newHashMap();

   public static Backup parse(JsonElement node) {
      JsonObject jsonObject = node.getAsJsonObject();
      Backup lv = new Backup();

      try {
         lv.backupId = JsonUtils.getStringOr("backupId", jsonObject, "");
         lv.lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", jsonObject);
         lv.size = JsonUtils.getLongOr("size", jsonObject, 0L);
         if (jsonObject.has("metadata")) {
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("metadata");
            Set set = jsonObject2.entrySet();
            Iterator var5 = set.iterator();

            while(var5.hasNext()) {
               Map.Entry entry = (Map.Entry)var5.next();
               if (!((JsonElement)entry.getValue()).isJsonNull()) {
                  lv.metadata.put(format((String)entry.getKey()), ((JsonElement)entry.getValue()).getAsString());
               }
            }
         }
      } catch (Exception var7) {
         LOGGER.error("Could not parse Backup: {}", var7.getMessage());
      }

      return lv;
   }

   private static String format(String key) {
      String[] strings = key.split("_");
      StringBuilder stringBuilder = new StringBuilder();
      String[] var3 = strings;
      int var4 = strings.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String string2 = var3[var5];
         if (string2 != null && string2.length() >= 1) {
            if ("of".equals(string2)) {
               stringBuilder.append(string2).append(" ");
            } else {
               char c = Character.toUpperCase(string2.charAt(0));
               stringBuilder.append(c).append(string2.substring(1)).append(" ");
            }
         }
      }

      return stringBuilder.toString();
   }

   public boolean isUploadedVersion() {
      return this.uploadedVersion;
   }

   public void setUploadedVersion(boolean uploadedVersion) {
      this.uploadedVersion = uploadedVersion;
   }
}
