/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.fix.ItemNbtFix;

public class OminousBannerItemRenameFix
extends ItemNbtFix {
    public OminousBannerItemRenameFix(Schema outputSchema) {
        super(outputSchema, "OminousBannerRenameFix", itemId -> itemId.equals("minecraft:white_banner"));
    }

    @Override
    protected <T> Dynamic<T> fixNbt(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("display").result();
        if (optional.isPresent()) {
            Dynamic<T> dynamic2 = optional.get();
            Optional<String> optional2 = dynamic2.get("Name").asString().result();
            if (optional2.isPresent()) {
                String string = optional2.get();
                string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
                dynamic2 = dynamic2.set("Name", dynamic2.createString(string));
            }
            return dynamic.set("display", dynamic2);
        }
        return dynamic;
    }
}

