/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumer {
    public VertexConsumer vertex(float var1, float var2, float var3);

    public VertexConsumer color(int var1, int var2, int var3, int var4);

    public VertexConsumer texture(float var1, float var2);

    public VertexConsumer method_60796(int var1, int var2);

    public VertexConsumer light(int var1, int var2);

    public VertexConsumer normal(float var1, float var2, float var3);

    default public void vertex(float x, float y, float z, int i, float green, float blue, int l, int m, float v, float o, float p) {
        this.vertex(x, y, z);
        this.color(i);
        this.texture(green, blue);
        this.overlay(l);
        this.method_60803(m);
        this.normal(v, o, p);
    }

    default public VertexConsumer color(float red, float green, float blue, float alpha) {
        return this.color((int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f), (int)(alpha * 255.0f));
    }

    default public VertexConsumer color(int argb) {
        return this.color(ColorHelper.Argb.getRed(argb), ColorHelper.Argb.getGreen(argb), ColorHelper.Argb.getBlue(argb), ColorHelper.Argb.getAlpha(argb));
    }

    default public VertexConsumer method_60832(int i) {
        return this.color(ColorHelper.Argb.withAlpha(i, -1));
    }

    default public VertexConsumer method_60803(int i) {
        return this.light(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public VertexConsumer overlay(int uv) {
        return this.method_60796(uv & 0xFFFF, uv >> 16 & 0xFFFF);
    }

    default public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float i, int j, int k) {
        this.quad(matrixEntry, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, red, green, blue, i, new int[]{j, j, j, j}, k, false);
    }

    default public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float i, int[] is, int j, boolean bl) {
        int[] js = quad.getVertexData();
        Vec3i lv = quad.getFace().getVector();
        Matrix4f matrix4f = matrixEntry.getPositionMatrix();
        Vector3f vector3f = matrixEntry.transformNormal(lv.getX(), lv.getY(), lv.getZ(), new Vector3f());
        int k = 8;
        int l = js.length / 8;
        int m = (int)(i * 255.0f);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int n = 0; n < l; ++n) {
                float w;
                float v;
                float u;
                float t;
                intBuffer.clear();
                intBuffer.put(js, n * 8, 8);
                float o = byteBuffer.getFloat(0);
                float p = byteBuffer.getFloat(4);
                float q = byteBuffer.getFloat(8);
                if (bl) {
                    float r = byteBuffer.get(12) & 0xFF;
                    float s = byteBuffer.get(13) & 0xFF;
                    t = byteBuffer.get(14) & 0xFF;
                    u = r * brightnesses[n] * red;
                    v = s * brightnesses[n] * green;
                    w = t * brightnesses[n] * blue;
                } else {
                    u = brightnesses[n] * red * 255.0f;
                    v = brightnesses[n] * green * 255.0f;
                    w = brightnesses[n] * blue * 255.0f;
                }
                int x = ColorHelper.Argb.getArgb(m, (int)u, (int)v, (int)w);
                int y = is[n];
                t = byteBuffer.getFloat(16);
                float z = byteBuffer.getFloat(20);
                Vector3f vector3f2 = matrix4f.transformPosition(o, p, q, new Vector3f());
                this.vertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), x, t, z, j, y, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default public VertexConsumer method_60830(Vector3f vector3f) {
        return this.vertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer vertex(MatrixStack.Entry matrix, float x, float y, float z) {
        return this.vertex(matrix.getPositionMatrix(), x, y, z);
    }

    default public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        return this.vertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer method_60831(MatrixStack.Entry arg, float f, float g, float h) {
        Vector3f vector3f = arg.transformNormal(f, g, h, new Vector3f());
        return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
    }
}

