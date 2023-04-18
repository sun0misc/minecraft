package net.minecraft.world.gen.root;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public abstract class RootPlacer {
   public static final Codec TYPE_CODEC;
   protected final IntProvider trunkOffsetY;
   protected final BlockStateProvider rootProvider;
   protected final Optional aboveRootPlacement;

   protected static Products.P3 method_43182(RecordCodecBuilder.Instance instance) {
      return instance.group(IntProvider.VALUE_CODEC.fieldOf("trunk_offset_y").forGetter((rootPlacer) -> {
         return rootPlacer.trunkOffsetY;
      }), BlockStateProvider.TYPE_CODEC.fieldOf("root_provider").forGetter((rootPlacer) -> {
         return rootPlacer.rootProvider;
      }), AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter((rootPlacer) -> {
         return rootPlacer.aboveRootPlacement;
      }));
   }

   public RootPlacer(IntProvider trunkOffsetY, BlockStateProvider rootProvider, Optional aboveRootPlacement) {
      this.trunkOffsetY = trunkOffsetY;
      this.rootProvider = rootProvider;
      this.aboveRootPlacement = aboveRootPlacement;
   }

   protected abstract RootPlacerType getType();

   public abstract boolean generate(TestableWorld world, BiConsumer replacer, Random random, BlockPos pos, BlockPos trunkPos, TreeFeatureConfig config);

   protected boolean canGrowThrough(TestableWorld world, BlockPos pos) {
      return TreeFeature.canReplace(world, pos);
   }

   protected void placeRoots(TestableWorld world, BiConsumer replacer, Random random, BlockPos pos, TreeFeatureConfig config) {
      if (this.canGrowThrough(world, pos)) {
         replacer.accept(pos, this.applyWaterlogging(world, pos, this.rootProvider.get(random, pos)));
         if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement lv = (AboveRootPlacement)this.aboveRootPlacement.get();
            BlockPos lv2 = pos.up();
            if (random.nextFloat() < lv.aboveRootPlacementChance() && world.testBlockState(lv2, AbstractBlock.AbstractBlockState::isAir)) {
               replacer.accept(lv2, this.applyWaterlogging(world, lv2, lv.aboveRootProvider().get(random, lv2)));
            }
         }

      }
   }

   protected BlockState applyWaterlogging(TestableWorld world, BlockPos pos, BlockState state) {
      if (state.contains(Properties.WATERLOGGED)) {
         boolean bl = world.testFluidState(pos, (fluidState) -> {
            return fluidState.isIn(FluidTags.WATER);
         });
         return (BlockState)state.with(Properties.WATERLOGGED, bl);
      } else {
         return state;
      }
   }

   public BlockPos trunkOffset(BlockPos pos, Random random) {
      return pos.up(this.trunkOffsetY.get(random));
   }

   static {
      TYPE_CODEC = Registries.ROOT_PLACER_TYPE.getCodec().dispatch(RootPlacer::getType, RootPlacerType::getCodec);
   }
}
