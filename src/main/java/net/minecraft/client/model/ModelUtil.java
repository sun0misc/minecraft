package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModelUtil {
   public static float interpolateAngle(float angle1, float angle2, float progress) {
      float i;
      for(i = angle2 - angle1; i < -3.1415927F; i += 6.2831855F) {
      }

      while(i >= 3.1415927F) {
         i -= 6.2831855F;
      }

      return angle1 + progress * i;
   }
}
