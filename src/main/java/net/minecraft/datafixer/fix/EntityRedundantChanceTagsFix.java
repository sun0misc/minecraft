/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;

public class EntityRedundantChanceTagsFix
extends DataFix {
    private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

    public EntityRedundantChanceTagsFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityRedundantChanceTagsFix", this.getInputSchema().getType(TypeReferences.ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            if (EntityRedundantChanceTagsFix.hasZeroDropChance(dynamic.get("HandDropChances"), 2)) {
                dynamic = dynamic.remove("HandDropChances");
            }
            if (EntityRedundantChanceTagsFix.hasZeroDropChance(dynamic.get("ArmorDropChances"), 4)) {
                dynamic = dynamic.remove("ArmorDropChances");
            }
            return dynamic;
        }));
    }

    private static boolean hasZeroDropChance(OptionalDynamic<?> listTag, int expectedLength) {
        return listTag.flatMap(FLOAT_LIST_CODEC::parse).map(list -> list.size() == expectedLength && list.stream().allMatch(chance -> chance.floatValue() == 0.0f)).result().orElse(false);
    }
}

