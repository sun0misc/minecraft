/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValueType;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record DamageItemEnchantmentEffectType(EnchantmentLevelBasedValueType amount) implements EnchantmentEntityEffectType
{
    public static final MapCodec<DamageItemEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("amount")).forGetter(arg -> arg.amount)).apply((Applicative<DamageItemEnchantmentEffectType, ?>)instance, DamageItemEnchantmentEffectType::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        ServerPlayerEntity lv;
        LivingEntity livingEntity = context.owner();
        ServerPlayerEntity lv2 = livingEntity instanceof ServerPlayerEntity ? (lv = (ServerPlayerEntity)livingEntity) : null;
        context.stack().damage((int)this.amount.getValue(level), world, lv2, context.onBreak());
    }

    public MapCodec<DamageItemEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

