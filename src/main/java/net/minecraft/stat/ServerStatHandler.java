package net.minecraft.stat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class ServerStatHandler extends StatHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftServer server;
   private final File file;
   private final Set pendingStats = Sets.newHashSet();

   public ServerStatHandler(MinecraftServer server, File file) {
      this.server = server;
      this.file = file;
      if (file.isFile()) {
         try {
            this.parse(server.getDataFixer(), FileUtils.readFileToString(file));
         } catch (IOException var4) {
            LOGGER.error("Couldn't read statistics file {}", file, var4);
         } catch (JsonParseException var5) {
            LOGGER.error("Couldn't parse statistics file {}", file, var5);
         }
      }

   }

   public void save() {
      try {
         FileUtils.writeStringToFile(this.file, this.asString());
      } catch (IOException var2) {
         LOGGER.error("Couldn't save stats", var2);
      }

   }

   public void setStat(PlayerEntity player, Stat stat, int value) {
      super.setStat(player, stat, value);
      this.pendingStats.add(stat);
   }

   private Set takePendingStats() {
      Set set = Sets.newHashSet(this.pendingStats);
      this.pendingStats.clear();
      return set;
   }

   public void parse(DataFixer dataFixer, String json) {
      try {
         JsonReader jsonReader = new JsonReader(new StringReader(json));

         label62: {
            try {
               jsonReader.setLenient(false);
               JsonElement jsonElement = Streams.parse(jsonReader);
               if (jsonElement.isJsonNull()) {
                  LOGGER.error("Unable to parse Stat data from {}", this.file);
                  break label62;
               }

               NbtCompound lv = jsonToCompound(jsonElement.getAsJsonObject());
               lv = DataFixTypes.STATS.update(dataFixer, lv, NbtHelper.getDataVersion(lv, 1343));
               if (lv.contains("stats", NbtElement.COMPOUND_TYPE)) {
                  NbtCompound lv2 = lv.getCompound("stats");
                  Iterator var7 = lv2.getKeys().iterator();

                  while(var7.hasNext()) {
                     String string2 = (String)var7.next();
                     if (lv2.contains(string2, NbtElement.COMPOUND_TYPE)) {
                        Util.ifPresentOrElse(Registries.STAT_TYPE.getOrEmpty(new Identifier(string2)), (statType) -> {
                           NbtCompound lv = lv2.getCompound(string2);
                           Iterator var5 = lv.getKeys().iterator();

                           while(var5.hasNext()) {
                              String string2x = (String)var5.next();
                              if (lv.contains(string2x, NbtElement.NUMBER_TYPE)) {
                                 Util.ifPresentOrElse(this.createStat(statType, string2x), (id) -> {
                                    this.statMap.put(id, lv.getInt(string2x));
                                 }, () -> {
                                    LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, string2x);
                                 });
                              } else {
                                 LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", new Object[]{this.file, lv.get(string2x), string2x});
                              }
                           }

                        }, () -> {
                           LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, string2);
                        });
                     }
                  }
               }
            } catch (Throwable var10) {
               try {
                  jsonReader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            jsonReader.close();
            return;
         }

         jsonReader.close();
      } catch (IOException | JsonParseException var11) {
         LOGGER.error("Unable to parse Stat data from {}", this.file, var11);
      }
   }

   private Optional createStat(StatType type, String id) {
      Optional var10000 = Optional.ofNullable(Identifier.tryParse(id));
      Registry var10001 = type.getRegistry();
      Objects.requireNonNull(var10001);
      var10000 = var10000.flatMap(var10001::getOrEmpty);
      Objects.requireNonNull(type);
      return var10000.map(type::getOrCreateStat);
   }

   private static NbtCompound jsonToCompound(JsonObject json) {
      NbtCompound lv = new NbtCompound();
      Iterator var2 = json.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         JsonElement jsonElement = (JsonElement)entry.getValue();
         if (jsonElement.isJsonObject()) {
            lv.put((String)entry.getKey(), jsonToCompound(jsonElement.getAsJsonObject()));
         } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isNumber()) {
               lv.putInt((String)entry.getKey(), jsonPrimitive.getAsInt());
            }
         }
      }

      return lv;
   }

   protected String asString() {
      Map map = Maps.newHashMap();
      ObjectIterator var2 = this.statMap.object2IntEntrySet().iterator();

      while(var2.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)var2.next();
         Stat lv = (Stat)entry.getKey();
         ((JsonObject)map.computeIfAbsent(lv.getType(), (statType) -> {
            return new JsonObject();
         })).addProperty(getStatId(lv).toString(), entry.getIntValue());
      }

      JsonObject jsonObject = new JsonObject();
      Iterator var6 = map.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry2 = (Map.Entry)var6.next();
         jsonObject.add(Registries.STAT_TYPE.getId((StatType)entry2.getKey()).toString(), (JsonElement)entry2.getValue());
      }

      JsonObject jsonObject2 = new JsonObject();
      jsonObject2.add("stats", jsonObject);
      jsonObject2.addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
      return jsonObject2.toString();
   }

   private static Identifier getStatId(Stat stat) {
      return stat.getType().getRegistry().getId(stat.getValue());
   }

   public void updateStatSet() {
      this.pendingStats.addAll(this.statMap.keySet());
   }

   public void sendStats(ServerPlayerEntity player) {
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      Iterator var3 = this.takePendingStats().iterator();

      while(var3.hasNext()) {
         Stat lv = (Stat)var3.next();
         object2IntMap.put(lv, this.getStat(lv));
      }

      player.networkHandler.sendPacket(new StatisticsS2CPacket(object2IntMap));
   }
}
