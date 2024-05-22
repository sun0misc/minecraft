/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource.featuretoggle;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureUniverse;
import org.jetbrains.annotations.Nullable;

public final class FeatureSet {
    private static final FeatureSet EMPTY = new FeatureSet(null, 0L);
    public static final int MAX_FEATURE_FLAGS = 64;
    @Nullable
    private final FeatureUniverse universe;
    private final long featuresMask;

    private FeatureSet(@Nullable FeatureUniverse universe, long featuresMask) {
        this.universe = universe;
        this.featuresMask = featuresMask;
    }

    static FeatureSet of(FeatureUniverse universe, Collection<FeatureFlag> features) {
        if (features.isEmpty()) {
            return EMPTY;
        }
        long l = FeatureSet.combineMask(universe, 0L, features);
        return new FeatureSet(universe, l);
    }

    public static FeatureSet empty() {
        return EMPTY;
    }

    public static FeatureSet of(FeatureFlag feature) {
        return new FeatureSet(feature.universe, feature.mask);
    }

    public static FeatureSet of(FeatureFlag feature1, FeatureFlag ... features) {
        long l = features.length == 0 ? feature1.mask : FeatureSet.combineMask(feature1.universe, feature1.mask, Arrays.asList(features));
        return new FeatureSet(feature1.universe, l);
    }

    private static long combineMask(FeatureUniverse universe, long featuresMask, Iterable<FeatureFlag> newFeatures) {
        for (FeatureFlag lv : newFeatures) {
            if (universe != lv.universe) {
                throw new IllegalStateException("Mismatched feature universe, expected '" + String.valueOf(universe) + "', but got '" + String.valueOf(lv.universe) + "'");
            }
            featuresMask |= lv.mask;
        }
        return featuresMask;
    }

    public boolean contains(FeatureFlag feature) {
        if (this.universe != feature.universe) {
            return false;
        }
        return (this.featuresMask & feature.mask) != 0L;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public boolean isSubsetOf(FeatureSet features) {
        if (this.universe == null) {
            return true;
        }
        if (this.universe != features.universe) {
            return false;
        }
        return (this.featuresMask & (features.featuresMask ^ 0xFFFFFFFFFFFFFFFFL)) == 0L;
    }

    public boolean intersects(FeatureSet features) {
        if (this.universe == null || features.universe == null || this.universe != features.universe) {
            return false;
        }
        return (this.featuresMask & features.featuresMask) != 0L;
    }

    public FeatureSet combine(FeatureSet features) {
        if (this.universe == null) {
            return features;
        }
        if (features.universe == null) {
            return this;
        }
        if (this.universe != features.universe) {
            throw new IllegalArgumentException("Mismatched set elements: '" + String.valueOf(this.universe) + "' != '" + String.valueOf(features.universe) + "'");
        }
        return new FeatureSet(this.universe, this.featuresMask | features.featuresMask);
    }

    public FeatureSet subtract(FeatureSet features) {
        if (this.universe == null || features.universe == null) {
            return this;
        }
        if (this.universe != features.universe) {
            throw new IllegalArgumentException("Mismatched set elements: '" + String.valueOf(this.universe) + "' != '" + String.valueOf(features.universe) + "'");
        }
        long l = this.featuresMask & (features.featuresMask ^ 0xFFFFFFFFFFFFFFFFL);
        if (l == 0L) {
            return EMPTY;
        }
        return new FeatureSet(this.universe, l);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeatureSet)) return false;
        FeatureSet lv = (FeatureSet)o;
        if (this.universe != lv.universe) return false;
        if (this.featuresMask != lv.featuresMask) return false;
        return true;
    }

    public int hashCode() {
        return (int)HashCommon.mix(this.featuresMask);
    }
}

