/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class WindowSettings {
    public final int width;
    public final int height;
    public final OptionalInt fullscreenWidth;
    public final OptionalInt fullscreenHeight;
    public final boolean fullscreen;

    public WindowSettings(int width, int height, OptionalInt fullscreenWidth, OptionalInt fullscreenHeight, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.fullscreenWidth = fullscreenWidth;
        this.fullscreenHeight = fullscreenHeight;
        this.fullscreen = fullscreen;
    }
}

