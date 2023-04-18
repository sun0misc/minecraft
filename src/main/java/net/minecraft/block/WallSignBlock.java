package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallSignBlock extends AbstractSignBlock {
   public static final DirectionProperty FACING;
   protected static final float field_31282 = 2.0F;
   protected static final float field_31283 = 4.5F;
   protected static final float field_31284 = 12.5F;
   private static final Map FACING_TO_SHAPE;

   public WallSignBlock(AbstractBlock.Settings arg, WoodType arg2) {
      super(arg.sounds(arg2.soundType()), arg2);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.offset(((Direction)state.get(FACING)).getOpposite())).getMaterial().isSolid();
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      WorldView lv3 = ctx.getWorld();
      BlockPos lv4 = ctx.getBlockPos();
      Direction[] lvs = ctx.getPlacementDirections();
      Direction[] var7 = lvs;
      int var8 = lvs.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Direction lv5 = var7[var9];
         if (lv5.getAxis().isHorizontal()) {
            Direction lv6 = lv5.getOpposite();
            lv = (BlockState)lv.with(FACING, lv6);
            if (lv.canPlaceAt(lv3, lv4)) {
               return (BlockState)lv.with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public float getRotationDegrees(BlockState state) {
      return ((Direction)state.get(FACING)).asRotation();
   }

   public Vec3d getCenter(BlockState state) {
      VoxelShape lv = (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
      return lv.getBoundingBox().getCenter();
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

   static {
      FACING = HorizontalFacingBlock.FACING;
      FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(0.0, 4.5, 14.0, 16.0, 12.5, 16.0), Direction.SOUTH, Block.createCuboidShape(0.0, 4.5, 0.0, 16.0, 12.5, 2.0), Direction.EAST, Block.createCuboidShape(0.0, 4.5, 0.0, 2.0, 12.5, 16.0), Direction.WEST, Block.createCuboidShape(14.0, 4.5, 0.0, 16.0, 12.5, 16.0)));
   }
}
