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

public class EntityShulkerColorFix
extends ChoiceFix {
    public EntityShulkerColorFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityShulkerColorFix", TypeReferences.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixShulkerColor(Dynamic<?> dynamic) {
        if (dynamic.get("Color").map(Dynamic::asNumber).result().isEmpty()) {
            return dynamic.set("Color", dynamic.createByte((byte)10));
        }
        return dynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixShulkerColor);
    }
}

