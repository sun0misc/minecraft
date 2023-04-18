package net.minecraft.util.math;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
   private static final float COT_PI_OVER_8 = 3.0F + 2.0F * Math.sqrt(2.0F);
   private static final GivensPair SIN_COS_PI_OVER_8 = GivensPair.fromAngle(0.7853982F);

   private MatrixUtil() {
   }

   public static Matrix4f scale(Matrix4f matrix, float scalar) {
      return matrix.set(matrix.m00() * scalar, matrix.m01() * scalar, matrix.m02() * scalar, matrix.m03() * scalar, matrix.m10() * scalar, matrix.m11() * scalar, matrix.m12() * scalar, matrix.m13() * scalar, matrix.m20() * scalar, matrix.m21() * scalar, matrix.m22() * scalar, matrix.m23() * scalar, matrix.m30() * scalar, matrix.m31() * scalar, matrix.m32() * scalar, matrix.m33() * scalar);
   }

   private static GivensPair approximateGivensQuaternion(float a11, float a12, float a22) {
      float i = 2.0F * (a11 - a22);
      return COT_PI_OVER_8 * a12 * a12 < i * i ? GivensPair.normalize(a12, i) : SIN_COS_PI_OVER_8;
   }

   private static GivensPair qrGivensQuaternion(float a1, float a2) {
      float h = (float)java.lang.Math.hypot((double)a1, (double)a2);
      float i = h > 1.0E-6F ? a2 : 0.0F;
      float j = Math.abs(a1) + Math.max(h, 1.0E-6F);
      if (a1 < 0.0F) {
         float k = i;
         i = j;
         j = k;
      }

      return GivensPair.normalize(i, j);
   }

   private static void method_49742(Matrix3f matrix3f, Matrix3f matrix3f2) {
      matrix3f.mul(matrix3f2);
      matrix3f2.transpose();
      matrix3f2.mul(matrix3f);
      matrix3f.set(matrix3f2);
   }

   private static void applyJacobiIteration(Matrix3f matrix3f, Matrix3f matrix3f2, Quaternionf quaternionf, Quaternionf quaternionf2) {
      GivensPair lv;
      Quaternionf quaternionf3;
      if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6F) {
         lv = approximateGivensQuaternion(matrix3f.m00, 0.5F * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
         quaternionf3 = lv.method_49735(quaternionf);
         quaternionf2.mul(quaternionf3);
         lv.method_49734(matrix3f2);
         method_49742(matrix3f, matrix3f2);
      }

      if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6F) {
         lv = approximateGivensQuaternion(matrix3f.m00, 0.5F * (matrix3f.m02 + matrix3f.m20), matrix3f.m22).negateSin();
         quaternionf3 = lv.method_49732(quaternionf);
         quaternionf2.mul(quaternionf3);
         lv.method_49731(matrix3f2);
         method_49742(matrix3f, matrix3f2);
      }

      if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6F) {
         lv = approximateGivensQuaternion(matrix3f.m11, 0.5F * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
         quaternionf3 = lv.method_49729(quaternionf);
         quaternionf2.mul(quaternionf3);
         lv.method_49728(matrix3f2);
         method_49742(matrix3f, matrix3f2);
      }

   }

   public static Quaternionf method_49741(Matrix3f matrix3f, int i) {
      Quaternionf quaternionf = new Quaternionf();
      Matrix3f matrix3f2 = new Matrix3f();
      Quaternionf quaternionf2 = new Quaternionf();

      for(int j = 0; j < i; ++j) {
         applyJacobiIteration(matrix3f, matrix3f2, quaternionf2, quaternionf);
      }

      quaternionf.normalize();
      return quaternionf;
   }

   public static Triple svdDecompose(Matrix3f A) {
      Matrix3f matrix3f2 = new Matrix3f(A);
      matrix3f2.transpose();
      matrix3f2.mul(A);
      Quaternionf quaternionf = method_49741(matrix3f2, 5);
      float f = matrix3f2.m00;
      float g = matrix3f2.m11;
      boolean bl = (double)f < 1.0E-6;
      boolean bl2 = (double)g < 1.0E-6;
      Matrix3f matrix3f4 = A.rotate(quaternionf);
      Quaternionf quaternionf2 = new Quaternionf();
      Quaternionf quaternionf3 = new Quaternionf();
      GivensPair lv;
      if (bl) {
         lv = qrGivensQuaternion(matrix3f4.m11, -matrix3f4.m10);
      } else {
         lv = qrGivensQuaternion(matrix3f4.m00, matrix3f4.m01);
      }

      Quaternionf quaternionf4 = lv.method_49735(quaternionf3);
      Matrix3f matrix3f5 = lv.method_49734(matrix3f2);
      quaternionf2.mul(quaternionf4);
      matrix3f5.transpose().mul(matrix3f4);
      if (bl) {
         lv = qrGivensQuaternion(matrix3f5.m22, -matrix3f5.m20);
      } else {
         lv = qrGivensQuaternion(matrix3f5.m00, matrix3f5.m02);
      }

      lv = lv.negateSin();
      Quaternionf quaternionf5 = lv.method_49732(quaternionf3);
      Matrix3f matrix3f6 = lv.method_49731(matrix3f4);
      quaternionf2.mul(quaternionf5);
      matrix3f6.transpose().mul(matrix3f5);
      if (bl2) {
         lv = qrGivensQuaternion(matrix3f6.m22, -matrix3f6.m21);
      } else {
         lv = qrGivensQuaternion(matrix3f6.m11, matrix3f6.m12);
      }

      Quaternionf quaternionf6 = lv.method_49729(quaternionf3);
      Matrix3f matrix3f7 = lv.method_49728(matrix3f5);
      quaternionf2.mul(quaternionf6);
      matrix3f7.transpose().mul(matrix3f6);
      Vector3f vector3f = new Vector3f(matrix3f7.m00, matrix3f7.m11, matrix3f7.m22);
      return Triple.of(quaternionf2, vector3f, quaternionf.conjugate());
   }
}
