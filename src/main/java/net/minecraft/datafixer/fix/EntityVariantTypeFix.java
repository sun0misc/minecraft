/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.datafixer.fix.ChoiceFix;

public class EntityVariantTypeFix
extends ChoiceFix {
    private final String variantKey;
    private final IntFunction<String> variantIntToId;

    public EntityVariantTypeFix(Schema outputSchema, String name, DSL.TypeReference type, String entityId, String variantKey, IntFunction<String> variantIntToId) {
        super(outputSchema, false, name, type, entityId);
        this.variantKey = variantKey;
        this.variantIntToId = variantIntToId;
    }

    private static <T> Dynamic<T> method_43072(Dynamic<T> dynamic, String string, String string2, Function<Dynamic<T>, Dynamic<T>> function) {
        return dynamic.map(object3 -> {
            DynamicOps<Object> dynamicOps = dynamic.getOps();
            Function<Object, Object> function2 = object -> ((Dynamic)function.apply(new Dynamic<Object>(dynamicOps, object))).getValue();
            return dynamicOps.get(object3, string).map(object2 -> dynamicOps.set(object3, string2, function2.apply(object2))).result().orElse(object3);
        });
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), dynamic2 -> EntityVariantTypeFix.method_43072(dynamic2, this.variantKey, "variant", dynamic -> DataFixUtils.orElse(dynamic.asNumber().map(number -> dynamic.createString(this.variantIntToId.apply(number.intValue()))).result(), dynamic)));
    }
}

