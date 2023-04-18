package net.minecraft.client.render.entity.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public record Transformation(Target target, Keyframe... keyframes) {
   public Transformation(Target arg, Keyframe... args) {
      this.target = arg;
      this.keyframes = args;
   }

   public Target target() {
      return this.target;
   }

   public Keyframe[] keyframes() {
      return this.keyframes;
   }

   @Environment(EnvType.CLIENT)
   public interface Target {
      void apply(ModelPart modelPart, Vector3f vector3f);
   }

   @Environment(EnvType.CLIENT)
   public static class Interpolations {
      public static final Interpolation LINEAR = (vector3f, delta, keyframes, start, end, g) -> {
         Vector3f vector3f2 = keyframes[start].target();
         Vector3f vector3f3 = keyframes[end].target();
         return vector3f2.lerp(vector3f3, delta, vector3f).mul(g);
      };
      public static final Interpolation CUBIC = (vector3f, delta, keyframes, start, end, g) -> {
         Vector3f vector3f2 = keyframes[Math.max(0, start - 1)].target();
         Vector3f vector3f3 = keyframes[start].target();
         Vector3f vector3f4 = keyframes[end].target();
         Vector3f vector3f5 = keyframes[Math.min(keyframes.length - 1, end + 1)].target();
         vector3f.set(MathHelper.catmullRom(delta, vector3f2.x(), vector3f3.x(), vector3f4.x(), vector3f5.x()) * g, MathHelper.catmullRom(delta, vector3f2.y(), vector3f3.y(), vector3f4.y(), vector3f5.y()) * g, MathHelper.catmullRom(delta, vector3f2.z(), vector3f3.z(), vector3f4.z(), vector3f5.z()) * g);
         return vector3f;
      };
   }

   @Environment(EnvType.CLIENT)
   public static class Targets {
      public static final Target TRANSLATE = ModelPart::translate;
      public static final Target ROTATE = ModelPart::rotate;
      public static final Target SCALE = ModelPart::scale;
   }

   @Environment(EnvType.CLIENT)
   public interface Interpolation {
      Vector3f apply(Vector3f vector3f, float delta, Keyframe[] keyframes, int start, int end, float g);
   }
}
