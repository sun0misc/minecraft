/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class GlyphRenderer {
    private final TextRenderLayerSet textRenderLayers;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float minX;
    private final float maxX;
    private final float minY;
    private final float maxY;

    public GlyphRenderer(TextRenderLayerSet textRenderLayers, float minU, float maxU, float minV, float maxV, float minX, float maxX, float minY, float maxY) {
        this.textRenderLayers = textRenderLayers;
        this.minU = minU;
        this.maxU = maxU;
        this.minV = minV;
        this.maxV = maxV;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public void draw(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
        float m = x + this.minX;
        float n = x + this.maxX;
        float o = y + this.minY;
        float p = y + this.maxY;
        float q = italic ? 1.0f - 0.25f * this.minY : 0.0f;
        float r = italic ? 1.0f - 0.25f * this.maxY : 0.0f;
        vertexConsumer.vertex(matrix, m + q, o, 0.0f).color(red, green, blue, alpha).texture(this.minU, this.minV).method_60803(light);
        vertexConsumer.vertex(matrix, m + r, p, 0.0f).color(red, green, blue, alpha).texture(this.minU, this.maxV).method_60803(light);
        vertexConsumer.vertex(matrix, n + r, p, 0.0f).color(red, green, blue, alpha).texture(this.maxU, this.maxV).method_60803(light);
        vertexConsumer.vertex(matrix, n + q, o, 0.0f).color(red, green, blue, alpha).texture(this.maxU, this.minV).method_60803(light);
    }

    public void drawRectangle(Rectangle rectangle, Matrix4f matrix, VertexConsumer vertexConsumer, int light) {
        vertexConsumer.vertex(matrix, rectangle.minX, rectangle.minY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.minU, this.minV).method_60803(light);
        vertexConsumer.vertex(matrix, rectangle.maxX, rectangle.minY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.minU, this.maxV).method_60803(light);
        vertexConsumer.vertex(matrix, rectangle.maxX, rectangle.maxY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.maxU, this.maxV).method_60803(light);
        vertexConsumer.vertex(matrix, rectangle.minX, rectangle.maxY, rectangle.zIndex).color(rectangle.red, rectangle.green, rectangle.blue, rectangle.alpha).texture(this.maxU, this.minV).method_60803(light);
    }

    public RenderLayer getLayer(TextRenderer.TextLayerType layerType) {
        return this.textRenderLayers.getRenderLayer(layerType);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Rectangle {
        protected final float minX;
        protected final float minY;
        protected final float maxX;
        protected final float maxY;
        protected final float zIndex;
        protected final float red;
        protected final float green;
        protected final float blue;
        protected final float alpha;

        public Rectangle(float minX, float minY, float maxX, float maxY, float zIndex, float red, float green, float blue, float alpha) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.zIndex = zIndex;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }
}

