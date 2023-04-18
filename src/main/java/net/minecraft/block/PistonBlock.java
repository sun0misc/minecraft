package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PistonBlock extends FacingBlock {
   public static final BooleanProperty EXTENDED;
   public static final int field_31373 = 0;
   public static final int field_31374 = 1;
   public static final int field_31375 = 2;
   public static final float field_31376 = 4.0F;
   protected static final VoxelShape EXTENDED_EAST_SHAPE;
   protected static final VoxelShape EXTENDED_WEST_SHAPE;
   protected static final VoxelShape EXTENDED_SOUTH_SHAPE;
   protected static final VoxelShape EXTENDED_NORTH_SHAPE;
   protected static final VoxelShape EXTENDED_UP_SHAPE;
   protected static final VoxelShape EXTENDED_DOWN_SHAPE;
   private final boolean sticky;

   public PistonBlock(boolean sticky, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(EXTENDED, false));
      this.sticky = sticky;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if ((Boolean)state.get(EXTENDED)) {
         switch ((Direction)state.get(FACING)) {
            case DOWN:
               return EXTENDED_DOWN_SHAPE;
            case UP:
            default:
               return EXTENDED_UP_SHAPE;
            case NORTH:
               return EXTENDED_NORTH_SHAPE;
            case SOUTH:
               return EXTENDED_SOUTH_SHAPE;
            case WEST:
               return EXTENDED_WEST_SHAPE;
            case EAST:
               return EXTENDED_EAST_SHAPE;
         }
      } else {
         return VoxelShapes.fullCube();
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (!world.isClient) {
         this.tryMove(world, pos, state);
      }

   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (!world.isClient) {
         this.tryMove(world, pos, state);
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         if (!world.isClient && world.getBlockEntity(pos) == null) {
            this.tryMove(world, pos, state);
         }

      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite())).with(EXTENDED, false);
   }

   private void tryMove(World world, BlockPos pos, BlockState state) {
      Direction lv = (Direction)state.get(FACING);
      boolean bl = this.shouldExtend(world, pos, lv);
      if (bl && !(Boolean)state.get(EXTENDED)) {
         if ((new PistonHandler(world, pos, lv, true)).calculatePush()) {
            world.addSyncedBlockEvent(pos, this, 0, lv.getId());
         }
      } else if (!bl && (Boolean)state.get(EXTENDED)) {
         BlockPos lv2 = pos.offset((Direction)lv, 2);
         BlockState lv3 = world.getBlockState(lv2);
         int i = 1;
         if (lv3.isOf(Blocks.MOVING_PISTON) && lv3.get(FACING) == lv) {
            BlockEntity lv4 = world.getBlockEntity(lv2);
            if (lv4 instanceof PistonBlockEntity) {
               PistonBlockEntity lv5 = (PistonBlockEntity)lv4;
               if (lv5.isExtending() && (lv5.getProgress(0.0F) < 0.5F || world.getTime() == lv5.getSavedWorldTime() || ((ServerWorld)world).isInBlockTick())) {
                  i = 2;
               }
            }
         }

         world.addSyncedBlockEvent(pos, this, i, lv.getId());
      }

   }

   private boolean shouldExtend(RedstoneView world, BlockPos pos, Direction pistonFace) {
      Direction[] var4 = Direction.values();
      int var5 = var4.length;

      int var6;
      for(var6 = 0; var6 < var5; ++var6) {
         Direction lv = var4[var6];
         if (lv != pistonFace && world.isEmittingRedstonePower(pos.offset(lv), lv)) {
            return true;
         }
      }

      if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos lv2 = pos.up();
         Direction[] var10 = Direction.values();
         var6 = var10.length;

         for(int var11 = 0; var11 < var6; ++var11) {
            Direction lv3 = var10[var11];
            if (lv3 != Direction.DOWN && world.isEmittingRedstonePower(lv2.offset(lv3), lv3)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      Direction lv = (Direction)state.get(FACING);
      BlockState lv2 = (BlockState)state.with(EXTENDED, true);
      if (!world.isClient) {
         boolean bl = this.shouldExtend(world, pos, lv);
         if (bl && (type == 1 || type == 2)) {
            world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
            return false;
         }

         if (!bl && type == 0) {
            return false;
         }
      }

      if (type == 0) {
         if (!this.move(world, pos, lv, true)) {
            return false;
         }

         world.setBlockState(pos, lv2, Block.NOTIFY_ALL | Block.MOVED);
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
         world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(lv2));
      } else if (type == 1 || type == 2) {
         BlockEntity lv3 = world.getBlockEntity(pos.offset(lv));
         if (lv3 instanceof PistonBlockEntity) {
            ((PistonBlockEntity)lv3).finish();
         }

         BlockState lv4 = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, lv)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
         world.setBlockState(pos, lv4, Block.NO_REDRAW | Block.FORCE_STATE);
         world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(pos, lv4, (BlockState)this.getDefaultState().with(FACING, Direction.byId(data & 7)), lv, false, true));
         world.updateNeighbors(pos, lv4.getBlock());
         lv4.updateNeighbors(world, pos, Block.NOTIFY_LISTENERS);
         if (this.sticky) {
            BlockPos lv5 = pos.add(lv.getOffsetX() * 2, lv.getOffsetY() * 2, lv.getOffsetZ() * 2);
            BlockState lv6 = world.getBlockState(lv5);
            boolean bl2 = false;
            if (lv6.isOf(Blocks.MOVING_PISTON)) {
               BlockEntity lv7 = world.getBlockEntity(lv5);
               if (lv7 instanceof PistonBlockEntity) {
                  PistonBlockEntity lv8 = (PistonBlockEntity)lv7;
                  if (lv8.getFacing() == lv && lv8.isExtending()) {
                     lv8.finish();
                     bl2 = true;
                  }
               }
            }

            if (!bl2) {
               if (type != 1 || lv6.isAir() || !isMovable(lv6, world, lv5, lv.getOpposite(), false, lv) || lv6.getPistonBehavior() != PistonBehavior.NORMAL && !lv6.isOf(Blocks.PISTON) && !lv6.isOf(Blocks.STICKY_PISTON)) {
                  world.removeBlock(pos.offset(lv), false);
               } else {
                  this.move(world, pos, lv, false);
               }
            }
         } else {
            world.removeBlock(pos.offset(lv), false);
         }

         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
         world.emitGameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Emitter.of(lv4));
      }

      return true;
   }

   public static boolean isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir) {
      if (pos.getY() >= world.getBottomY() && pos.getY() <= world.getTopY() - 1 && world.getWorldBorder().contains(pos)) {
         if (state.isAir()) {
            return true;
         } else if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.CRYING_OBSIDIAN) && !state.isOf(Blocks.RESPAWN_ANCHOR) && !state.isOf(Blocks.REINFORCED_DEEPSLATE)) {
            if (direction == Direction.DOWN && pos.getY() == world.getBottomY()) {
               return false;
            } else if (direction == Direction.UP && pos.getY() == world.getTopY() - 1) {
               return false;
            } else {
               if (!state.isOf(Blocks.PISTON) && !state.isOf(Blocks.STICKY_PISTON)) {
                  if (state.getHardness(world, pos) == -1.0F) {
                     return false;
                  }

                  switch (state.getPistonBehavior()) {
                     case BLOCK:
                        return false;
                     case DESTROY:
                        return canBreak;
                     case PUSH_ONLY:
                        return direction == pistonDir;
                  }
               } else if ((Boolean)state.get(EXTENDED)) {
                  return false;
               }

               return !state.hasBlockEntity();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
      BlockPos lv = pos.offset(dir);
      if (!retract && world.getBlockState(lv).isOf(Blocks.PISTON_HEAD)) {
         world.setBlockState(lv, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
      }

      PistonHandler lv2 = new PistonHandler(world, pos, dir, retract);
      if (!lv2.calculatePush()) {
         return false;
      } else {
         Map map = Maps.newHashMap();
         List list = lv2.getMovedBlocks();
         List list2 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos lv3 = (BlockPos)list.get(i);
            BlockState lv4 = world.getBlockState(lv3);
            list2.add(lv4);
            map.put(lv3, lv4);
         }

         List list3 = lv2.getBrokenBlocks();
         BlockState[] lvs = new BlockState[list.size() + list3.size()];
         Direction lv5 = retract ? dir : dir.getOpposite();
         int j = 0;

         int k;
         BlockPos lv6;
         BlockState lv7;
         for(k = list3.size() - 1; k >= 0; --k) {
            lv6 = (BlockPos)list3.get(k);
            lv7 = world.getBlockState(lv6);
            BlockEntity lv8 = lv7.hasBlockEntity() ? world.getBlockEntity(lv6) : null;
            dropStacks(lv7, world, lv6, lv8);
            world.setBlockState(lv6, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, lv6, GameEvent.Emitter.of(lv7));
            if (!lv7.isIn(BlockTags.FIRE)) {
               world.addBlockBreakParticles(lv6, lv7);
            }

            lvs[j++] = lv7;
         }

         for(k = list.size() - 1; k >= 0; --k) {
            lv6 = (BlockPos)list.get(k);
            lv7 = world.getBlockState(lv6);
            lv6 = lv6.offset(lv5);
            map.remove(lv6);
            BlockState lv9 = (BlockState)Blocks.MOVING_PISTON.getDefaultState().with(FACING, dir);
            world.setBlockState(lv6, lv9, Block.NO_REDRAW | Block.MOVED);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(lv6, lv9, (BlockState)list2.get(k), dir, retract, false));
            lvs[j++] = lv7;
         }

         if (retract) {
            PistonType lv10 = this.sticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState lv11 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.FACING, dir)).with(PistonHeadBlock.TYPE, lv10);
            lv7 = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, dir)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(lv);
            world.setBlockState(lv, lv7, Block.NO_REDRAW | Block.MOVED);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(lv, lv7, lv11, dir, true, true));
         }

         BlockState lv12 = Blocks.AIR.getDefaultState();
         Iterator var25 = map.keySet().iterator();

         while(var25.hasNext()) {
            BlockPos lv13 = (BlockPos)var25.next();
            world.setBlockState(lv13, lv12, Block.NOTIFY_LISTENERS | Block.FORCE_STATE | Block.MOVED);
         }

         var25 = map.entrySet().iterator();

         BlockPos lv14;
         while(var25.hasNext()) {
            Map.Entry entry = (Map.Entry)var25.next();
            lv14 = (BlockPos)entry.getKey();
            BlockState lv15 = (BlockState)entry.getValue();
            lv15.prepare(world, lv14, 2);
            lv12.updateNeighbors(world, lv14, Block.NOTIFY_LISTENERS);
            lv12.prepare(world, lv14, 2);
         }

         j = 0;

         int l;
         for(l = list3.size() - 1; l >= 0; --l) {
            lv7 = lvs[j++];
            lv14 = (BlockPos)list3.get(l);
            lv7.prepare(world, lv14, 2);
            world.updateNeighborsAlways(lv14, lv7.getBlock());
         }

         for(l = list.size() - 1; l >= 0; --l) {
            world.updateNeighborsAlways((BlockPos)list.get(l), lvs[j++].getBlock());
         }

         if (retract) {
            world.updateNeighborsAlways(lv, Blocks.PISTON_HEAD);
         }

         return true;
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, EXTENDED);
   }

   public boolean hasSidedTransparency(BlockState state) {
      return (Boolean)state.get(EXTENDED);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      EXTENDED = Properties.EXTENDED;
      EXTENDED_EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
      EXTENDED_WEST_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      EXTENDED_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
      EXTENDED_NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
      EXTENDED_UP_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
      EXTENDED_DOWN_SHAPE = Block.createCuboidShape(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
   }
}
