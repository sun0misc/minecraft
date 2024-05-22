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

public class EntityPaintingFieldsRenameFix
extends ChoiceFix {
    public EntityPaintingFieldsRenameFix(Schema outputSchema) {
        super(outputSchema, false, "EntityPaintingFieldsRenameFix", TypeReferences.ENTITY, "minecraft:painting");
    }

    public Dynamic<?> rename(Dynamic<?> dynamic) {
        return dynamic.renameField("Motive", "variant").renameField("Facing", "facing");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::rename);
    }
}

