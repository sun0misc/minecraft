package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.FeaturePlacementContext;

public class CarvingMaskPlacementModifier extends PlacementModifier {
   public static final Codec MODIFIER_CODEC;
   private final GenerationStep.Carver step;

   private CarvingMaskPlacementModifier(GenerationStep.Carver step) {
      this.step = step;
   }

   public static CarvingMaskPlacementModifier of(GenerationStep.Carver step) {
      return new CarvingMaskPlacementModifier(step);
   }

   public Stream getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
      ChunkPos lv = new ChunkPos(pos);
      return context.getOrCreateCarvingMask(lv, this.step).streamBlockPos(lv);
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.CARVING_MASK;
   }

   static {
      MODIFIER_CODEC = GenerationStep.Carver.CODEC.fieldOf("step").xmap(CarvingMaskPlacementModifier::new, (config) -> {
         return config.step;
      }).codec();
   }
}
