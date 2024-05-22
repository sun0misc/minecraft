/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
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
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class FurnaceRecipesFix
extends DataFix {
    public FurnaceRecipesFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.updateBlockEntities(this.getOutputSchema().getTypeRaw(TypeReferences.RECIPE));
    }

    private <R> TypeRewriteRule updateBlockEntities(Type<R> type) {
        Type type2 = DSL.and(DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(type, DSL.intType()), DSL.remainderType()))), DSL.remainderType());
        OpticFinder<?> opticFinder = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:furnace"));
        OpticFinder<?> opticFinder2 = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:blast_furnace"));
        OpticFinder<?> opticFinder3 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:smoker"));
        Type<?> type3 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:furnace");
        Type<?> type4 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:blast_furnace");
        Type<?> type5 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:smoker");
        Type<?> type6 = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
        Type<?> type7 = this.getOutputSchema().getType(TypeReferences.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("FurnaceRecipesFix", type6, type7, (Typed<?> typed2) -> typed2.updateTyped(opticFinder, type3, typed -> this.updateBlockEntityData(type, type2, (Typed<?>)typed)).updateTyped(opticFinder2, type4, typed -> this.updateBlockEntityData(type, type2, (Typed<?>)typed)).updateTyped(opticFinder3, type5, typed -> this.updateBlockEntityData(type, type2, (Typed<?>)typed)));
    }

    private <R> Typed<?> updateBlockEntityData(Type<R> type, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type2, Typed<?> typed) {
        Dynamic<?> dynamic2 = typed.getOrCreate(DSL.remainderFinder());
        int i = dynamic2.get("RecipesUsedSize").asInt(0);
        dynamic2 = dynamic2.remove("RecipesUsedSize");
        ArrayList list = Lists.newArrayList();
        for (int j = 0; j < i; ++j) {
            String string = "RecipeLocation" + j;
            String string2 = "RecipeAmount" + j;
            Optional<Dynamic<?>> optional = dynamic2.get(string).result();
            int k = dynamic2.get(string2).asInt(0);
            if (k > 0) {
                optional.ifPresent(dynamic -> {
                    Optional optional = type.read(dynamic).result();
                    optional.ifPresent(pair -> list.add(Pair.of(pair.getFirst(), k)));
                });
            }
            dynamic2 = dynamic2.remove(string).remove(string2);
        }
        return typed.set(DSL.remainderFinder(), type2, Pair.of(Either.left(Pair.of(list, dynamic2.emptyMap())), dynamic2));
    }
}

