package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeaturePlacementContext;

public class EnvironmentScanPlacementModifier extends PlacementModifier {
   private final Direction direction;
   private final BlockPredicate targetPredicate;
   private final BlockPredicate allowedSearchPredicate;
   private final int maxSteps;
   public static final Codec MODIFIER_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter((arg) -> {
         return arg.direction;
      }), BlockPredicate.BASE_CODEC.fieldOf("target_condition").forGetter((arg) -> {
         return arg.targetPredicate;
      }), BlockPredicate.BASE_CODEC.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue()).forGetter((arg) -> {
         return arg.allowedSearchPredicate;
      }), Codec.intRange(1, 32).fieldOf("max_steps").forGetter((arg) -> {
         return arg.maxSteps;
      })).apply(instance, EnvironmentScanPlacementModifier::new);
   });

   private EnvironmentScanPlacementModifier(Direction direction, BlockPredicate targetPredicate, BlockPredicate allowedSearchPredicate, int maxSteps) {
      this.direction = direction;
      this.targetPredicate = targetPredicate;
      this.allowedSearchPredicate = allowedSearchPredicate;
      this.maxSteps = maxSteps;
   }

   public static EnvironmentScanPlacementModifier of(Direction direction, BlockPredicate targetPredicate, BlockPredicate allowedSearchPredicate, int maxSteps) {
      return new EnvironmentScanPlacementModifier(direction, targetPredicate, allowedSearchPredicate, maxSteps);
   }

   public static EnvironmentScanPlacementModifier of(Direction direction, BlockPredicate targetPredicate, int maxSteps) {
      return of(direction, targetPredicate, BlockPredicate.alwaysTrue(), maxSteps);
   }

   public Stream getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
      BlockPos.Mutable lv = pos.mutableCopy();
      StructureWorldAccess lv2 = context.getWorld();
      if (!this.allowedSearchPredicate.test(lv2, lv)) {
         return Stream.of();
      } else {
         int i = 0;

         while(true) {
            if (i < this.maxSteps) {
               if (this.targetPredicate.test(lv2, lv)) {
                  return Stream.of(lv);
               }

               lv.move(this.direction);
               if (lv2.isOutOfHeightLimit(lv.getY())) {
                  return Stream.of();
               }

               if (this.allowedSearchPredicate.test(lv2, lv)) {
                  ++i;
                  continue;
               }
            }

            if (this.targetPredicate.test(lv2, lv)) {
               return Stream.of(lv);
            }

            return Stream.of();
         }
      }
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.ENVIRONMENT_SCAN;
   }
}
