package net.minecraft.util.math;

import java.util.Locale;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.math.NumberUtils;

public class MathHelper {
   private static final long field_29852 = 61440L;
   private static final long HALF_PI_RADIANS_SINE_TABLE_INDEX = 16384L;
   private static final long field_29854 = -4611686018427387904L;
   private static final long field_29855 = Long.MIN_VALUE;
   public static final float PI = 3.1415927F;
   public static final float HALF_PI = 1.5707964F;
   public static final float TAU = 6.2831855F;
   public static final float RADIANS_PER_DEGREE = 0.017453292F;
   public static final float DEGREES_PER_RADIAN = 57.295776F;
   public static final float EPSILON = 1.0E-5F;
   public static final float SQUARE_ROOT_OF_TWO = sqrt(2.0F);
   private static final float DEGREES_TO_SINE_TABLE_INDEX = 10430.378F;
   private static final float[] SINE_TABLE = (float[])Util.make(new float[65536], (sineTable) -> {
      for(int i = 0; i < sineTable.length; ++i) {
         sineTable[i] = (float)Math.sin((double)i * Math.PI * 2.0 / 65536.0);
      }

   });
   private static final Random RANDOM = Random.createThreadSafe();
   private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
   private static final double field_29857 = 0.16666666666666666;
   private static final int field_29858 = 8;
   private static final int field_29859 = 257;
   private static final double SMALLEST_FRACTION_FREE_DOUBLE = Double.longBitsToDouble(4805340802404319232L);
   private static final double[] ARCSINE_TABLE = new double[257];
   private static final double[] COSINE_TABLE = new double[257];

   public static float sin(float value) {
      return SINE_TABLE[(int)(value * 10430.378F) & '\uffff'];
   }

   public static float cos(float value) {
      return SINE_TABLE[(int)(value * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static float sqrt(float value) {
      return (float)Math.sqrt((double)value);
   }

   public static int floor(float value) {
      int i = (int)value;
      return value < (float)i ? i - 1 : i;
   }

   public static int floor(double value) {
      int i = (int)value;
      return value < (double)i ? i - 1 : i;
   }

   public static long lfloor(double value) {
      long l = (long)value;
      return value < (double)l ? l - 1L : l;
   }

   public static float abs(float value) {
      return Math.abs(value);
   }

   public static int abs(int value) {
      return Math.abs(value);
   }

   public static int ceil(float value) {
      int i = (int)value;
      return value > (float)i ? i + 1 : i;
   }

   public static int ceil(double value) {
      int i = (int)value;
      return value > (double)i ? i + 1 : i;
   }

   public static int clamp(int value, int min, int max) {
      return Math.min(Math.max(value, min), max);
   }

   public static float clamp(float value, float min, float max) {
      return value < min ? min : Math.min(value, max);
   }

   public static double clamp(double value, double min, double max) {
      return value < min ? min : Math.min(value, max);
   }

   public static double clampedLerp(double start, double end, double delta) {
      if (delta < 0.0) {
         return start;
      } else {
         return delta > 1.0 ? end : lerp(delta, start, end);
      }
   }

   public static float clampedLerp(float start, float end, float delta) {
      if (delta < 0.0F) {
         return start;
      } else {
         return delta > 1.0F ? end : lerp(delta, start, end);
      }
   }

   public static double absMax(double a, double b) {
      if (a < 0.0) {
         a = -a;
      }

      if (b < 0.0) {
         b = -b;
      }

      return Math.max(a, b);
   }

   public static int floorDiv(int dividend, int divisor) {
      return Math.floorDiv(dividend, divisor);
   }

   public static int nextInt(Random random, int min, int max) {
      return min >= max ? min : random.nextInt(max - min + 1) + min;
   }

   public static float nextFloat(Random random, float min, float max) {
      return min >= max ? min : random.nextFloat() * (max - min) + min;
   }

   public static double nextDouble(Random random, double min, double max) {
      return min >= max ? min : random.nextDouble() * (max - min) + min;
   }

   public static boolean approximatelyEquals(float a, float b) {
      return Math.abs(b - a) < 1.0E-5F;
   }

   public static boolean approximatelyEquals(double a, double b) {
      return Math.abs(b - a) < 9.999999747378752E-6;
   }

   public static int floorMod(int dividend, int divisor) {
      return Math.floorMod(dividend, divisor);
   }

   public static float floorMod(float dividend, float divisor) {
      return (dividend % divisor + divisor) % divisor;
   }

   public static double floorMod(double dividend, double divisor) {
      return (dividend % divisor + divisor) % divisor;
   }

   public static boolean isMultipleOf(int a, int b) {
      return a % b == 0;
   }

   public static int wrapDegrees(int degrees) {
      int j = degrees % 360;
      if (j >= 180) {
         j -= 360;
      }

      if (j < -180) {
         j += 360;
      }

      return j;
   }

   public static float wrapDegrees(float degrees) {
      float g = degrees % 360.0F;
      if (g >= 180.0F) {
         g -= 360.0F;
      }

      if (g < -180.0F) {
         g += 360.0F;
      }

      return g;
   }

   public static double wrapDegrees(double degrees) {
      double e = degrees % 360.0;
      if (e >= 180.0) {
         e -= 360.0;
      }

      if (e < -180.0) {
         e += 360.0;
      }

      return e;
   }

   public static float subtractAngles(float start, float end) {
      return wrapDegrees(end - start);
   }

   public static float angleBetween(float first, float second) {
      return abs(subtractAngles(first, second));
   }

   public static float clampAngle(float value, float mean, float delta) {
      float i = subtractAngles(value, mean);
      float j = clamp(i, -delta, delta);
      return mean - j;
   }

   public static float stepTowards(float from, float to, float step) {
      step = abs(step);
      return from < to ? clamp(from + step, from, to) : clamp(from - step, to, from);
   }

   public static float stepUnwrappedAngleTowards(float from, float to, float step) {
      float i = subtractAngles(from, to);
      return stepTowards(from, from + i, step);
   }

   public static int parseInt(String string, int fallback) {
      return NumberUtils.toInt(string, fallback);
   }

   public static int smallestEncompassingPowerOfTwo(int value) {
      int j = value - 1;
      j |= j >> 1;
      j |= j >> 2;
      j |= j >> 4;
      j |= j >> 8;
      j |= j >> 16;
      return j + 1;
   }

   public static boolean isPowerOfTwo(int value) {
      return value != 0 && (value & value - 1) == 0;
   }

   public static int ceilLog2(int value) {
      value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
      return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)value * 125613361L >> 27) & 31];
   }

