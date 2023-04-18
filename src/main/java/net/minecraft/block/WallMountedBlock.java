package net.minecraft.block;

import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallMountedBlock extends HorizontalFacingBlock {
   public static final EnumProperty FACE;

   protected WallMountedBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return canPlaceAt(world, pos, getDirection(state).getOpposite());
   }

   public static boolean canPlaceAt(WorldView world, BlockPos pos, Direction direction) {
      BlockPos lv = pos.offset(direction);
      return world.getBlockState(lv).isSideSolidFullSquare(world, lv, direction.getOpposite());
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction[] var2 = ctx.getPlacementDirections();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         BlockState lv2;
         if (lv.getAxis() == Direction.Axis.Y) {
            lv2 = (BlockState)((BlockState)this.getDefaultState().with(FACE, lv == Direction.UP ? WallMountLocation.CEILING : WallMountLocation.FLOOR)).with(FACING, ctx.getHorizontalPlayerFacing());
         } else {
            lv2 = (BlockState)((BlockState)this.getDefaultState().with(FACE, WallMountLocation.WALL)).with(FACING, lv.getOpposite());
         }

         if (lv2.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return lv2;
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return getDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   protected static Direction getDirection(BlockState state) {
      switch ((WallMountLocation)state.get(FACE)) {
         case CEILING:
            return Direction.DOWN;
         case FLOOR:
            return Direction.UP;
         default:
            return (Direction)state.get(FACING);
      }
   }

   static {
      FACE = Properties.WALL_MOUNT_LOCATION;
   }
}
