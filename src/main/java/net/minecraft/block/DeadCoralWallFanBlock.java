package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class DeadCoralWallFanBlock extends DeadCoralFanBlock {
   public static final DirectionProperty FACING;
   private static final Map FACING_TO_SHAPE;

   protected DeadCoralWallFanBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, true));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, WATERLOGGED);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = (Direction)state.get(FACING);
      BlockPos lv2 = pos.offset(lv.getOpposite());
      BlockState lv3 = world.getBlockState(lv2);
      return lv3.isSideSolidFullSquare(world, lv2, lv);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = super.getPlacementState(ctx);
      WorldView lv2 = ctx.getWorld();
      BlockPos lv3 = ctx.getBlockPos();
      Direction[] lvs = ctx.getPlacementDirections();
      Direction[] var6 = lvs;
      int var7 = lvs.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv4 = var6[var8];
         if (lv4.getAxis().isHorizontal()) {
            lv = (BlockState)lv.with(FACING, lv4.getOpposite());
            if (lv.canPlaceAt(lv2, lv3)) {
               return lv;
            }
         }
      }

      return null;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(0.0, 4.0, 5.0, 16.0, 12.0, 16.0), Direction.SOUTH, Block.createCuboidShape(0.0, 4.0, 0.0, 16.0, 12.0, 11.0), Direction.WEST, Block.createCuboidShape(5.0, 4.0, 0.0, 16.0, 12.0, 16.0), Direction.EAST, Block.createCuboidShape(0.0, 4.0, 0.0, 11.0, 12.0, 16.0)));
   }
}