   public static int floorLog2(int value) {
      return ceilLog2(value) - (isPowerOfTwo(value) ? 0 : 1);
   }

   public static int packRgb(float r, float g, float b) {
      return ColorHelper.Argb.getArgb(0, floor(r * 255.0F), floor(g * 255.0F), floor(b * 255.0F));
   }

   public static float fractionalPart(float value) {
      return value - (float)floor(value);
   }

   public static double fractionalPart(double value) {
      return value - (double)lfloor(value);
   }

   /** @deprecated */
   @Deprecated
   public static long hashCode(Vec3i vec) {
      return hashCode(vec.getX(), vec.getY(), vec.getZ());
   }

   /** @deprecated */
   @Deprecated
   public static long hashCode(int x, int y, int z) {
      long l = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
      l = l * l * 42317861L + l * 11L;
      return l >> 16;
   }

   public static UUID randomUuid(Random random) {
      long l = random.nextLong() & -61441L | 16384L;
      long m = random.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
      return new UUID(l, m);
   }

   public static UUID randomUuid() {
      return randomUuid(RANDOM);
   }

   public static double getLerpProgress(double value, double start, double end) {
      return (value - start) / (end - start);
   }

   public static float getLerpProgress(float value, float start, float end) {
      return (value - start) / (end - start);
   }

   public static boolean method_34945(Vec3d arg, Vec3d arg2, Box arg3) {
      double d = (arg3.minX + arg3.maxX) * 0.5;
      double e = (arg3.maxX - arg3.minX) * 0.5;
      double f = arg.x - d;
      if (Math.abs(f) > e && f * arg2.x >= 0.0) {
         return false;
      } else {
         double g = (arg3.minY + arg3.maxY) * 0.5;
         double h = (arg3.maxY - arg3.minY) * 0.5;
         double i = arg.y - g;
         if (Math.abs(i) > h && i * arg2.y >= 0.0) {
            return false;
         } else {
            double j = (arg3.minZ + arg3.maxZ) * 0.5;
            double k = (arg3.maxZ - arg3.minZ) * 0.5;
            double l = arg.z - j;
            if (Math.abs(l) > k && l * arg2.z >= 0.0) {
               return false;
            } else {
               double m = Math.abs(arg2.x);
               double n = Math.abs(arg2.y);
               double o = Math.abs(arg2.z);
               double p = arg2.y * l - arg2.z * i;
               if (Math.abs(p) > h * o + k * n) {
                  return false;
               } else {
                  p = arg2.z * f - arg2.x * l;
                  if (Math.abs(p) > e * o + k * m) {
                     return false;
                  } else {
                     p = arg2.x * i - arg2.y * f;
                     return Math.abs(p) < e * n + h * m;
                  }
               }
            }
         }
      }
   }

