/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValueType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public record RandomChanceWithEnchantedBonusLootCondition(EnchantmentLevelBasedValueType chance, RegistryEntry<Enchantment> enchantment) implements LootCondition
{
    public static final MapCodec<RandomChanceWithEnchantedBonusLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValueType.CODEC.fieldOf("chance")).forGetter(RandomChanceWithEnchantedBonusLootCondition::chance), ((MapCodec)Enchantment.ENTRY_CODEC.fieldOf("enchantment")).forGetter(RandomChanceWithEnchantedBonusLootCondition::enchantment)).apply((Applicative<RandomChanceWithEnchantedBonusLootCondition, ?>)instance, RandomChanceWithEnchantedBonusLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext arg) {
        int i;
        Entity lv = arg.get(LootContextParameters.ATTACKING_ENTITY);
        if (lv instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)lv;
            i = EnchantmentHelper.getEquipmentLevel(this.enchantment, lv2);
        } else {
            i = 0;
        }
        return arg.getRandom().nextFloat() < this.chance.getValue(i);
    }

    public static LootCondition.Builder builder(RegistryWrapper.WrapperLookup registryLookup, float base, float perLevelAboveFirst) {
        RegistryWrapper.Impl<Enchantment> lv = registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return () -> new RandomChanceWithEnchantedBonusLootCondition(new EnchantmentLevelBasedValueType.Linear(base + perLevelAboveFirst, perLevelAboveFirst), lv.getOrThrow(Enchantments.LOOTING));
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

