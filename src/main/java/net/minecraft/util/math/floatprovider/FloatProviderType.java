package net.minecraft.util.math.floatprovider;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface FloatProviderType {
   FloatProviderType CONSTANT = register("constant", ConstantFloatProvider.CODEC);
   FloatProviderType UNIFORM = register("uniform", UniformFloatProvider.CODEC);
   FloatProviderType CLAMPED_NORMAL = register("clamped_normal", ClampedNormalFloatProvider.CODEC);
   FloatProviderType TRAPEZOID = register("trapezoid", TrapezoidFloatProvider.CODEC);

   Codec codec();

   static FloatProviderType register(String id, Codec codec) {
      return (FloatProviderType)Registry.register(Registries.FLOAT_PROVIDER_TYPE, (String)id, () -> {
         return codec;
      });
   }
}
