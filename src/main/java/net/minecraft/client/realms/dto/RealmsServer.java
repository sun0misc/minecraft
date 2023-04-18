package net.minecraft.client.realms.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.client.realms.util.RealmsUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServer extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String description;
   public State state;
   public String owner;
   public String ownerUUID;
   public List players;
   public Map slots;
   public boolean expired;
   public boolean expiredTrial;
   public int daysLeft;
   public WorldType worldType;
   public int activeSlot;
   public String minigameName;
   public int minigameId;
   public String minigameImage;
   public RealmsServerPing serverPing = new RealmsServerPing();

   public String getDescription() {
      return this.description;
   }

   public String getName() {
      return this.name;
   }

   public String getMinigameName() {
      return this.minigameName;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public void updateServerPing(RealmsServerPlayerList serverPlayerList) {
      List list = Lists.newArrayList();
      int i = 0;
      Iterator var4 = serverPlayerList.players.iterator();

      while(true) {
         String string;
         do {
            if (!var4.hasNext()) {
               this.serverPing.nrOfPlayers = String.valueOf(i);
               this.serverPing.playerList = Joiner.on('\n').join(list);
               return;
            }

            string = (String)var4.next();
         } while(string.equals(MinecraftClient.getInstance().getSession().getUuid()));

         String string2 = "";

         try {
            string2 = RealmsUtil.uuidToName(string);
         } catch (Exception var8) {
            LOGGER.error("Could not get name for {}", string, var8);
            continue;
         }

         list.add(string2);
         ++i;
      }
   }

   public static RealmsServer parse(JsonObject node) {
      RealmsServer lv = new RealmsServer();

      try {
         lv.id = JsonUtils.getLongOr("id", node, -1L);
         lv.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", node, (String)null);
         lv.name = JsonUtils.getStringOr("name", node, (String)null);
         lv.description = JsonUtils.getStringOr("motd", node, (String)null);
         lv.state = getState(JsonUtils.getStringOr("state", node, RealmsServer.State.CLOSED.name()));
         lv.owner = JsonUtils.getStringOr("owner", node, (String)null);
         if (node.get("players") != null && node.get("players").isJsonArray()) {
            lv.players = parseInvited(node.get("players").getAsJsonArray());
            sortInvited(lv);
         } else {
            lv.players = Lists.newArrayList();
         }

         lv.daysLeft = JsonUtils.getIntOr("daysLeft", node, 0);
         lv.expired = JsonUtils.getBooleanOr("expired", node, false);
         lv.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", node, false);
         lv.worldType = getWorldType(JsonUtils.getStringOr("worldType", node, RealmsServer.WorldType.NORMAL.name()));
         lv.ownerUUID = JsonUtils.getStringOr("ownerUUID", node, "");
         if (node.get("slots") != null && node.get("slots").isJsonArray()) {
            lv.slots = parseSlots(node.get("slots").getAsJsonArray());
         } else {
            lv.slots = getEmptySlots();
         }

         lv.minigameName = JsonUtils.getStringOr("minigameName", node, (String)null);
         lv.activeSlot = JsonUtils.getIntOr("activeSlot", node, -1);
         lv.minigameId = JsonUtils.getIntOr("minigameId", node, -1);
         lv.minigameImage = JsonUtils.getStringOr("minigameImage", node, (String)null);
      } catch (Exception var3) {
         LOGGER.error("Could not parse McoServer: {}", var3.getMessage());
      }

      return lv;
   }

   private static void sortInvited(RealmsServer server) {
      server.players.sort((a, b) -> {
         return ComparisonChain.start().compareFalseFirst(b.isAccepted(), a.isAccepted()).compare(a.getName().toLowerCase(Locale.ROOT), b.getName().toLowerCase(Locale.ROOT)).result();
      });
   }

   private static List parseInvited(JsonArray jsonArray) {
      List list = Lists.newArrayList();
      Iterator var2 = jsonArray.iterator();

      while(var2.hasNext()) {
         JsonElement jsonElement = (JsonElement)var2.next();

         try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            PlayerInfo lv = new PlayerInfo();
            lv.setName(JsonUtils.getStringOr("name", jsonObject, (String)null));
            lv.setUuid(JsonUtils.getStringOr("uuid", jsonObject, (String)null));
            lv.setOperator(JsonUtils.getBooleanOr("operator", jsonObject, false));
            lv.setAccepted(JsonUtils.getBooleanOr("accepted", jsonObject, false));
            lv.setOnline(JsonUtils.getBooleanOr("online", jsonObject, false));
            list.add(lv);
         } catch (Exception var6) {
         }
      }

      return list;
   }

   private static Map parseSlots(JsonArray json) {
      Map map = Maps.newHashMap();
      Iterator var2 = json.iterator();

      while(var2.hasNext()) {
         JsonElement jsonElement = (JsonElement)var2.next();

         try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement2 = jsonParser.parse(jsonObject.get("options").getAsString());
            RealmsWorldOptions lv;
            if (jsonElement2 == null) {
               lv = RealmsWorldOptions.getDefaults();
            } else {
               lv = RealmsWorldOptions.parse(jsonElement2.getAsJsonObject());
            }

            int i = JsonUtils.getIntOr("slotId", jsonObject, -1);
            map.put(i, lv);
         } catch (Exception var9) {
         }
      }

      for(int j = 1; j <= 3; ++j) {
         if (!map.containsKey(j)) {
            map.put(j, RealmsWorldOptions.getEmptyDefaults());
         }
      }

      return map;
   }

   private static Map getEmptySlots() {
      Map map = Maps.newHashMap();
      map.put(1, RealmsWorldOptions.getEmptyDefaults());
      map.put(2, RealmsWorldOptions.getEmptyDefaults());
      map.put(3, RealmsWorldOptions.getEmptyDefaults());
      return map;
   }

   public static RealmsServer parse(String json) {
      try {
         return parse((new JsonParser()).parse(json).getAsJsonObject());
      } catch (Exception var2) {
         LOGGER.error("Could not parse McoServer: {}", var2.getMessage());
         return new RealmsServer();
      }
   }

   private static State getState(String state) {
      try {
         return RealmsServer.State.valueOf(state);
      } catch (Exception var2) {
         return RealmsServer.State.CLOSED;
      }
   }

   private static WorldType getWorldType(String state) {
      try {
         return RealmsServer.WorldType.valueOf(state);
      } catch (Exception var2) {
         return RealmsServer.WorldType.NORMAL;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name, this.description, this.state, this.owner, this.expired});
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         RealmsServer lv = (RealmsServer)o;
         return (new EqualsBuilder()).append(this.id, lv.id).append(this.name, lv.name).append(this.description, lv.description).append(this.state, lv.state).append(this.owner, lv.owner).append(this.expired, lv.expired).append(this.worldType, this.worldType).isEquals();
      }
   }

   public RealmsServer clone() {
      RealmsServer lv = new RealmsServer();
      lv.id = this.id;
      lv.remoteSubscriptionId = this.remoteSubscriptionId;
      lv.name = this.name;
      lv.description = this.description;
      lv.state = this.state;
      lv.owner = this.owner;
      lv.players = this.players;
      lv.slots = this.cloneSlots(this.slots);
      lv.expired = this.expired;
      lv.expiredTrial = this.expiredTrial;
      lv.daysLeft = this.daysLeft;
      lv.serverPing = new RealmsServerPing();
      lv.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
      lv.serverPing.playerList = this.serverPing.playerList;
      lv.worldType = this.worldType;
      lv.ownerUUID = this.ownerUUID;
      lv.minigameName = this.minigameName;
      lv.activeSlot = this.activeSlot;
      lv.minigameId = this.minigameId;
      lv.minigameImage = this.minigameImage;
      return lv;
   }

   public Map cloneSlots(Map slots) {
      Map map2 = Maps.newHashMap();
      Iterator var3 = slots.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         map2.put((Integer)entry.getKey(), ((RealmsWorldOptions)entry.getValue()).clone());
      }

      return map2;
   }

   public String getWorldName(int slotId) {
      return this.name + " (" + ((RealmsWorldOptions)this.slots.get(slotId)).getSlotName(slotId) + ")";
   }

   public ServerInfo createServerInfo(String address) {
      return new ServerInfo(this.name, address, false);
   }

   // $FF: synthetic method
   public Object clone() throws CloneNotSupportedException {
      return this.clone();
   }

   @Environment(EnvType.CLIENT)
   public static enum State {
      CLOSED,
      OPEN,
      UNINITIALIZED;

      // $FF: synthetic method
      private static State[] method_36848() {
         return new State[]{CLOSED, OPEN, UNINITIALIZED};
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum WorldType {
      NORMAL,
      MINIGAME,
      ADVENTUREMAP,
      EXPERIENCE,
      INSPIRATION;

      // $FF: synthetic method
      private static WorldType[] method_36849() {
         return new WorldType[]{NORMAL, MINIGAME, ADVENTUREMAP, EXPERIENCE, INSPIRATION};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class McoServerComparator implements Comparator {
      private final String refOwner;

      public McoServerComparator(String owner) {
         this.refOwner = owner;
      }

      public int compare(RealmsServer arg, RealmsServer arg2) {
         return ComparisonChain.start().compareTrueFirst(arg.state == RealmsServer.State.UNINITIALIZED, arg2.state == RealmsServer.State.UNINITIALIZED).compareTrueFirst(arg.expiredTrial, arg2.expiredTrial).compareTrueFirst(arg.owner.equals(this.refOwner), arg2.owner.equals(this.refOwner)).compareFalseFirst(arg.expired, arg2.expired).compareTrueFirst(arg.state == RealmsServer.State.OPEN, arg2.state == RealmsServer.State.OPEN).compare(arg.id, arg2.id).result();
      }

      // $FF: synthetic method
      public int compare(Object one, Object two) {
         return this.compare((RealmsServer)one, (RealmsServer)two);
      }
   }
}
