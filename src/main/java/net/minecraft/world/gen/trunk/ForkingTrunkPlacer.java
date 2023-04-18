package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillTrunkPlacerFields(instance).apply(instance, ForkingTrunkPlacer::new);
   });

   public ForkingTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType getType() {
      return TrunkPlacerType.FORKING_TRUNK_PLACER;
   }

   public List generate(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
      setToDirt(world, replacer, random, startPos.down(), config);
      List list = Lists.newArrayList();
      Direction lv = Direction.Type.HORIZONTAL.random(random);
      int j = height - random.nextInt(4) - 1;
      int k = 3 - random.nextInt(3);
      BlockPos.Mutable lv2 = new BlockPos.Mutable();
      int l = startPos.getX();
      int m = startPos.getZ();
      OptionalInt optionalInt = OptionalInt.empty();

      int o;
      for(int n = 0; n < height; ++n) {
         o = startPos.getY() + n;
         if (n >= j && k > 0) {
            l += lv.getOffsetX();
            m += lv.getOffsetZ();
            --k;
         }

         if (this.getAndSetState(world, replacer, random, lv2.set(l, o, m), config)) {
            optionalInt = OptionalInt.of(o + 1);
         }
      }

      if (optionalInt.isPresent()) {
         list.add(new FoliagePlacer.TreeNode(new BlockPos(l, optionalInt.getAsInt(), m), 1, false));
      }

      l = startPos.getX();
      m = startPos.getZ();
      Direction lv3 = Direction.Type.HORIZONTAL.random(random);
      if (lv3 != lv) {
         o = j - random.nextInt(2) - 1;
         int p = 1 + random.nextInt(3);
         optionalInt = OptionalInt.empty();

         for(int q = o; q < height && p > 0; --p) {
            if (q >= 1) {
               int r = startPos.getY() + q;
               l += lv3.getOffsetX();
               m += lv3.getOffsetZ();
               if (this.getAndSetState(world, replacer, random, lv2.set(l, r, m), config)) {
                  optionalInt = OptionalInt.of(r + 1);
               }
            }

            ++q;
         }

         if (optionalInt.isPresent()) {
            list.add(new FoliagePlacer.TreeNode(new BlockPos(l, optionalInt.getAsInt(), m), 0, false));
         }
      }

      return list;
   }
}
