package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SugarCaneBlock extends Block {
   public static final IntProperty AGE;
   protected static final float field_31258 = 6.0F;
   protected static final VoxelShape SHAPE;

   protected SugarCaneBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.isAir(pos.up())) {
         int i;
         for(i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) {
         }

         if (i < 3) {
            int j = (Integer)state.get(AGE);
            if (j == 15) {
               world.setBlockState(pos.up(), this.getDefaultState());
               world.setBlockState(pos, (BlockState)state.with(AGE, 0), Block.NO_REDRAW);
            } else {
               world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), Block.NO_REDRAW);
            }
         }
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      if (lv.isOf(this)) {
         return true;
      } else {
         if (lv.isIn(BlockTags.DIRT) || lv.isIn(BlockTags.SAND)) {
            BlockPos lv2 = pos.down();
            Iterator var6 = Direction.Type.HORIZONTAL.iterator();

            while(var6.hasNext()) {
               Direction lv3 = (Direction)var6.next();
               BlockState lv4 = world.getBlockState(lv2.offset(lv3));
               FluidState lv5 = world.getFluidState(lv2.offset(lv3));
               if (lv5.isIn(FluidTags.WATER) || lv4.isOf(Blocks.FROSTED_ICE)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   static {
      AGE = Properties.AGE_15;
      SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   }
}
