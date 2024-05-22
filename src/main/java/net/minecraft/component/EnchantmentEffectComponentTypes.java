/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffectType;
import net.minecraft.enchantment.effect.DamageImmunityEnchantmentEffectType;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffectType;
import net.minecraft.enchantment.effect.EnchantmentValueEffectType;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffectType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Unit;

public interface EnchantmentEffectComponentTypes {
    public static final Codec<ComponentType<?>> COMPONENT_TYPE_CODEC = Codec.lazyInitialized(() -> Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.getCodec());
    public static final Codec<ComponentMap> COMPONENT_MAP_CODEC = ComponentMap.createCodec(COMPONENT_TYPE_CODEC);
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> DAMAGE_PROTECTION = EnchantmentEffectComponentTypes.register("damage_protection", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<DamageImmunityEnchantmentEffectType>>> DAMAGE_IMMUNITY = EnchantmentEffectComponentTypes.register("damage_immunity", builder -> builder.codec(EnchantmentEffectEntry.createCodec(DamageImmunityEnchantmentEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> DAMAGE = EnchantmentEffectComponentTypes.register("damage", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> SMASH_DAMAGE_PER_FALLEN_BLOCK = EnchantmentEffectComponentTypes.register("smash_damage_per_fallen_block", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> KNOCKBACK = EnchantmentEffectComponentTypes.register("knockback", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> ARMOR_EFFECTIVENESS = EnchantmentEffectComponentTypes.register("armor_effectiveness", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<TargetedEnchantmentEffectType<EnchantmentEntityEffectType>>> POST_ATTACK = EnchantmentEffectComponentTypes.register("post_attack", builder -> builder.codec(TargetedEnchantmentEffectType.createPostAttackCodec(EnchantmentEntityEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffectType>>> HIT_BLOCK = EnchantmentEffectComponentTypes.register("hit_block", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffectType.CODEC, LootContextTypes.HIT_BLOCK).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> ITEM_DAMAGE = EnchantmentEffectComponentTypes.register("item_damage", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()));
    public static final ComponentType<List<AttributeEnchantmentEffectType>> ATTRIBUTES = EnchantmentEffectComponentTypes.register("attributes", builder -> builder.codec(AttributeEnchantmentEffectType.CODEC.codec().listOf()));
    public static final ComponentType<List<TargetedEnchantmentEffectType<EnchantmentValueEffectType>>> EQUIPMENT_DROPS = EnchantmentEffectComponentTypes.register("equipment_drops", builder -> builder.codec(TargetedEnchantmentEffectType.createEquipmentDropsCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentLocationBasedEffectType>>> LOCATION_CHANGED = EnchantmentEffectComponentTypes.register("location_changed", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentLocationBasedEffectType.CODEC, LootContextTypes.ENCHANTED_LOCATION).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffectType>>> TICK = EnchantmentEffectComponentTypes.register("tick", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> AMMO_USE = EnchantmentEffectComponentTypes.register("ammo_use", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> PROJECTILE_PIERCING = EnchantmentEffectComponentTypes.register("projectile_piercing", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffectType>>> PROJECTILE_SPAWNED = EnchantmentEffectComponentTypes.register("projectile_spawned", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> PROJECTILE_SPREAD = EnchantmentEffectComponentTypes.register("projectile_spread", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> PROJECTILE_COUNT = EnchantmentEffectComponentTypes.register("projectile_count", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> TRIDENT_RETURN_ACCELERATION = EnchantmentEffectComponentTypes.register("trident_return_acceleration", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> FISHING_TIME_REDUCTION = EnchantmentEffectComponentTypes.register("fishing_time_reduction", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> FISHING_LUCK_BONUS = EnchantmentEffectComponentTypes.register("fishing_luck_bonus", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> BLOCK_EXPERIENCE = EnchantmentEffectComponentTypes.register("block_experience", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> MOB_EXPERIENCE = EnchantmentEffectComponentTypes.register("mob_experience", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()));
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffectType>>> REPAIR_WITH_XP = EnchantmentEffectComponentTypes.register("repair_with_xp", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentValueEffectType.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()));
    public static final ComponentType<EnchantmentValueEffectType> CROSSBOW_CHARGE_TIME = EnchantmentEffectComponentTypes.register("crossbow_charge_time", builder -> builder.codec(EnchantmentValueEffectType.CODEC));
    public static final ComponentType<List<CrossbowItem.LoadingSounds>> CROSSBOW_CHARGING_SOUNDS = EnchantmentEffectComponentTypes.register("crossbow_charging_sounds", builder -> builder.codec(CrossbowItem.LoadingSounds.CODEC.listOf()));
    public static final ComponentType<List<RegistryEntry<SoundEvent>>> TRIDENT_SOUND = EnchantmentEffectComponentTypes.register("trident_sound", builder -> builder.codec(SoundEvent.ENTRY_CODEC.listOf()));
    public static final ComponentType<Unit> PREVENT_EQUIPMENT_DROP = EnchantmentEffectComponentTypes.register("prevent_equipment_drop", builder -> builder.codec(Unit.CODEC));
    public static final ComponentType<Unit> PREVENT_ARMOR_CHANGE = EnchantmentEffectComponentTypes.register("prevent_armor_change", builder -> builder.codec(Unit.CODEC));
    public static final ComponentType<EnchantmentValueEffectType> TRIDENT_SPIN_ATTACK_STRENGTH = EnchantmentEffectComponentTypes.register("trident_spin_attack_strength", builder -> builder.codec(EnchantmentValueEffectType.CODEC));

    public static ComponentType<?> getDefault(Registry<ComponentType<?>> registry) {
        return DAMAGE_PROTECTION;
    }

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id, ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build());
    }
}

