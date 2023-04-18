package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlantPartBlock extends Block {
   protected final Direction growthDirection;
   protected final boolean tickWater;
   protected final VoxelShape outlineShape;

   protected AbstractPlantPartBlock(AbstractBlock.Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater) {
      super(settings);
      this.growthDirection = growthDirection;
      this.outlineShape = outlineShape;
      this.tickWater = tickWater;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(this.growthDirection));
      return !lv.isOf(this.getStem()) && !lv.isOf(this.getPlant()) ? this.getRandomGrowthState(ctx.getWorld()) : this.getPlant().getDefaultState();
   }

   public BlockState getRandomGrowthState(WorldAccess world) {
      return this.getDefaultState();
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.offset(this.growthDirection.getOpposite());
      BlockState lv2 = world.getBlockState(lv);
      if (!this.canAttachTo(lv2)) {
         return false;
      } else {
         return lv2.isOf(this.getStem()) || lv2.isOf(this.getPlant()) || lv2.isSideSolidFullSquare(world, lv, this.growthDirection);
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   protected boolean canAttachTo(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.outlineShape;
   }

   protected abstract AbstractPlantStemBlock getStem();

   protected abstract Block getPlant();
}
