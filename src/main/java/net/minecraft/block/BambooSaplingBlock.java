package net.minecraft.block;

import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BambooSaplingBlock extends Block implements Fertilizable {
   protected static final float field_31005 = 4.0F;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);

   public BambooSaplingBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Vec3d lv = state.getModelOffset(world, pos);
      return SHAPE.offset(lv.x, lv.y, lv.z);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9) {
         this.grow(world, pos);
      }

   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         if (direction == Direction.UP && neighborState.isOf(Blocks.BAMBOO)) {
            world.setBlockState(pos, Blocks.BAMBOO.getDefaultState(), Block.NOTIFY_LISTENERS);
         }

         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Items.BAMBOO);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return world.getBlockState(pos.up()).isAir();
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.grow(world, pos);
   }

   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      return player.getMainHandStack().getItem() instanceof SwordItem ? 1.0F : super.calcBlockBreakingDelta(state, player, world, pos);
   }

   protected void grow(World world, BlockPos pos) {
      world.setBlockState(pos.up(), (BlockState)Blocks.BAMBOO.getDefaultState().with(BambooBlock.LEAVES, BambooLeaves.SMALL), Block.NOTIFY_ALL);
   }
}
