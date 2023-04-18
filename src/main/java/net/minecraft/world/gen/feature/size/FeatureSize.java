package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.registry.Registries;

public abstract class FeatureSize {
   public static final Codec TYPE_CODEC;
   protected static final int field_31522 = 16;
   protected final OptionalInt minClippedHeight;

   protected static RecordCodecBuilder createCodec() {
      return Codec.intRange(0, 80).optionalFieldOf("min_clipped_height").xmap((optional) -> {
         return (OptionalInt)optional.map(OptionalInt::of).orElse(OptionalInt.empty());
      }, (optionalInt) -> {
         return optionalInt.isPresent() ? Optional.of(optionalInt.getAsInt()) : Optional.empty();
      }).forGetter((arg) -> {
         return arg.minClippedHeight;
      });
   }

   public FeatureSize(OptionalInt minClippedHeight) {
      this.minClippedHeight = minClippedHeight;
   }

   protected abstract FeatureSizeType getType();

   public abstract int getRadius(int height, int y);

   public OptionalInt getMinClippedHeight() {
      return this.minClippedHeight;
   }

   static {
      TYPE_CODEC = Registries.FEATURE_SIZE_TYPE.getCodec().dispatch(FeatureSize::getType, FeatureSizeType::getCodec);
   }
}
