/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class GlDebugInfo {
    public static ByteBuffer allocateMemory(int size) {
        return MemoryUtil.memAlloc(size);
    }

    public static void freeMemory(Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }

    public static String getVendor() {
        return GlStateManager._getString(GL11.GL_VENDOR);
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return GlStateManager._getString(GL11.GL_RENDERER);
    }

    public static String getVersion() {
        return GlStateManager._getString(GL11.GL_VERSION);
    }
}

