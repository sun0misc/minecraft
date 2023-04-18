package net.minecraft.util.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class AffineTransformation {
   private final Matrix4f matrix;
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codecs.VECTOR_3F.fieldOf("translation").forGetter((arg) -> {
         return arg.translation;
      }), Codecs.ROTATION.fieldOf("left_rotation").forGetter((affineTransformation) -> {
         return affineTransformation.leftRotation;
      }), Codecs.VECTOR_3F.fieldOf("scale").forGetter((affineTransformation) -> {
         return affineTransformation.scale;
      }), Codecs.ROTATION.fieldOf("right_rotation").forGetter((affineTransformation) -> {
         return affineTransformation.rightRotation;
      })).apply(instance, AffineTransformation::new);
   });
   public static final Codec ANY_CODEC;
   private boolean initialized;
   @Nullable
   private Vector3f translation;
   @Nullable
   private Quaternionf leftRotation;
   @Nullable
   private Vector3f scale;
   @Nullable
   private Quaternionf rightRotation;
   private static final AffineTransformation IDENTITY;

   public AffineTransformation(@Nullable Matrix4f matrix) {
      if (matrix == null) {
         this.matrix = new Matrix4f();
      } else {
         this.matrix = matrix;
      }

   }

   public AffineTransformation(@Nullable Vector3f translation, @Nullable Quaternionf leftRotation, @Nullable Vector3f scale, @Nullable Quaternionf rightRotation) {
      this.matrix = setup(translation, leftRotation, scale, rightRotation);
      this.translation = translation != null ? translation : new Vector3f();
      this.leftRotation = leftRotation != null ? leftRotation : new Quaternionf();
      this.scale = scale != null ? scale : new Vector3f(1.0F, 1.0F, 1.0F);
      this.rightRotation = rightRotation != null ? rightRotation : new Quaternionf();
      this.initialized = true;
   }

   public static AffineTransformation identity() {
      return IDENTITY;
   }

   public AffineTransformation multiply(AffineTransformation other) {
      Matrix4f matrix4f = this.getMatrix();
      matrix4f.mul(other.getMatrix());
      return new AffineTransformation(matrix4f);
   }

   @Nullable
   public AffineTransformation invert() {
      if (this == IDENTITY) {
         return this;
      } else {
         Matrix4f matrix4f = this.getMatrix().invert();
         return matrix4f.isFinite() ? new AffineTransformation(matrix4f) : null;
      }
   }

   private void init() {
      if (!this.initialized) {
         float f = 1.0F / this.matrix.m33();
         Triple triple = MatrixUtil.svdDecompose((new Matrix3f(this.matrix)).scale(f));
         this.translation = this.matrix.getTranslation(new Vector3f()).mul(f);
         this.leftRotation = new Quaternionf((Quaternionfc)triple.getLeft());
         this.scale = new Vector3f((Vector3fc)triple.getMiddle());
         this.rightRotation = new Quaternionf((Quaternionfc)triple.getRight());
         this.initialized = true;
      }

   }

   private static Matrix4f setup(@Nullable Vector3f translation, @Nullable Quaternionf leftRotation, @Nullable Vector3f scale, @Nullable Quaternionf rightRotation) {
      Matrix4f matrix4f = new Matrix4f();
      if (translation != null) {
         matrix4f.translation(translation);
      }

      if (leftRotation != null) {
         matrix4f.rotate(leftRotation);
      }

      if (scale != null) {
         matrix4f.scale(scale);
      }

      if (rightRotation != null) {
         matrix4f.rotate(rightRotation);
      }

      return matrix4f;
   }

   public Matrix4f getMatrix() {
      return new Matrix4f(this.matrix);
   }

   public Vector3f getTranslation() {
      this.init();
      return new Vector3f(this.translation);
   }

   public Quaternionf getLeftRotation() {
      this.init();
      return new Quaternionf(this.leftRotation);
   }

   public Vector3f getScale() {
      this.init();
      return new Vector3f(this.scale);
   }

   public Quaternionf getRightRotation() {
      this.init();
      return new Quaternionf(this.rightRotation);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AffineTransformation lv = (AffineTransformation)o;
         return Objects.equals(this.matrix, lv.matrix);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.matrix});
   }

   public AffineTransformation interpolate(AffineTransformation target, float factor) {
      Vector3f vector3f = this.getTranslation();
      Quaternionf quaternionf = this.getLeftRotation();
      Vector3f vector3f2 = this.getScale();
      Quaternionf quaternionf2 = this.getRightRotation();
      vector3f.lerp(target.getTranslation(), factor);
      quaternionf.slerp(target.getLeftRotation(), factor);
      vector3f2.lerp(target.getScale(), factor);
      quaternionf2.slerp(target.getRightRotation(), factor);
      return new AffineTransformation(vector3f, quaternionf, vector3f2, quaternionf2);
   }

   static {
      ANY_CODEC = Codec.either(CODEC, Codecs.MATRIX4F.xmap(AffineTransformation::new, AffineTransformation::getMatrix)).xmap((either) -> {
         return (AffineTransformation)either.map((affineTransformation) -> {
            return affineTransformation;
         }, (affineTransformation) -> {
            return affineTransformation;
         });
      }, Either::left);
      IDENTITY = (AffineTransformation)Util.make(() -> {
         AffineTransformation lv = new AffineTransformation(new Matrix4f());
         lv.translation = new Vector3f();
         lv.leftRotation = new Quaternionf();
         lv.scale = new Vector3f(1.0F, 1.0F, 1.0F);
         lv.rightRotation = new Quaternionf();
         lv.initialized = true;
         return lv;
      });
   }
}
