package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class DarkOakTrunkPlacer extends TrunkPlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillTrunkPlacerFields(instance).apply(instance, DarkOakTrunkPlacer::new);
   });

   public DarkOakTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType getType() {
      return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
   }

   public List generate(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
      List list = Lists.newArrayList();
      BlockPos lv = startPos.down();
      setToDirt(world, replacer, random, lv, config);
      setToDirt(world, replacer, random, lv.east(), config);
      setToDirt(world, replacer, random, lv.south(), config);
      setToDirt(world, replacer, random, lv.south().east(), config);
      Direction lv2 = Direction.Type.HORIZONTAL.random(random);
      int j = height - random.nextInt(4);
      int k = 2 - random.nextInt(3);
      int l = startPos.getX();
      int m = startPos.getY();
      int n = startPos.getZ();
      int o = l;
      int p = n;
      int q = m + height - 1;

      int r;
      int s;
      for(r = 0; r < height; ++r) {
         if (r >= j && k > 0) {
            o += lv2.getOffsetX();
            p += lv2.getOffsetZ();
            --k;
         }

         s = m + r;
         BlockPos lv3 = new BlockPos(o, s, p);
         if (TreeFeature.isAirOrLeaves(world, lv3)) {
            this.getAndSetState(world, replacer, random, lv3, config);
            this.getAndSetState(world, replacer, random, lv3.east(), config);
            this.getAndSetState(world, replacer, random, lv3.south(), config);
            this.getAndSetState(world, replacer, random, lv3.east().south(), config);
         }
      }

      list.add(new FoliagePlacer.TreeNode(new BlockPos(o, q, p), 0, true));

      for(r = -1; r <= 2; ++r) {
         for(s = -1; s <= 2; ++s) {
            if ((r < 0 || r > 1 || s < 0 || s > 1) && random.nextInt(3) <= 0) {
               int t = random.nextInt(3) + 2;

               for(int u = 0; u < t; ++u) {
                  this.getAndSetState(world, replacer, random, new BlockPos(l + r, q - u - 1, n + s), config);
               }

               list.add(new FoliagePlacer.TreeNode(new BlockPos(o + r, q, p + s), 0, false));
            }
         }
      }

      return list;
   }
}
