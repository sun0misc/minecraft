/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;

public class FoodComponents {
    public static final FoodComponent APPLE = new FoodComponent.Builder().nutrition(4).saturationModifier(0.3f).build();
    public static final FoodComponent BAKED_POTATO = new FoodComponent.Builder().nutrition(5).saturationModifier(0.6f).build();
    public static final FoodComponent BEEF = new FoodComponent.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodComponent BEETROOT = new FoodComponent.Builder().nutrition(1).saturationModifier(0.6f).build();
    public static final FoodComponent BEETROOT_SOUP = FoodComponents.createStew(6).build();
    public static final FoodComponent BREAD = new FoodComponent.Builder().nutrition(5).saturationModifier(0.6f).build();
    public static final FoodComponent CARROT = new FoodComponent.Builder().nutrition(3).saturationModifier(0.6f).build();
    public static final FoodComponent CHICKEN = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.3f).build();
    public static final FoodComponent CHORUS_FRUIT = new FoodComponent.Builder().nutrition(4).saturationModifier(0.3f).alwaysEdible().build();
    public static final FoodComponent COD = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodComponent COOKED_BEEF = new FoodComponent.Builder().nutrition(8).saturationModifier(0.8f).build();
    public static final FoodComponent COOKED_CHICKEN = new FoodComponent.Builder().nutrition(6).saturationModifier(0.6f).build();
    public static final FoodComponent COOKED_COD = new FoodComponent.Builder().nutrition(5).saturationModifier(0.6f).build();
    public static final FoodComponent COOKED_MUTTON = new FoodComponent.Builder().nutrition(6).saturationModifier(0.8f).build();
    public static final FoodComponent COOKED_PORKCHOP = new FoodComponent.Builder().nutrition(8).saturationModifier(0.8f).build();
    public static final FoodComponent COOKED_RABBIT = new FoodComponent.Builder().nutrition(5).saturationModifier(0.6f).build();
    public static final FoodComponent COOKED_SALMON = new FoodComponent.Builder().nutrition(6).saturationModifier(0.8f).build();
    public static final FoodComponent COOKIE = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodComponent DRIED_KELP = new FoodComponent.Builder().nutrition(1).saturationModifier(0.3f).snack().build();
    public static final FoodComponent ENCHANTED_GOLDEN_APPLE = new FoodComponent.Builder().nutrition(4).saturationModifier(1.2f).statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 0), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 3), 1.0f).alwaysEdible().build();
    public static final FoodComponent GOLDEN_APPLE = new FoodComponent.Builder().nutrition(4).saturationModifier(1.2f).statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0), 1.0f).alwaysEdible().build();
    public static final FoodComponent GOLDEN_CARROT = new FoodComponent.Builder().nutrition(6).saturationModifier(1.2f).build();
    public static final FoodComponent HONEY_BOTTLE = new FoodComponent.Builder().nutrition(6).saturationModifier(0.1f).build();
    public static final FoodComponent MELON_SLICE = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).build();
    public static final FoodComponent MUSHROOM_STEW = FoodComponents.createStew(6).build();
    public static final FoodComponent MUTTON = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).build();
    public static final FoodComponent POISONOUS_POTATO = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 0.6f).build();
    public static final FoodComponent PORKCHOP = new FoodComponent.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodComponent POTATO = new FoodComponent.Builder().nutrition(1).saturationModifier(0.3f).build();
    public static final FoodComponent PUFFERFISH = new FoodComponent.Builder().nutrition(1).saturationModifier(0.1f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 1200, 1), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2), 1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0), 1.0f).build();
    public static final FoodComponent PUMPKIN_PIE = new FoodComponent.Builder().nutrition(8).saturationModifier(0.3f).build();
    public static final FoodComponent RABBIT = new FoodComponent.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodComponent RABBIT_STEW = FoodComponents.createStew(10).build();
    public static final FoodComponent ROTTEN_FLESH = new FoodComponent.Builder().nutrition(4).saturationModifier(0.1f).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.8f).build();
    public static final FoodComponent SALMON = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodComponent SPIDER_EYE = new FoodComponent.Builder().nutrition(2).saturationModifier(0.8f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 1.0f).build();
    public static final FoodComponent SUSPICIOUS_STEW = FoodComponents.createStew(6).alwaysEdible().build();
    public static final FoodComponent SWEET_BERRIES = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodComponent GLOW_BERRIES = new FoodComponent.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodComponent TROPICAL_FISH = new FoodComponent.Builder().nutrition(1).saturationModifier(0.1f).build();
    public static final FoodComponent OMINOUS_BOTTLE = new FoodComponent.Builder().nutrition(1).saturationModifier(0.1f).build();

    private static FoodComponent.Builder createStew(int hunger) {
        return new FoodComponent.Builder().nutrition(hunger).saturationModifier(0.6f).usingConvertsTo(Items.BOWL);
    }
}

