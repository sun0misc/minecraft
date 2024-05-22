/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;

public record TargetedEnchantmentEffectType<T>(EnchantmentEffectTarget enchanted, EnchantmentEffectTarget affected, T effect, Optional<LootCondition> requirements) {
    public static <S> Codec<TargetedEnchantmentEffectType<S>> createPostAttackCodec(Codec<S> effectCodec, LootContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)EnchantmentEffectTarget.CODEC.fieldOf("enchanted")).forGetter(TargetedEnchantmentEffectType::enchanted), ((MapCodec)EnchantmentEffectTarget.CODEC.fieldOf("affected")).forGetter(TargetedEnchantmentEffectType::affected), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedEnchantmentEffectType::effect), EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffectType::requirements)).apply((Applicative<TargetedEnchantmentEffectType, ?>)instance, TargetedEnchantmentEffectType::new));
    }

    public static <S> Codec<TargetedEnchantmentEffectType<S>> createEquipmentDropsCodec(Codec<S> effectCodec, LootContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)EnchantmentEffectTarget.CODEC.validate(enchanted -> enchanted != EnchantmentEffectTarget.DAMAGING_ENTITY ? DataResult.success(enchanted) : DataResult.error(() -> "enchanted must be attacker or victim")).fieldOf("enchanted")).forGetter(TargetedEnchantmentEffectType::enchanted), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedEnchantmentEffectType::effect), EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffectType::requirements)).apply((Applicative<TargetedEnchantmentEffectType, ?>)instance, (enchantedx, effect, requirements) -> new TargetedEnchantmentEffectType<Object>((EnchantmentEffectTarget)enchantedx, EnchantmentEffectTarget.VICTIM, effect, (Optional<LootCondition>)requirements)));
    }

    public boolean test(LootContext lootContext) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(lootContext);
    }
}

