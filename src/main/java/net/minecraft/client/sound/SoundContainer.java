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
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public interface SoundContainer<T> {
    public int getWeight();

    public T getSound(Random var1);

    public void preload(SoundSystem var1);
}

