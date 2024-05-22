/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.floatprovider;

import java.util.Arrays;
import net.minecraft.util.math.floatprovider.FloatSupplier;
import net.minecraft.util.math.random.Random;

public class MultipliedFloatSupplier
implements FloatSupplier {
    private final FloatSupplier[] multipliers;

    public MultipliedFloatSupplier(FloatSupplier ... multipliers) {
        this.multipliers = multipliers;
    }

    @Override
    public float get(Random random) {
        float f = 1.0f;
        for (FloatSupplier lv : this.multipliers) {
            f *= lv.get(random);
        }
        return f;
    }

    public String toString() {
        return "MultipliedFloats" + Arrays.toString(this.multipliers);
    }
}

