/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderCallStorage;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class GlfwUtil {
    public static void accessRecordingQueue(RenderCallStorage storage, float f) {
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = storage.getRecordingQueue();
    }

    public static void accessProcessingQueue(RenderCallStorage storage, float f) {
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = storage.getProcessingQueue();
    }

    public static void makeJvmCrash() {
        MemoryUtil.memSet(0L, 0, 1L);
    }

    public static double getTime() {
        return GLFW.glfwGetTime();
    }
}

