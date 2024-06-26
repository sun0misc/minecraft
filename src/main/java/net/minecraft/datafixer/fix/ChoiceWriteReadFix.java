/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import net.minecraft.util.Util;

public abstract class ChoiceWriteReadFix
extends DataFix {
    private final String name;
    private final String choiceName;
    private final DSL.TypeReference type;

    public ChoiceWriteReadFix(Schema outputSchema, boolean changesType, String name, DSL.TypeReference type, String choiceName) {
        super(outputSchema, changesType);
        this.name = name;
        this.type = type;
        this.choiceName = choiceName;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        Type<?> type2 = this.getInputSchema().getChoiceType(this.type, this.choiceName);
        Type<?> type3 = this.getOutputSchema().getType(this.type);
        Type<?> type4 = this.getOutputSchema().getChoiceType(this.type, this.choiceName);
        OpticFinder<?> opticFinder = DSL.namedChoice(this.choiceName, type2);
        Type<?> type5 = type2.all(ChoiceWriteReadFix.method_56640(type, type3), true, false).view().newType();
        return this.method_56641(type, type3, opticFinder, type4, type5);
    }

    private <S, T, A, B> TypeRewriteRule method_56641(Type<S> type, Type<T> type2, OpticFinder<A> opticFinder, Type<B> type3, Type<?> type4) {
        return this.fixTypeEverywhere(this.name, type, type2, dynamicOps -> object2 -> {
            Typed<Object> typed = new Typed<Object>(type, (DynamicOps<?>)dynamicOps, object2);
            return typed.update(opticFinder, type3, object -> {
                Typed<Object> typed = new Typed<Object>((Type<Object>)type4, (DynamicOps<?>)dynamicOps, object);
                return Util.apply(typed, type3, this::transform).getValue();
            }).getValue();
        });
    }

    private static <A, B> TypeRewriteRule method_56640(Type<A> type, Type<B> type2) {
        RewriteResult<A, B> rewriteResult = RewriteResult.create(View.create("Patcher", type, type2, dynamicOps -> object -> {
            throw new UnsupportedOperationException();
        }), new BitSet());
        return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(type, rewriteResult), PointFreeRule.nop(), true, true);
    }

    protected abstract <T> Dynamic<T> transform(Dynamic<T> var1);
}

