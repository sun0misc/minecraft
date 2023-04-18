package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.intRange(0, 81).fieldOf("limit").orElse(1).forGetter((arg) -> {
         return arg.limit;
      }), Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter((arg) -> {
         return arg.lowerSize;
      }), Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter((arg) -> {
         return arg.upperSize;
      }), createCodec()).apply(instance, TwoLayersFeatureSize::new);
   });
   private final int limit;
   private final int lowerSize;
   private final int upperSize;

   public TwoLayersFeatureSize(int limit, int lowerSize, int upperSize) {
      this(limit, lowerSize, upperSize, OptionalInt.empty());
   }

   public TwoLayersFeatureSize(int limit, int lowerSize, int upperSize, OptionalInt minClippedHeight) {
      super(minClippedHeight);
      this.limit = limit;
      this.lowerSize = lowerSize;
      this.upperSize = upperSize;
   }

   protected FeatureSizeType getType() {
      return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
   }

   public int getRadius(int height, int y) {
      return y < this.limit ? this.lowerSize : this.upperSize;
   }
}
