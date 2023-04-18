package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CactusBlock extends Block {
   public static final IntProperty AGE;
   public static final int MAX_AGE = 15;
   protected static final int field_31045 = 1;
   protected static final VoxelShape COLLISION_SHAPE;
   protected static final VoxelShape OUTLINE_SHAPE;

   protected CactusBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockPos lv = pos.up();
      if (world.isAir(lv)) {
         int i;
         for(i = 1; world.getBlockState(pos.down(i)).isOf(this); ++i) {
         }

         if (i < 3) {
            int j = (Integer)state.get(AGE);
            if (j == 15) {
               world.setBlockState(lv, this.getDefaultState());
               BlockState lv2 = (BlockState)state.with(AGE, 0);
               world.setBlockState(pos, lv2, Block.NO_REDRAW);
               world.updateNeighbor(lv2, lv, this, pos, false);
            } else {
               world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), Block.NO_REDRAW);
            }

         }
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return OUTLINE_SHAPE;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      Direction lv;
      Material lv3;
      do {
         if (!var4.hasNext()) {
            BlockState lv4 = world.getBlockState(pos.down());
            return (lv4.isOf(Blocks.CACTUS) || lv4.isIn(BlockTags.SAND)) && !world.getBlockState(pos.up()).isLiquid();
         }

         lv = (Direction)var4.next();
         BlockState lv2 = world.getBlockState(pos.offset(lv));
         lv3 = lv2.getMaterial();
      } while(!lv3.isSolid() && !world.getFluidState(pos.offset(lv)).isIn(FluidTags.LAVA));

      return false;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      entity.damage(world.getDamageSources().cactus(), 1.0F);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      AGE = Properties.AGE_15;
      COLLISION_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
      OUTLINE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   }
}
