/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class EmptyItemInVillagerTradeFix
extends DataFix {
    public EmptyItemInVillagerTradeFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.VILLAGER_TRADE);
        return this.writeFixAndRead("EmptyItemInVillagerTradeFix", type, type, dynamic -> {
            Dynamic dynamic2 = dynamic.get("buyB").orElseEmptyMap();
            String string = IdentifierNormalizingSchema.normalize(dynamic2.get("id").asString("minecraft:air"));
            int i = dynamic2.get("count").asInt(0);
            if (string.equals("minecraft:air") || i == 0) {
                return dynamic.remove("buyB");
            }
            return dynamic;
        });
    }
}

