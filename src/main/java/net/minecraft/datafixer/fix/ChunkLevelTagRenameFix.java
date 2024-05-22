/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;

public class ChunkLevelTagRenameFix
extends DataFix {
    public ChunkLevelTagRenameFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        OpticFinder<?> opticFinder = type.findField("Level");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Structures");
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
        Type<?> type3 = type2.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", type, type2, (Typed<?> typed2) -> {
            Typed<Dynamic<?>> typed22 = typed2.getTyped(opticFinder);
            Typed<Pair<String, Object>> typed3 = ChunkLevelTagRenameFix.method_39269(typed22);
            typed3 = typed3.set(DSL.remainderFinder(), ChunkLevelTagRenameFix.method_39270(typed2, typed22.get(DSL.remainderFinder())));
            typed3 = ChunkLevelTagRenameFix.rename(typed3, "TileEntities", "block_entities");
            typed3 = ChunkLevelTagRenameFix.rename(typed3, "TileTicks", "block_ticks");
            typed3 = ChunkLevelTagRenameFix.rename(typed3, "Entities", "entities");
            typed3 = ChunkLevelTagRenameFix.rename(typed3, "Sections", "sections");
            typed3 = typed3.updateTyped(opticFinder2, type3, typed -> ChunkLevelTagRenameFix.rename(typed, "Starts", "starts"));
            typed3 = ChunkLevelTagRenameFix.rename(typed3, "Structures", "structures");
            return typed3.update(DSL.remainderFinder(), dynamic -> dynamic.remove("Level"));
        });
    }

    private static Typed<?> rename(Typed<?> typed, String oldKey, String newKey) {
        return ChunkLevelTagRenameFix.rename(typed, oldKey, newKey, typed.getType().findFieldType(oldKey)).update(DSL.remainderFinder(), dynamic -> dynamic.remove(oldKey));
    }

    private static <A> Typed<?> rename(Typed<?> typed, String oldKey, String newKey, Type<A> type) {
        Type<Either<A, Unit>> type2 = DSL.optional(DSL.field(oldKey, type));
        Type<Either<A, Unit>> type3 = DSL.optional(DSL.field(newKey, type));
        return typed.update(type2.finder(), type3, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> method_39269(Typed<A> typed) {
        return new Typed<Pair<String, A>>(DSL.named("chunk", typed.getType()), typed.getOps(), Pair.of("chunk", typed.getValue()));
    }

    private static <T> Dynamic<T> method_39270(Typed<?> typed, Dynamic<T> dynamic) {
        DynamicOps dynamicOps = dynamic.getOps();
        Dynamic dynamic2 = typed.get(DSL.remainderFinder()).convert(dynamicOps);
        DataResult dataResult = dynamicOps.getMap(dynamic.getValue()).flatMap(mapLike -> dynamicOps.mergeToMap(dynamic2.getValue(), (MapLike)mapLike));
        return dataResult.result().map(object -> new Dynamic<Object>(dynamicOps, object)).orElse(dynamic);
    }
}

