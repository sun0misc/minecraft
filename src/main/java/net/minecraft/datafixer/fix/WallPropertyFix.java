/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;

public class WallPropertyFix
extends DataFix {
    private static final Set<String> TARGET_BLOCK_IDS = ImmutableSet.of("minecraft:andesite_wall", "minecraft:brick_wall", "minecraft:cobblestone_wall", "minecraft:diorite_wall", "minecraft:end_stone_brick_wall", "minecraft:granite_wall", new String[]{"minecraft:mossy_cobblestone_wall", "minecraft:mossy_stone_brick_wall", "minecraft:nether_brick_wall", "minecraft:prismarine_wall", "minecraft:red_nether_brick_wall", "minecraft:red_sandstone_wall", "minecraft:sandstone_wall", "minecraft:stone_brick_wall"});

    public WallPropertyFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WallPropertyFix", this.getInputSchema().getType(TypeReferences.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), WallPropertyFix::updateWallProperties));
    }

    private static String booleanToWallType(String value) {
        return "true".equals(value) ? "low" : "none";
    }

    private static <T> Dynamic<T> updateWallValueReference(Dynamic<T> dynamic2, String string) {
        return dynamic2.update(string, dynamic -> DataFixUtils.orElse(dynamic.asString().result().map(WallPropertyFix::booleanToWallType).map(dynamic::createString), dynamic));
    }

    private static <T> Dynamic<T> updateWallProperties(Dynamic<T> dynamic2) {
        boolean bl = dynamic2.get("Name").asString().result().filter(TARGET_BLOCK_IDS::contains).isPresent();
        if (!bl) {
            return dynamic2;
        }
        return dynamic2.update("Properties", dynamic -> {
            Dynamic dynamic2 = WallPropertyFix.updateWallValueReference(dynamic, "east");
            dynamic2 = WallPropertyFix.updateWallValueReference(dynamic2, "west");
            dynamic2 = WallPropertyFix.updateWallValueReference(dynamic2, "north");
            return WallPropertyFix.updateWallValueReference(dynamic2, "south");
        });
    }
}

