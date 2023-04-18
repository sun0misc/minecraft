package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class HangingSignBlock extends AbstractSignBlock {
   public static final IntProperty ROTATION;
   public static final BooleanProperty ATTACHED;
   protected static final float field_40302 = 5.0F;
   protected static final VoxelShape DEFAULT_SHAPE;
   private static final Map SHAPES_FOR_ROTATION;

   public HangingSignBlock(AbstractBlock.Settings arg, WoodType arg2) {
      super(arg.sounds(arg2.hangingSignSoundType()), arg2);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0)).with(ATTACHED, false)).with(WATERLOGGED, false));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity var8 = world.getBlockEntity(pos);
      if (var8 instanceof SignBlockEntity lv) {
         ItemStack lv2 = player.getStackInHand(hand);
         if (this.shouldTryAttaching(player, hit, lv, lv2)) {
            return ActionResult.PASS;
         }
      }

      return super.onUse(state, world, pos, player, hand, hit);
   }

   private boolean shouldTryAttaching(PlayerEntity player, BlockHitResult hitResult, SignBlockEntity sign, ItemStack stack) {
      return !sign.canRunCommandClickEvent(sign.isPlayerFacingFront(player), player) && stack.getItem() instanceof HangingSignItem && hitResult.getSide().equals(Direction.DOWN);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.CENTER);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World lv = ctx.getWorld();
      FluidState lv2 = lv.getFluidState(ctx.getBlockPos());
      BlockPos lv3 = ctx.getBlockPos().up();
      BlockState lv4 = lv.getBlockState(lv3);
      boolean bl = lv4.isIn(BlockTags.ALL_HANGING_SIGNS);
      Direction lv5 = Direction.fromRotation((double)ctx.getPlayerYaw());
      boolean bl2 = !Block.isFaceFullSquare(lv4.getCollisionShape(lv, lv3), Direction.DOWN) || ctx.shouldCancelInteraction();
      if (bl && !ctx.shouldCancelInteraction()) {
         if (lv4.contains(WallHangingSignBlock.FACING)) {
            Direction lv6 = (Direction)lv4.get(WallHangingSignBlock.FACING);
            if (lv6.getAxis().test(lv5)) {
               bl2 = false;
            }
         } else if (lv4.contains(ROTATION)) {
            Optional optional = RotationPropertyHelper.toDirection((Integer)lv4.get(ROTATION));
            if (optional.isPresent() && ((Direction)optional.get()).getAxis().test(lv5)) {
               bl2 = false;
            }
         }
      }

      int i = !bl2 ? RotationPropertyHelper.fromDirection(lv5.getOpposite()) : RotationPropertyHelper.fromYaw(ctx.getPlayerYaw() + 180.0F);
      return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(ATTACHED, bl2)).with(ROTATION, i)).with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      VoxelShape lv = (VoxelShape)SHAPES_FOR_ROTATION.get(state.get(ROTATION));
      return lv == null ? DEFAULT_SHAPE : lv;
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return this.getOutlineShape(state, world, pos, ShapeContext.absent());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.UP && !this.canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public float getRotationDegrees(BlockState state) {
      return RotationPropertyHelper.toDegrees((Integer)state.get(ROTATION));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ROTATION, rotation.rotate((Integer)state.get(ROTATION), 16));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ROTATION, mirror.mirror((Integer)state.get(ROTATION), 16));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(ROTATION, ATTACHED, WATERLOGGED);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new HangingSignBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }

   static {
      ROTATION = Properties.ROTATION;
      ATTACHED = Properties.ATTACHED;
      DEFAULT_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
      SHAPES_FOR_ROTATION = Maps.newHashMap(ImmutableMap.of(0, Block.createCuboidShape(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), 4, Block.createCuboidShape(7.0, 0.0, 1.0, 9.0, 10.0, 15.0), 8, Block.createCuboidShape(1.0, 0.0, 7.0, 15.0, 10.0, 9.0), 12, Block.createCuboidShape(7.0, 0.0, 1.0, 9.0, 10.0, 15.0)));
   }
}
