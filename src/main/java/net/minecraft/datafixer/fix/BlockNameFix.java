/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public abstract class BlockNameFix
extends DataFix {
    private final String name;

    public BlockNameFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.name = name;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type2;
        Type<?> type = this.getInputSchema().getType(TypeReferences.BLOCK_NAME);
        if (!Objects.equals(type, type2 = DSL.named(TypeReferences.BLOCK_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()))) {
            throw new IllegalStateException("block type is not what was expected.");
        }
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhere(this.name + " for block", type2, dynamicOps -> pair -> pair.mapSecond(this::rename));
        TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(TypeReferences.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::fixBlockState));
        TypeRewriteRule typeRewriteRule3 = this.fixTypeEverywhereTyped(this.name + " for flat_block_state", this.getInputSchema().getType(TypeReferences.FLAT_BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), dynamic -> DataFixUtils.orElse(dynamic.asString().result().map(this::fixFlatBlockState).map(dynamic::createString), dynamic)));
        return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2, typeRewriteRule3);
    }

    private Dynamic<?> fixBlockState(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.get("Name").asString().result();
        if (optional.isPresent()) {
            return dynamic.set("Name", dynamic.createString(this.rename(optional.get())));
        }
        return dynamic;
    }

    private String fixFlatBlockState(String string) {
        int i = string.indexOf(91);
        int j = string.indexOf(123);
        int k = string.length();
        if (i > 0) {
            k = i;
        }
        if (j > 0) {
            k = Math.min(k, j);
        }
        String string2 = string.substring(0, k);
        String string3 = this.rename(string2);
        return string3 + string.substring(k);
    }

    protected abstract String rename(String var1);

    public static DataFix create(Schema outputSchema, String name, final Function<String, String> rename) {
        return new BlockNameFix(outputSchema, name){

            @Override
            protected String rename(String oldName) {
                return (String)rename.apply(oldName);
            }
        };
    }
}

