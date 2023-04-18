package net.minecraft.world.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;

public class GenerationSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final GenerationSettings INSTANCE = new GenerationSettings(ImmutableMap.of(), ImmutableList.of());
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      Codec var10001 = GenerationStep.Carver.CODEC;
      Codec var10002 = ConfiguredCarver.LIST_CODEC;
      Logger var10004 = LOGGER;
      Objects.requireNonNull(var10004);
      RecordCodecBuilder var1 = Codec.simpleMap(var10001, var10002.promotePartial(Util.addPrefix("Carver: ", var10004::error)), StringIdentifiable.toKeyable(GenerationStep.Carver.values())).fieldOf("carvers").forGetter((generationSettings) -> {
         return generationSettings.carvers;
      });
      var10002 = PlacedFeature.LISTS_CODEC;
      var10004 = LOGGER;
      Objects.requireNonNull(var10004);
      return instance.group(var1, var10002.promotePartial(Util.addPrefix("Features: ", var10004::error)).fieldOf("features").forGetter((generationSettings) -> {
         return generationSettings.features;
      })).apply(instance, GenerationSettings::new);
   });
   private final Map carvers;
   private final List features;
   private final Supplier flowerFeatures;
   private final Supplier allowedFeatures;

   GenerationSettings(Map carvers, List features) {
      this.carvers = carvers;
      this.features = features;
      this.flowerFeatures = Suppliers.memoize(() -> {
         return (List)features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).flatMap(PlacedFeature::getDecoratedFeatures).filter((feature) -> {
            return feature.feature() == Feature.FLOWER;
         }).collect(ImmutableList.toImmutableList());
      });
      this.allowedFeatures = Suppliers.memoize(() -> {
         return (Set)features.stream().flatMap(RegistryEntryList::stream).map(RegistryEntry::value).collect(Collectors.toSet());
      });
   }

   public Iterable getCarversForStep(GenerationStep.Carver carverStep) {
      return (Iterable)Objects.requireNonNullElseGet((Iterable)this.carvers.get(carverStep), List::of);
   }

   public List getFlowerFeatures() {
      return (List)this.flowerFeatures.get();
   }

   public List getFeatures() {
      return this.features;
   }

   public boolean isFeatureAllowed(PlacedFeature feature) {
      return ((Set)this.allowedFeatures.get()).contains(feature);
   }

   public static class LookupBackedBuilder extends Builder {
      private final RegistryEntryLookup placedFeatureLookup;
      private final RegistryEntryLookup configuredCarverLookup;

      public LookupBackedBuilder(RegistryEntryLookup placedFeatureLookup, RegistryEntryLookup configuredCarverLookup) {
         this.placedFeatureLookup = placedFeatureLookup;
         this.configuredCarverLookup = configuredCarverLookup;
      }

      public LookupBackedBuilder feature(GenerationStep.Feature featureStep, RegistryKey featureKey) {
         this.addFeature(featureStep.ordinal(), this.placedFeatureLookup.getOrThrow(featureKey));
         return this;
      }

      public LookupBackedBuilder carver(GenerationStep.Carver carverStep, RegistryKey carverKey) {
         this.carver(carverStep, this.configuredCarverLookup.getOrThrow(carverKey));
         return this;
      }
   }

   public static class Builder {
      private final Map carverStepsToCarvers = Maps.newLinkedHashMap();
      private final List indexedFeaturesList = Lists.newArrayList();

      public Builder feature(GenerationStep.Feature featureStep, RegistryEntry featureEntry) {
         return this.addFeature(featureStep.ordinal(), featureEntry);
      }

      public Builder addFeature(int ordinal, RegistryEntry featureEntry) {
         this.fillFeaturesList(ordinal);
         ((List)this.indexedFeaturesList.get(ordinal)).add(featureEntry);
         return this;
      }

      public Builder carver(GenerationStep.Carver carverStep, RegistryEntry carverEntry) {
         ((List)this.carverStepsToCarvers.computeIfAbsent(carverStep, (step) -> {
            return Lists.newArrayList();
         })).add(carverEntry);
         return this;
      }

      private void fillFeaturesList(int size) {
         while(this.indexedFeaturesList.size() <= size) {
            this.indexedFeaturesList.add(Lists.newArrayList());
         }

      }

      public GenerationSettings build() {
         return new GenerationSettings((Map)this.carverStepsToCarvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> {
            return RegistryEntryList.of((List)entry.getValue());
         })), (List)this.indexedFeaturesList.stream().map(RegistryEntryList::of).collect(ImmutableList.toImmutableList()));
      }
   }
}
