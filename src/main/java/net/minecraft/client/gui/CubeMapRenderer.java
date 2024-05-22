/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@Environment(value=EnvType.CLIENT)
public class CubeMapRenderer {
    private static final int FACES_COUNT = 6;
    private final Identifier[] faces = new Identifier[6];

    public CubeMapRenderer(Identifier faces) {
        for (int i = 0; i < 6; ++i) {
            this.faces[i] = faces.withPath(faces.getPath() + "_" + i + ".png");
        }
    }

    public void draw(MinecraftClient client, float x, float y, float alpha) {
        Tessellator lv = Tessellator.getInstance();
        Matrix4f matrix4f = new Matrix4f().setPerspective(1.4835298f, (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(), 0.05f, 10.0f);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_DISTANCE);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.rotationX((float)Math.PI);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        int i = 2;
        for (int j = 0; j < 4; ++j) {
            matrix4fStack.pushMatrix();
            float k = ((float)(j % 2) / 2.0f - 0.5f) / 256.0f;
            float l = ((float)(j / 2) / 2.0f - 0.5f) / 256.0f;
            float m = 0.0f;
            matrix4fStack.translate(k, l, 0.0f);
            matrix4fStack.rotateX(x * ((float)Math.PI / 180));
            matrix4fStack.rotateY(y * ((float)Math.PI / 180));
            RenderSystem.applyModelViewMatrix();
            for (int n = 0; n < 6; ++n) {
                RenderSystem.setShaderTexture(0, this.faces[n]);
                BufferBuilder lv2 = lv.method_60827(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                int o = Math.round(255.0f * alpha) / (j + 1);
                if (n == 0) {
                    lv2.vertex(-1.0f, -1.0f, 1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(-1.0f, 1.0f, 1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, 1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, -1.0f, 1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                if (n == 1) {
                    lv2.vertex(1.0f, -1.0f, 1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, 1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, -1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, -1.0f, -1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                if (n == 2) {
                    lv2.vertex(1.0f, -1.0f, -1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, -1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(-1.0f, 1.0f, -1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(-1.0f, -1.0f, -1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                if (n == 3) {
                    lv2.vertex(-1.0f, -1.0f, -1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(-1.0f, 1.0f, -1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(-1.0f, 1.0f, 1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(-1.0f, -1.0f, 1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                if (n == 4) {
                    lv2.vertex(-1.0f, -1.0f, -1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(-1.0f, -1.0f, 1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, -1.0f, 1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, -1.0f, -1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                if (n == 5) {
                    lv2.vertex(-1.0f, 1.0f, 1.0f).texture(0.0f, 0.0f).method_60832(o);
                    lv2.vertex(-1.0f, 1.0f, -1.0f).texture(0.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, -1.0f).texture(1.0f, 1.0f).method_60832(o);
                    lv2.vertex(1.0f, 1.0f, 1.0f).texture(1.0f, 0.0f).method_60832(o);
                }
                BufferRenderer.drawWithGlobalProgram(lv2.method_60800());
            }
            matrix4fStack.popMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.restoreProjectionMatrix();
        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public CompletableFuture<Void> loadTexturesAsync(TextureManager textureManager, Executor executor) {
        CompletableFuture[] completableFutures = new CompletableFuture[6];
        for (int i = 0; i < completableFutures.length; ++i) {
            completableFutures[i] = textureManager.loadTextureAsync(this.faces[i], executor);
        }
        return CompletableFuture.allOf(completableFutures);
    }
}

