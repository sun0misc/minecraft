package net.minecraft.client.util.math;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class MatrixStack {
   private final Deque stack = (Deque)Util.make(Queues.newArrayDeque(), (stack) -> {
      Matrix4f matrix4f = new Matrix4f();
      Matrix3f matrix3f = new Matrix3f();
      stack.add(new Entry(matrix4f, matrix3f));
   });

   public void translate(double x, double y, double z) {
      this.translate((float)x, (float)y, (float)z);
   }

   public void translate(float x, float y, float z) {
      Entry lv = (Entry)this.stack.getLast();
      lv.positionMatrix.translate(x, y, z);
   }

   public void scale(float x, float y, float z) {
      Entry lv = (Entry)this.stack.getLast();
      lv.positionMatrix.scale(x, y, z);
      if (x == y && y == z) {
         if (x > 0.0F) {
            return;
         }

         lv.normalMatrix.scale(-1.0F);
      }

      float i = 1.0F / x;
      float j = 1.0F / y;
      float k = 1.0F / z;
      float l = MathHelper.fastInverseCbrt(i * j * k);
      lv.normalMatrix.scale(l * i, l * j, l * k);
   }

   public void multiply(Quaternionf quaternion) {
      Entry lv = (Entry)this.stack.getLast();
      lv.positionMatrix.rotate(quaternion);
      lv.normalMatrix.rotate(quaternion);
   }

   public void multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
      Entry lv = (Entry)this.stack.getLast();
      lv.positionMatrix.rotateAround(quaternion, originX, originY, originZ);
      lv.normalMatrix.rotate(quaternion);
   }

   public void push() {
      Entry lv = (Entry)this.stack.getLast();
      this.stack.addLast(new Entry(new Matrix4f(lv.positionMatrix), new Matrix3f(lv.normalMatrix)));
   }

   public void pop() {
      this.stack.removeLast();
   }

   public Entry peek() {
      return (Entry)this.stack.getLast();
   }

   public boolean isEmpty() {
      return this.stack.size() == 1;
   }

   public void loadIdentity() {
      Entry lv = (Entry)this.stack.getLast();
      lv.positionMatrix.identity();
      lv.normalMatrix.identity();
   }

   public void multiplyPositionMatrix(Matrix4f matrix) {
      ((Entry)this.stack.getLast()).positionMatrix.mul(matrix);
   }

   @Environment(EnvType.CLIENT)
   public static final class Entry {
      final Matrix4f positionMatrix;
      final Matrix3f normalMatrix;

      Entry(Matrix4f positionMatrix, Matrix3f normalMatrix) {
         this.positionMatrix = positionMatrix;
         this.normalMatrix = normalMatrix;
      }

      public Matrix4f getPositionMatrix() {
         return this.positionMatrix;
      }

      public Matrix3f getNormalMatrix() {
         return this.normalMatrix;
      }
   }
}
