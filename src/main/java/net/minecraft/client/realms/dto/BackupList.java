package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BackupList extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public List backups;

   public static BackupList parse(String json) {
      JsonParser jsonParser = new JsonParser();
      BackupList lv = new BackupList();
      lv.backups = Lists.newArrayList();

      try {
         JsonElement jsonElement = jsonParser.parse(json).getAsJsonObject().get("backups");
         if (jsonElement.isJsonArray()) {
            Iterator iterator = jsonElement.getAsJsonArray().iterator();

            while(iterator.hasNext()) {
               lv.backups.add(Backup.parse((JsonElement)iterator.next()));
            }
         }
      } catch (Exception var5) {
         LOGGER.error("Could not parse BackupList: {}", var5.getMessage());
      }

      return lv;
   }
}
