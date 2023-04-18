package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;

public class RarityFilterPlacementModifier extends AbstractConditionalPlacementModifier {
   public static final Codec MODIFIER_CODEC;
   private final int chance;

   private RarityFilterPlacementModifier(int chance) {
      this.chance = chance;
   }

   public static RarityFilterPlacementModifier of(int chance) {
      return new RarityFilterPlacementModifier(chance);
   }

   protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
      return random.nextFloat() < 1.0F / (float)this.chance;
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.RARITY_FILTER;
   }

   static {
      MODIFIER_CODEC = Codecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilterPlacementModifier::new, (arg) -> {
         return arg.chance;
      }).codec();
   }
}
