package net.minecraft.world.gen.trunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;

public class CherryTrunkPlacer extends TrunkPlacer {
   private static final Codec BRANCH_START_OFFSET_FROM_TOP_CODEC;
   public static final Codec CODEC;
   private final IntProvider branchCount;
   private final IntProvider branchHorizontalLength;
   private final UniformIntProvider branchStartOffsetFromTop;
   private final UniformIntProvider secondBranchStartOffsetFromTop;
   private final IntProvider branchEndOffsetFromTop;

   public CherryTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, IntProvider branchCount, IntProvider branchHorizontalLength, UniformIntProvider branchStartOffsetFromTop, IntProvider branchEndOffsetFromTop) {
      super(baseHeight, firstRandomHeight, secondRandomHeight);
      this.branchCount = branchCount;
      this.branchHorizontalLength = branchHorizontalLength;
      this.branchStartOffsetFromTop = branchStartOffsetFromTop;
      this.secondBranchStartOffsetFromTop = UniformIntProvider.create(branchStartOffsetFromTop.getMin(), branchStartOffsetFromTop.getMax() - 1);
      this.branchEndOffsetFromTop = branchEndOffsetFromTop;
   }

   protected TrunkPlacerType getType() {
      return TrunkPlacerType.CHERRY_TRUNK_PLACER;
   }

   public List generate(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
      setToDirt(world, replacer, random, startPos.down(), config);
      int j = Math.max(0, height - 1 + this.branchStartOffsetFromTop.get(random));
      int k = Math.max(0, height - 1 + this.secondBranchStartOffsetFromTop.get(random));
      if (k >= j) {
         ++k;
      }

      int l = this.branchCount.get(random);
      boolean bl = l == 3;
      boolean bl2 = l >= 2;
      int m;
      if (bl) {
         m = height;
      } else if (bl2) {
         m = Math.max(j, k) + 1;
      } else {
         m = j + 1;
      }

      for(int n = 0; n < m; ++n) {
         this.getAndSetState(world, replacer, random, startPos.up(n), config);
      }

      List list = new ArrayList();
      if (bl) {
         list.add(new FoliagePlacer.TreeNode(startPos.up(m), 0, false));
      }

      BlockPos.Mutable lv = new BlockPos.Mutable();
      Direction lv2 = Direction.Type.HORIZONTAL.random(random);
      Function function = (state) -> {
         return (BlockState)state.withIfExists(PillarBlock.AXIS, lv2.getAxis());
      };
      list.add(this.generateBranch(world, replacer, random, height, startPos, config, function, lv2, j, j < m - 1, lv));
      if (bl2) {
         list.add(this.generateBranch(world, replacer, random, height, startPos, config, function, lv2.getOpposite(), k, k < m - 1, lv));
      }

      return list;
   }

   private FoliagePlacer.TreeNode generateBranch(TestableWorld world, BiConsumer replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config, Function withAxisFunction, Direction direction, int branchStartOffset, boolean branchBelowHeight, BlockPos.Mutable mutablePos) {
      mutablePos.set(startPos).move(Direction.UP, branchStartOffset);
      int k = height - 1 + this.branchEndOffsetFromTop.get(random);
      boolean bl2 = branchBelowHeight || k < branchStartOffset;
      int l = this.branchHorizontalLength.get(random) + (bl2 ? 1 : 0);
      BlockPos lv = startPos.offset(direction, l).up(k);
      int m = bl2 ? 2 : 1;

      for(int n = 0; n < m; ++n) {
         this.getAndSetState(world, replacer, random, mutablePos.move(direction), config, withAxisFunction);
      }

      Direction lv2 = lv.getY() > mutablePos.getY() ? Direction.UP : Direction.DOWN;

      while(true) {
         int o = mutablePos.getManhattanDistance(lv);
         if (o == 0) {
            return new FoliagePlacer.TreeNode(lv.up(), 0, false);
         }

         float f = (float)Math.abs(lv.getY() - mutablePos.getY()) / (float)o;
         boolean bl3 = random.nextFloat() < f;
         mutablePos.move(bl3 ? lv2 : direction);
         this.getAndSetState(world, replacer, random, mutablePos, config, bl3 ? Function.identity() : withAxisFunction);
      }
   }

   static {
      BRANCH_START_OFFSET_FROM_TOP_CODEC = Codecs.validate(UniformIntProvider.CODEC, (branchStartOffsetFromTop) -> {
         return branchStartOffsetFromTop.getMax() - branchStartOffsetFromTop.getMin() < 1 ? DataResult.error(() -> {
            return "Need at least 2 blocks variation for the branch starts to fit both branches";
         }) : DataResult.success(branchStartOffsetFromTop);
      });
      CODEC = RecordCodecBuilder.create((instance) -> {
         return fillTrunkPlacerFields(instance).and(instance.group(IntProvider.createValidatingCodec(1, 3).fieldOf("branch_count").forGetter((trunkPlacer) -> {
            return trunkPlacer.branchCount;
         }), IntProvider.createValidatingCodec(2, 16).fieldOf("branch_horizontal_length").forGetter((trunkPlacer) -> {
            return trunkPlacer.branchHorizontalLength;
         }), IntProvider.createValidatingCodec(-16, 0, BRANCH_START_OFFSET_FROM_TOP_CODEC).fieldOf("branch_start_offset_from_top").forGetter((trunkPlacer) -> {
            return trunkPlacer.branchStartOffsetFromTop;
         }), IntProvider.createValidatingCodec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter((trunkPlacer) -> {
            return trunkPlacer.branchEndOffsetFromTop;
         }))).apply(instance, CherryTrunkPlacer::new);
      });
   }
}
