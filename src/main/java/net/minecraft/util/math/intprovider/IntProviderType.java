package net.minecraft.util.math.intprovider;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface IntProviderType {
   IntProviderType CONSTANT = register("constant", ConstantIntProvider.CODEC);
   IntProviderType UNIFORM = register("uniform", UniformIntProvider.CODEC);
   IntProviderType BIASED_TO_BOTTOM = register("biased_to_bottom", BiasedToBottomIntProvider.CODEC);
   IntProviderType CLAMPED = register("clamped", ClampedIntProvider.CODEC);
   IntProviderType WEIGHTED_LIST = register("weighted_list", WeightedListIntProvider.CODEC);
   IntProviderType CLAMPED_NORMAL = register("clamped_normal", ClampedNormalIntProvider.CODEC);

   Codec codec();

   static IntProviderType register(String id, Codec codec) {
      return (IntProviderType)Registry.register(Registries.INT_PROVIDER_TYPE, (String)id, () -> {
         return codec;
      });
   }
}
