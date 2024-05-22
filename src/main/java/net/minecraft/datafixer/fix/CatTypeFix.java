/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class CatTypeFix
extends ChoiceFix {
    public CatTypeFix(Schema schema, boolean bl) {
        super(schema, bl, "CatTypeFix", TypeReferences.ENTITY, "minecraft:cat");
    }

    public Dynamic<?> fixCatTypeData(Dynamic<?> dynamic) {
        if (dynamic.get("CatType").asInt(0) == 9) {
            return dynamic.set("CatType", dynamic.createInt(10));
        }
        return dynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixCatTypeData);
    }
}

