package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class DispenserBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty TRIGGERED;
   private static final Map BEHAVIORS;
   private static final int SCHEDULED_TICK_DELAY = 4;

   public static void registerBehavior(ItemConvertible provider, DispenserBehavior behavior) {
      BEHAVIORS.put(provider.asItem(), behavior);
   }

   protected DispenserBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(TRIGGERED, false));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof DispenserBlockEntity) {
            player.openHandledScreen((DispenserBlockEntity)lv);
            if (lv instanceof DropperBlockEntity) {
               player.incrementStat(Stats.INSPECT_DROPPER);
            } else {
               player.incrementStat(Stats.INSPECT_DISPENSER);
            }
         }

         return ActionResult.CONSUME;
      }
   }

   protected void dispense(ServerWorld world, BlockPos pos) {
      BlockPointerImpl lv = new BlockPointerImpl(world, pos);
      DispenserBlockEntity lv2 = (DispenserBlockEntity)lv.getBlockEntity();
      int i = lv2.chooseNonEmptySlot(world.random);
      if (i < 0) {
         world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
         world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(lv2.getCachedState()));
      } else {
         ItemStack lv3 = lv2.getStack(i);
         DispenserBehavior lv4 = this.getBehaviorForItem(lv3);
         if (lv4 != DispenserBehavior.NOOP) {
            lv2.setStack(i, lv4.dispense(lv, lv3));
         }

      }
   }

   protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
      return (DispenserBehavior)BEHAVIORS.get(stack.getItem());
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      boolean bl2 = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
      boolean bl3 = (Boolean)state.get(TRIGGERED);
      if (bl2 && !bl3) {
         world.scheduleBlockTick(pos, this, 4);
         world.setBlockState(pos, (BlockState)state.with(TRIGGERED, true), Block.NO_REDRAW);
      } else if (!bl2 && bl3) {
         world.setBlockState(pos, (BlockState)state.with(TRIGGERED, false), Block.NO_REDRAW);
      }

   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.dispense(world, pos);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new DispenserBlockEntity(pos, state);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)lv).setCustomName(itemStack.getName());
         }
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof DispenserBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((DispenserBlockEntity)lv));
            world.updateComparators(pos, this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public static Position getOutputLocation(BlockPointer pointer) {
      Direction lv = (Direction)pointer.getBlockState().get(FACING);
      double d = pointer.getX() + 0.7 * (double)lv.getOffsetX();
      double e = pointer.getY() + 0.7 * (double)lv.getOffsetY();
      double f = pointer.getZ() + 0.7 * (double)lv.getOffsetZ();
      return new PositionImpl(d, e, f);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, TRIGGERED);
   }

   static {
      FACING = FacingBlock.FACING;
      TRIGGERED = Properties.TRIGGERED;
      BEHAVIORS = (Map)Util.make(new Object2ObjectOpenHashMap(), (map) -> {
         map.defaultReturnValue(new ItemDispenserBehavior());
      });
   }
}
