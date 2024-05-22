/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class ServerStatHandler
extends StatHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> pendingStats = Sets.newHashSet();

    public ServerStatHandler(MinecraftServer server, File file) {
        this.server = server;
        this.file = file;
        if (file.isFile()) {
            try {
                this.parse(server.getDataFixer(), FileUtils.readFileToString(file));
            } catch (IOException iOException) {
                LOGGER.error("Couldn't read statistics file {}", (Object)file, (Object)iOException);
            } catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)file, (Object)jsonParseException);
            }
        }
    }

    public void save() {
        try {
            FileUtils.writeStringToFile(this.file, this.asString());
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save stats", iOException);
        }
    }

    @Override
    public void setStat(PlayerEntity player, Stat<?> stat, int value) {
        super.setStat(player, stat, value);
        this.pendingStats.add(stat);
    }

    private Set<Stat<?>> takePendingStats() {
        HashSet<Stat<?>> set = Sets.newHashSet(this.pendingStats);
        this.pendingStats.clear();
        return set;
    }

    public void parse(DataFixer dataFixer, String json) {
        try (JsonReader jsonReader = new JsonReader(new StringReader(json));){
            jsonReader.setLenient(false);
            JsonElement jsonElement = Streams.parse(jsonReader);
            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
                return;
            }
            NbtCompound lv = ServerStatHandler.jsonToCompound(jsonElement.getAsJsonObject());
            if ((lv = DataFixTypes.STATS.update(dataFixer, lv, NbtHelper.getDataVersion(lv, 1343))).contains("stats", NbtElement.COMPOUND_TYPE)) {
                NbtCompound lv2 = lv.getCompound("stats");
                for (String string2 : lv2.getKeys()) {
                    if (!lv2.contains(string2, NbtElement.COMPOUND_TYPE)) continue;
                    Util.ifPresentOrElse(Registries.STAT_TYPE.getOrEmpty(Identifier.method_60654(string2)), statType -> {
                        NbtCompound lv = lv2.getCompound(string2);
                        for (String string2 : lv.getKeys()) {
                            if (lv.contains(string2, NbtElement.NUMBER_TYPE)) {
                                Util.ifPresentOrElse(this.createStat((StatType)statType, string2), id -> this.statMap.put(id, lv.getInt(string2)), () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                                continue;
                            }
                            LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, lv.get(string2), string2);
                        }
                    }, () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                }
            }
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Unable to parse Stat data from {}", (Object)this.file, (Object)exception);
        }
    }

    private <T> Optional<Stat<T>> createStat(StatType<T> type, String id) {
        return Optional.ofNullable(Identifier.tryParse(id)).flatMap(type.getRegistry()::getOrEmpty).map(type::getOrCreateStat);
    }

    private static NbtCompound jsonToCompound(JsonObject json) {
        NbtCompound lv = new NbtCompound();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonPrimitive jsonPrimitive;
            JsonElement jsonElement = entry.getValue();
            if (jsonElement.isJsonObject()) {
                lv.put(entry.getKey(), ServerStatHandler.jsonToCompound(jsonElement.getAsJsonObject()));
                continue;
            }
            if (!jsonElement.isJsonPrimitive() || !(jsonPrimitive = jsonElement.getAsJsonPrimitive()).isNumber()) continue;
            lv.putInt(entry.getKey(), jsonPrimitive.getAsInt());
        }
        return lv;
    }

    protected String asString() {
        HashMap<StatType, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry entry : this.statMap.object2IntEntrySet()) {
            Stat stat = (Stat)entry.getKey();
            map.computeIfAbsent(stat.getType(), statType -> new JsonObject()).addProperty(ServerStatHandler.getStatId(stat).toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry entry : map.entrySet()) {
            jsonObject.add(Registries.STAT_TYPE.getId((StatType)entry.getKey()).toString(), (JsonElement)entry.getValue());
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("stats", jsonObject);
        jsonObject2.addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
        return jsonObject2.toString();
    }

    private static <T> Identifier getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    public void updateStatSet() {
        this.pendingStats.addAll(this.statMap.keySet());
    }

    public void sendStats(ServerPlayerEntity player) {
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        for (Stat<?> lv : this.takePendingStats()) {
            object2IntMap.put(lv, this.getStat(lv));
        }
        player.networkHandler.sendPacket(new StatisticsS2CPacket(object2IntMap));
    }
}

