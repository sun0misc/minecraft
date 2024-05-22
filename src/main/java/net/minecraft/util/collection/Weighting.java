/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.collection;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.random.Random;

public class Weighting {
    private Weighting() {
    }

    public static int getWeightSum(List<? extends Weighted> pool) {
        long l = 0L;
        for (Weighted weighted : pool) {
            l += (long)weighted.getWeight().getValue();
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T extends Weighted> Optional<T> getRandom(Random random, List<T> pool, int totalWeight) {
        if (totalWeight < 0) {
            throw Util.throwOrPause(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (totalWeight == 0) {
            return Optional.empty();
        }
        int j = random.nextInt(totalWeight);
        return Weighting.getAt(pool, j);
    }

    public static <T extends Weighted> Optional<T> getAt(List<T> pool, int totalWeight) {
        for (Weighted lv : pool) {
            if ((totalWeight -= lv.getWeight().getValue()) >= 0) continue;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public static <T extends Weighted> Optional<T> getRandom(Random random, List<T> pool) {
        return Weighting.getRandom(random, pool, Weighting.getWeightSum(pool));
    }
}

