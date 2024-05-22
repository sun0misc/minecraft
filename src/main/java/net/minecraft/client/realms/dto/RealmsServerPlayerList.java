/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServerPlayerList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public Map<Long, List<ProfileResult>> field_52121;

    public static RealmsServerPlayerList parse(String string) {
        RealmsServerPlayerList lv = new RealmsServerPlayerList();
        ImmutableMap.Builder builder = ImmutableMap.builder();
        try {
            JsonObject jsonObject = JsonHelper.deserialize(string);
            if (JsonHelper.hasArray(jsonObject, "lists")) {
                JsonArray jsonArray = jsonObject.getAsJsonArray("lists");
                for (JsonElement jsonElement : jsonArray) {
                    JsonElement jsonElement2;
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    String string2 = JsonUtils.getNullableStringOr("playerList", jsonObject2, null);
                    List<Object> list = string2 != null ? ((jsonElement2 = JsonParser.parseString(string2)).isJsonArray() ? RealmsServerPlayerList.parsePlayers(jsonElement2.getAsJsonArray()) : Lists.newArrayList()) : Lists.newArrayList();
                    builder.put(JsonUtils.getLongOr("serverId", jsonObject2, -1L), list);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerPlayerLists: {}", (Object)exception.getMessage());
        }
        lv.field_52121 = builder.build();
        return lv;
    }

    private static List<ProfileResult> parsePlayers(JsonArray jsonArray) {
        ArrayList<ProfileResult> list = new ArrayList<ProfileResult>(jsonArray.size());
        MinecraftSessionService minecraftSessionService = MinecraftClient.getInstance().getSessionService();
        for (JsonElement jsonElement : jsonArray) {
            UUID uUID;
            if (!jsonElement.isJsonObject() || (uUID = JsonUtils.getUuidOr("playerId", jsonElement.getAsJsonObject(), null)) == null || MinecraftClient.getInstance().uuidEquals(uUID)) continue;
            try {
                ProfileResult profileResult = minecraftSessionService.fetchProfile(uUID, false);
                if (profileResult == null) continue;
                list.add(profileResult);
            } catch (Exception exception) {
                LOGGER.error("Could not get name for {}", (Object)uUID, (Object)exception);
            }
        }
        return list;
    }

    public List<ProfileResult> method_60863(long l) {
        List<ProfileResult> list = this.field_52121.get(l);
        if (list != null) {
            return list;
        }
        return List.of();
    }
}

