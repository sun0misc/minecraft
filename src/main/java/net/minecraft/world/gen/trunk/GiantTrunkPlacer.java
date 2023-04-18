package net.minecraft.world.gen.trunk;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class GiantTrunkPlacer extends TrunkPlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillTrunkPlacerFields(instance).apply(instance, GiantTrunkPlacer::new);
   });

   public GiantTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType getType() {
      return TrunkPlacerType.GIANT_TRUNK_PLACER;
   }

   public List generate(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
      BlockPos lv = startPos.down();
      setToDirt(world, replacer, random, lv, config);
      setToDirt(world, replacer, random, lv.east(), config);
      setToDirt(world, replacer, random, lv.south(), config);
      setToDirt(world, replacer, random, lv.south().east(), config);
      BlockPos.Mutable lv2 = new BlockPos.Mutable();

      for(int j = 0; j < height; ++j) {
         this.setLog(world, replacer, random, lv2, config, startPos, 0, j, 0);
         if (j < height - 1) {
            this.setLog(world, replacer, random, lv2, config, startPos, 1, j, 0);
            this.setLog(world, replacer, random, lv2, config, startPos, 1, j, 1);
            this.setLog(world, replacer, random, lv2, config, startPos, 0, j, 1);
         }
      }

      return ImmutableList.of(new FoliagePlacer.TreeNode(startPos.up(height), 0, true));
   }

   private void setLog(TestableWorld arg, BiConsumer biConsumer, Random arg2, BlockPos.Mutable arg3, TreeFeatureConfig arg4, BlockPos arg5, int i, int j, int k) {
      arg3.set((Vec3i)arg5, i, j, k);
      this.trySetState(arg, biConsumer, arg2, arg3, arg4);
   }
}
