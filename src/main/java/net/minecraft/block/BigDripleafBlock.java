package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import net.minecraft.block.enums.Tilt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BigDripleafBlock extends HorizontalFacingBlock implements Fertilizable, Waterloggable {
   private static final BooleanProperty WATERLOGGED;
   private static final EnumProperty TILT;
   private static final int field_31015 = -1;
   private static final Object2IntMap NEXT_TILT_DELAYS;
   private static final int field_31016 = 5;
   private static final int field_31017 = 6;
   private static final int field_31018 = 11;
   private static final int field_31019 = 13;
   private static final Map SHAPES_FOR_TILT;
   private static final VoxelShape BASE_SHAPE;
   private static final Map SHAPES_FOR_DIRECTION;
   private final Map shapes;

   protected BigDripleafBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false)).with(FACING, Direction.NORTH)).with(TILT, Tilt.NONE));
      this.shapes = this.getShapesForStates(BigDripleafBlock::getShapeForState);
   }

   private static VoxelShape getShapeForState(BlockState state) {
      return VoxelShapes.union((VoxelShape)SHAPES_FOR_TILT.get(state.get(TILT)), (VoxelShape)SHAPES_FOR_DIRECTION.get(state.get(FACING)));
   }

   public static void grow(WorldAccess world, Random random, BlockPos pos, Direction direction) {
      int i = MathHelper.nextInt(random, 2, 5);
      BlockPos.Mutable lv = pos.mutableCopy();
      int j = 0;

      while(j < i && canGrowInto(world, lv, world.getBlockState(lv))) {
         ++j;
         lv.move(Direction.UP);
      }

      int k = pos.getY() + j - 1;
      lv.setY(pos.getY());

      while(lv.getY() < k) {
         BigDripleafStemBlock.placeStemAt(world, lv, world.getFluidState(lv), direction);
         lv.move(Direction.UP);
      }

      placeDripleafAt(world, lv, world.getFluidState(lv), direction);
   }

   private static boolean canGrowInto(BlockState state) {
      return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.SMALL_DRIPLEAF);
   }

   protected static boolean canGrowInto(HeightLimitView world, BlockPos pos, BlockState state) {
      return !world.isOutOfHeightLimit(pos) && canGrowInto(state);
   }

   protected static boolean placeDripleafAt(WorldAccess world, BlockPos pos, FluidState fluidState, Direction direction) {
      BlockState lv = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF.getDefaultState().with(WATERLOGGED, fluidState.isEqualAndStill(Fluids.WATER))).with(FACING, direction);
      return world.setBlockState(pos, lv, Block.NOTIFY_ALL);
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      this.changeTilt(state, world, hit.getBlockPos(), Tilt.FULL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      BlockState lv2 = world.getBlockState(lv);
      return lv2.isOf(this) || lv2.isOf(Blocks.BIG_DRIPLEAF_STEM) || lv2.isIn(BlockTags.BIG_DRIPLEAF_PLACEABLE);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         if ((Boolean)state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return direction == Direction.UP && neighborState.isOf(this) ? Blocks.BIG_DRIPLEAF_STEM.getStateWithProperties(state) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      BlockState lv = world.getBlockState(pos.up());
      return canGrowInto(lv);
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockPos lv = pos.up();
      BlockState lv2 = world.getBlockState(lv);
      if (canGrowInto(world, lv, lv2)) {
         Direction lv3 = (Direction)state.get(FACING);
         BigDripleafStemBlock.placeStemAt(world, pos, state.getFluidState(), lv3);
         placeDripleafAt(world, lv, lv2.getFluidState(), lv3);
      }

   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         if (state.get(TILT) == Tilt.NONE && isEntityAbove(pos, entity) && !world.isReceivingRedstonePower(pos)) {
            this.changeTilt(state, world, pos, Tilt.UNSTABLE, (SoundEvent)null);
         }

      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.isReceivingRedstonePower(pos)) {
         resetTilt(state, world, pos);
      } else {
         Tilt lv = (Tilt)state.get(TILT);
         if (lv == Tilt.UNSTABLE) {
            this.changeTilt(state, world, pos, Tilt.PARTIAL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
         } else if (lv == Tilt.PARTIAL) {
            this.changeTilt(state, world, pos, Tilt.FULL, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
         } else if (lv == Tilt.FULL) {
            resetTilt(state, world, pos);
         }

      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (world.isReceivingRedstonePower(pos)) {
         resetTilt(state, world, pos);
      }

   }

   private static void playTiltSound(World world, BlockPos pos, SoundEvent soundEvent) {
      float f = MathHelper.nextBetween(world.random, 0.8F, 1.2F);
      world.playSound((PlayerEntity)null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, f);
   }

   private static boolean isEntityAbove(BlockPos pos, Entity entity) {
      return entity.isOnGround() && entity.getPos().y > (double)((float)pos.getY() + 0.6875F);
   }

   private void changeTilt(BlockState state, World world, BlockPos pos, Tilt tilt, @Nullable SoundEvent sound) {
      changeTilt(state, world, pos, tilt);
      if (sound != null) {
         playTiltSound(world, pos, sound);
      }

      int i = NEXT_TILT_DELAYS.getInt(tilt);
      if (i != -1) {
         world.scheduleBlockTick(pos, this, i);
      }

   }

   private static void resetTilt(BlockState state, World world, BlockPos pos) {
      changeTilt(state, world, pos, Tilt.NONE);
      if (state.get(TILT) != Tilt.NONE) {
         playTiltSound(world, pos, SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_UP);
      }

   }

   private static void changeTilt(BlockState state, World world, BlockPos pos, Tilt tilt) {
      Tilt lv = (Tilt)state.get(TILT);
      world.setBlockState(pos, (BlockState)state.with(TILT, tilt), Block.NOTIFY_LISTENERS);
      if (tilt.isStable() && tilt != lv) {
         world.emitGameEvent((Entity)null, GameEvent.BLOCK_CHANGE, pos);
      }

   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)SHAPES_FOR_TILT.get(state.get(TILT));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.shapes.get(state);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
      FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      boolean bl = lv.isOf(Blocks.BIG_DRIPLEAF) || lv.isOf(Blocks.BIG_DRIPLEAF_STEM);
      return (BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, lv2.isEqualAndStill(Fluids.WATER))).with(FACING, bl ? (Direction)lv.get(FACING) : ctx.getHorizontalPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(WATERLOGGED, FACING, TILT);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      TILT = Properties.TILT;
      NEXT_TILT_DELAYS = (Object2IntMap)Util.make(new Object2IntArrayMap(), (delays) -> {
         delays.defaultReturnValue(-1);
         delays.put(Tilt.UNSTABLE, 10);
         delays.put(Tilt.PARTIAL, 10);
         delays.put(Tilt.FULL, 100);
      });
      SHAPES_FOR_TILT = ImmutableMap.of(Tilt.NONE, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 15.0, 16.0), Tilt.UNSTABLE, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 15.0, 16.0), Tilt.PARTIAL, Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 13.0, 16.0), Tilt.FULL, VoxelShapes.empty());
      BASE_SHAPE = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
      SHAPES_FOR_DIRECTION = ImmutableMap.of(Direction.NORTH, VoxelShapes.combine(BigDripleafStemBlock.NORTH_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.SOUTH, VoxelShapes.combine(BigDripleafStemBlock.SOUTH_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.EAST, VoxelShapes.combine(BigDripleafStemBlock.EAST_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST), Direction.WEST, VoxelShapes.combine(BigDripleafStemBlock.WEST_SHAPE, BASE_SHAPE, BooleanBiFunction.ONLY_FIRST));
   }
}
