package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
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
import org.jetbrains.annotations.Nullable;

public class WallHangingSignBlock extends AbstractSignBlock {
   public static final DirectionProperty FACING;
   public static final VoxelShape NORTH_SOUTH_COLLISION_SHAPE;
   public static final VoxelShape EAST_WEST_COLLISION_SHAPE;
   public static final VoxelShape NORTH_SOUTH_SHAPE;
   public static final VoxelShape EAST_WEST_SHAPE;
   private static final Map OUTLINE_SHAPES;

   public WallHangingSignBlock(AbstractBlock.Settings arg, WoodType arg2) {
      super(arg.sounds(arg2.hangingSignSoundType()), arg2);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity var8 = world.getBlockEntity(pos);
      if (var8 instanceof SignBlockEntity lv) {
         ItemStack lv2 = player.getStackInHand(hand);
         if (this.shouldTryAttaching(state, player, hit, lv, lv2)) {
            return ActionResult.PASS;
         }
      }

      return super.onUse(state, world, pos, player, hand, hit);
   }

   private boolean shouldTryAttaching(BlockState state, PlayerEntity player, BlockHitResult hitResult, SignBlockEntity sign, ItemStack stack) {
      return !sign.canRunCommandClickEvent(sign.isPlayerFacingFront(player), player) && stack.getItem() instanceof HangingSignItem && !this.isHitOnFacingAxis(hitResult, state);
   }

   private boolean isHitOnFacingAxis(BlockHitResult hitResult, BlockState state) {
      return hitResult.getSide().getAxis() == ((Direction)state.get(FACING)).getAxis();
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)OUTLINE_SHAPES.get(state.get(FACING));
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return this.getOutlineShape(state, world, pos, ShapeContext.absent());
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction)state.get(FACING)) {
         case EAST:
         case WEST:
            return EAST_WEST_COLLISION_SHAPE;
         default:
            return NORTH_SOUTH_COLLISION_SHAPE;
      }
   }

   public boolean canAttachAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = ((Direction)state.get(FACING)).rotateYClockwise();
      Direction lv2 = ((Direction)state.get(FACING)).rotateYCounterclockwise();
      return this.canAttachTo(world, state, pos.offset(lv), lv2) || this.canAttachTo(world, state, pos.offset(lv2), lv);
   }

   public boolean canAttachTo(WorldView world, BlockState state, BlockPos toPos, Direction direction) {
      BlockState lv = world.getBlockState(toPos);
      return lv.isIn(BlockTags.WALL_HANGING_SIGNS) ? ((Direction)lv.get(FACING)).getAxis().test((Direction)state.get(FACING)) : lv.isSideSolid(world, toPos, direction, SideShapeType.FULL);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      WorldView lv3 = ctx.getWorld();
      BlockPos lv4 = ctx.getBlockPos();
      Direction[] var6 = ctx.getPlacementDirections();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv5 = var6[var8];
         if (lv5.getAxis().isHorizontal() && !lv5.getAxis().test(ctx.getSide())) {
            Direction lv6 = lv5.getOpposite();
            lv = (BlockState)lv.with(FACING, lv6);
            if (lv.canPlaceAt(lv3, lv4) && this.canAttachAt(lv, lv3, lv4)) {
               return (BlockState)lv.with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction.getAxis() == ((Direction)state.get(FACING)).rotateYClockwise().getAxis() && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public float getRotationDegrees(BlockState state) {
      return ((Direction)state.get(FACING)).asRotation();
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, WATERLOGGED);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new HangingSignBlockEntity(pos, state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      NORTH_SOUTH_COLLISION_SHAPE = Block.createCuboidShape(0.0, 14.0, 6.0, 16.0, 16.0, 10.0);
      EAST_WEST_COLLISION_SHAPE = Block.createCuboidShape(6.0, 14.0, 0.0, 10.0, 16.0, 16.0);
      NORTH_SOUTH_SHAPE = VoxelShapes.union(NORTH_SOUTH_COLLISION_SHAPE, Block.createCuboidShape(1.0, 0.0, 7.0, 15.0, 10.0, 9.0));
      EAST_WEST_SHAPE = VoxelShapes.union(EAST_WEST_COLLISION_SHAPE, Block.createCuboidShape(7.0, 0.0, 1.0, 9.0, 10.0, 15.0));
      OUTLINE_SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH_SOUTH_SHAPE, Direction.SOUTH, NORTH_SOUTH_SHAPE, Direction.EAST, EAST_WEST_SHAPE, Direction.WEST, EAST_WEST_SHAPE));
   }
}
