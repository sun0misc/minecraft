package net.minecraft.world.gen.trunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillTrunkPlacerFields(instance).and(instance.group(IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter((trunkPlacer) -> {
         return trunkPlacer.extraBranchSteps;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter((trunkPlacer) -> {
         return trunkPlacer.placeBranchPerLogProbability;
      }), IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter((trunkPlacer) -> {
         return trunkPlacer.extraBranchLength;
      }), RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("can_grow_through").forGetter((trunkPlacer) -> {
         return trunkPlacer.canGrowThrough;
      }))).apply(instance, UpwardsBranchingTrunkPlacer::new);
   });
   private final IntProvider extraBranchSteps;
   private final float placeBranchPerLogProbability;
   private final IntProvider extraBranchLength;
   private final RegistryEntryList canGrowThrough;

   public UpwardsBranchingTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, IntProvider extraBranchSteps, float placeBranchPerLogProbability, IntProvider extraBranchLength, RegistryEntryList canGrowThrough) {
      super(baseHeight, firstRandomHeight, secondRandomHeight);
      this.extraBranchSteps = extraBranchSteps;
      this.placeBranchPerLogProbability = placeBranchPerLogProbability;
      this.extraBranchLength = extraBranchLength;
      this.canGrowThrough = canGrowThrough;
   }

   protected TrunkPlacerType getType() {
      return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
   }

   public List generate(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
      List list = Lists.newArrayList();
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int j = 0; j < height; ++j) {
         int k = startPos.getY() + j;
         if (this.getAndSetState(world, replacer, random, lv.set(startPos.getX(), k, startPos.getZ()), config) && j < height - 1 && random.nextFloat() < this.placeBranchPerLogProbability) {
            Direction lv2 = Direction.Type.HORIZONTAL.random(random);
            int l = this.extraBranchLength.get(random);
            int m = Math.max(0, l - this.extraBranchLength.get(random) - 1);
            int n = this.extraBranchSteps.get(random);
            this.generateExtraBranch(world, replacer, random, height, config, list, lv, k, lv2, m, n);
         }

         if (j == height - 1) {
            list.add(new FoliagePlacer.TreeNode(lv.set(startPos.getX(), k + 1, startPos.getZ()), 0, false));
         }
      }

      return list;
   }

   private void generateExtraBranch(TestableWorld world, BiConsumer replacer, Random random, int height, TreeFeatureConfig config, List nodes, BlockPos.Mutable pos, int yOffset, Direction direction, int length, int steps) {
      int m = yOffset + length;
      int n = pos.getX();
      int o = pos.getZ();

      for(int p = length; p < height && steps > 0; --steps) {
         if (p >= 1) {
            int q = yOffset + p;
            n += direction.getOffsetX();
            o += direction.getOffsetZ();
            m = q;
            if (this.getAndSetState(world, replacer, random, pos.set(n, q, o), config)) {
               m = q + 1;
            }

            nodes.add(new FoliagePlacer.TreeNode(pos.toImmutable(), 0, false));
         }

         ++p;
      }

      if (m - yOffset > 1) {
         BlockPos lv = new BlockPos(n, m, o);
         nodes.add(new FoliagePlacer.TreeNode(lv, 0, false));
         nodes.add(new FoliagePlacer.TreeNode(lv.down(2), 0, false));
      }

   }

   protected boolean canReplace(TestableWorld world, BlockPos pos) {
      return super.canReplace(world, pos) || world.testBlockState(pos, (state) -> {
         return state.isIn(this.canGrowThrough);
      });
   }
}
