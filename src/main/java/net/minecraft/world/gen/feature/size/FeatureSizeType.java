package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class FeatureSizeType {
   public static final FeatureSizeType TWO_LAYERS_FEATURE_SIZE;
   public static final FeatureSizeType THREE_LAYERS_FEATURE_SIZE;
   private final Codec codec;

   private static FeatureSizeType register(String id, Codec codec) {
      return (FeatureSizeType)Registry.register(Registries.FEATURE_SIZE_TYPE, (String)id, new FeatureSizeType(codec));
   }

   private FeatureSizeType(Codec codec) {
      this.codec = codec;
   }

   public Codec getCodec() {
      return this.codec;
   }

   static {
      TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize.CODEC);
      THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize.CODEC);
   }
}
