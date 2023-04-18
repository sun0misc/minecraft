package net.minecraft.world.gen.feature.util;

import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.enums.Thickness;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;

public class DripstoneHelper {
   protected static double scaleHeightFromRadius(double radius, double scale, double heightScale, double bluntness) {
      if (radius < bluntness) {
         radius = bluntness;
      }

      double h = 0.384;
      double i = radius / scale * 0.384;
      double j = 0.75 * Math.pow(i, 1.3333333333333333);
      double k = Math.pow(i, 0.6666666666666666);
      double l = 0.3333333333333333 * Math.log(i);
      double m = heightScale * (j - k - l);
      m = Math.max(m, 0.0);
      return m / 0.384 * scale;
   }

   protected static boolean canGenerateBase(StructureWorldAccess world, BlockPos pos, int height) {
      if (canGenerateOrLava(world, pos)) {
         return false;
      } else {
         float f = 6.0F;
         float g = 6.0F / (float)height;

         for(float h = 0.0F; h < 6.2831855F; h += g) {
            int j = (int)(MathHelper.cos(h) * (float)height);
            int k = (int)(MathHelper.sin(h) * (float)height);
            if (canGenerateOrLava(world, pos.add(j, 0, k))) {
               return false;
            }
         }

         return true;
      }
   }

   protected static boolean canGenerate(WorldAccess world, BlockPos pos) {
      return world.testBlockState(pos, DripstoneHelper::canGenerate);
   }

   protected static boolean canGenerateOrLava(WorldAccess world, BlockPos pos) {
      return world.testBlockState(pos, DripstoneHelper::canGenerateOrLava);
   }

   protected static void getDripstoneThickness(Direction direction, int height, boolean merge, Consumer callback) {
      if (height >= 3) {
         callback.accept(getState(direction, Thickness.BASE));

         for(int j = 0; j < height - 3; ++j) {
            callback.accept(getState(direction, Thickness.MIDDLE));
         }
      }

      if (height >= 2) {
         callback.accept(getState(direction, Thickness.FRUSTUM));
      }

      if (height >= 1) {
         callback.accept(getState(direction, merge ? Thickness.TIP_MERGE : Thickness.TIP));
      }

   }

   protected static void generatePointedDripstone(WorldAccess world, BlockPos pos, Direction direction, int height, boolean merge) {
      if (canReplace(world.getBlockState(pos.offset(direction.getOpposite())))) {
         BlockPos.Mutable lv = pos.mutableCopy();
         getDripstoneThickness(direction, height, merge, (state) -> {
            if (state.isOf(Blocks.POINTED_DRIPSTONE)) {
               state = (BlockState)state.with(PointedDripstoneBlock.WATERLOGGED, world.isWater(lv));
            }

            world.setBlockState(lv, state, Block.NOTIFY_LISTENERS);
            lv.move(direction);
         });
      }
   }

   protected static boolean generateDripstoneBlock(WorldAccess world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      if (lv.isIn(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS)) {
         world.setBlockState(pos, Blocks.DRIPSTONE_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
         return true;
      } else {
         return false;
      }
   }

   private static BlockState getState(Direction direction, Thickness thickness) {
      return (BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.getDefaultState().with(PointedDripstoneBlock.VERTICAL_DIRECTION, direction)).with(PointedDripstoneBlock.THICKNESS, thickness);
   }

   public static boolean canReplaceOrLava(BlockState state) {
      return canReplace(state) || state.isOf(Blocks.LAVA);
   }

   public static boolean canReplace(BlockState state) {
      return state.isOf(Blocks.DRIPSTONE_BLOCK) || state.isIn(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS);
   }

   public static boolean canGenerate(BlockState state) {
      return state.isAir() || state.isOf(Blocks.WATER);
   }

   public static boolean cannotGenerate(BlockState state) {
      return !state.isAir() && !state.isOf(Blocks.WATER);
   }

   public static boolean canGenerateOrLava(BlockState state) {
      return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
   }
}
