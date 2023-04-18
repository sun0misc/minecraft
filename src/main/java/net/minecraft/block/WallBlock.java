package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.enums.WallShape;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class WallBlock extends Block implements Waterloggable {
   public static final BooleanProperty UP;
   public static final EnumProperty EAST_SHAPE;
   public static final EnumProperty NORTH_SHAPE;
   public static final EnumProperty SOUTH_SHAPE;
   public static final EnumProperty WEST_SHAPE;
   public static final BooleanProperty WATERLOGGED;
   private final Map shapeMap;
   private final Map collisionShapeMap;
   private static final int field_31276 = 3;
   private static final int field_31277 = 14;
   private static final int field_31278 = 4;
   private static final int field_31279 = 1;
   private static final int field_31280 = 7;
   private static final int field_31281 = 9;
   private static final VoxelShape TALL_POST_SHAPE;
   private static final VoxelShape TALL_NORTH_SHAPE;
   private static final VoxelShape TALL_SOUTH_SHAPE;
   private static final VoxelShape TALL_WEST_SHAPE;
   private static final VoxelShape TALL_EAST_SHAPE;

   public WallBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, true)).with(NORTH_SHAPE, WallShape.NONE)).with(EAST_SHAPE, WallShape.NONE)).with(SOUTH_SHAPE, WallShape.NONE)).with(WEST_SHAPE, WallShape.NONE)).with(WATERLOGGED, false));
      this.shapeMap = this.getShapeMap(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
      this.collisionShapeMap = this.getShapeMap(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
   }

   private static VoxelShape getVoxelShape(VoxelShape base, WallShape wallShape, VoxelShape tall, VoxelShape low) {
      if (wallShape == WallShape.TALL) {
         return VoxelShapes.union(base, low);
      } else {
         return wallShape == WallShape.LOW ? VoxelShapes.union(base, tall) : base;
      }
   }

   private Map getShapeMap(float f, float g, float h, float i, float j, float k) {
      float l = 8.0F - f;
      float m = 8.0F + f;
      float n = 8.0F - g;
      float o = 8.0F + g;
      VoxelShape lv = Block.createCuboidShape((double)l, 0.0, (double)l, (double)m, (double)h, (double)m);
      VoxelShape lv2 = Block.createCuboidShape((double)n, (double)i, 0.0, (double)o, (double)j, (double)o);
      VoxelShape lv3 = Block.createCuboidShape((double)n, (double)i, (double)n, (double)o, (double)j, 16.0);
      VoxelShape lv4 = Block.createCuboidShape(0.0, (double)i, (double)n, (double)o, (double)j, (double)o);
      VoxelShape lv5 = Block.createCuboidShape((double)n, (double)i, (double)n, 16.0, (double)j, (double)o);
      VoxelShape lv6 = Block.createCuboidShape((double)n, (double)i, 0.0, (double)o, (double)k, (double)o);
      VoxelShape lv7 = Block.createCuboidShape((double)n, (double)i, (double)n, (double)o, (double)k, 16.0);
      VoxelShape lv8 = Block.createCuboidShape(0.0, (double)i, (double)n, (double)o, (double)k, (double)o);
      VoxelShape lv9 = Block.createCuboidShape((double)n, (double)i, (double)n, 16.0, (double)k, (double)o);
      ImmutableMap.Builder builder = ImmutableMap.builder();
      Iterator var21 = UP.getValues().iterator();

      while(var21.hasNext()) {
         Boolean boolean_ = (Boolean)var21.next();
         Iterator var23 = EAST_SHAPE.getValues().iterator();

         while(var23.hasNext()) {
            WallShape lv10 = (WallShape)var23.next();
            Iterator var25 = NORTH_SHAPE.getValues().iterator();

            while(var25.hasNext()) {
               WallShape lv11 = (WallShape)var25.next();
               Iterator var27 = WEST_SHAPE.getValues().iterator();

               while(var27.hasNext()) {
                  WallShape lv12 = (WallShape)var27.next();
                  Iterator var29 = SOUTH_SHAPE.getValues().iterator();

                  while(var29.hasNext()) {
                     WallShape lv13 = (WallShape)var29.next();
                     VoxelShape lv14 = VoxelShapes.empty();
                     lv14 = getVoxelShape(lv14, lv10, lv5, lv9);
                     lv14 = getVoxelShape(lv14, lv12, lv4, lv8);
                     lv14 = getVoxelShape(lv14, lv11, lv2, lv6);
                     lv14 = getVoxelShape(lv14, lv13, lv3, lv7);
                     if (boolean_) {
                        lv14 = VoxelShapes.union(lv14, lv);
                     }

                     BlockState lv15 = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(UP, boolean_)).with(EAST_SHAPE, lv10)).with(WEST_SHAPE, lv12)).with(NORTH_SHAPE, lv11)).with(SOUTH_SHAPE, lv13);
                     builder.put((BlockState)lv15.with(WATERLOGGED, false), lv14);
                     builder.put((BlockState)lv15.with(WATERLOGGED, true), lv14);
                  }
               }
            }
         }
      }

      return builder.build();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.shapeMap.get(state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.collisionShapeMap.get(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   private boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side) {
      Block lv = state.getBlock();
      boolean bl2 = lv instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, side);
      return state.isIn(BlockTags.WALLS) || !cannotConnect(state) && faceFullSquare || lv instanceof PaneBlock || bl2;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldView lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      BlockPos lv4 = lv2.north();
      BlockPos lv5 = lv2.east();
      BlockPos lv6 = lv2.south();
      BlockPos lv7 = lv2.west();
      BlockPos lv8 = lv2.up();
      BlockState lv9 = lv.getBlockState(lv4);
      BlockState lv10 = lv.getBlockState(lv5);
      BlockState lv11 = lv.getBlockState(lv6);
      BlockState lv12 = lv.getBlockState(lv7);
      BlockState lv13 = lv.getBlockState(lv8);
      boolean bl = this.shouldConnectTo(lv9, lv9.isSideSolidFullSquare(lv, lv4, Direction.SOUTH), Direction.SOUTH);
      boolean bl2 = this.shouldConnectTo(lv10, lv10.isSideSolidFullSquare(lv, lv5, Direction.WEST), Direction.WEST);
      boolean bl3 = this.shouldConnectTo(lv11, lv11.isSideSolidFullSquare(lv, lv6, Direction.NORTH), Direction.NORTH);
      boolean bl4 = this.shouldConnectTo(lv12, lv12.isSideSolidFullSquare(lv, lv7, Direction.EAST), Direction.EAST);
      BlockState lv14 = (BlockState)this.getDefaultState().with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
      return this.getStateWith(lv, lv14, lv8, lv13, bl, bl2, bl3, bl4);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      if (direction == Direction.DOWN) {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         return direction == Direction.UP ? this.getStateAt(world, state, neighborPos, neighborState) : this.getStateWithNeighbor(world, pos, state, neighborPos, neighborState, direction);
      }
   }

   private static boolean isConnected(BlockState state, Property property) {
      return state.get(property) != WallShape.NONE;
   }

   private static boolean shouldUseTallShape(VoxelShape aboveShape, VoxelShape tallShape) {
      return !VoxelShapes.matchesAnywhere(tallShape, aboveShape, BooleanBiFunction.ONLY_FIRST);
   }

   private BlockState getStateAt(WorldView world, BlockState state, BlockPos pos, BlockState aboveState) {
      boolean bl = isConnected(state, NORTH_SHAPE);
      boolean bl2 = isConnected(state, EAST_SHAPE);
      boolean bl3 = isConnected(state, SOUTH_SHAPE);
      boolean bl4 = isConnected(state, WEST_SHAPE);
      return this.getStateWith(world, state, pos, aboveState, bl, bl2, bl3, bl4);
   }

   private BlockState getStateWithNeighbor(WorldView world, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction direction) {
      Direction lv = direction.getOpposite();
      boolean bl = direction == Direction.NORTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, lv), lv) : isConnected(state, NORTH_SHAPE);
      boolean bl2 = direction == Direction.EAST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, lv), lv) : isConnected(state, EAST_SHAPE);
      boolean bl3 = direction == Direction.SOUTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, lv), lv) : isConnected(state, SOUTH_SHAPE);
      boolean bl4 = direction == Direction.WEST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, lv), lv) : isConnected(state, WEST_SHAPE);
      BlockPos lv2 = pos.up();
      BlockState lv3 = world.getBlockState(lv2);
      return this.getStateWith(world, state, lv2, lv3, bl, bl2, bl3, bl4);
   }

   private BlockState getStateWith(WorldView world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west) {
      VoxelShape lv = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
      BlockState lv2 = this.getStateWith(state, north, east, south, west, lv);
      return (BlockState)lv2.with(UP, this.shouldHavePost(lv2, aboveState, lv));
   }

   private boolean shouldHavePost(BlockState state, BlockState aboveState, VoxelShape aboveShape) {
      boolean bl = aboveState.getBlock() instanceof WallBlock && (Boolean)aboveState.get(UP);
      if (bl) {
         return true;
      } else {
         WallShape lv = (WallShape)state.get(NORTH_SHAPE);
         WallShape lv2 = (WallShape)state.get(SOUTH_SHAPE);
         WallShape lv3 = (WallShape)state.get(EAST_SHAPE);
         WallShape lv4 = (WallShape)state.get(WEST_SHAPE);
         boolean bl2 = lv2 == WallShape.NONE;
         boolean bl3 = lv4 == WallShape.NONE;
         boolean bl4 = lv3 == WallShape.NONE;
         boolean bl5 = lv == WallShape.NONE;
         boolean bl6 = bl5 && bl2 && bl3 && bl4 || bl5 != bl2 || bl3 != bl4;
         if (bl6) {
            return true;
         } else {
            boolean bl7 = lv == WallShape.TALL && lv2 == WallShape.TALL || lv3 == WallShape.TALL && lv4 == WallShape.TALL;
            if (bl7) {
               return false;
            } else {
               return aboveState.isIn(BlockTags.WALL_POST_OVERRIDE) || shouldUseTallShape(aboveShape, TALL_POST_SHAPE);
            }
         }
      }
   }

   private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
      return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, this.getWallShape(north, aboveShape, TALL_NORTH_SHAPE))).with(EAST_SHAPE, this.getWallShape(east, aboveShape, TALL_EAST_SHAPE))).with(SOUTH_SHAPE, this.getWallShape(south, aboveShape, TALL_SOUTH_SHAPE))).with(WEST_SHAPE, this.getWallShape(west, aboveShape, TALL_WEST_SHAPE));
   }

   private WallShape getWallShape(boolean connected, VoxelShape aboveShape, VoxelShape tallShape) {
      if (connected) {
         return shouldUseTallShape(aboveShape, tallShape) ? WallShape.TALL : WallShape.LOW;
      } else {
         return WallShape.NONE;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return !(Boolean)state.get(WATERLOGGED);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(UP, NORTH_SHAPE, EAST_SHAPE, WEST_SHAPE, SOUTH_SHAPE, WATERLOGGED);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, (WallShape)state.get(SOUTH_SHAPE))).with(EAST_SHAPE, (WallShape)state.get(WEST_SHAPE))).with(SOUTH_SHAPE, (WallShape)state.get(NORTH_SHAPE))).with(WEST_SHAPE, (WallShape)state.get(EAST_SHAPE));
         case COUNTERCLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, (WallShape)state.get(EAST_SHAPE))).with(EAST_SHAPE, (WallShape)state.get(SOUTH_SHAPE))).with(SOUTH_SHAPE, (WallShape)state.get(WEST_SHAPE))).with(WEST_SHAPE, (WallShape)state.get(NORTH_SHAPE));
         case CLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, (WallShape)state.get(WEST_SHAPE))).with(EAST_SHAPE, (WallShape)state.get(NORTH_SHAPE))).with(SOUTH_SHAPE, (WallShape)state.get(EAST_SHAPE))).with(WEST_SHAPE, (WallShape)state.get(SOUTH_SHAPE));
         default:
            return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return (BlockState)((BlockState)state.with(NORTH_SHAPE, (WallShape)state.get(SOUTH_SHAPE))).with(SOUTH_SHAPE, (WallShape)state.get(NORTH_SHAPE));
         case FRONT_BACK:
            return (BlockState)((BlockState)state.with(EAST_SHAPE, (WallShape)state.get(WEST_SHAPE))).with(WEST_SHAPE, (WallShape)state.get(EAST_SHAPE));
         default:
            return super.mirror(state, mirror);
      }
   }

   static {
      UP = Properties.UP;
      EAST_SHAPE = Properties.EAST_WALL_SHAPE;
      NORTH_SHAPE = Properties.NORTH_WALL_SHAPE;
      SOUTH_SHAPE = Properties.SOUTH_WALL_SHAPE;
      WEST_SHAPE = Properties.WEST_WALL_SHAPE;
      WATERLOGGED = Properties.WATERLOGGED;
      TALL_POST_SHAPE = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
      TALL_NORTH_SHAPE = Block.createCuboidShape(7.0, 0.0, 0.0, 9.0, 16.0, 9.0);
      TALL_SOUTH_SHAPE = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 16.0);
      TALL_WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 7.0, 9.0, 16.0, 9.0);
      TALL_EAST_SHAPE = Block.createCuboidShape(7.0, 0.0, 7.0, 16.0, 16.0, 9.0);
   }
}
