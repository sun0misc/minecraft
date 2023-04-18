package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class HugeMushroomFeatureConfig implements FeatureConfig {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("cap_provider").forGetter((arg) -> {
         return arg.capProvider;
      }), BlockStateProvider.TYPE_CODEC.fieldOf("stem_provider").forGetter((arg) -> {
         return arg.stemProvider;
      }), Codec.INT.fieldOf("foliage_radius").orElse(2).forGetter((arg) -> {
         return arg.foliageRadius;
      })).apply(instance, HugeMushroomFeatureConfig::new);
   });
   public final BlockStateProvider capProvider;
   public final BlockStateProvider stemProvider;
   public final int foliageRadius;

   public HugeMushroomFeatureConfig(BlockStateProvider capProvider, BlockStateProvider stemProvider, int foliageRadius) {
      this.capProvider = capProvider;
      this.stemProvider = stemProvider;
      this.foliageRadius = foliageRadius;
   }
}
