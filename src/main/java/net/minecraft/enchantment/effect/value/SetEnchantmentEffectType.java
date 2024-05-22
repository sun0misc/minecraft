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

public record SetEnchantmentEffectType(EnchantmentLevelBasedValueType value) implements EnchantmentValueEffectType
{
    public static final MapCodec<SetEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("value")).forGetter(SetEnchantmentEffectType::value)).apply((Applicative<SetEnchantmentEffectType, ?>)instance, SetEnchantmentEffectType::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        return this.value.getValue(level);
    }

    public MapCodec<SetEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

