/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class OminousBannerBlockEntityRenameFix
extends ChoiceFix {
    public OminousBannerBlockEntityRenameFix(Schema schema, boolean bl) {
        super(schema, bl, "OminousBannerBlockEntityRenameFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), this::fixBannerName);
    }

    private Dynamic<?> fixBannerName(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.get("CustomName").asString().result();
        if (optional.isPresent()) {
            String string = optional.get();
            string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
            return dynamic.set("CustomName", dynamic.createString(string));
        }
        return dynamic;
    }
}

