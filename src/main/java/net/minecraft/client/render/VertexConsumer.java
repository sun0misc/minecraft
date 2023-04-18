package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
   VertexConsumer vertex(double x, double y, double z);

   VertexConsumer color(int red, int green, int blue, int alpha);

   VertexConsumer texture(float u, float v);

   VertexConsumer overlay(int u, int v);

   VertexConsumer light(int u, int v);

   VertexConsumer normal(float x, float y, float z);

   void next();

   default void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.vertex((double)x, (double)y, (double)z);
      this.color(red, green, blue, alpha);
      this.texture(u, v);
      this.overlay(overlay);
      this.light(light);
      this.normal(normalX, normalY, normalZ);
      this.next();
   }

   void fixedColor(int red, int green, int blue, int alpha);

   void unfixColor();

   default VertexConsumer color(float red, float green, float blue, float alpha) {
      return this.color((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
   }

   default VertexConsumer color(int argb) {
      return this.color(ColorHelper.Argb.getRed(argb), ColorHelper.Argb.getGreen(argb), ColorHelper.Argb.getBlue(argb), ColorHelper.Argb.getAlpha(argb));
   }

   default VertexConsumer light(int uv) {
      return this.light(uv & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | '／'), uv >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | '／'));
   }

   default VertexConsumer overlay(int uv) {
      return this.overlay(uv & '\uffff', uv >> 16 & '\uffff');
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
      this.quad(matrixEntry, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, new int[]{light, light, light, light}, overlay, false);
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
      float[] gs = new float[]{brightnesses[0], brightnesses[1], brightnesses[2], brightnesses[3]};
      int[] js = new int[]{lights[0], lights[1], lights[2], lights[3]};
      int[] ks = quad.getVertexData();
      Vec3i lv = quad.getFace().getVector();
      Matrix4f matrix4f = matrixEntry.getPositionMatrix();
      Vector3f vector3f = matrixEntry.getNormalMatrix().transform(new Vector3f((float)lv.getX(), (float)lv.getY(), (float)lv.getZ()));
      int j = true;
      int k = ks.length / 8;
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
         IntBuffer intBuffer = byteBuffer.asIntBuffer();

         for(int l = 0; l < k; ++l) {
            intBuffer.clear();
            intBuffer.put(ks, l * 8, 8);
            float m = byteBuffer.getFloat(0);
            float n = byteBuffer.getFloat(4);
            float o = byteBuffer.getFloat(8);
            float s;
            float t;
            float u;
            float q;
            float r;
            if (useQuadColorData) {
               float p = (float)(byteBuffer.get(12) & 255) / 255.0F;
               q = (float)(byteBuffer.get(13) & 255) / 255.0F;
               r = (float)(byteBuffer.get(14) & 255) / 255.0F;
               s = p * gs[l] * red;
               t = q * gs[l] * green;
               u = r * gs[l] * blue;
            } else {
               s = gs[l] * red;
               t = gs[l] * green;
               u = gs[l] * blue;
            }

            int v = js[l];
            q = byteBuffer.getFloat(16);
            r = byteBuffer.getFloat(20);
            Vector4f vector4f = matrix4f.transform(new Vector4f(m, n, o, 1.0F));
            this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), s, t, u, 1.0F, q, r, overlay, v, vector3f.x(), vector3f.y(), vector3f.z());
         }
      } catch (Throwable var33) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var32) {
               var33.addSuppressed(var32);
            }
         }

         throw var33;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

   }

   default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
      Vector4f vector4f = matrix.transform(new Vector4f(x, y, z, 1.0F));
      return this.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
   }

   default VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
      Vector3f vector3f = matrix.transform(new Vector3f(x, y, z));
      return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
   }
}
