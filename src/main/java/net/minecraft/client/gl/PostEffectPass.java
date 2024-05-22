/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class PostEffectPass
implements AutoCloseable {
    private final JsonEffectShaderProgram program;
    public final Framebuffer input;
    public final Framebuffer output;
    private final List<IntSupplier> samplerValues = Lists.newArrayList();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerWidths = Lists.newArrayList();
    private final List<Integer> samplerHeights = Lists.newArrayList();
    private Matrix4f projectionMatrix;
    private final int texFilter;

    public PostEffectPass(ResourceFactory resourceFactory, String programName, Framebuffer input, Framebuffer output, boolean linear) throws IOException {
        this.program = new JsonEffectShaderProgram(resourceFactory, programName);
        this.input = input;
        this.output = output;
        this.texFilter = linear ? 9729 : 9728;
    }

    @Override
    public void close() {
        this.program.close();
    }

    public final String getName() {
        return this.program.getName();
    }

    public void addAuxTarget(String name, IntSupplier valueSupplier, int width, int height) {
        this.samplerNames.add(this.samplerNames.size(), name);
        this.samplerValues.add(this.samplerValues.size(), valueSupplier);
        this.samplerWidths.add(this.samplerWidths.size(), width);
        this.samplerHeights.add(this.samplerHeights.size(), height);
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void render(float time) {
        this.input.endWrite();
        float g = this.output.textureWidth;
        float h = this.output.textureHeight;
        RenderSystem.viewport(0, 0, (int)g, (int)h);
        this.program.bindSampler("DiffuseSampler", this.input::getColorAttachment);
        for (int i = 0; i < this.samplerValues.size(); ++i) {
            this.program.bindSampler(this.samplerNames.get(i), this.samplerValues.get(i));
            this.program.getUniformByNameOrDummy("AuxSize" + i).set((float)this.samplerWidths.get(i).intValue(), (float)this.samplerHeights.get(i).intValue());
        }
        this.program.getUniformByNameOrDummy("ProjMat").set(this.projectionMatrix);
        this.program.getUniformByNameOrDummy("InSize").set((float)this.input.textureWidth, (float)this.input.textureHeight);
        this.program.getUniformByNameOrDummy("OutSize").set(g, h);
        this.program.getUniformByNameOrDummy("Time").set(time);
        MinecraftClient lv = MinecraftClient.getInstance();
        this.program.getUniformByNameOrDummy("ScreenSize").set((float)lv.getWindow().getFramebufferWidth(), (float)lv.getWindow().getFramebufferHeight());
        this.program.enable();
        this.output.clear(MinecraftClient.IS_SYSTEM_MAC);
        this.output.beginWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder lv2 = Tessellator.getInstance().method_60827(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        lv2.vertex(0.0f, 0.0f, 500.0f);
        lv2.vertex(g, 0.0f, 500.0f);
        lv2.vertex(g, h, 500.0f);
        lv2.vertex(0.0f, h, 500.0f);
        BufferRenderer.draw(lv2.method_60800());
        RenderSystem.depthFunc(515);
        this.program.disable();
        this.output.endWrite();
        this.input.endRead();
        for (IntSupplier object : this.samplerValues) {
            if (!(object instanceof Framebuffer)) continue;
            ((Framebuffer)((Object)object)).endRead();
        }
    }

    public JsonEffectShaderProgram getProgram() {
        return this.program;
    }

    public int getTexFilter() {
        return this.texFilter;
    }
}

