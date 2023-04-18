package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class MipmapHelper {
   private static final int field_32949 = 96;
   private static final float[] COLOR_FRACTIONS = (float[])Util.make(new float[256], (list) -> {
      for(int i = 0; i < list.length; ++i) {
         list[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2);
      }

   });

   private MipmapHelper() {
   }

   public static NativeImage[] getMipmapLevelsImages(NativeImage[] originals, int mipmap) {
      if (mipmap + 1 <= originals.length) {
         return originals;
      } else {
         NativeImage[] lvs = new NativeImage[mipmap + 1];
         lvs[0] = originals[0];
         boolean bl = hasAlpha(lvs[0]);

         for(int j = 1; j <= mipmap; ++j) {
            if (j < originals.length) {
               lvs[j] = originals[j];
            } else {
               NativeImage lv = lvs[j - 1];
               NativeImage lv2 = new NativeImage(lv.getWidth() >> 1, lv.getHeight() >> 1, false);
               int k = lv2.getWidth();
               int l = lv2.getHeight();

               for(int m = 0; m < k; ++m) {
                  for(int n = 0; n < l; ++n) {
                     lv2.setColor(m, n, blend(lv.getColor(m * 2 + 0, n * 2 + 0), lv.getColor(m * 2 + 1, n * 2 + 0), lv.getColor(m * 2 + 0, n * 2 + 1), lv.getColor(m * 2 + 1, n * 2 + 1), bl));
                  }
               }

               lvs[j] = lv2;
            }
         }

         return lvs;
      }
   }

   private static boolean hasAlpha(NativeImage image) {
      for(int i = 0; i < image.getWidth(); ++i) {
         for(int j = 0; j < image.getHeight(); ++j) {
            if (image.getColor(i, j) >> 24 == 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static int blend(int one, int two, int three, int four, boolean checkAlpha) {
      if (checkAlpha) {
         float f = 0.0F;
         float g = 0.0F;
         float h = 0.0F;
         float m = 0.0F;
         if (one >> 24 != 0) {
            f += getColorFraction(one >> 24);
            g += getColorFraction(one >> 16);
            h += getColorFraction(one >> 8);
            m += getColorFraction(one >> 0);
         }

         if (two >> 24 != 0) {
            f += getColorFraction(two >> 24);
            g += getColorFraction(two >> 16);
            h += getColorFraction(two >> 8);
            m += getColorFraction(two >> 0);
         }

         if (three >> 24 != 0) {
            f += getColorFraction(three >> 24);
            g += getColorFraction(three >> 16);
            h += getColorFraction(three >> 8);
            m += getColorFraction(three >> 0);
         }

         if (four >> 24 != 0) {
            f += getColorFraction(four >> 24);
            g += getColorFraction(four >> 16);
            h += getColorFraction(four >> 8);
            m += getColorFraction(four >> 0);
         }

         f /= 4.0F;
         g /= 4.0F;
         h /= 4.0F;
         m /= 4.0F;
         int n = (int)(Math.pow((double)f, 0.45454545454545453) * 255.0);
         int o = (int)(Math.pow((double)g, 0.45454545454545453) * 255.0);
         int p = (int)(Math.pow((double)h, 0.45454545454545453) * 255.0);
         int q = (int)(Math.pow((double)m, 0.45454545454545453) * 255.0);
         if (n < 96) {
            n = 0;
         }

         return n << 24 | o << 16 | p << 8 | q;
      } else {
         int r = getColorComponent(one, two, three, four, 24);
         int s = getColorComponent(one, two, three, four, 16);
         int t = getColorComponent(one, two, three, four, 8);
         int u = getColorComponent(one, two, three, four, 0);
         return r << 24 | s << 16 | t << 8 | u;
      }
   }

   private static int getColorComponent(int one, int two, int three, int four, int bits) {
      float f = getColorFraction(one >> bits);
      float g = getColorFraction(two >> bits);
      float h = getColorFraction(three >> bits);
      float n = getColorFraction(four >> bits);
      float o = (float)((double)((float)Math.pow((double)(f + g + h + n) * 0.25, 0.45454545454545453)));
      return (int)((double)o * 255.0);
   }

   private static float getColorFraction(int value) {
      return COLOR_FRACTIONS[value & 255];
   }
}
