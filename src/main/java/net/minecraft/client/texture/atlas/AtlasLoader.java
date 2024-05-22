/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class AtlasLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceFinder FINDER = new ResourceFinder("atlases", ".json");
    private final List<AtlasSource> sources;

    private AtlasLoader(List<AtlasSource> sources) {
        this.sources = sources;
    }

    public List<Function<SpriteOpener, SpriteContents>> loadSources(ResourceManager resourceManager) {
        final HashMap map = new HashMap();
        AtlasSource.SpriteRegions lv = new AtlasSource.SpriteRegions(){

            @Override
            public void add(Identifier arg, AtlasSource.SpriteRegion region) {
                AtlasSource.SpriteRegion lv = map.put(arg, region);
                if (lv != null) {
                    lv.close();
                }
            }

            @Override
            public void removeIf(Predicate<Identifier> predicate) {
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    if (!predicate.test((Identifier)entry.getKey())) continue;
                    ((AtlasSource.SpriteRegion)entry.getValue()).close();
                    iterator.remove();
                }
            }
        };
        this.sources.forEach(source -> source.load(resourceManager, lv));
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(opener -> MissingSprite.createSpriteContents());
        builder.addAll((Iterable)map.values());
        return builder.build();
    }

    public static AtlasLoader of(ResourceManager resourceManager, Identifier id) {
        Identifier lv = FINDER.toResourcePath(id);
        ArrayList<AtlasSource> list = new ArrayList<AtlasSource>();
        for (Resource lv2 : resourceManager.getAllResources(lv)) {
            try {
                BufferedReader bufferedReader = lv2.getReader();
                try {
                    Dynamic<JsonElement> dynamic = new Dynamic<JsonElement>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader));
                    list.addAll((Collection)AtlasSourceManager.LIST_CODEC.parse(dynamic).getOrThrow());
                } finally {
                    if (bufferedReader == null) continue;
                    bufferedReader.close();
                }
            } catch (Exception exception) {
                LOGGER.error("Failed to parse atlas definition {} in pack {}", lv, lv2.getPackId(), exception);
            }
        }
        return new AtlasLoader(list);
    }
}

