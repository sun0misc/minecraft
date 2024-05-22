/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

class SaturationStatusEffect
extends InstantStatusEffect {
    protected SaturationStatusEffect(StatusEffectCategory arg, int i) {
        super(arg, i);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            lv.getHungerManager().add(amplifier + 1, 1.0f);
        }
        return true;
    }
}

