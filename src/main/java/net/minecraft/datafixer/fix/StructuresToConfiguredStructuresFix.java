/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import net.minecraft.datafixer.TypeReferences;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructuresToConfiguredStructuresFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Mapping> STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING = ImmutableMap.builder().put("mineshaft", Mapping.create(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put("shipwreck", Mapping.create(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put("ocean_ruin", Mapping.create(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put("village", Mapping.create(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put("ruined_portal", Mapping.create(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put("pillager_outpost", Mapping.create("minecraft:pillager_outpost")).put("mansion", Mapping.create("minecraft:mansion")).put("jungle_pyramid", Mapping.create("minecraft:jungle_pyramid")).put("desert_pyramid", Mapping.create("minecraft:desert_pyramid")).put("igloo", Mapping.create("minecraft:igloo")).put("swamp_hut", Mapping.create("minecraft:swamp_hut")).put("stronghold", Mapping.create("minecraft:stronghold")).put("monument", Mapping.create("minecraft:monument")).put("fortress", Mapping.create("minecraft:fortress")).put("endcity", Mapping.create("minecraft:end_city")).put("buried_treasure", Mapping.create("minecraft:buried_treasure")).put("nether_fossil", Mapping.create("minecraft:nether_fossil")).put("bastion_remnant", Mapping.create("minecraft:bastion_remnant")).build();

    public StructuresToConfiguredStructuresFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        Type<?> type2 = this.getInputSchema().getType(TypeReferences.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::method_41012);
    }

    private Dynamic<?> method_41012(Dynamic<?> dynamic) {
        return dynamic.update("structures", dynamic22 -> dynamic22.update("starts", dynamic2 -> this.method_41015((Dynamic<?>)dynamic2, dynamic)).update("References", dynamic2 -> this.method_41020((Dynamic<?>)dynamic2, dynamic)));
    }

    private Dynamic<?> method_41015(Dynamic<?> dynamic, Dynamic<?> dynamic22) {
        Map<Dynamic, Dynamic> map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic2, dynamic32) -> {
            if (dynamic32.get("id").asString("INVALID").equals("INVALID")) {
                return;
            }
            Dynamic<?> dynamic4 = this.method_41022((Dynamic<?>)dynamic2, dynamic22);
            if (dynamic4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2.asString("<missing key>"));
                return;
            }
            hashMap.computeIfAbsent(dynamic4, dynamic3 -> dynamic32.set("id", dynamic4));
        });
        return dynamic22.createMap(hashMap);
    }

    private Dynamic<?> method_41020(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Map<Dynamic, Dynamic> map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic22, dynamic32) -> {
            if (dynamic32.asLongStream().count() == 0L) {
                return;
            }
            Dynamic<?> dynamic4 = this.method_41022((Dynamic<?>)dynamic22, dynamic2);
            if (dynamic4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic22.asString("<missing key>"));
                return;
            }
            hashMap.compute(dynamic4, (dynamic2, dynamic3) -> {
                if (dynamic3 == null) {
                    return dynamic32;
                }
                return dynamic32.createLongList(LongStream.concat(dynamic3.asLongStream(), dynamic32.asLongStream()));
            });
        });
        return dynamic2.createMap(hashMap);
    }

    @Nullable
    private Dynamic<?> method_41022(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Optional<String> optional;
        String string = dynamic.asString("UNKNOWN").toLowerCase(Locale.ROOT);
        Mapping lv = STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING.get(string);
        if (lv == null) {
            return null;
        }
        String string2 = lv.fallback;
        if (!lv.biomeMapping().isEmpty() && (optional = this.method_41013(dynamic2, lv)).isPresent()) {
            string2 = optional.get();
        }
        return dynamic2.createString(string2);
    }

    private Optional<String> method_41013(Dynamic<?> dynamic, Mapping arg) {
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        dynamic.get("sections").asList(Function.identity()).forEach(dynamic2 -> dynamic2.get("biomes").get("palette").asList(Function.identity()).forEach(dynamic -> {
            String string = arg.biomeMapping().get(dynamic.asString(""));
            if (string != null) {
                object2IntArrayMap.mergeInt(string, 1, Integer::sum);
            }
        }));
        return object2IntArrayMap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
    }

    record Mapping(Map<String, String> biomeMapping, String fallback) {
        public static Mapping create(String mapping) {
            return new Mapping(Map.of(), mapping);
        }

        public static Mapping create(Map<List<String>, String> biomeMapping, String fallback) {
            return new Mapping(Mapping.flattenBiomeMapping(biomeMapping), fallback);
        }

        private static Map<String, String> flattenBiomeMapping(Map<List<String>, String> biomeMapping) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Map.Entry<List<String>, String> entry : biomeMapping.entrySet()) {
                entry.getKey().forEach(string -> builder.put(string, (String)entry.getValue()));
            }
            return builder.build();
        }
    }
}

