/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.google.common.collect.HashMultimap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValueType;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Vec3d;

public record AttributeEnchantmentEffectType(Identifier id, RegistryEntry<EntityAttribute> attribute, EnchantmentLevelBasedValueType amount, EntityAttributeModifier.Operation operation) implements EnchantmentLocationBasedEffectType
{
    public static final MapCodec<AttributeEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(AttributeEnchantmentEffectType::id), ((MapCodec)EntityAttribute.CODEC.fieldOf("attribute")).forGetter(AttributeEnchantmentEffectType::attribute), ((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("amount")).forGetter(AttributeEnchantmentEffectType::amount), ((MapCodec)EntityAttributeModifier.Operation.CODEC.fieldOf("operation")).forGetter(AttributeEnchantmentEffectType::operation)).apply((Applicative<AttributeEnchantmentEffectType, ?>)instance, AttributeEnchantmentEffectType::new));

    private Identifier method_60769(StringIdentifiable arg) {
        return this.id.withSuffixedPath("/" + arg.asString());
    }

    public EntityAttributeModifier createAttributeModifier(int value, StringIdentifiable arg) {
        return new EntityAttributeModifier(this.method_60769(arg), this.amount().getValue(value), this.operation());
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, boolean newlyApplied) {
        if (newlyApplied && user instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)user;
            lv.getAttributes().addTemporaryModifiers(this.getModifiers(level, context.slot()));
        }
    }

    @Override
    public void remove(EnchantmentEffectContext context, Entity user, Vec3d pos, int level) {
        if (user instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)user;
            lv.getAttributes().removeModifiers(this.getModifiers(level, context.slot()));
        }
    }

    private HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(int level, EquipmentSlot arg) {
        HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> hashMultimap = HashMultimap.create();
        hashMultimap.put(this.attribute, (Object)this.createAttributeModifier(level, arg));
        return hashMultimap;
    }

    public MapCodec<AttributeEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

