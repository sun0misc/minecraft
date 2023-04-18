package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.FeaturePlacementContext;

/** @deprecated */
@Deprecated
public class CountMultilayerPlacementModifier extends PlacementModifier {
   public static final Codec MODIFIER_CODEC = IntProvider.createValidatingCodec(0, 256).fieldOf("count").xmap(CountMultilayerPlacementModifier::new, (arg) -> {
      return arg.count;
   }).codec();
   private final IntProvider count;

   private CountMultilayerPlacementModifier(IntProvider count) {
      this.count = count;
   }

   public static CountMultilayerPlacementModifier of(IntProvider count) {
      return new CountMultilayerPlacementModifier(count);
   }

   public static CountMultilayerPlacementModifier of(int count) {
      return of(ConstantIntProvider.create(count));
   }

   public Stream getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
      Stream.Builder builder = Stream.builder();
      int i = 0;

      boolean bl;
      do {
         bl = false;

         for(int j = 0; j < this.count.get(random); ++j) {
            int k = random.nextInt(16) + pos.getX();
            int l = random.nextInt(16) + pos.getZ();
            int m = context.getTopY(Heightmap.Type.MOTION_BLOCKING, k, l);
            int n = findPos(context, k, m, l, i);
            if (n != Integer.MAX_VALUE) {
               builder.add(new BlockPos(k, n, l));
               bl = true;
            }
         }

         ++i;
      } while(bl);

      return builder.build();
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.COUNT_ON_EVERY_LAYER;
   }

   private static int findPos(FeaturePlacementContext context, int x, int y, int z, int targetY) {
      BlockPos.Mutable lv = new BlockPos.Mutable(x, y, z);
      int m = 0;
      BlockState lv2 = context.getBlockState(lv);

      for(int n = y; n >= context.getBottomY() + 1; --n) {
         lv.setY(n - 1);
         BlockState lv3 = context.getBlockState(lv);
         if (!blocksSpawn(lv3) && blocksSpawn(lv2) && !lv3.isOf(Blocks.BEDROCK)) {
            if (m == targetY) {
               return lv.getY() + 1;
            }

            ++m;
         }

         lv2 = lv3;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean blocksSpawn(BlockState state) {
      return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
   }
}
