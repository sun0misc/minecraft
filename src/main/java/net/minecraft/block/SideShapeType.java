package net.minecraft.block;

import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public enum SideShapeType {
   FULL {
      public boolean matches(BlockState state, BlockView world, BlockPos pos, Direction direction) {
         return Block.isFaceFullSquare(state.getSidesShape(world, pos), direction);
      }
   },
   CENTER {
      private final int radius = 1;
      private final VoxelShape squareCuboid = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 10.0, 9.0);

      public boolean matches(BlockState state, BlockView world, BlockPos pos, Direction direction) {
         return !VoxelShapes.matchesAnywhere(state.getSidesShape(world, pos).getFace(direction), this.squareCuboid, BooleanBiFunction.ONLY_SECOND);
      }
   },
   RIGID {
      private final int ringWidth = 2;
      private final VoxelShape hollowSquareCuboid;

      {
         this.hollowSquareCuboid = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), BooleanBiFunction.ONLY_FIRST);
      }

      public boolean matches(BlockState state, BlockView world, BlockPos pos, Direction direction) {
         return !VoxelShapes.matchesAnywhere(state.getSidesShape(world, pos).getFace(direction), this.hollowSquareCuboid, BooleanBiFunction.ONLY_SECOND);
      }
   };

   public abstract boolean matches(BlockState state, BlockView world, BlockPos pos, Direction direction);

   // $FF: synthetic method
   private static SideShapeType[] method_36711() {
      return new SideShapeType[]{FULL, CENTER, RIGID};
   }
}
