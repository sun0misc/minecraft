/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class LightningEntityRenderer
extends EntityRenderer<LightningEntity> {
    public LightningEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public void render(LightningEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        float[] fs = new float[8];
        float[] gs = new float[8];
        float h = 0.0f;
        float j = 0.0f;
        Random lv = Random.create(arg.seed);
        for (int k = 7; k >= 0; --k) {
            fs[k] = h;
            gs[k] = j;
            h += (float)(lv.nextInt(11) - 5);
            j += (float)(lv.nextInt(11) - 5);
        }
        VertexConsumer lv2 = arg3.getBuffer(RenderLayer.getLightning());
        Matrix4f matrix4f = arg2.peek().getPositionMatrix();
        for (int l = 0; l < 4; ++l) {
            Random lv3 = Random.create(arg.seed);
            for (int m = 0; m < 3; ++m) {
                int n = 7;
                int o = 0;
                if (m > 0) {
                    n = 7 - m;
                }
                if (m > 0) {
                    o = n - 2;
                }
                float p = fs[n] - h;
                float q = gs[n] - j;
                for (int r = n; r >= o; --r) {
                    float s = p;
                    float t = q;
                    if (m == 0) {
                        p += (float)(lv3.nextInt(11) - 5);
                        q += (float)(lv3.nextInt(11) - 5);
                    } else {
                        p += (float)(lv3.nextInt(31) - 15);
                        q += (float)(lv3.nextInt(31) - 15);
                    }
                    float u = 0.5f;
                    float v = 0.45f;
                    float w = 0.45f;
                    float x = 0.5f;
                    float y = 0.1f + (float)l * 0.2f;
                    if (m == 0) {
                        y *= (float)r * 0.1f + 1.0f;
                    }
                    float z = 0.1f + (float)l * 0.2f;
                    if (m == 0) {
                        z *= ((float)r - 1.0f) * 0.1f + 1.0f;
                    }
                    LightningEntityRenderer.drawBranch(matrix4f, lv2, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, false, false, true, false);
                    LightningEntityRenderer.drawBranch(matrix4f, lv2, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, true, false, true, true);
                    LightningEntityRenderer.drawBranch(matrix4f, lv2, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, true, true, false, true);
                    LightningEntityRenderer.drawBranch(matrix4f, lv2, p, q, r, s, t, 0.45f, 0.45f, 0.5f, y, z, false, true, false, false);
                }
            }
        }
    }

    private static void drawBranch(Matrix4f matrix, VertexConsumer buffer, float x1, float z1, int y, float x2, float z2, float red, float green, float blue, float offset2, float offset1, boolean shiftEast1, boolean shiftSouth1, boolean shiftEast2, boolean shiftSouth2) {
        buffer.vertex(matrix, x1 + (shiftEast1 ? offset1 : -offset1), (float)(y * 16), z1 + (shiftSouth1 ? offset1 : -offset1)).color(red, green, blue, 0.3f);
        buffer.vertex(matrix, x2 + (shiftEast1 ? offset2 : -offset2), (float)((y + 1) * 16), z2 + (shiftSouth1 ? offset2 : -offset2)).color(red, green, blue, 0.3f);
        buffer.vertex(matrix, x2 + (shiftEast2 ? offset2 : -offset2), (float)((y + 1) * 16), z2 + (shiftSouth2 ? offset2 : -offset2)).color(red, green, blue, 0.3f);
        buffer.vertex(matrix, x1 + (shiftEast2 ? offset1 : -offset1), (float)(y * 16), z1 + (shiftSouth2 ? offset1 : -offset1)).color(red, green, blue, 0.3f);
    }

    @Override
    public Identifier getTexture(LightningEntity arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}

