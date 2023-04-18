package net.minecraft.world.biome.source;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;

public class BiomeSources {
   public static Codec registerAndGetDefault(Registry registry) {
      Registry.register(registry, (String)"fixed", FixedBiomeSource.CODEC);
      Registry.register(registry, (String)"multi_noise", MultiNoiseBiomeSource.CODEC);
      Registry.register(registry, (String)"checkerboard", CheckerboardBiomeSource.CODEC);
      return (Codec)Registry.register(registry, (String)"the_end", TheEndBiomeSource.CODEC);
   }
}
