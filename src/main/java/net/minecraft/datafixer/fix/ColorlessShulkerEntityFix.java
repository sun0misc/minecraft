/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class ColorlessShulkerEntityFix
extends ChoiceFix {
    public ColorlessShulkerEntityFix(Schema schema, boolean bl) {
        super(schema, bl, "Colorless shulker entity fix", TypeReferences.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), dynamic -> {
            if (dynamic.get("Color").asInt(0) == 10) {
                return dynamic.set("Color", dynamic.createByte((byte)16));
            }
            return dynamic;
        });
    }
}

