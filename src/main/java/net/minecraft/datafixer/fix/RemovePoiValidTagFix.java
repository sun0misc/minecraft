/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;

public class RemovePoiValidTagFix
extends DataFix {
    public RemovePoiValidTagFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(TypeReferences.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(TypeReferences.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere("POI rebuild", type, dynamicOps -> pair -> pair.mapSecond(RemovePoiValidTagFix::removeValidTag));
    }

    private static <T> Dynamic<T> removeValidTag(Dynamic<T> dynamic2) {
        return dynamic2.update("Sections", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic -> dynamic.remove("Valid"))));
    }
}

