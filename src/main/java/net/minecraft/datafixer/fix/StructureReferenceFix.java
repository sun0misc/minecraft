/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class StructureReferenceFix
extends DataFix {
    public StructureReferenceFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("Structure Reference Fix", type, typed -> typed.update(DSL.remainderFinder(), StructureReferenceFix::updateReferences));
    }

    private static <T> Dynamic<T> updateReferences(Dynamic<T> dynamic2) {
        return dynamic2.update("references", dynamic -> dynamic.createInt(dynamic.asNumber().map(Number::intValue).result().filter(integer -> integer > 0).orElse(1)));
    }
}