   public static double atan2(double y, double x) {
      double f = x * x + y * y;
      if (Double.isNaN(f)) {
         return Double.NaN;
      } else {
         boolean bl = y < 0.0;
         if (bl) {
            y = -y;
         }

         boolean bl2 = x < 0.0;
         if (bl2) {
            x = -x;
         }

         boolean bl3 = y > x;
         double g;
         if (bl3) {
            g = x;
            x = y;
            y = g;
         }

         g = fastInverseSqrt(f);
         x *= g;
         y *= g;
         double h = SMALLEST_FRACTION_FREE_DOUBLE + y;
         int i = (int)Double.doubleToRawLongBits(h);
         double j = ARCSINE_TABLE[i];
         double k = COSINE_TABLE[i];
         double l = h - SMALLEST_FRACTION_FREE_DOUBLE;
         double m = y * k - x * l;
         double n = (6.0 + m * m) * m * 0.16666666666666666;
         double o = j + n;
         if (bl3) {
            o = 1.5707963267948966 - o;
         }

         if (bl2) {
            o = Math.PI - o;
         }

         if (bl) {
            o = -o;
         }

         return o;
      }
   }

   public static float inverseSqrt(float x) {
      return org.joml.Math.invsqrt(x);
   }

   public static double inverseSqrt(double x) {
      return org.joml.Math.invsqrt(x);
   }

   /** @deprecated */
   @Deprecated
   public static double fastInverseSqrt(double x) {
      double e = 0.5 * x;
      long l = Double.doubleToRawLongBits(x);
      l = 6910469410427058090L - (l >> 1);
      x = Double.longBitsToDouble(l);
      x *= 1.5 - e * x * x;
      return x;
   }

   public static float fastInverseCbrt(float x) {
      int i = Float.floatToIntBits(x);
      i = 1419967116 - i / 3;
      float g = Float.intBitsToFloat(i);
      g = 0.6666667F * g + 1.0F / (3.0F * g * g * x);
      g = 0.6666667F * g + 1.0F / (3.0F * g * g * x);
      return g;
   }

   public static int hsvToRgb(float hue, float saturation, float value) {
      int i = (int)(hue * 6.0F) % 6;
      float j = hue * 6.0F - (float)i;
      float k = value * (1.0F - saturation);
      float l = value * (1.0F - j * saturation);
      float m = value * (1.0F - (1.0F - j) * saturation);
      float n;
      float o;
      float p;
      switch (i) {
         case 0:
            n = value;
            o = m;
            p = k;
            break;
         case 1:
            n = l;
            o = value;
            p = k;
            break;
         case 2:
            n = k;
            o = value;
            p = m;
            break;
         case 3:
            n = k;
            o = l;
            p = value;
            break;
         case 4:
            n = m;
            o = k;
            p = value;
            break;
         case 5:
            n = value;
            o = k;
            p = l;
            break;
         default:
            throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
      }

      return ColorHelper.Argb.getArgb(0, clamp((int)(n * 255.0F), 0, 255), clamp((int)(o * 255.0F), 0, 255), clamp((int)(p * 255.0F), 0, 255));
   }

   public static int idealHash(int value) {
      value ^= value >>> 16;
      value *= -2048144789;
      value ^= value >>> 13;
      value *= -1028477387;
      value ^= value >>> 16;
      return value;
   }

   public static int binarySearch(int min, int max, IntPredicate predicate) {
      int k = max - min;

      while(k > 0) {
         int l = k / 2;
         int m = min + l;
         if (predicate.test(m)) {
            k = l;
         } else {
            min = m + 1;
            k -= l + 1;
         }
      }

      return min;
   }

   public static int lerp(float delta, int start, int end) {
      return start + floor(delta * (float)(end - start));
   }

   public static float lerp(float delta, float start, float end) {
      return start + delta * (end - start);
   }

   public static double lerp(double delta, double start, double end) {
      return start + delta * (end - start);
   }

   public static double lerp2(double deltaX, double deltaY, double x0y0, double x1y0, double x0y1, double x1y1) {
      return lerp(deltaY, lerp(deltaX, x0y0, x1y0), lerp(deltaX, x0y1, x1y1));
   }

