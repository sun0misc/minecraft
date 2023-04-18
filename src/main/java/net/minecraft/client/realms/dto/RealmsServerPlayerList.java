package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.JsonUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerPlayerList extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final JsonParser JSON_PARSER = new JsonParser();
   public long serverId;
   public List players;

   public static RealmsServerPlayerList parse(JsonObject node) {
      RealmsServerPlayerList lv = new RealmsServerPlayerList();

      try {
         lv.serverId = JsonUtils.getLongOr("serverId", node, -1L);
         String string = JsonUtils.getStringOr("playerList", node, (String)null);
         if (string != null) {
            JsonElement jsonElement = JSON_PARSER.parse(string);
            if (jsonElement.isJsonArray()) {
               lv.players = parsePlayers(jsonElement.getAsJsonArray());
            } else {
               lv.players = Lists.newArrayList();
            }
         } else {
            lv.players = Lists.newArrayList();
         }
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsServerPlayerList: {}", var4.getMessage());
      }

      return lv;
   }

   private static List parsePlayers(JsonArray jsonArray) {
      List list = Lists.newArrayList();
      Iterator var2 = jsonArray.iterator();

      while(var2.hasNext()) {
         JsonElement jsonElement = (JsonElement)var2.next();

         try {
            list.add(jsonElement.getAsString());
         } catch (Exception var5) {
         }
      }

      return list;
   }
}
