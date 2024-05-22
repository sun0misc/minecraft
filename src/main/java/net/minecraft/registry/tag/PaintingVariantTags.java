/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class PaintingVariantTags {
    public static final TagKey<PaintingVariant> PLACEABLE = PaintingVariantTags.of("placeable");

    private PaintingVariantTags() {
    }

    private static TagKey<PaintingVariant> of(String id) {
        return TagKey.of(RegistryKeys.PAINTING_VARIANT, Identifier.method_60656(id));
    }
}

