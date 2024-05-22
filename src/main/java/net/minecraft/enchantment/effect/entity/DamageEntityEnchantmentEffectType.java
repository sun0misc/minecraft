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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record DamageEntityEnchantmentEffectType(EnchantmentLevelBasedValueType minDamage, EnchantmentLevelBasedValueType maxDamage, RegistryEntry<DamageType> damageType) implements EnchantmentEntityEffectType
{
    public static final MapCodec<DamageEntityEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("min_damage")).forGetter(DamageEntityEnchantmentEffectType::minDamage), ((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("max_damage")).forGetter(DamageEntityEnchantmentEffectType::maxDamage), ((MapCodec)DamageType.ENTRY_CODEC.fieldOf("damage_type")).forGetter(DamageEntityEnchantmentEffectType::damageType)).apply((Applicative<DamageEntityEnchantmentEffectType, ?>)instance, DamageEntityEnchantmentEffectType::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        float f = MathHelper.nextBetween(user.getRandom(), this.minDamage.getValue(level), this.maxDamage.getValue(level));
        user.damage(new DamageSource(this.damageType, context.owner()), f);
    }

    public MapCodec<DamageEntityEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

