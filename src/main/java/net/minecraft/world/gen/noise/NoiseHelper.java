/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.noise;

import java.util.Locale;

public class NoiseHelper {
    public static double method_35479(double d, double e) {
        return d + Math.sin(Math.PI * d) * e / Math.PI;
    }

    public static void appendDebugInfo(StringBuilder builder, double originX, double originY, double originZ, byte[] permutation) {
        builder.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", Float.valueOf((float)originX), Float.valueOf((float)originY), Float.valueOf((float)originZ), permutation[0], permutation[255]));
    }

    public static void appendDebugInfo(StringBuilder builder, double originX, double originY, double originZ, int[] permutation) {
        builder.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", Float.valueOf((float)originX), Float.valueOf((float)originY), Float.valueOf((float)originZ), permutation[0], permutation[255]));
    }
}

