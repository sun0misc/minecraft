package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class CropBlock extends PlantBlock implements Fertilizable, Crop {
   public static final int MAX_AGE = 7;
   public static final IntProperty AGE;
   private static final VoxelShape[] AGE_TO_SHAPE;

   protected CropBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(this.getAgeProperty(), 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return AGE_TO_SHAPE[(Integer)state.get(this.getAgeProperty())];
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isOf(Blocks.FARMLAND);
   }

   public IntProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected int getAge(BlockState state) {
      return (Integer)state.get(this.getAgeProperty());
   }

   public BlockState withAge(int age) {
      return (BlockState)this.getDefaultState().with(this.getAgeProperty(), age);
   }

   public boolean isMature(BlockState state) {
      return (Integer)state.get(this.getAgeProperty()) >= this.getMaxAge();
   }

   public boolean hasRandomTicks(BlockState state) {
      return !this.isMature(state);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getBaseLightLevel(pos, 0) >= 9) {
         int i = this.getAge(state);
         if (i < this.getMaxAge()) {
            float f = getAvailableMoisture(this, world, pos);
            if (random.nextInt((int)(25.0F / f) + 1) == 0) {
               world.setBlockState(pos, this.withAge(i + 1), Block.NOTIFY_LISTENERS);
            }
         }
      }

   }

   public void applyGrowth(World world, BlockPos pos, BlockState state) {
      int i = this.getAge(state) + this.getGrowthAmount(world);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      world.setBlockState(pos, this.withAge(i), Block.NOTIFY_LISTENERS);
   }

   protected int getGrowthAmount(World world) {
      return MathHelper.nextInt(world.random, 2, 5);
   }

   protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
      float f = 1.0F;
      BlockPos lv = pos.down();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            float g = 0.0F;
            BlockState lv2 = world.getBlockState(lv.add(i, 0, j));
            if (lv2.isOf(Blocks.FARMLAND)) {
               g = 1.0F;
               if ((Integer)lv2.get(FarmlandBlock.MOISTURE) > 0) {
                  g = 3.0F;
               }
            }

            if (i != 0 || j != 0) {
               g /= 4.0F;
            }

            f += g;
         }
      }

      BlockPos lv3 = pos.north();
      BlockPos lv4 = pos.south();
      BlockPos lv5 = pos.west();
      BlockPos lv6 = pos.east();
      boolean bl = world.getBlockState(lv5).isOf(block) || world.getBlockState(lv6).isOf(block);
      boolean bl2 = world.getBlockState(lv3).isOf(block) || world.getBlockState(lv4).isOf(block);
      if (bl && bl2) {
         f /= 2.0F;
      } else {
         boolean bl3 = world.getBlockState(lv5.north()).isOf(block) || world.getBlockState(lv6.north()).isOf(block) || world.getBlockState(lv6.south()).isOf(block) || world.getBlockState(lv5.south()).isOf(block);
         if (bl3) {
            f /= 2.0F;
         }
      }

      return f;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return (world.getBaseLightLevel(pos, 0) >= 8 || world.isSkyVisible(pos)) && super.canPlaceAt(state, world, pos);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (entity instanceof RavagerEntity && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
         world.breakBlock(pos, true, entity);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   protected ItemConvertible getSeedsItem() {
      return Items.WHEAT_SEEDS;
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(this.getSeedsItem());
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return !this.isMature(state);
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.applyGrowth(world, pos, state);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   static {
      AGE = Properties.AGE_7;
      AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)};
   }
}
