package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SnowBlock extends Block {
   public static final int MAX_LAYERS = 8;
   public static final IntProperty LAYERS;
   protected static final VoxelShape[] LAYERS_TO_SHAPE;
   public static final int field_31248 = 5;

   protected SnowBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LAYERS, 1));
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return (Integer)state.get(LAYERS) < 5;
         case WATER:
            return false;
         case AIR:
            return false;
         default:
            return false;
      }
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS) - 1];
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS)];
   }

   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS)];
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return (Integer)state.get(LAYERS) == 8 ? 0.2F : 1.0F;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      if (lv.isIn(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
         return false;
      } else if (lv.isIn(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
         return true;
      } else {
         return Block.isFaceFullSquare(lv.getCollisionShape(world, pos.down()), Direction.UP) || lv.isOf(this) && (Integer)lv.get(LAYERS) == 8;
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
         dropStacks(state, world, pos);
         world.removeBlock(pos, false);
      }

   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      int i = (Integer)state.get(LAYERS);
      if (context.getStack().isOf(this.asItem()) && i < 8) {
         if (context.canReplaceExisting()) {
            return context.getSide() == Direction.UP;
         } else {
            return true;
         }
      } else {
         return i == 1;
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
      if (lv.isOf(this)) {
         int i = (Integer)lv.get(LAYERS);
         return (BlockState)lv.with(LAYERS, Math.min(8, i + 1));
      } else {
         return super.getPlacementState(ctx);
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LAYERS);
   }

   static {
      LAYERS = Properties.LAYERS;
      LAYERS_TO_SHAPE = new VoxelShape[]{VoxelShapes.empty(), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)};
   }
}
