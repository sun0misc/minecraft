/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;

@Environment(value=EnvType.CLIENT)
public class SimpleFramebuffer
extends Framebuffer {
    public SimpleFramebuffer(int width, int height, boolean useDepth, boolean getError) {
        super(useDepth);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, getError);
    }
}