   public static double lerp3(double deltaX, double deltaY, double deltaZ, double x0y0z0, double x1y0z0, double x0y1z0, double x1y1z0, double x0y0z1, double x1y0z1, double x0y1z1, double x1y1z1) {
      return lerp(deltaZ, lerp2(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0), lerp2(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
   }

   public static float catmullRom(float delta, float p0, float p1, float p2, float p3) {
      return 0.5F * (2.0F * p1 + (p2 - p0) * delta + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * delta * delta + (3.0F * p1 - p0 - 3.0F * p2 + p3) * delta * delta * delta);
   }

   public static double perlinFade(double value) {
      return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
   }

   public static double perlinFadeDerivative(double value) {
      return 30.0 * value * value * (value - 1.0) * (value - 1.0);
   }

   public static int sign(double value) {
      if (value == 0.0) {
         return 0;
      } else {
         return value > 0.0 ? 1 : -1;
      }
   }

   public static float lerpAngleDegrees(float delta, float start, float end) {
      return start + delta * wrapDegrees(end - start);
   }

   public static float wrap(float value, float maxDeviation) {
      return (Math.abs(value % maxDeviation - maxDeviation * 0.5F) - maxDeviation * 0.25F) / (maxDeviation * 0.25F);
   }

   public static float square(float n) {
      return n * n;
   }

   public static double square(double n) {
      return n * n;
   }

   public static int square(int n) {
      return n * n;
   }

   public static long square(long n) {
      return n * n;
   }

   public static double clampedMap(double value, double oldStart, double oldEnd, double newStart, double newEnd) {
      return clampedLerp(newStart, newEnd, getLerpProgress(value, oldStart, oldEnd));
   }

   public static float clampedMap(float value, float oldStart, float oldEnd, float newStart, float newEnd) {
      return clampedLerp(newStart, newEnd, getLerpProgress(value, oldStart, oldEnd));
   }

   public static double map(double value, double oldStart, double oldEnd, double newStart, double newEnd) {
      return lerp(getLerpProgress(value, oldStart, oldEnd), newStart, newEnd);
   }

   public static float map(float value, float oldStart, float oldEnd, float newStart, float newEnd) {
      return lerp(getLerpProgress(value, oldStart, oldEnd), newStart, newEnd);
   }

   public static double method_34957(double d) {
      return d + (2.0 * Random.create((long)floor(d * 3000.0)).nextDouble() - 1.0) * 1.0E-7 / 2.0;
   }

   public static int roundUpToMultiple(int value, int divisor) {
      return ceilDiv(value, divisor) * divisor;
   }

   public static int ceilDiv(int a, int b) {
      return -Math.floorDiv(-a, b);
   }

   public static int nextBetween(Random random, int min, int max) {
      return random.nextInt(max - min + 1) + min;
   }

   public static float nextBetween(Random random, float min, float max) {
      return random.nextFloat() * (max - min) + min;
   }

   public static float nextGaussian(Random random, float mean, float deviation) {
      return mean + (float)random.nextGaussian() * deviation;
   }

   public static double squaredHypot(double a, double b) {
      return a * a + b * b;
   }

   public static double hypot(double a, double b) {
      return Math.sqrt(squaredHypot(a, b));
   }

   public static double squaredMagnitude(double a, double b, double c) {
      return a * a + b * b + c * c;
   }

   public static double magnitude(double a, double b, double c) {
      return Math.sqrt(squaredMagnitude(a, b, c));
   }

   public static int roundDownToMultiple(double a, int b) {
      return floor(a / (double)b) * b;
   }

   public static IntStream stream(int seed, int lowerBound, int upperBound) {
      return stream(seed, lowerBound, upperBound, 1);
   }

   public static IntStream stream(int seed, int lowerBound, int upperBound, int steps) {
      if (lowerBound > upperBound) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "upperbound %d expected to be > lowerBound %d", upperBound, lowerBound));
      } else if (steps < 1) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "steps expected to be >= 1, was %d", steps));
      } else {
         return seed >= lowerBound && seed <= upperBound ? IntStream.iterate(seed, (i) -> {
            int m = Math.abs(seed - i);
            return seed - m >= lowerBound || seed + m <= upperBound;
         }, (i) -> {
            boolean bl = i <= seed;
            int n = Math.abs(seed - i);
            boolean bl2 = seed + n + steps <= upperBound;
            if (!bl || !bl2) {
               int o = seed - n - (bl ? steps : 0);
               if (o >= lowerBound) {
                  return o;
               }
            }

            return seed + n + steps;
         }) : IntStream.empty();
      }
   }

   static {
      for(int i = 0; i < 257; ++i) {
         double d = (double)i / 256.0;
         double e = Math.asin(d);
         COSINE_TABLE[i] = Math.cos(e);
         ARCSINE_TABLE[i] = e;
      }

   }
}
