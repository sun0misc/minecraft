package net.minecraft.resource.featuretoggle;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class FeatureManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final FeatureUniverse universe;
   private final Map featureFlags;
   private final FeatureSet featureSet;

   FeatureManager(FeatureUniverse universe, FeatureSet featureSet, Map featureFlags) {
      this.universe = universe;
      this.featureFlags = featureFlags;
      this.featureSet = featureSet;
   }

   public boolean contains(FeatureSet features) {
      return features.isSubsetOf(this.featureSet);
   }

   public FeatureSet getFeatureSet() {
      return this.featureSet;
   }

   public FeatureSet featureSetOf(Iterable features) {
      return this.featureSetOf(features, (feature) -> {
         LOGGER.warn("Unknown feature flag: {}", feature);
      });
   }

   public FeatureSet featureSetOf(FeatureFlag... features) {
      return FeatureSet.of((FeatureUniverse)this.universe, (Collection)Arrays.asList(features));
   }

   public FeatureSet featureSetOf(Iterable features, Consumer unknownFlagConsumer) {
      Set set = Sets.newIdentityHashSet();
      Iterator var4 = features.iterator();

      while(var4.hasNext()) {
         Identifier lv = (Identifier)var4.next();
         FeatureFlag lv2 = (FeatureFlag)this.featureFlags.get(lv);
         if (lv2 == null) {
            unknownFlagConsumer.accept(lv);
         } else {
            set.add(lv2);
         }
      }

      return FeatureSet.of((FeatureUniverse)this.universe, (Collection)set);
   }

   public Set toId(FeatureSet features) {
      Set set = new HashSet();
      this.featureFlags.forEach((identifier, featureFlag) -> {
         if (features.contains(featureFlag)) {
            set.add(identifier);
         }

      });
      return set;
   }

   public Codec getCodec() {
      return Identifier.CODEC.listOf().comapFlatMap((featureIds) -> {
         Set set = new HashSet();
         Objects.requireNonNull(set);
         FeatureSet lv = this.featureSetOf(featureIds, set::add);
         return !set.isEmpty() ? DataResult.error(() -> {
            return "Unknown feature ids: " + set;
         }, lv) : DataResult.success(lv);
      }, (features) -> {
         return List.copyOf(this.toId(features));
      });
   }

   public static class Builder {
      private final FeatureUniverse universe;
      private int id;
      private final Map featureFlags = new LinkedHashMap();

      public Builder(String universe) {
         this.universe = new FeatureUniverse(universe);
      }

      public FeatureFlag addVanillaFlag(String feature) {
         return this.addFlag(new Identifier("minecraft", feature));
      }

      public FeatureFlag addFlag(Identifier feature) {
         if (this.id >= 64) {
            throw new IllegalStateException("Too many feature flags");
         } else {
            FeatureFlag lv = new FeatureFlag(this.universe, this.id++);
            FeatureFlag lv2 = (FeatureFlag)this.featureFlags.put(feature, lv);
            if (lv2 != null) {
               throw new IllegalStateException("Duplicate feature flag " + feature);
            } else {
               return lv;
            }
         }
      }

      public FeatureManager build() {
         FeatureSet lv = FeatureSet.of(this.universe, this.featureFlags.values());
         return new FeatureManager(this.universe, lv, Map.copyOf(this.featureFlags));
      }
   }
}
