package net.minecraft.world.biome.source.util;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctions;

public class VanillaTerrainParametersCreator {
   private static final float field_38024 = -0.51F;
   private static final float field_38025 = -0.4F;
   private static final float field_38026 = 0.1F;
   private static final float field_38027 = -0.15F;
   private static final ToFloatFunction IDENTITY;
   private static final ToFloatFunction OFFSET_AMPLIFIER;
   private static final ToFloatFunction FACTOR_AMPLIFIER;
   private static final ToFloatFunction JAGGEDNESS_AMPLIFIER;

   public static Spline createOffsetSpline(ToFloatFunction continents, ToFloatFunction erosion, ToFloatFunction ridgesFolded, boolean amplified) {
      ToFloatFunction lv = amplified ? OFFSET_AMPLIFIER : IDENTITY;
      Spline lv2 = createContinentalOffsetSpline(erosion, ridgesFolded, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, lv);
      Spline lv3 = createContinentalOffsetSpline(erosion, ridgesFolded, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false, lv);
      Spline lv4 = createContinentalOffsetSpline(erosion, ridgesFolded, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true, lv);
      Spline lv5 = createContinentalOffsetSpline(erosion, ridgesFolded, -0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true, lv);
      return Spline.builder(continents, lv).add(-1.1F, 0.044F).add(-1.02F, -0.2222F).add(-0.51F, -0.2222F).add(-0.44F, -0.12F).add(-0.18F, -0.12F).add(-0.16F, lv2).add(-0.15F, lv2).add(-0.1F, lv3).add(0.25F, lv4).add(1.0F, lv5).build();
   }

   public static Spline createFactorSpline(ToFloatFunction continents, ToFloatFunction erosion, ToFloatFunction ridges, ToFloatFunction ridgesFolded, boolean amplified) {
      ToFloatFunction lv = amplified ? FACTOR_AMPLIFIER : IDENTITY;
      return Spline.builder(continents, IDENTITY).add(-0.19F, 3.95F).add(-0.15F, method_42054(erosion, ridges, ridgesFolded, 6.25F, true, IDENTITY)).add(-0.1F, method_42054(erosion, ridges, ridgesFolded, 5.47F, true, lv)).add(0.03F, method_42054(erosion, ridges, ridgesFolded, 5.08F, true, lv)).add(0.06F, method_42054(erosion, ridges, ridgesFolded, 4.69F, false, lv)).build();
   }

   public static Spline createJaggednessSpline(ToFloatFunction continents, ToFloatFunction erosion, ToFloatFunction ridges, ToFloatFunction ridgesFolded, boolean amplified) {
      ToFloatFunction lv = amplified ? JAGGEDNESS_AMPLIFIER : IDENTITY;
      float f = 0.65F;
      return Spline.builder(continents, lv).add(-0.11F, 0.0F).add(0.03F, method_42053(erosion, ridges, ridgesFolded, 1.0F, 0.5F, 0.0F, 0.0F, lv)).add(0.65F, method_42053(erosion, ridges, ridgesFolded, 1.0F, 1.0F, 1.0F, 0.0F, lv)).build();
   }

   private static Spline method_42053(ToFloatFunction erosion, ToFloatFunction ridges, ToFloatFunction ridgesFolded, float f, float g, float h, float i, ToFloatFunction amplifier) {
      float j = -0.5775F;
      Spline lv = method_42052(ridges, ridgesFolded, f, h, amplifier);
      Spline lv2 = method_42052(ridges, ridgesFolded, g, i, amplifier);
      return Spline.builder(erosion, amplifier).add(-1.0F, lv).add(-0.78F, lv2).add(-0.5775F, lv2).add(-0.375F, 0.0F).build();
   }

