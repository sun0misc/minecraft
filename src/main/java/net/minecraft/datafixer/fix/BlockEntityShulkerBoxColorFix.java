/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class BlockEntityShulkerBoxColorFix
extends ChoiceFix {
    public BlockEntityShulkerBoxColorFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntityShulkerBoxColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:shulker_box");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), dynamic -> dynamic.remove("Color"));
    }
}

