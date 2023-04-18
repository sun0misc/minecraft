package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.FeaturePlacementContext;

public class SurfaceThresholdFilterPlacementModifier extends AbstractConditionalPlacementModifier {
   public static final Codec MODIFIER_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Heightmap.Type.CODEC.fieldOf("heightmap").forGetter((arg) -> {
         return arg.heightmap;
      }), Codec.INT.optionalFieldOf("min_inclusive", Integer.MIN_VALUE).forGetter((arg) -> {
         return arg.min;
      }), Codec.INT.optionalFieldOf("max_inclusive", Integer.MAX_VALUE).forGetter((arg) -> {
         return arg.max;
      })).apply(instance, SurfaceThresholdFilterPlacementModifier::new);
   });
   private final Heightmap.Type heightmap;
   private final int min;
   private final int max;

   private SurfaceThresholdFilterPlacementModifier(Heightmap.Type heightmap, int min, int max) {
      this.heightmap = heightmap;
      this.min = min;
      this.max = max;
   }

   public static SurfaceThresholdFilterPlacementModifier of(Heightmap.Type heightmap, int min, int max) {
      return new SurfaceThresholdFilterPlacementModifier(heightmap, min, max);
   }

   protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
      long l = (long)context.getTopY(this.heightmap, pos.getX(), pos.getZ());
      long m = l + (long)this.min;
      long n = l + (long)this.max;
      return m <= (long)pos.getY() && (long)pos.getY() <= n;
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
   }
}
