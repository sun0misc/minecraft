package net.minecraft.block;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TripwireHookBlock extends Block {
   public static final DirectionProperty FACING;
   public static final BooleanProperty POWERED;
   public static final BooleanProperty ATTACHED;
   protected static final int field_31268 = 1;
   protected static final int field_31269 = 42;
   private static final int SCHEDULED_TICK_DELAY = 10;
   protected static final int field_31270 = 3;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   protected static final VoxelShape WEST_SHAPE;

   public TripwireHookBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(ATTACHED, false));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction)state.get(FACING)) {
         case EAST:
         default:
            return WEST_SHAPE;
         case WEST:
            return EAST_SHAPE;
         case SOUTH:
            return NORTH_SHAPE;
         case NORTH:
            return SOUTH_SHAPE;
      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = (Direction)state.get(FACING);
      BlockPos lv2 = pos.offset(lv.getOpposite());
      BlockState lv3 = world.getBlockState(lv2);
      return lv.getAxis().isHorizontal() && lv3.isSideSolidFullSquare(world, lv2, lv);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = (BlockState)((BlockState)this.getDefaultState().with(POWERED, false)).with(ATTACHED, false);
      WorldView lv2 = ctx.getWorld();
      BlockPos lv3 = ctx.getBlockPos();
      Direction[] lvs = ctx.getPlacementDirections();
      Direction[] var6 = lvs;
      int var7 = lvs.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv4 = var6[var8];
         if (lv4.getAxis().isHorizontal()) {
            Direction lv5 = lv4.getOpposite();
            lv = (BlockState)lv.with(FACING, lv5);
            if (lv.canPlaceAt(lv2, lv3)) {
               return lv;
            }
         }
      }

      return null;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      this.update(world, pos, state, false, false, -1, (BlockState)null);
   }

   public void update(World world, BlockPos pos, BlockState state, boolean beingRemoved, boolean bl2, int i, @Nullable BlockState arg4) {
      Direction lv = (Direction)state.get(FACING);
      boolean bl3 = (Boolean)state.get(ATTACHED);
      boolean bl4 = (Boolean)state.get(POWERED);
      boolean bl5 = !beingRemoved;
      boolean bl6 = false;
      int j = 0;
      BlockState[] lvs = new BlockState[42];

      BlockPos lv2;
      for(int k = 1; k < 42; ++k) {
         lv2 = pos.offset(lv, k);
         BlockState lv3 = world.getBlockState(lv2);
         if (lv3.isOf(Blocks.TRIPWIRE_HOOK)) {
            if (lv3.get(FACING) == lv.getOpposite()) {
               j = k;
            }
            break;
         }

         if (!lv3.isOf(Blocks.TRIPWIRE) && k != i) {
            lvs[k] = null;
            bl5 = false;
         } else {
            if (k == i) {
               lv3 = (BlockState)MoreObjects.firstNonNull(arg4, lv3);
            }

            boolean bl7 = !(Boolean)lv3.get(TripwireBlock.DISARMED);
            boolean bl8 = (Boolean)lv3.get(TripwireBlock.POWERED);
            bl6 |= bl7 && bl8;
            lvs[k] = lv3;
            if (k == i) {
               world.scheduleBlockTick(pos, this, 10);
               bl5 &= bl7;
            }
         }
      }

      bl5 &= j > 1;
      bl6 &= bl5;
      BlockState lv4 = (BlockState)((BlockState)this.getDefaultState().with(ATTACHED, bl5)).with(POWERED, bl6);
      if (j > 0) {
         lv2 = pos.offset(lv, j);
         Direction lv5 = lv.getOpposite();
         world.setBlockState(lv2, (BlockState)lv4.with(FACING, lv5), Block.NOTIFY_ALL);
         this.updateNeighborsOnAxis(world, lv2, lv5);
         this.playSound(world, lv2, bl5, bl6, bl3, bl4);
      }

      this.playSound(world, pos, bl5, bl6, bl3, bl4);
      if (!beingRemoved) {
         world.setBlockState(pos, (BlockState)lv4.with(FACING, lv), Block.NOTIFY_ALL);
         if (bl2) {
            this.updateNeighborsOnAxis(world, pos, lv);
         }
      }

      if (bl3 != bl5) {
         for(int l = 1; l < j; ++l) {
            BlockPos lv6 = pos.offset(lv, l);
            BlockState lv7 = lvs[l];
            if (lv7 != null) {
               world.setBlockState(lv6, (BlockState)lv7.with(ATTACHED, bl5), Block.NOTIFY_ALL);
               if (!world.getBlockState(lv6).isAir()) {
               }
            }
         }
      }

   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.update(world, pos, state, false, true, -1, (BlockState)null);
   }

   private void playSound(World world, BlockPos pos, boolean attached, boolean on, boolean detached, boolean off) {
      if (on && !off) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
         world.emitGameEvent((Entity)null, GameEvent.BLOCK_ACTIVATE, pos);
      } else if (!on && off) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
         world.emitGameEvent((Entity)null, GameEvent.BLOCK_DEACTIVATE, pos);
      } else if (attached && !detached) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
         world.emitGameEvent((Entity)null, GameEvent.BLOCK_ATTACH, pos);
      } else if (!attached && detached) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (world.random.nextFloat() * 0.2F + 0.9F));
         world.emitGameEvent((Entity)null, GameEvent.BLOCK_DETACH, pos);
      }

   }

   private void updateNeighborsOnAxis(World world, BlockPos pos, Direction direction) {
      world.updateNeighborsAlways(pos, this);
      world.updateNeighborsAlways(pos.offset(direction.getOpposite()), this);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         boolean bl2 = (Boolean)state.get(ATTACHED);
         boolean bl3 = (Boolean)state.get(POWERED);
         if (bl2 || bl3) {
            this.update(world, pos, state, true, false, -1, (BlockState)null);
         }

         if (bl3) {
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.offset(((Direction)state.get(FACING)).getOpposite()), this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      if (!(Boolean)state.get(POWERED)) {
         return 0;
      } else {
         return state.get(FACING) == direction ? 15 : 0;
      }
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, POWERED, ATTACHED);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      POWERED = Properties.POWERED;
      ATTACHED = Properties.ATTACHED;
      SOUTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
      NORTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
      EAST_SHAPE = Block.createCuboidShape(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
      WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);
   }
}
