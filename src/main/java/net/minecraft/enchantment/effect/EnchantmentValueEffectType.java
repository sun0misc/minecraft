/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffectTypes;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffectType;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffectType;
import net.minecraft.enchantment.effect.value.RemoveBinomialEnchantmentEffectType;
import net.minecraft.enchantment.effect.value.SetEnchantmentEffectType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.Random;

public interface EnchantmentValueEffectType {
    public static final Codec<EnchantmentValueEffectType> CODEC = Registries.ENCHANTMENT_VALUE_EFFECT_TYPE.getCodec().dispatch(EnchantmentValueEffectType::getCodec, Function.identity());

    public static MapCodec<? extends EnchantmentValueEffectType> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentValueEffectType>> registry) {
        Registry.register(registry, "add", AddEnchantmentEffectType.CODEC);
        Registry.register(registry, "all_of", AllOfEnchantmentEffectTypes.ValueEffects.CODEC);
        Registry.register(registry, "multiply", MultiplyEnchantmentEffectType.CODEC);
        Registry.register(registry, "remove_binomial", RemoveBinomialEnchantmentEffectType.CODEC);
        return Registry.register(registry, "set", SetEnchantmentEffectType.CODEC);
    }

    public float apply(int var1, Random var2, float var3);

    public MapCodec<? extends EnchantmentValueEffectType> getCodec();
}

