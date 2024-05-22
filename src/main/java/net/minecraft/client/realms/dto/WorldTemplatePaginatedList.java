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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.util.JsonUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldTemplatePaginatedList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<WorldTemplate> templates;
    public int page;
    public int size;
    public int total;

    public WorldTemplatePaginatedList() {
    }

    public WorldTemplatePaginatedList(int size) {
        this.templates = Collections.emptyList();
        this.page = 0;
        this.size = size;
        this.total = -1;
    }

    public boolean isLastPage() {
        return this.page * this.size >= this.total && this.page > 0 && this.total > 0 && this.size > 0;
    }

    public static WorldTemplatePaginatedList parse(String json) {
        WorldTemplatePaginatedList lv = new WorldTemplatePaginatedList();
        lv.templates = Lists.newArrayList();
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
            if (jsonObject.get("templates").isJsonArray()) {
                Iterator<JsonElement> iterator = jsonObject.get("templates").getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    lv.templates.add(WorldTemplate.parse(iterator.next().getAsJsonObject()));
                }
            }
            lv.page = JsonUtils.getIntOr("page", jsonObject, 0);
            lv.size = JsonUtils.getIntOr("size", jsonObject, 0);
            lv.total = JsonUtils.getIntOr("total", jsonObject, 0);
        } catch (Exception exception) {
            LOGGER.error("Could not parse WorldTemplatePaginatedList: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