   private static Spline method_42052(ToFloatFunction ridges, ToFloatFunction ridgesFolded, float f, float g, ToFloatFunction amplifier) {
      float h = DensityFunctions.getPeaksValleysNoise(0.4F);
      float i = DensityFunctions.getPeaksValleysNoise(0.56666666F);
      float j = (h + i) / 2.0F;
      Spline.Builder lv = Spline.builder(ridgesFolded, amplifier);
      lv.add(h, 0.0F);
      if (g > 0.0F) {
         lv.add(j, method_42049(ridges, g, amplifier));
      } else {
         lv.add(j, 0.0F);
      }

      if (f > 0.0F) {
         lv.add(1.0F, method_42049(ridges, f, amplifier));
      } else {
         lv.add(1.0F, 0.0F);
      }

      return lv.build();
   }

   private static Spline method_42049(ToFloatFunction ridges, float f, ToFloatFunction amplifier) {
      float g = 0.63F * f;
      float h = 0.3F * f;
      return Spline.builder(ridges, amplifier).add(-0.01F, g).add(0.01F, h).build();
   }

   private static Spline method_42054(ToFloatFunction erosion, ToFloatFunction ridges, ToFloatFunction ridgesFolded, float f, boolean bl, ToFloatFunction amplifier) {
      Spline lv = Spline.builder(ridges, amplifier).add(-0.2F, 6.3F).add(0.2F, f).build();
      Spline.Builder lv2 = Spline.builder(erosion, amplifier).add(-0.6F, lv).add(-0.5F, Spline.builder(ridges, amplifier).add(-0.05F, 6.3F).add(0.05F, 2.67F).build()).add(-0.35F, lv).add(-0.25F, lv).add(-0.1F, Spline.builder(ridges, amplifier).add(-0.05F, 2.67F).add(0.05F, 6.3F).build()).add(0.03F, lv);
      Spline lv3;
      Spline lv4;
      if (bl) {
         lv3 = Spline.builder(ridges, amplifier).add(0.0F, f).add(0.1F, 0.625F).build();
         lv4 = Spline.builder(ridgesFolded, amplifier).add(-0.9F, f).add(-0.69F, lv3).build();
         lv2.add(0.35F, f).add(0.45F, lv4).add(0.55F, lv4).add(0.62F, f);
      } else {
         lv3 = Spline.builder(ridgesFolded, amplifier).add(-0.7F, lv).add(-0.15F, 1.37F).build();
         lv4 = Spline.builder(ridgesFolded, amplifier).add(0.45F, lv).add(0.7F, 1.56F).build();
         lv2.add(0.05F, lv4).add(0.4F, lv4).add(0.45F, lv3).add(0.55F, lv3).add(0.58F, f);
      }

      return lv2.build();
   }

   private static float method_42047(float f, float g, float h, float i) {
      return (g - f) / (i - h);
   }

   private static Spline method_42050(ToFloatFunction ridgesFolded, float f, boolean bl, ToFloatFunction amplifier) {
      Spline.Builder lv = Spline.builder(ridgesFolded, amplifier);
      float g = -0.7F;
      float h = -1.0F;
      float i = getOffsetValue(-1.0F, f, -0.7F);
      float j = 1.0F;
      float k = getOffsetValue(1.0F, f, -0.7F);
      float l = method_42045(f);
      float m = -0.65F;
      float n;
      if (-0.65F < l && l < 1.0F) {
         n = getOffsetValue(-0.65F, f, -0.7F);
         float o = -0.75F;
         float p = getOffsetValue(-0.75F, f, -0.7F);
         float q = method_42047(i, p, -1.0F, -0.75F);
         lv.add(-1.0F, i, q);
         lv.add(-0.75F, p);
         lv.add(-0.65F, n);
         float r = getOffsetValue(l, f, -0.7F);
         float s = method_42047(r, k, l, 1.0F);
         float t = 0.01F;
         lv.add(l - 0.01F, r);
         lv.add(l, r, s);
         lv.add(1.0F, k, s);
      } else {
         n = method_42047(i, k, -1.0F, 1.0F);
         if (bl) {
            lv.add(-1.0F, Math.max(0.2F, i));
            lv.add(0.0F, MathHelper.lerp(0.5F, i, k), n);
         } else {
            lv.add(-1.0F, i, n);
         }

         lv.add(1.0F, k, n);
      }

      return lv.build();
   }

