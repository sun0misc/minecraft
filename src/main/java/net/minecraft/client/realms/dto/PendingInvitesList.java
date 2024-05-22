/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.PendingInvite;
import net.minecraft.client.realms.dto.ValueObject;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PendingInvitesList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<PendingInvite> pendingInvites = Lists.newArrayList();

    public static PendingInvitesList parse(String json) {
        PendingInvitesList lv = new PendingInvitesList();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            if (jsonObject.get("invites").isJsonArray()) {
                Iterator<JsonElement> iterator = jsonObject.get("invites").getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    lv.pendingInvites.add(PendingInvite.parse(iterator.next().getAsJsonObject()));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse PendingInvitesList: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

