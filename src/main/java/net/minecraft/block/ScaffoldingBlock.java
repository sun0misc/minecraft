package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ScaffoldingBlock extends Block implements Waterloggable {
   private static final int field_31238 = 1;
   private static final VoxelShape NORMAL_OUTLINE_SHAPE;
   private static final VoxelShape BOTTOM_OUTLINE_SHAPE;
   private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
   private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.fullCube().offset(0.0, -1.0, 0.0);
   public static final int MAX_DISTANCE = 7;
   public static final IntProperty DISTANCE;
   public static final BooleanProperty WATERLOGGED;
   public static final BooleanProperty BOTTOM;

   protected ScaffoldingBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(WATERLOGGED, false)).with(BOTTOM, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(DISTANCE, WATERLOGGED, BOTTOM);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (!context.isHolding(state.getBlock().asItem())) {
         return (Boolean)state.get(BOTTOM) ? BOTTOM_OUTLINE_SHAPE : NORMAL_OUTLINE_SHAPE;
      } else {
         return VoxelShapes.fullCube();
      }
   }

   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.fullCube();
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return context.getStack().isOf(this.asItem());
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos lv = ctx.getBlockPos();
      World lv2 = ctx.getWorld();
      int i = calculateDistance(lv2, lv);
      return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, lv2.getFluidState(lv).getFluid() == Fluids.WATER)).with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(lv2, lv, i));
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.isClient) {
         world.scheduleBlockTick(pos, this, 1);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      if (!world.isClient()) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return state;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      int i = calculateDistance(world, pos);
      BlockState lv = (BlockState)((BlockState)state.with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(world, pos, i));
      if ((Integer)lv.get(DISTANCE) == 7) {
         if ((Integer)state.get(DISTANCE) == 7) {
            FallingBlockEntity.spawnFromBlock(world, pos, lv);
         } else {
            world.breakBlock(pos, true);
         }
      } else if (state != lv) {
         world.setBlockState(pos, lv, Block.NOTIFY_ALL);
      }

   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return calculateDistance(world, pos) < 7;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (context.isAbove(VoxelShapes.fullCube(), pos, true) && !context.isDescending()) {
         return NORMAL_OUTLINE_SHAPE;
      } else {
         return (Integer)state.get(DISTANCE) != 0 && (Boolean)state.get(BOTTOM) && context.isAbove(OUTLINE_SHAPE, pos, true) ? COLLISION_SHAPE : VoxelShapes.empty();
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   private boolean shouldBeBottom(BlockView world, BlockPos pos, int distance) {
      return distance > 0 && !world.getBlockState(pos.down()).isOf(this);
   }

   public static int calculateDistance(BlockView world, BlockPos pos) {
      BlockPos.Mutable lv = pos.mutableCopy().move(Direction.DOWN);
      BlockState lv2 = world.getBlockState(lv);
      int i = 7;
      if (lv2.isOf(Blocks.SCAFFOLDING)) {
         i = (Integer)lv2.get(DISTANCE);
      } else if (lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
         return 0;
      }

      Iterator var5 = Direction.Type.HORIZONTAL.iterator();

      while(var5.hasNext()) {
         Direction lv3 = (Direction)var5.next();
         BlockState lv4 = world.getBlockState(lv.set(pos, (Direction)lv3));
         if (lv4.isOf(Blocks.SCAFFOLDING)) {
            i = Math.min(i, (Integer)lv4.get(DISTANCE) + 1);
            if (i == 1) {
               break;
            }
         }
      }

      return i;
   }

   static {
      DISTANCE = Properties.DISTANCE_0_7;
      WATERLOGGED = Properties.WATERLOGGED;
      BOTTOM = Properties.BOTTOM;
      VoxelShape lv = Block.createCuboidShape(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
      VoxelShape lv2 = Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 16.0, 2.0);
      VoxelShape lv3 = Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 16.0, 2.0);
      VoxelShape lv4 = Block.createCuboidShape(0.0, 0.0, 14.0, 2.0, 16.0, 16.0);
      VoxelShape lv5 = Block.createCuboidShape(14.0, 0.0, 14.0, 16.0, 16.0, 16.0);
      NORMAL_OUTLINE_SHAPE = VoxelShapes.union(lv, lv2, lv3, lv4, lv5);
      VoxelShape lv6 = Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 2.0, 16.0);
      VoxelShape lv7 = Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 2.0, 16.0);
      VoxelShape lv8 = Block.createCuboidShape(0.0, 0.0, 14.0, 16.0, 2.0, 16.0);
      VoxelShape lv9 = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 2.0);
      BOTTOM_OUTLINE_SHAPE = VoxelShapes.union(COLLISION_SHAPE, NORMAL_OUTLINE_SHAPE, lv7, lv6, lv9, lv8);
   }
}
