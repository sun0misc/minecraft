package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EndRodBlock extends RodBlock {
   protected EndRodBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getSide();
      BlockState lv2 = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(lv.getOpposite()));
      return lv2.isOf(this) && lv2.get(FACING) == lv ? (BlockState)this.getDefaultState().with(FACING, lv.getOpposite()) : (BlockState)this.getDefaultState().with(FACING, lv);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      Direction lv = (Direction)state.get(FACING);
      double d = (double)pos.getX() + 0.55 - (double)(random.nextFloat() * 0.1F);
      double e = (double)pos.getY() + 0.55 - (double)(random.nextFloat() * 0.1F);
      double f = (double)pos.getZ() + 0.55 - (double)(random.nextFloat() * 0.1F);
      double g = (double)(0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F);
      if (random.nextInt(5) == 0) {
         world.addParticle(ParticleTypes.END_ROD, d + (double)lv.getOffsetX() * g, e + (double)lv.getOffsetY() * g, f + (double)lv.getOffsetZ() * g, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005);
      }

   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING);
   }
}
