/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WorldGenSettingsHeightAndBiomeFix
extends DataFix {
    private static final String NAME = "WorldGenSettingsHeightAndBiomeFix";
    public static final String HAS_INCREASED_HEIGHT_ALREADY_KEY = "has_increased_height_already";

    public WorldGenSettingsHeightAndBiomeFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.WORLD_GEN_SETTINGS);
        OpticFinder<?> opticFinder = type.findField("dimensions");
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.WORLD_GEN_SETTINGS);
        Type<?> type3 = type2.findFieldType("dimensions");
        return this.fixTypeEverywhereTyped(NAME, type, type2, (Typed<?> typed2) -> {
            OptionalDynamic<?> optionalDynamic = typed2.get(DSL.remainderFinder()).get(HAS_INCREASED_HEIGHT_ALREADY_KEY);
            boolean bl = optionalDynamic.result().isEmpty();
            boolean bl2 = optionalDynamic.asBoolean(true);
            return typed2.update(DSL.remainderFinder(), dynamic -> dynamic.remove(HAS_INCREASED_HEIGHT_ALREADY_KEY)).updateTyped(opticFinder, type3, typed -> Util.apply(typed, type3, dynamic2 -> dynamic2.update("minecraft:overworld", dynamic -> dynamic.update("generator", dynamic2 -> {
                String string = dynamic2.get("type").asString("");
                if ("minecraft:noise".equals(string)) {
                    MutableBoolean mutableBoolean = new MutableBoolean();
                    dynamic2 = dynamic2.update("biome_source", dynamic -> {
                        String string = dynamic.get("type").asString("");
                        if ("minecraft:vanilla_layered".equals(string) || bl && "minecraft:multi_noise".equals(string)) {
                            if (dynamic.get("large_biomes").asBoolean(false)) {
                                mutableBoolean.setTrue();
                            }
                            return dynamic.createMap(ImmutableMap.of(dynamic.createString("preset"), dynamic.createString("minecraft:overworld"), dynamic.createString("type"), dynamic.createString("minecraft:multi_noise")));
                        }
                        return dynamic;
                    });
                    if (mutableBoolean.booleanValue()) {
                        return dynamic2.update("settings", dynamic -> {
                            if ("minecraft:overworld".equals(dynamic.asString(""))) {
                                return dynamic.createString("minecraft:large_biomes");
                            }
                            return dynamic;
                        });
                    }
                    return dynamic2;
                }
                if ("minecraft:flat".equals(string)) {
                    if (bl2) {
                        return dynamic2;
                    }
                    return dynamic2.update("settings", dynamic -> dynamic.update("layers", WorldGenSettingsHeightAndBiomeFix::fillWithAir));
                }
                return dynamic2;
            }))));
        });
    }

    private static Dynamic<?> fillWithAir(Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.createMap(ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(64), dynamic.createString("block"), dynamic.createString("minecraft:air")));
        return dynamic.createList(Stream.concat(Stream.of(dynamic2), dynamic.asStream()));
    }
}

