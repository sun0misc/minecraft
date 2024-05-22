/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9801;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BufferRenderer {
    @Nullable
    private static VertexBuffer currentVertexBuffer;

    public static void reset() {
        if (currentVertexBuffer != null) {
            BufferRenderer.resetCurrentVertexBuffer();
            VertexBuffer.unbind();
        }
    }

    public static void resetCurrentVertexBuffer() {
        currentVertexBuffer = null;
    }

    public static void drawWithGlobalProgram(class_9801 buffer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> BufferRenderer.drawWithGlobalProgramInternal(buffer));
        } else {
            BufferRenderer.drawWithGlobalProgramInternal(buffer);
        }
    }

    private static void drawWithGlobalProgramInternal(class_9801 buffer) {
        VertexBuffer lv = BufferRenderer.upload(buffer);
        lv.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    public static void draw(class_9801 buffer) {
        VertexBuffer lv = BufferRenderer.upload(buffer);
        lv.draw();
    }

    private static VertexBuffer upload(class_9801 buffer) {
        RenderSystem.assertOnRenderThread();
        VertexBuffer lv = BufferRenderer.bind(buffer.method_60822().format());
        lv.upload(buffer);
        return lv;
    }

    private static VertexBuffer bind(VertexFormat vertexFormat) {
        VertexBuffer lv = vertexFormat.getBuffer();
        BufferRenderer.bind(lv);
        return lv;
    }

    private static void bind(VertexBuffer vertexBuffer) {
        if (vertexBuffer != currentVertexBuffer) {
            vertexBuffer.bind();
            currentVertexBuffer = vertexBuffer;
        }
    }
}

