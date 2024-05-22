/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class HorseChestIndexingFix
extends DataFix {
    public HorseChestIndexingFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder = DSL.typeFinder(this.getInputSchema().getType(TypeReferences.ITEM_STACK));
        Type<?> type = this.getInputSchema().getType(TypeReferences.ENTITY);
        return TypeRewriteRule.seq(this.fixIndexing(opticFinder, type, "minecraft:llama"), this.fixIndexing(opticFinder, type, "minecraft:trader_llama"), this.fixIndexing(opticFinder, type, "minecraft:mule"), this.fixIndexing(opticFinder, type, "minecraft:donkey"));
    }

    private TypeRewriteRule fixIndexing(OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticFinder, Type<?> type, String entityId) {
        Type<?> type2 = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, entityId);
        OpticFinder<?> opticFinder2 = DSL.namedChoice(entityId, type2);
        OpticFinder<?> opticFinder3 = type2.findField("Items");
        return this.fixTypeEverywhereTyped("Fix non-zero indexing in chest horse type " + entityId, type, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(opticFinder, pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(dynamic2 -> dynamic2.update("Slot", dynamic -> dynamic.createByte((byte)(dynamic.asInt(2) - 2))))))))));
    }
}

