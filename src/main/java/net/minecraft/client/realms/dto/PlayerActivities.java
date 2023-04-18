package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.JsonUtils;

@Environment(EnvType.CLIENT)
public class PlayerActivities extends ValueObject {
   public long periodInMillis;
   public List playerActivityDto = Lists.newArrayList();

   public static PlayerActivities parse(String json) {
      PlayerActivities lv = new PlayerActivities();
      JsonParser jsonParser = new JsonParser();

      try {
         JsonElement jsonElement = jsonParser.parse(json);
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         lv.periodInMillis = JsonUtils.getLongOr("periodInMillis", jsonObject, -1L);
         JsonElement jsonElement2 = jsonObject.get("playerActivityDto");
         if (jsonElement2 != null && jsonElement2.isJsonArray()) {
            JsonArray jsonArray = jsonElement2.getAsJsonArray();
            Iterator var7 = jsonArray.iterator();

            while(var7.hasNext()) {
               JsonElement jsonElement3 = (JsonElement)var7.next();
               PlayerActivity lv2 = PlayerActivity.parse(jsonElement3.getAsJsonObject());
               lv.playerActivityDto.add(lv2);
            }
         }
      } catch (Exception var10) {
      }

      return lv;
   }
}
