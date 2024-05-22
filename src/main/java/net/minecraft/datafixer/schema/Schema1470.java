/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema100;

public class Schema1470
extends IdentifierNormalizingSchema {
    public Schema1470(int i, Schema schema) {
        super(i, schema);
    }

    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> Schema100.targetItems(schema));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        Schema1470.targetEntityItems(schema, map, "minecraft:turtle");
        Schema1470.targetEntityItems(schema, map, "minecraft:cod_mob");
        Schema1470.targetEntityItems(schema, map, "minecraft:tropical_fish");
        Schema1470.targetEntityItems(schema, map, "minecraft:salmon_mob");
        Schema1470.targetEntityItems(schema, map, "minecraft:puffer_fish");
        Schema1470.targetEntityItems(schema, map, "minecraft:phantom");
        Schema1470.targetEntityItems(schema, map, "minecraft:dolphin");
        Schema1470.targetEntityItems(schema, map, "minecraft:drowned");
        schema.register(map, "minecraft:trident", (String name) -> DSL.optionalFields("inBlockState", TypeReferences.BLOCK_STATE.in(schema), "Trident", TypeReferences.ITEM_STACK.in(schema)));
        return map;
    }
}

