package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class WallPiglinHeadBlock extends WallSkullBlock {
   private static final Map SHAPES;

   public WallPiglinHeadBlock(AbstractBlock.Settings arg) {
      super(SkullBlock.Type.PIGLIN, arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)SHAPES.get(state.get(FACING));
   }

   static {
      SHAPES = Maps.immutableEnumMap(Map.of(Direction.NORTH, Block.createCuboidShape(3.0, 4.0, 8.0, 13.0, 12.0, 16.0), Direction.SOUTH, Block.createCuboidShape(3.0, 4.0, 0.0, 13.0, 12.0, 8.0), Direction.EAST, Block.createCuboidShape(0.0, 4.0, 3.0, 8.0, 12.0, 13.0), Direction.WEST, Block.createCuboidShape(8.0, 4.0, 3.0, 16.0, 12.0, 13.0)));
   }
}
