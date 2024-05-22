/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

public interface Saddleable {
    public boolean canBeSaddled();

    public void saddle(@Nullable SoundCategory var1);

    default public SoundEvent getSaddleSound() {
        return SoundEvents.ENTITY_HORSE_SADDLE;
    }

    public boolean isSaddled();
}

