package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;

public class BiomePlacementModifier extends AbstractConditionalPlacementModifier {
   private static final BiomePlacementModifier INSTANCE = new BiomePlacementModifier();
   public static Codec MODIFIER_CODEC = Codec.unit(() -> {
      return INSTANCE;
   });

   private BiomePlacementModifier() {
   }

   public static BiomePlacementModifier of() {
      return INSTANCE;
   }

   protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
      PlacedFeature lv = (PlacedFeature)context.getPlacedFeature().orElseThrow(() -> {
         return new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome");
      });
      RegistryEntry lv2 = context.getWorld().getBiome(pos);
      return context.getChunkGenerator().getGenerationSettings(lv2).isFeatureAllowed(lv);
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.BIOME;
   }
}
