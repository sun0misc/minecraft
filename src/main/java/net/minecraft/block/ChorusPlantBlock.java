package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ChorusPlantBlock extends ConnectingBlock {
   protected ChorusPlantBlock(AbstractBlock.Settings arg) {
      super(0.3125F, arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(UP, false)).with(DOWN, false));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.withConnectionProperties(ctx.getWorld(), ctx.getBlockPos());
   }

   public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      BlockState lv2 = world.getBlockState(pos.up());
      BlockState lv3 = world.getBlockState(pos.north());
      BlockState lv4 = world.getBlockState(pos.east());
      BlockState lv5 = world.getBlockState(pos.south());
      BlockState lv6 = world.getBlockState(pos.west());
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(DOWN, lv.isOf(this) || lv.isOf(Blocks.CHORUS_FLOWER) || lv.isOf(Blocks.END_STONE))).with(UP, lv2.isOf(this) || lv2.isOf(Blocks.CHORUS_FLOWER))).with(NORTH, lv3.isOf(this) || lv3.isOf(Blocks.CHORUS_FLOWER))).with(EAST, lv4.isOf(this) || lv4.isOf(Blocks.CHORUS_FLOWER))).with(SOUTH, lv5.isOf(this) || lv5.isOf(Blocks.CHORUS_FLOWER))).with(WEST, lv6.isOf(this) || lv6.isOf(Blocks.CHORUS_FLOWER));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         boolean bl = neighborState.isOf(this) || neighborState.isOf(Blocks.CHORUS_FLOWER) || direction == Direction.DOWN && neighborState.isOf(Blocks.END_STONE);
         return (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), bl);
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      boolean bl = !world.getBlockState(pos.up()).isAir() && !lv.isAir();
      Iterator var6 = Direction.Type.HORIZONTAL.iterator();

      BlockState lv5;
      do {
         BlockPos lv3;
         BlockState lv4;
         do {
            if (!var6.hasNext()) {
               return lv.isOf(this) || lv.isOf(Blocks.END_STONE);
            }

            Direction lv2 = (Direction)var6.next();
            lv3 = pos.offset(lv2);
            lv4 = world.getBlockState(lv3);
         } while(!lv4.isOf(this));

         if (bl) {
            return false;
         }

         lv5 = world.getBlockState(lv3.down());
      } while(!lv5.isOf(this) && !lv5.isOf(Blocks.END_STONE));

      return true;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }
}
