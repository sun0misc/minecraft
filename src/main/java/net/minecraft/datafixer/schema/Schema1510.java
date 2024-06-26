/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema1510
extends IdentifierNormalizingSchema {
    public Schema1510(int i, Schema schema) {
        super(i, schema);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        map.put("minecraft:command_block_minecart", map.remove("minecraft:commandblock_minecart"));
        map.put("minecraft:end_crystal", map.remove("minecraft:ender_crystal"));
        map.put("minecraft:snow_golem", map.remove("minecraft:snowman"));
        map.put("minecraft:evoker", map.remove("minecraft:evocation_illager"));
        map.put("minecraft:evoker_fangs", map.remove("minecraft:evocation_fangs"));
        map.put("minecraft:illusioner", map.remove("minecraft:illusion_illager"));
        map.put("minecraft:vindicator", map.remove("minecraft:vindication_illager"));
        map.put("minecraft:iron_golem", map.remove("minecraft:villager_golem"));
        map.put("minecraft:experience_orb", map.remove("minecraft:xp_orb"));
        map.put("minecraft:experience_bottle", map.remove("minecraft:xp_bottle"));
        map.put("minecraft:eye_of_ender", map.remove("minecraft:eye_of_ender_signal"));
        map.put("minecraft:firework_rocket", map.remove("minecraft:fireworks_rocket"));
        return map;
    }
}

