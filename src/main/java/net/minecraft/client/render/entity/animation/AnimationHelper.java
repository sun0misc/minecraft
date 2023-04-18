package net.minecraft.client.render.entity.animation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class AnimationHelper {
   public static void animate(SinglePartEntityModel model, Animation animation, long runningTime, float f, Vector3f vector3f) {
      float g = getRunningSeconds(animation, runningTime);
      Iterator var7 = animation.boneAnimations().entrySet().iterator();

      while(var7.hasNext()) {
         Map.Entry entry = (Map.Entry)var7.next();
         Optional optional = model.getChild((String)entry.getKey());
         List list = (List)entry.getValue();
         optional.ifPresent((part) -> {
            list.forEach((transformation) -> {
               Keyframe[] lvs = transformation.keyframes();
               int i = Math.max(0, MathHelper.binarySearch(0, lvs.length, (index) -> {
                  return g <= lvs[index].timestamp();
               }) - 1);
               int j = Math.min(lvs.length - 1, i + 1);
               Keyframe lv = lvs[i];
               Keyframe lv2 = lvs[j];
               float h = g - lv.timestamp();
               float k = MathHelper.clamp(h / (lv2.timestamp() - lv.timestamp()), 0.0F, 1.0F);
               lv2.interpolation().apply(vector3f, k, lvs, i, j, f);
               transformation.target().apply(part, vector3f);
            });
         });
      }

   }

   private static float getRunningSeconds(Animation animation, long runningTime) {
      float f = (float)runningTime / 1000.0F;
      return animation.looping() ? f % animation.lengthInSeconds() : f;
   }

   public static Vector3f createTranslationalVector(float f, float g, float h) {
      return new Vector3f(f, -g, h);
   }

   public static Vector3f createRotationalVector(float f, float g, float h) {
      return new Vector3f(f * 0.017453292F, g * 0.017453292F, h * 0.017453292F);
   }

   public static Vector3f createScalingVector(double d, double e, double f) {
      return new Vector3f((float)(d - 1.0), (float)(e - 1.0), (float)(f - 1.0));
   }
}
