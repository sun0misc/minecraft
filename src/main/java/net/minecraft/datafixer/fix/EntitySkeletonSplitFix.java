/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.fix.EntitySimpleTransformFix;

public class EntitySkeletonSplitFix
extends EntitySimpleTransformFix {
    public EntitySkeletonSplitFix(Schema schema, boolean bl) {
        super("EntitySkeletonSplitFix", schema, bl);
    }

    @Override
    protected Pair<String, Dynamic<?>> transform(String choice, Dynamic<?> dynamic) {
        if (Objects.equals(choice, "Skeleton")) {
            int i = dynamic.get("SkeletonType").asInt(0);
            if (i == 1) {
                choice = "WitherSkeleton";
            } else if (i == 2) {
                choice = "Stray";
            }
        }
        return Pair.of(choice, dynamic);
    }
}

