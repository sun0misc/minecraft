package net.minecraft.block;

import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Attachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BellBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final EnumProperty ATTACHMENT;
   public static final BooleanProperty POWERED;
   private static final VoxelShape NORTH_SOUTH_SHAPE;
   private static final VoxelShape EAST_WEST_SHAPE;
   private static final VoxelShape BELL_WAIST_SHAPE;
   private static final VoxelShape BELL_LIP_SHAPE;
   private static final VoxelShape BELL_SHAPE;
   private static final VoxelShape NORTH_SOUTH_WALLS_SHAPE;
   private static final VoxelShape EAST_WEST_WALLS_SHAPE;
   private static final VoxelShape WEST_WALL_SHAPE;
   private static final VoxelShape EAST_WALL_SHAPE;
   private static final VoxelShape NORTH_WALL_SHAPE;
   private static final VoxelShape SOUTH_WALL_SHAPE;
   private static final VoxelShape HANGING_SHAPE;
   public static final int field_31014 = 1;

   public BellBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(ATTACHMENT, Attachment.FLOOR)).with(POWERED, false));
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      boolean bl2 = world.isReceivingRedstonePower(pos);
      if (bl2 != (Boolean)state.get(POWERED)) {
         if (bl2) {
            this.ring(world, pos, (Direction)null);
         }

         world.setBlockState(pos, (BlockState)state.with(POWERED, bl2), Block.NOTIFY_ALL);
      }

   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      Entity lv = projectile.getOwner();
      PlayerEntity lv2 = lv instanceof PlayerEntity ? (PlayerEntity)lv : null;
      this.ring(world, state, hit, lv2, true);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      return this.ring(world, state, hit, player, true) ? ActionResult.success(world.isClient) : ActionResult.PASS;
   }

   public boolean ring(World world, BlockState state, BlockHitResult hitResult, @Nullable PlayerEntity player, boolean checkHitPos) {
      Direction lv = hitResult.getSide();
      BlockPos lv2 = hitResult.getBlockPos();
      boolean bl2 = !checkHitPos || this.isPointOnBell(state, lv, hitResult.getPos().y - (double)lv2.getY());
      if (bl2) {
         boolean bl3 = this.ring(player, world, lv2, lv);
         if (bl3 && player != null) {
            player.incrementStat(Stats.BELL_RING);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isPointOnBell(BlockState state, Direction side, double y) {
      if (side.getAxis() != Direction.Axis.Y && !(y > 0.8123999834060669)) {
         Direction lv = (Direction)state.get(FACING);
         Attachment lv2 = (Attachment)state.get(ATTACHMENT);
         switch (lv2) {
            case FLOOR:
               return lv.getAxis() == side.getAxis();
            case SINGLE_WALL:
            case DOUBLE_WALL:
               return lv.getAxis() != side.getAxis();
            case CEILING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean ring(World world, BlockPos pos, @Nullable Direction direction) {
      return this.ring((Entity)null, world, pos, direction);
   }

   public boolean ring(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (!world.isClient && lv instanceof BellBlockEntity) {
         if (direction == null) {
            direction = (Direction)world.getBlockState(pos).get(FACING);
         }

         ((BellBlockEntity)lv).activate(direction);
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
         world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
         return true;
      } else {
         return false;
      }
   }

   private VoxelShape getShape(BlockState state) {
      Direction lv = (Direction)state.get(FACING);
      Attachment lv2 = (Attachment)state.get(ATTACHMENT);
      if (lv2 == Attachment.FLOOR) {
         return lv != Direction.NORTH && lv != Direction.SOUTH ? EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE;
      } else if (lv2 == Attachment.CEILING) {
         return HANGING_SHAPE;
      } else if (lv2 == Attachment.DOUBLE_WALL) {
         return lv != Direction.NORTH && lv != Direction.SOUTH ? EAST_WEST_WALLS_SHAPE : NORTH_SOUTH_WALLS_SHAPE;
      } else if (lv == Direction.NORTH) {
         return NORTH_WALL_SHAPE;
      } else if (lv == Direction.SOUTH) {
         return SOUTH_WALL_SHAPE;
      } else {
         return lv == Direction.EAST ? EAST_WALL_SHAPE : WEST_WALL_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShape(state);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShape(state);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getSide();
      BlockPos lv2 = ctx.getBlockPos();
      World lv3 = ctx.getWorld();
      Direction.Axis lv4 = lv.getAxis();
      BlockState lv5;
      if (lv4 == Direction.Axis.Y) {
         lv5 = (BlockState)((BlockState)this.getDefaultState().with(ATTACHMENT, lv == Direction.DOWN ? Attachment.CEILING : Attachment.FLOOR)).with(FACING, ctx.getHorizontalPlayerFacing());
         if (lv5.canPlaceAt(ctx.getWorld(), lv2)) {
            return lv5;
         }
      } else {
         boolean bl = lv4 == Direction.Axis.X && lv3.getBlockState(lv2.west()).isSideSolidFullSquare(lv3, lv2.west(), Direction.EAST) && lv3.getBlockState(lv2.east()).isSideSolidFullSquare(lv3, lv2.east(), Direction.WEST) || lv4 == Direction.Axis.Z && lv3.getBlockState(lv2.north()).isSideSolidFullSquare(lv3, lv2.north(), Direction.SOUTH) && lv3.getBlockState(lv2.south()).isSideSolidFullSquare(lv3, lv2.south(), Direction.NORTH);
         lv5 = (BlockState)((BlockState)this.getDefaultState().with(FACING, lv.getOpposite())).with(ATTACHMENT, bl ? Attachment.DOUBLE_WALL : Attachment.SINGLE_WALL);
         if (lv5.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return lv5;
         }

         boolean bl2 = lv3.getBlockState(lv2.down()).isSideSolidFullSquare(lv3, lv2.down(), Direction.UP);
         lv5 = (BlockState)lv5.with(ATTACHMENT, bl2 ? Attachment.FLOOR : Attachment.CEILING);
         if (lv5.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return lv5;
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      Attachment lv = (Attachment)state.get(ATTACHMENT);
      Direction lv2 = getPlacementSide(state).getOpposite();
      if (lv2 == direction && !state.canPlaceAt(world, pos) && lv != Attachment.DOUBLE_WALL) {
         return Blocks.AIR.getDefaultState();
      } else {
         if (direction.getAxis() == ((Direction)state.get(FACING)).getAxis()) {
            if (lv == Attachment.DOUBLE_WALL && !neighborState.isSideSolidFullSquare(world, neighborPos, direction)) {
               return (BlockState)((BlockState)state.with(ATTACHMENT, Attachment.SINGLE_WALL)).with(FACING, direction.getOpposite());
            }

            if (lv == Attachment.SINGLE_WALL && lv2.getOpposite() == direction && neighborState.isSideSolidFullSquare(world, neighborPos, (Direction)state.get(FACING))) {
               return (BlockState)state.with(ATTACHMENT, Attachment.DOUBLE_WALL);
            }
         }

         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = getPlacementSide(state).getOpposite();
      return lv == Direction.UP ? Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN) : WallMountedBlock.canPlaceAt(world, pos, lv);
   }

   private static Direction getPlacementSide(BlockState state) {
      switch ((Attachment)state.get(ATTACHMENT)) {
         case FLOOR:
            return Direction.UP;
         case CEILING:
            return Direction.DOWN;
         default:
            return ((Direction)state.get(FACING)).getOpposite();
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, ATTACHMENT, POWERED);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BellBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.BELL, world.isClient ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      ATTACHMENT = Properties.ATTACHMENT;
      POWERED = Properties.POWERED;
      NORTH_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
      EAST_WEST_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
      BELL_WAIST_SHAPE = Block.createCuboidShape(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
      BELL_LIP_SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
      BELL_SHAPE = VoxelShapes.union(BELL_LIP_SHAPE, BELL_WAIST_SHAPE);
      NORTH_SOUTH_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
      EAST_WEST_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
      WEST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
      EAST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
      NORTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
      SOUTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
      HANGING_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
   }
}
