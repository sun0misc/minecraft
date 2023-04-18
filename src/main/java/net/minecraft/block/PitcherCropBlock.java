package net.minecraft.block;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PitcherCropBlock extends TallPlantBlock implements Fertilizable, Crop {
   public static final IntProperty AGE;
   public static final int field_43240 = 4;
   private static final int field_43241 = 3;
   private static final int field_43391 = 1;
   private static final VoxelShape GROWN_UPPER_OUTLINE_SHAPE;
   private static final VoxelShape GROWN_LOWER_OUTLINE_SHAPE;
   private static final VoxelShape AGE_0_SHAPE;
   private static final VoxelShape LOWER_COLLISION_SHAPE;
   private static final VoxelShape[] UPPER_OUTLINE_SHAPES;
   private static final VoxelShape[] LOWER_OUTLINE_SHAPES;

   public PitcherCropBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   private boolean isFullyGrown(BlockState state) {
      return (Integer)state.get(AGE) >= 4;
   }

   public boolean hasRandomTicks(BlockState state) {
      return state.get(HALF) == DoubleBlockHalf.LOWER && !this.isFullyGrown(state);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getDefaultState();
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if ((Integer)state.get(AGE) == 0) {
         return AGE_0_SHAPE;
      } else {
         return state.get(HALF) == DoubleBlockHalf.LOWER ? LOWER_COLLISION_SHAPE : super.getCollisionShape(state, world, pos, context);
      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      if (state.get(HALF) == DoubleBlockHalf.LOWER && (Integer)state.get(AGE) >= 3) {
         BlockState lv = world.getBlockState(pos.up());
         return lv.isOf(this) && lv.get(HALF) == DoubleBlockHalf.UPPER && this.canPlantOnTop(world.getBlockState(pos.down()), world, pos);
      } else {
         return (world.getBaseLightLevel(pos, 0) >= 8 || world.isSkyVisible(pos)) && super.canPlaceAt(state, world, pos);
      }
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isOf(Blocks.FARMLAND);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
      super.appendProperties(builder);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return state.get(HALF) == DoubleBlockHalf.UPPER ? UPPER_OUTLINE_SHAPES[Math.abs(4 - ((Integer)state.get(AGE) + 1))] : LOWER_OUTLINE_SHAPES[(Integer)state.get(AGE)];
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (entity instanceof RavagerEntity && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
         world.breakBlock(pos, true, entity);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return false;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      float f = CropBlock.getAvailableMoisture(this, world, pos);
      boolean bl = random.nextInt((int)(25.0F / f) + 1) == 0;
      if (bl) {
         this.tryGrow(world, state, pos, 1);
      }

   }

   private void tryGrow(ServerWorld world, BlockState state, BlockPos pos, int amount) {
      int j = Math.min((Integer)state.get(AGE) + amount, 4);
      if (j < 3 || canGrowAt(world, isLowerHalf(state) ? pos.up() : pos)) {
         world.setBlockState(pos, (BlockState)state.with(AGE, j), Block.NOTIFY_LISTENERS);
         if (j >= 3) {
            BlockPos lv;
            if (isLowerHalf(state)) {
               lv = pos.up();
               world.setBlockState(lv, withWaterloggedState(world, pos, (BlockState)((BlockState)this.getDefaultState().with(AGE, j)).with(HALF, DoubleBlockHalf.UPPER)), Block.NOTIFY_ALL);
            } else {
               lv = pos.down();
               world.setBlockState(lv, withWaterloggedState(world, pos, (BlockState)((BlockState)this.getDefaultState().with(AGE, j)).with(HALF, DoubleBlockHalf.LOWER)), Block.NOTIFY_ALL);
            }

         }
      }
   }

   private static boolean canGrowAt(ServerWorld world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return lv.isAir() || lv.isOf(Blocks.PITCHER_CROP);
   }

   private static boolean isLowerHalf(BlockState state) {
      return state.get(HALF) == DoubleBlockHalf.LOWER;
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      boolean var10000;
      label32: {
         if (!this.isFullyGrown(state)) {
            if ((Integer)state.get(AGE) < 2) {
               break label32;
            }

            if (world instanceof ServerWorld) {
               ServerWorld lv = (ServerWorld)world;
               if (canGrowAt(lv, isLowerHalf(state) ? pos.up() : pos)) {
                  break label32;
               }
            }
         }

         var10000 = false;
         return var10000;
      }

      var10000 = true;
      return var10000;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.tryGrow(world, state, pos, 1);
   }

   static {
      AGE = Properties.AGE_4;
      GROWN_UPPER_OUTLINE_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
      GROWN_LOWER_OUTLINE_SHAPE = Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
      AGE_0_SHAPE = Block.createCuboidShape(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
      LOWER_COLLISION_SHAPE = Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
      UPPER_OUTLINE_SHAPES = new VoxelShape[]{Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), GROWN_UPPER_OUTLINE_SHAPE};
      LOWER_OUTLINE_SHAPES = new VoxelShape[]{AGE_0_SHAPE, Block.createCuboidShape(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), GROWN_LOWER_OUTLINE_SHAPE, GROWN_LOWER_OUTLINE_SHAPE, GROWN_LOWER_OUTLINE_SHAPE};
   }
}
