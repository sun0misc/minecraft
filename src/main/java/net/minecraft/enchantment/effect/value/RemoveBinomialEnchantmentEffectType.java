/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.value;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValueType;
import net.minecraft.enchantment.effect.EnchantmentValueEffectType;
import net.minecraft.util.math.random.Random;

public record RemoveBinomialEnchantmentEffectType(EnchantmentLevelBasedValueType chance) implements EnchantmentValueEffectType
{
    public static final MapCodec<RemoveBinomialEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("chance")).forGetter(RemoveBinomialEnchantmentEffectType::chance)).apply((Applicative<RemoveBinomialEnchantmentEffectType, ?>)instance, RemoveBinomialEnchantmentEffectType::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        float g = this.chance.getValue(level);
        int j = 0;
        int k = 0;
        while ((float)k < inputValue) {
            if (random.nextFloat() < g) {
                ++j;
            }
            ++k;
        }
        return inputValue - (float)j;
    }

    public MapCodec<RemoveBinomialEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

