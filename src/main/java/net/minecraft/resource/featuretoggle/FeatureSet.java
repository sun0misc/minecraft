package net.minecraft.resource.featuretoggle;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

public final class FeatureSet {
   private static final FeatureSet EMPTY = new FeatureSet((FeatureUniverse)null, 0L);
   public static final int MAX_FEATURE_FLAGS = 64;
   @Nullable
   private final FeatureUniverse universe;
   private final long featuresMask;

   private FeatureSet(@Nullable FeatureUniverse universe, long featuresMask) {
      this.universe = universe;
      this.featuresMask = featuresMask;
   }

   static FeatureSet of(FeatureUniverse universe, Collection features) {
      if (features.isEmpty()) {
         return EMPTY;
      } else {
         long l = combineMask(universe, 0L, features);
         return new FeatureSet(universe, l);
      }
   }

   public static FeatureSet empty() {
      return EMPTY;
   }

   public static FeatureSet of(FeatureFlag feature) {
      return new FeatureSet(feature.universe, feature.mask);
   }

   public static FeatureSet of(FeatureFlag feature1, FeatureFlag... features) {
      long l = features.length == 0 ? feature1.mask : combineMask(feature1.universe, feature1.mask, Arrays.asList(features));
      return new FeatureSet(feature1.universe, l);
   }

   private static long combineMask(FeatureUniverse universe, long featuresMask, Iterable newFeatures) {
      FeatureFlag lv;
      for(Iterator var4 = newFeatures.iterator(); var4.hasNext(); featuresMask |= lv.mask) {
         lv = (FeatureFlag)var4.next();
         if (universe != lv.universe) {
            throw new IllegalStateException("Mismatched feature universe, expected '" + universe + "', but got '" + lv.universe + "'");
         }
      }

      return featuresMask;
   }

   public boolean contains(FeatureFlag feature) {
      if (this.universe != feature.universe) {
         return false;
      } else {
         return (this.featuresMask & feature.mask) != 0L;
      }
   }

   public boolean isSubsetOf(FeatureSet features) {
      if (this.universe == null) {
         return true;
      } else if (this.universe != features.universe) {
         return false;
      } else {
         return (this.featuresMask & ~features.featuresMask) == 0L;
      }
   }

   public FeatureSet combine(FeatureSet features) {
      if (this.universe == null) {
         return features;
      } else if (features.universe == null) {
         return this;
      } else if (this.universe != features.universe) {
         throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + features.universe + "'");
      } else {
         return new FeatureSet(this.universe, this.featuresMask | features.featuresMask);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof FeatureSet) {
            FeatureSet lv = (FeatureSet)o;
            if (this.universe == lv.universe && this.featuresMask == lv.featuresMask) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      return (int)HashCommon.mix(this.featuresMask);
   }
}
