/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class VertexBuffer
implements AutoCloseable {
    private final Usage usage;
    private int vertexBufferId;
    private int indexBufferId;
    private int vertexArrayId;
    @Nullable
    private VertexFormat vertexFormat;
    @Nullable
    private RenderSystem.ShapeIndexBuffer sharedSequentialIndexBuffer;
    private VertexFormat.IndexType indexType;
    private int indexCount;
    private VertexFormat.DrawMode drawMode;

    public VertexBuffer(Usage usage) {
        this.usage = usage;
        RenderSystem.assertOnRenderThread();
        this.vertexBufferId = GlStateManager._glGenBuffers();
        this.indexBufferId = GlStateManager._glGenBuffers();
        this.vertexArrayId = GlStateManager._glGenVertexArrays();
    }

    public void upload(class_9801 arg) {
        try (class_9801 class_98012 = arg;){
            if (this.isClosed()) {
                return;
            }
            RenderSystem.assertOnRenderThread();
            class_9801.DrawParameters lv = arg.method_60822();
            this.vertexFormat = this.uploadVertexBuffer(lv, arg.method_60818());
            this.sharedSequentialIndexBuffer = this.uploadIndexBuffer(lv, arg.method_60821());
            this.indexCount = lv.indexCount();
            this.indexType = lv.indexType();
            this.drawMode = lv.mode();
        }
    }

    public void method_60829(class_9799.class_9800 arg) {
        try (class_9799.class_9800 class_98002 = arg;){
            if (this.isClosed()) {
                return;
            }
            RenderSystem.assertOnRenderThread();
            GlStateManager._glBindBuffer(GlConst.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
            RenderSystem.glBufferData(GlConst.GL_ELEMENT_ARRAY_BUFFER, arg.method_60817(), this.usage.id);
            this.sharedSequentialIndexBuffer = null;
        }
    }

    private VertexFormat uploadVertexBuffer(class_9801.DrawParameters parameters, @Nullable ByteBuffer vertexBuffer) {
        boolean bl = false;
        if (!parameters.format().equals(this.vertexFormat)) {
            if (this.vertexFormat != null) {
                this.vertexFormat.clearState();
            }
            GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
            parameters.format().setupState();
            bl = true;
        }
        if (vertexBuffer != null) {
            if (!bl) {
                GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
            }
            RenderSystem.glBufferData(GlConst.GL_ARRAY_BUFFER, vertexBuffer, this.usage.id);
        }
        return parameters.format();
    }

    @Nullable
    private RenderSystem.ShapeIndexBuffer uploadIndexBuffer(class_9801.DrawParameters parameters, @Nullable ByteBuffer indexBuffer) {
        if (indexBuffer == null) {
            RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(parameters.mode());
            if (lv != this.sharedSequentialIndexBuffer || !lv.isLargeEnough(parameters.indexCount())) {
                lv.bindAndGrow(parameters.indexCount());
            }
            return lv;
        }
        GlStateManager._glBindBuffer(GlConst.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
        RenderSystem.glBufferData(GlConst.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, this.usage.id);
        return null;
    }

    public void bind() {
        BufferRenderer.resetCurrentVertexBuffer();
        GlStateManager._glBindVertexArray(this.vertexArrayId);
    }

    public static void unbind() {
        BufferRenderer.resetCurrentVertexBuffer();
        GlStateManager._glBindVertexArray(0);
    }

    public void draw() {
        RenderSystem.drawElements(this.drawMode.glMode, this.indexCount, this.getIndexType().glType);
    }

    private VertexFormat.IndexType getIndexType() {
        RenderSystem.ShapeIndexBuffer lv = this.sharedSequentialIndexBuffer;
        return lv != null ? lv.getIndexType() : this.indexType;
    }

    public void draw(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.drawInternal(new Matrix4f(viewMatrix), new Matrix4f(projectionMatrix), program));
        } else {
            this.drawInternal(viewMatrix, projectionMatrix, program);
        }
    }

    private void drawInternal(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram arg) {
        arg.method_60897(this.drawMode, viewMatrix, projectionMatrix, MinecraftClient.getInstance().getWindow());
        arg.bind();
        this.draw();
        arg.unbind();
    }

    @Override
    public void close() {
        if (this.vertexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = -1;
        }
        if (this.indexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }
        if (this.vertexArrayId >= 0) {
            RenderSystem.glDeleteVertexArrays(this.vertexArrayId);
            this.vertexArrayId = -1;
        }
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public boolean isClosed() {
        return this.vertexArrayId == -1;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Usage {
        STATIC(35044),
        DYNAMIC(35048);

        final int id;

        private Usage(int id) {
            this.id = id;
        }
    }
}

