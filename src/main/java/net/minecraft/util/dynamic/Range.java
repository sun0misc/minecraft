package net.minecraft.util.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record Range(Comparable minInclusive, Comparable maxInclusive) {
   public static final Codec CODEC;

   public Range(Comparable minInclusive, Comparable maxInclusive) {
      if (minInclusive.compareTo(maxInclusive) > 0) {
         throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
      } else {
         this.minInclusive = minInclusive;
         this.maxInclusive = maxInclusive;
      }
   }

   public static Codec createCodec(Codec elementCodec) {
      return Codecs.createCodecForPairObject(elementCodec, "min_inclusive", "max_inclusive", Range::validate, Range::minInclusive, Range::maxInclusive);
   }

   public static Codec createRangedCodec(Codec codec, Comparable minInclusive, Comparable maxInclusive) {
      return Codecs.validate(createCodec(codec), (range) -> {
         if (range.minInclusive().compareTo(minInclusive) < 0) {
            return DataResult.error(() -> {
               return "Range limit too low, expected at least " + minInclusive + " [" + range.minInclusive() + "-" + range.maxInclusive() + "]";
            });
         } else {
            return range.maxInclusive().compareTo(maxInclusive) > 0 ? DataResult.error(() -> {
               return "Range limit too high, expected at most " + maxInclusive + " [" + range.minInclusive() + "-" + range.maxInclusive() + "]";
            }) : DataResult.success(range);
         }
      });
   }

   public static DataResult validate(Comparable minInclusive, Comparable maxInclusive) {
      return minInclusive.compareTo(maxInclusive) <= 0 ? DataResult.success(new Range(minInclusive, maxInclusive)) : DataResult.error(() -> {
         return "min_inclusive must be less than or equal to max_inclusive";
      });
   }

   public boolean contains(Comparable value) {
      return value.compareTo(this.minInclusive) >= 0 && value.compareTo(this.maxInclusive) <= 0;
   }

   public boolean contains(Range other) {
      return other.minInclusive().compareTo(this.minInclusive) >= 0 && other.maxInclusive.compareTo(this.maxInclusive) <= 0;
   }

   public String toString() {
      return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
   }

   public Comparable minInclusive() {
      return this.minInclusive;
   }

   public Comparable maxInclusive() {
      return this.maxInclusive;
   }

   static {
      CODEC = createCodec(Codec.INT);
   }
}
