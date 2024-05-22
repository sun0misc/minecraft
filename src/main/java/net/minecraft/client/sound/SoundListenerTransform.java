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
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public record SoundListenerTransform(Vec3d position, Vec3d forward, Vec3d up) {
    public static final SoundListenerTransform DEFAULT = new SoundListenerTransform(Vec3d.ZERO, new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 1.0, 0.0));

    public Vec3d right() {
        return this.forward.crossProduct(this.up);
    }
}

