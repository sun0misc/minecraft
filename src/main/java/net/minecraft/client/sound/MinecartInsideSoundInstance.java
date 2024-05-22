/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class MinecartInsideSoundInstance
extends MovingSoundInstance {
    private static final float field_33006 = 0.0f;
    private static final float field_33007 = 0.75f;
    private final PlayerEntity player;
    private final AbstractMinecartEntity minecart;
    private final boolean underwater;

    public MinecartInsideSoundInstance(PlayerEntity player, AbstractMinecartEntity minecart, boolean underwater) {
        super(underwater ? SoundEvents.ENTITY_MINECART_INSIDE_UNDERWATER : SoundEvents.ENTITY_MINECART_INSIDE, SoundCategory.NEUTRAL, SoundInstance.createRandom());
        this.player = player;
        this.minecart = minecart;
        this.underwater = underwater;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
    }

    @Override
    public boolean canPlay() {
        return !this.minecart.isSilent();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if (this.minecart.isRemoved() || !this.player.hasVehicle() || this.player.getVehicle() != this.minecart) {
            this.setDone();
            return;
        }
        if (this.underwater != this.player.isSubmergedInWater()) {
            this.volume = 0.0f;
            return;
        }
        float f = (float)this.minecart.getVelocity().horizontalLength();
        this.volume = f >= 0.01f ? MathHelper.clampedLerp(0.0f, 0.75f, f) : 0.0f;
    }
}

