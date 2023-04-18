package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;

public class CountPlacementModifier extends AbstractCountPlacementModifier {
   public static final Codec MODIFIER_CODEC = IntProvider.createValidatingCodec(0, 256).fieldOf("count").xmap(CountPlacementModifier::new, (arg) -> {
      return arg.count;
   }).codec();
   private final IntProvider count;

   private CountPlacementModifier(IntProvider count) {
      this.count = count;
   }

   public static CountPlacementModifier of(IntProvider count) {
      return new CountPlacementModifier(count);
   }

   public static CountPlacementModifier of(int count) {
      return of(ConstantIntProvider.create(count));
   }

   protected int getCount(Random random, BlockPos pos) {
      return this.count.get(random);
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.COUNT;
   }
}
