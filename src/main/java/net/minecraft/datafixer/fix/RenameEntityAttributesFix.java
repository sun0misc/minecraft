/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class RenameEntityAttributesFix
extends DataFix {
    private final String description;
    private final UnaryOperator<String> renames;

    public RenameEntityAttributesFix(Schema outputSchema, String description, UnaryOperator<String> renames) {
        super(outputSchema, false);
        this.description = description;
        this.renames = renames;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped(this.description + " (ItemStack)", type, typed -> typed.updateTyped(opticFinder, this::updateAttributeModifiers)), this.fixTypeEverywhereTyped(this.description + " (Entity)", this.getInputSchema().getType(TypeReferences.ENTITY), this::updateEntityAttributes), this.fixTypeEverywhereTyped(this.description + " (Player)", this.getInputSchema().getType(TypeReferences.PLAYER), this::updateEntityAttributes));
    }

    private Dynamic<?> updateAttributeName(Dynamic<?> dynamic) {
        return DataFixUtils.orElse(dynamic.asString().result().map(this.renames).map(dynamic::createString), dynamic);
    }

    private Typed<?> updateAttributeModifiers(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("AttributeModifiers", dynamic -> DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(dynamic -> dynamic.update("AttributeName", this::updateAttributeName))).map(dynamic::createList), dynamic)));
    }

    private Typed<?> updateEntityAttributes(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("Attributes", dynamic -> DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(dynamic -> dynamic.update("Name", this::updateAttributeName))).map(dynamic::createList), dynamic)));
    }
}

