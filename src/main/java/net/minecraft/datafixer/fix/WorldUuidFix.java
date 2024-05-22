/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import org.slf4j.Logger;

public class WorldUuidFix
extends AbstractUuidFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public WorldUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.LEVEL);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelUUIDFix", this.getInputSchema().getType(this.typeReference), typed2 -> typed2.updateTyped(DSL.remainderFinder(), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.fixCustomBossEvents((Dynamic<?>)dynamic);
            dynamic = this.fixDragonUuid((Dynamic<?>)dynamic);
            dynamic = this.fixWanderingTraderId((Dynamic<?>)dynamic);
            return dynamic;
        })));
    }

    private Dynamic<?> fixWanderingTraderId(Dynamic<?> dynamic) {
        return WorldUuidFix.updateStringUuid(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
    }

    private Dynamic<?> fixDragonUuid(Dynamic<?> dynamic2) {
        return dynamic2.update("DimensionData", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic2 -> dynamic2.update("DragonFight", dynamic -> WorldUuidFix.updateRegularMostLeast(dynamic, "DragonUUID", "Dragon").orElse((Dynamic<?>)dynamic)))));
    }

    private Dynamic<?> fixCustomBossEvents(Dynamic<?> dynamic2) {
        return dynamic2.update("CustomBossEvents", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic -> dynamic.update("Players", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> WorldUuidFix.createArrayFromCompoundUuid(dynamic).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return dynamic;
        })))))));
    }
}

