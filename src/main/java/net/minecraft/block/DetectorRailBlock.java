package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DetectorRailBlock extends AbstractRailBlock {
   public static final EnumProperty SHAPE;
   public static final BooleanProperty POWERED;
   private static final int SCHEDULED_TICK_DELAY = 20;

   public DetectorRailBlock(AbstractBlock.Settings arg) {
      super(true, arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(SHAPE, RailShape.NORTH_SOUTH)).with(WATERLOGGED, false));
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         if (!(Boolean)state.get(POWERED)) {
            this.updatePoweredStatus(world, pos, state);
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         this.updatePoweredStatus(world, pos, state);
      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      if (!(Boolean)state.get(POWERED)) {
         return 0;
      } else {
         return direction == Direction.UP ? 15 : 0;
      }
   }

   private void updatePoweredStatus(World world, BlockPos pos, BlockState state) {
      if (this.canPlaceAt(state, world, pos)) {
         boolean bl = (Boolean)state.get(POWERED);
         boolean bl2 = false;
         List list = this.getCarts(world, pos, AbstractMinecartEntity.class, (entity) -> {
            return true;
         });
         if (!list.isEmpty()) {
            bl2 = true;
         }

         BlockState lv;
         if (bl2 && !bl) {
            lv = (BlockState)state.with(POWERED, true);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.updateNearbyRails(world, pos, lv, true);
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, lv);
         }

         if (!bl2 && bl) {
            lv = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.updateNearbyRails(world, pos, lv, false);
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, lv);
         }

         if (bl2) {
            world.scheduleBlockTick(pos, this, 20);
         }

         world.updateComparators(pos, this);
      }
   }

   protected void updateNearbyRails(World world, BlockPos pos, BlockState state, boolean unpowering) {
      RailPlacementHelper lv = new RailPlacementHelper(world, pos, state);
      List list = lv.getNeighbors();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         BlockPos lv2 = (BlockPos)var7.next();
         BlockState lv3 = world.getBlockState(lv2);
         world.updateNeighbor(lv3, lv2, lv3.getBlock(), pos, false);
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         BlockState lv = this.updateCurves(state, world, pos, notify);
         this.updatePoweredStatus(world, pos, lv);
      }
   }

   public Property getShapeProperty() {
      return SHAPE;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      if ((Boolean)state.get(POWERED)) {
         List list = this.getCarts(world, pos, CommandBlockMinecartEntity.class, (cart) -> {
            return true;
         });
         if (!list.isEmpty()) {
            return ((CommandBlockMinecartEntity)list.get(0)).getCommandExecutor().getSuccessCount();
         }

         List list2 = this.getCarts(world, pos, AbstractMinecartEntity.class, EntityPredicates.VALID_INVENTORIES);
         if (!list2.isEmpty()) {
            return ScreenHandler.calculateComparatorOutput((Inventory)list2.get(0));
         }
      }

      return 0;
   }

   private List getCarts(World world, BlockPos pos, Class entityClass, Predicate entityPredicate) {
      return world.getEntitiesByClass(entityClass, this.getCartDetectionBox(pos), entityPredicate);
   }

   private Box getCartDetectionBox(BlockPos pos) {
      double d = 0.2;
      return new Box((double)pos.getX() + 0.2, (double)pos.getY(), (double)pos.getZ() + 0.2, (double)(pos.getX() + 1) - 0.2, (double)(pos.getY() + 1) - 0.2, (double)(pos.getZ() + 1) - 0.2);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
            }
         case COUNTERCLOCKWISE_90:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
            }
         case CLOCKWISE_90:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
            }
         default:
            return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      RailShape lv = (RailShape)state.get(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            switch (lv) {
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               default:
                  return super.mirror(state, mirror);
            }
         case FRONT_BACK:
            switch (lv) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               default:
                  break;
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
            }
      }

      return super.mirror(state, mirror);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(SHAPE, POWERED, WATERLOGGED);
   }

   static {
      SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
      POWERED = Properties.POWERED;
   }
}
