package net.minecraft.block;

import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class LeverBlock extends WallMountedBlock {
   public static final BooleanProperty POWERED;
   protected static final int field_31184 = 6;
   protected static final int field_31185 = 6;
   protected static final int field_31186 = 8;
   protected static final VoxelShape NORTH_WALL_SHAPE;
   protected static final VoxelShape SOUTH_WALL_SHAPE;
   protected static final VoxelShape WEST_WALL_SHAPE;
   protected static final VoxelShape EAST_WALL_SHAPE;
   protected static final VoxelShape FLOOR_Z_AXIS_SHAPE;
   protected static final VoxelShape FLOOR_X_AXIS_SHAPE;
   protected static final VoxelShape CEILING_Z_AXIS_SHAPE;
   protected static final VoxelShape CEILING_X_AXIS_SHAPE;

   protected LeverBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(FACE, WallMountLocation.WALL));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((WallMountLocation)state.get(FACE)) {
         case FLOOR:
            switch (((Direction)state.get(FACING)).getAxis()) {
               case X:
                  return FLOOR_X_AXIS_SHAPE;
               case Z:
               default:
                  return FLOOR_Z_AXIS_SHAPE;
            }
         case WALL:
            switch ((Direction)state.get(FACING)) {
               case EAST:
                  return EAST_WALL_SHAPE;
               case WEST:
                  return WEST_WALL_SHAPE;
               case SOUTH:
                  return SOUTH_WALL_SHAPE;
               case NORTH:
               default:
                  return NORTH_WALL_SHAPE;
            }
         case CEILING:
         default:
            switch (((Direction)state.get(FACING)).getAxis()) {
               case X:
                  return CEILING_X_AXIS_SHAPE;
               case Z:
               default:
                  return CEILING_Z_AXIS_SHAPE;
            }
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockState lv;
      if (world.isClient) {
         lv = (BlockState)state.cycle(POWERED);
         if ((Boolean)lv.get(POWERED)) {
            spawnParticles(lv, world, pos, 1.0F);
         }

         return ActionResult.SUCCESS;
      } else {
         lv = this.togglePower(state, world, pos);
         float f = (Boolean)lv.get(POWERED) ? 0.6F : 0.5F;
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
         world.emitGameEvent(player, (Boolean)lv.get(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
         return ActionResult.CONSUME;
      }
   }

   public BlockState togglePower(BlockState state, World world, BlockPos pos) {
      state = (BlockState)state.cycle(POWERED);
      world.setBlockState(pos, state, Block.NOTIFY_ALL);
      this.updateNeighbors(state, world, pos);
      return state;
   }

   private static void spawnParticles(BlockState state, WorldAccess world, BlockPos pos, float alpha) {
      Direction lv = ((Direction)state.get(FACING)).getOpposite();
      Direction lv2 = getDirection(state).getOpposite();
      double d = (double)pos.getX() + 0.5 + 0.1 * (double)lv.getOffsetX() + 0.2 * (double)lv2.getOffsetX();
      double e = (double)pos.getY() + 0.5 + 0.1 * (double)lv.getOffsetY() + 0.2 * (double)lv2.getOffsetY();
      double g = (double)pos.getZ() + 0.5 + 0.1 * (double)lv.getOffsetZ() + 0.2 * (double)lv2.getOffsetZ();
      world.addParticle(new DustParticleEffect(DustParticleEffect.RED, alpha), d, e, g, 0.0, 0.0, 0.0);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED) && random.nextFloat() < 0.25F) {
         spawnParticles(state, world, pos, 0.5F);
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         if ((Boolean)state.get(POWERED)) {
            this.updateNeighbors(state, world, pos);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) && getDirection(state) == direction ? 15 : 0;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   private void updateNeighbors(BlockState state, World world, BlockPos pos) {
      world.updateNeighborsAlways(pos, this);
      world.updateNeighborsAlways(pos.offset(getDirection(state).getOpposite()), this);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACE, FACING, POWERED);
   }

   static {
      POWERED = Properties.POWERED;
      NORTH_WALL_SHAPE = Block.createCuboidShape(5.0, 4.0, 10.0, 11.0, 12.0, 16.0);
      SOUTH_WALL_SHAPE = Block.createCuboidShape(5.0, 4.0, 0.0, 11.0, 12.0, 6.0);
      WEST_WALL_SHAPE = Block.createCuboidShape(10.0, 4.0, 5.0, 16.0, 12.0, 11.0);
      EAST_WALL_SHAPE = Block.createCuboidShape(0.0, 4.0, 5.0, 6.0, 12.0, 11.0);
      FLOOR_Z_AXIS_SHAPE = Block.createCuboidShape(5.0, 0.0, 4.0, 11.0, 6.0, 12.0);
      FLOOR_X_AXIS_SHAPE = Block.createCuboidShape(4.0, 0.0, 5.0, 12.0, 6.0, 11.0);
      CEILING_Z_AXIS_SHAPE = Block.createCuboidShape(5.0, 10.0, 4.0, 11.0, 16.0, 12.0);
      CEILING_X_AXIS_SHAPE = Block.createCuboidShape(4.0, 10.0, 5.0, 12.0, 16.0, 11.0);
   }
}
