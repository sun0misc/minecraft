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

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, typed -> typed.update(DSL.remainderFinder(), IglooMetadataRemovalFix::removeMetadata));
    }

    private static <T> Dynamic<T> removeMetadata(Dynamic<T> dynamic) {
        boolean bl = dynamic.get("Children").asStreamOpt().map(stream -> stream.allMatch(IglooMetadataRemovalFix::isIgloo)).result().orElse(false);
        if (bl) {
            return dynamic.set("id", dynamic.createString("Igloo")).remove("Children");
        }
        return dynamic.update("Children", IglooMetadataRemovalFix::removeIgloos);
    }

    private static <T> Dynamic<T> removeIgloos(Dynamic<T> dynamic) {
        return dynamic.asStreamOpt().map(stream -> stream.filter(dynamic -> !IglooMetadataRemovalFix.isIgloo(dynamic))).map(dynamic::createList).result().orElse(dynamic);
    }

    private static boolean isIgloo(Dynamic<?> dynamic) {
        return dynamic.get("id").asString("").equals("Iglu");
    }
}

