package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;

public class ChunkGenerators {
   public static Codec registerAndGetDefault(Registry registry) {
      Registry.register(registry, (String)"noise", NoiseChunkGenerator.CODEC);
      Registry.register(registry, (String)"flat", FlatChunkGenerator.CODEC);
      return (Codec)Registry.register(registry, (String)"debug", DebugChunkGenerator.CODEC);
   }
}
