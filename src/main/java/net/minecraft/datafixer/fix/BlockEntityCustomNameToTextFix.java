/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityCustomNameToTextFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class BlockEntityCustomNameToTextFix
extends DataFix {
    public BlockEntityCustomNameToTextFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<String> opticFinder = DSL.fieldFinder("id", IdentifierNormalizingSchema.getIdentifierType());
        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(optional.get(), "minecraft:command_block")) {
                return dynamic;
            }
            return EntityCustomNameToTextFix.fixCustomName(dynamic);
        }));
    }
}

