package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RootSystemFeature extends Feature {
   public RootSystemFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      if (!lv.getBlockState(lv2).isAir()) {
         return false;
      } else {
         Random lv3 = context.getRandom();
         BlockPos lv4 = context.getOrigin();
         RootSystemFeatureConfig lv5 = (RootSystemFeatureConfig)context.getConfig();
         BlockPos.Mutable lv6 = lv4.mutableCopy();
         if (generateTreeAndRoots(lv, context.getGenerator(), lv5, lv3, lv6, lv4)) {
            generateHangingRoots(lv, lv5, lv3, lv4, lv6);
         }

         return true;
      }
   }

   private static boolean hasSpaceForTree(StructureWorldAccess world, RootSystemFeatureConfig config, BlockPos pos) {
      BlockPos.Mutable lv = pos.mutableCopy();

      for(int i = 1; i <= config.requiredVerticalSpaceForTree; ++i) {
         lv.move(Direction.UP);
         BlockState lv2 = world.getBlockState(lv);
         if (!isAirOrWater(lv2, i, config.allowedVerticalWaterForTree)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isAirOrWater(BlockState state, int height, int allowedVerticalWaterForTree) {
      if (state.isAir()) {
         return true;
      } else {
         int k = height + 1;
         return k <= allowedVerticalWaterForTree && state.getFluidState().isIn(FluidTags.WATER);
      }
   }

   private static boolean generateTreeAndRoots(StructureWorldAccess world, ChunkGenerator generator, RootSystemFeatureConfig config, Random arg4, BlockPos.Mutable mutablePos, BlockPos pos) {
      for(int i = 0; i < config.maxRootColumnHeight; ++i) {
         mutablePos.move(Direction.UP);
         if (config.predicate.test(world, mutablePos) && hasSpaceForTree(world, config, mutablePos)) {
            BlockPos lv = mutablePos.down();
            if (world.getFluidState(lv).isIn(FluidTags.LAVA) || !world.getBlockState(lv).getMaterial().isSolid()) {
               return false;
            }

            if (((PlacedFeature)config.feature.value()).generateUnregistered(world, generator, arg4, mutablePos)) {
               generateRootsColumn(pos, pos.getY() + i, world, config, arg4);
               return true;
            }
         }
      }

      return false;
   }

   private static void generateRootsColumn(BlockPos pos, int maxY, StructureWorldAccess world, RootSystemFeatureConfig config, Random arg4) {
      int j = pos.getX();
      int k = pos.getZ();
      BlockPos.Mutable lv = pos.mutableCopy();

      for(int l = pos.getY(); l < maxY; ++l) {
         generateRoots(world, config, arg4, j, k, lv.set(j, l, k));
      }

   }

   private static void generateRoots(StructureWorldAccess world, RootSystemFeatureConfig config, Random arg3, int x, int z, BlockPos.Mutable mutablePos) {
      int k = config.rootRadius;
      Predicate predicate = (state) -> {
         return state.isIn(config.rootReplaceable);
      };

      for(int l = 0; l < config.rootPlacementAttempts; ++l) {
         mutablePos.set((Vec3i)mutablePos, arg3.nextInt(k) - arg3.nextInt(k), 0, arg3.nextInt(k) - arg3.nextInt(k));
         if (predicate.test(world.getBlockState(mutablePos))) {
            world.setBlockState(mutablePos, config.rootStateProvider.get(arg3, mutablePos), Block.NOTIFY_LISTENERS);
         }

         mutablePos.setX(x);
         mutablePos.setZ(z);
      }

   }

   private static void generateHangingRoots(StructureWorldAccess world, RootSystemFeatureConfig config, Random arg3, BlockPos pos, BlockPos.Mutable mutablePos) {
      int i = config.hangingRootRadius;
      int j = config.hangingRootVerticalSpan;

      for(int k = 0; k < config.hangingRootPlacementAttempts; ++k) {
         mutablePos.set((Vec3i)pos, arg3.nextInt(i) - arg3.nextInt(i), arg3.nextInt(j) - arg3.nextInt(j), arg3.nextInt(i) - arg3.nextInt(i));
         if (world.isAir(mutablePos)) {
            BlockState lv = config.hangingRootStateProvider.get(arg3, mutablePos);
            if (lv.canPlaceAt(world, mutablePos) && world.getBlockState(mutablePos.up()).isSideSolidFullSquare(world, mutablePos, Direction.DOWN)) {
               world.setBlockState(mutablePos, lv, Block.NOTIFY_LISTENERS);
            }
         }
      }

   }
}
