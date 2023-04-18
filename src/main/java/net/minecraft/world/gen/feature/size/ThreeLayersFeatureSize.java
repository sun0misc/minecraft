package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.intRange(0, 80).fieldOf("limit").orElse(1).forGetter((arg) -> {
         return arg.limit;
      }), Codec.intRange(0, 80).fieldOf("upper_limit").orElse(1).forGetter((arg) -> {
         return arg.upperLimit;
      }), Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter((arg) -> {
         return arg.lowerSize;
      }), Codec.intRange(0, 16).fieldOf("middle_size").orElse(1).forGetter((arg) -> {
         return arg.middleSize;
      }), Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter((arg) -> {
         return arg.upperSize;
      }), createCodec()).apply(instance, ThreeLayersFeatureSize::new);
   });
   private final int limit;
   private final int upperLimit;
   private final int lowerSize;
   private final int middleSize;
   private final int upperSize;

   public ThreeLayersFeatureSize(int limit, int upperLimit, int lowerSize, int middleSize, int upperSize, OptionalInt minClippedHeight) {
      super(minClippedHeight);
      this.limit = limit;
      this.upperLimit = upperLimit;
      this.lowerSize = lowerSize;
      this.middleSize = middleSize;
      this.upperSize = upperSize;
   }

   protected FeatureSizeType getType() {
      return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
   }

   public int getRadius(int height, int y) {
      if (y < this.limit) {
         return this.lowerSize;
      } else {
         return y >= height - this.upperLimit ? this.upperSize : this.middleSize;
      }
   }
}