   private static float getOffsetValue(float f, float g, float h) {
      float i = 1.17F;
      float j = 0.46082947F;
      float k = 1.0F - (1.0F - g) * 0.5F;
      float l = 0.5F * (1.0F - g);
      float m = (f + 1.17F) * 0.46082947F;
      float n = m * k - l;
      return f < h ? Math.max(n, -0.2222F) : Math.max(n, 0.0F);
   }

   private static float method_42045(float f) {
      float g = 1.17F;
      float h = 0.46082947F;
      float i = 1.0F - (1.0F - f) * 0.5F;
      float j = 0.5F * (1.0F - f);
      return j / (0.46082947F * i) - 1.17F;
   }

   public static Spline createContinentalOffsetSpline(ToFloatFunction erosion, ToFloatFunction ridgesFolded, float continentalness, float g, float h, float i, float j, float k, boolean bl, boolean bl2, ToFloatFunction amplifier) {
      float l = 0.6F;
      float m = 0.5F;
      float n = 0.5F;
      Spline lv = method_42050(ridgesFolded, MathHelper.lerp(i, 0.6F, 1.5F), bl2, amplifier);
      Spline lv2 = method_42050(ridgesFolded, MathHelper.lerp(i, 0.6F, 1.0F), bl2, amplifier);
      Spline lv3 = method_42050(ridgesFolded, i, bl2, amplifier);
      Spline lv4 = method_42048(ridgesFolded, continentalness - 0.15F, 0.5F * i, MathHelper.lerp(0.5F, 0.5F, 0.5F) * i, 0.5F * i, 0.6F * i, 0.5F, amplifier);
      Spline lv5 = method_42048(ridgesFolded, continentalness, j * i, g * i, 0.5F * i, 0.6F * i, 0.5F, amplifier);
      Spline lv6 = method_42048(ridgesFolded, continentalness, j, j, g, h, 0.5F, amplifier);
      Spline lv7 = method_42048(ridgesFolded, continentalness, j, j, g, h, 0.5F, amplifier);
      Spline lv8 = Spline.builder(ridgesFolded, amplifier).add(-1.0F, continentalness).add(-0.4F, lv6).add(0.0F, h + 0.07F).build();
      Spline lv9 = method_42048(ridgesFolded, -0.02F, k, k, g, h, 0.0F, amplifier);
      Spline.Builder lv10 = Spline.builder(erosion, amplifier).add(-0.85F, lv).add(-0.7F, lv2).add(-0.4F, lv3).add(-0.35F, lv4).add(-0.1F, lv5).add(0.2F, lv6);
      if (bl) {
         lv10.add(0.4F, lv7).add(0.45F, lv8).add(0.55F, lv8).add(0.58F, lv7);
      }

      lv10.add(0.7F, lv9);
      return lv10.build();
   }

   private static Spline method_42048(ToFloatFunction ridgesFolded, float continentalness, float g, float h, float i, float j, float k, ToFloatFunction amplifier) {
      float l = Math.max(0.5F * (g - continentalness), k);
      float m = 5.0F * (h - g);
      return Spline.builder(ridgesFolded, amplifier).add(-1.0F, continentalness, l).add(-0.4F, g, Math.min(l, m)).add(0.0F, h, m).add(0.4F, i, 2.0F * (i - h)).add(1.0F, j, 0.7F * (j - i)).build();
   }

   static {
      IDENTITY = ToFloatFunction.IDENTITY;
      OFFSET_AMPLIFIER = ToFloatFunction.fromFloat((value) -> {
         return value < 0.0F ? value : value * 2.0F;
      });
      FACTOR_AMPLIFIER = ToFloatFunction.fromFloat((value) -> {
         return 1.25F - 6.25F / (value + 5.0F);
      });
      JAGGEDNESS_AMPLIFIER = ToFloatFunction.fromFloat((value) -> {
         return value * 2.0F;
      });
   }
}
