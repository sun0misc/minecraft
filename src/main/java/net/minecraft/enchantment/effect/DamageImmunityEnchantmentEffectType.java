/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;

public record DamageImmunityEnchantmentEffectType() {
    public static final DamageImmunityEnchantmentEffectType INSTANCE = new DamageImmunityEnchantmentEffectType();
    public static final Codec<DamageImmunityEnchantmentEffectType> CODEC = Codec.unit(() -> INSTANCE);
}

