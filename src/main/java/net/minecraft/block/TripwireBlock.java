package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class TripwireBlock extends Block {
   public static final BooleanProperty POWERED;
   public static final BooleanProperty ATTACHED;
   public static final BooleanProperty DISARMED;
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   private static final Map FACING_PROPERTIES;
   protected static final VoxelShape ATTACHED_SHAPE;
   protected static final VoxelShape DETACHED_SHAPE;
   private static final int SCHEDULED_TICK_DELAY = 10;
   private final TripwireHookBlock hookBlock;

   public TripwireBlock(TripwireHookBlock hookBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(ATTACHED, false)).with(DISARMED, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
      this.hookBlock = hookBlock;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (Boolean)state.get(ATTACHED) ? ATTACHED_SHAPE : DETACHED_SHAPE;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockView lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.shouldConnectTo(lv.getBlockState(lv2.north()), Direction.NORTH))).with(EAST, this.shouldConnectTo(lv.getBlockState(lv2.east()), Direction.EAST))).with(SOUTH, this.shouldConnectTo(lv.getBlockState(lv2.south()), Direction.SOUTH))).with(WEST, this.shouldConnectTo(lv.getBlockState(lv2.west()), Direction.WEST));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction.getAxis().isHorizontal() ? (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), this.shouldConnectTo(neighborState, direction)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.update(world, pos, state);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         this.update(world, pos, (BlockState)state.with(POWERED, true));
      }
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && !player.getMainHandStack().isEmpty() && player.getMainHandStack().isOf(Items.SHEARS)) {
         world.setBlockState(pos, (BlockState)state.with(DISARMED, true), Block.NO_REDRAW);
         world.emitGameEvent(player, GameEvent.SHEAR, pos);
      }

      super.onBreak(world, pos, state, player);
   }

   private void update(World world, BlockPos pos, BlockState state) {
      Direction[] var4 = new Direction[]{Direction.SOUTH, Direction.WEST};
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction lv = var4[var6];

         for(int i = 1; i < 42; ++i) {
            BlockPos lv2 = pos.offset(lv, i);
            BlockState lv3 = world.getBlockState(lv2);
            if (lv3.isOf(this.hookBlock)) {
               if (lv3.get(TripwireHookBlock.FACING) == lv.getOpposite()) {
                  this.hookBlock.update(world, lv2, lv3, false, true, i, state);
               }
               break;
            }

            if (!lv3.isOf(this)) {
               break;
            }
         }
      }

   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         if (!(Boolean)state.get(POWERED)) {
            this.updatePowered(world, pos);
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)world.getBlockState(pos).get(POWERED)) {
         this.updatePowered(world, pos);
      }
   }

   private void updatePowered(World world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      boolean bl = (Boolean)lv.get(POWERED);
      boolean bl2 = false;
      List list = world.getOtherEntities((Entity)null, lv.getOutlineShape(world, pos).getBoundingBox().offset(pos));
      if (!list.isEmpty()) {
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            Entity lv2 = (Entity)var7.next();
            if (!lv2.canAvoidTraps()) {
               bl2 = true;
               break;
            }
         }
      }

      if (bl2 != bl) {
         lv = (BlockState)lv.with(POWERED, bl2);
         world.setBlockState(pos, lv, Block.NOTIFY_ALL);
         this.update(world, pos, lv);
      }

      if (bl2) {
         world.scheduleBlockTick(new BlockPos(pos), this, 10);
      }

   }

   public boolean shouldConnectTo(BlockState state, Direction facing) {
      if (state.isOf(this.hookBlock)) {
         return state.get(TripwireHookBlock.FACING) == facing.getOpposite();
      } else {
         return state.isOf(this);
      }
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

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
   }

   static {
      POWERED = Properties.POWERED;
      ATTACHED = Properties.ATTACHED;
      DISARMED = Properties.DISARMED;
      NORTH = ConnectingBlock.NORTH;
      EAST = ConnectingBlock.EAST;
      SOUTH = ConnectingBlock.SOUTH;
      WEST = ConnectingBlock.WEST;
      FACING_PROPERTIES = HorizontalConnectingBlock.FACING_PROPERTIES;
      ATTACHED_SHAPE = Block.createCuboidShape(0.0, 1.0, 0.0, 16.0, 2.5, 16.0);
      DETACHED_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   }
}
