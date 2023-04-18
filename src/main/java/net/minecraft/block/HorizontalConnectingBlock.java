package net.minecraft.block;

import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class HorizontalConnectingBlock extends Block implements Waterloggable {
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   public static final BooleanProperty WATERLOGGED;
   protected static final Map FACING_PROPERTIES;
   protected final VoxelShape[] collisionShapes;
   protected final VoxelShape[] boundingShapes;
   private final Object2IntMap SHAPE_INDEX_CACHE = new Object2IntOpenHashMap();

   protected HorizontalConnectingBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, AbstractBlock.Settings settings) {
      super(settings);
      this.collisionShapes = this.createShapes(radius1, radius2, collisionHeight, 0.0F, collisionHeight);
      this.boundingShapes = this.createShapes(radius1, radius2, boundingHeight1, 0.0F, boundingHeight2);
      UnmodifiableIterator var7 = this.stateManager.getStates().iterator();

      while(var7.hasNext()) {
         BlockState lv = (BlockState)var7.next();
         this.getShapeIndex(lv);
      }

   }

   protected VoxelShape[] createShapes(float radius1, float radius2, float height1, float offset2, float height2) {
      float k = 8.0F - radius1;
      float l = 8.0F + radius1;
      float m = 8.0F - radius2;
      float n = 8.0F + radius2;
      VoxelShape lv = Block.createCuboidShape((double)k, 0.0, (double)k, (double)l, (double)height1, (double)l);
      VoxelShape lv2 = Block.createCuboidShape((double)m, (double)offset2, 0.0, (double)n, (double)height2, (double)n);
      VoxelShape lv3 = Block.createCuboidShape((double)m, (double)offset2, (double)m, (double)n, (double)height2, 16.0);
      VoxelShape lv4 = Block.createCuboidShape(0.0, (double)offset2, (double)m, (double)n, (double)height2, (double)n);
      VoxelShape lv5 = Block.createCuboidShape((double)m, (double)offset2, (double)m, 16.0, (double)height2, (double)n);
      VoxelShape lv6 = VoxelShapes.union(lv2, lv5);
      VoxelShape lv7 = VoxelShapes.union(lv3, lv4);
      VoxelShape[] lvs = new VoxelShape[]{VoxelShapes.empty(), lv3, lv4, lv7, lv2, VoxelShapes.union(lv3, lv2), VoxelShapes.union(lv4, lv2), VoxelShapes.union(lv7, lv2), lv5, VoxelShapes.union(lv3, lv5), VoxelShapes.union(lv4, lv5), VoxelShapes.union(lv7, lv5), lv6, VoxelShapes.union(lv3, lv6), VoxelShapes.union(lv4, lv6), VoxelShapes.union(lv7, lv6)};

      for(int o = 0; o < 16; ++o) {
         lvs[o] = VoxelShapes.union(lv, lvs[o]);
      }

      return lvs;
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return !(Boolean)state.get(WATERLOGGED);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.boundingShapes[this.getShapeIndex(state)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.collisionShapes[this.getShapeIndex(state)];
   }

   private static int getDirectionMask(Direction dir) {
      return 1 << dir.getHorizontal();
   }

   protected int getShapeIndex(BlockState state) {
      return this.SHAPE_INDEX_CACHE.computeIntIfAbsent(state, (statex) -> {
         int i = 0;
         if ((Boolean)statex.get(NORTH)) {
            i |= getDirectionMask(Direction.NORTH);
         }

         if ((Boolean)statex.get(EAST)) {
            i |= getDirectionMask(Direction.EAST);
         }

         if ((Boolean)statex.get(SOUTH)) {
            i |= getDirectionMask(Direction.SOUTH);
         }

         if ((Boolean)statex.get(WEST)) {
            i |= getDirectionMask(Direction.WEST);
         }

         return i;
      });
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(SOUTH))).with(EAST, (Boolean)state.get(WEST))).with(SOUTH, (Boolean)state.get(NORTH))).with(WEST, (Boolean)state.get(EAST));
         case COUNTERCLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(EAST))).with(EAST, (Boolean)state.get(SOUTH))).with(SOUTH, (Boolean)state.get(WEST))).with(WEST, (Boolean)state.get(NORTH));
         case CLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(WEST))).with(EAST, (Boolean)state.get(NORTH))).with(SOUTH, (Boolean)state.get(EAST))).with(WEST, (Boolean)state.get(SOUTH));
         default:
            return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return (BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(SOUTH))).with(SOUTH, (Boolean)state.get(NORTH));
         case FRONT_BACK:
            return (BlockState)((BlockState)state.with(EAST, (Boolean)state.get(WEST))).with(WEST, (Boolean)state.get(EAST));
         default:
            return super.mirror(state, mirror);
      }
   }

   static {
      NORTH = ConnectingBlock.NORTH;
      EAST = ConnectingBlock.EAST;
      SOUTH = ConnectingBlock.SOUTH;
      WEST = ConnectingBlock.WEST;
      WATERLOGGED = Properties.WATERLOGGED;
      FACING_PROPERTIES = (Map)ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter((entry) -> {
         return ((Direction)entry.getKey()).getAxis().isHorizontal();
      }).collect(Util.toMap());
   }
}
