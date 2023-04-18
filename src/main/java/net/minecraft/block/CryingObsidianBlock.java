package net.minecraft.block;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CryingObsidianBlock extends Block {
   public CryingObsidianBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (random.nextInt(5) == 0) {
         Direction lv = Direction.random(random);
         if (lv != Direction.UP) {
            BlockPos lv2 = pos.offset(lv);
            BlockState lv3 = world.getBlockState(lv2);
            if (!state.isOpaque() || !lv3.isSideSolidFullSquare(world, lv2, lv.getOpposite())) {
               double d = lv.getOffsetX() == 0 ? random.nextDouble() : 0.5 + (double)lv.getOffsetX() * 0.6;
               double e = lv.getOffsetY() == 0 ? random.nextDouble() : 0.5 + (double)lv.getOffsetY() * 0.6;
               double f = lv.getOffsetZ() == 0 ? random.nextDouble() : 0.5 + (double)lv.getOffsetZ() * 0.6;
               world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + f, 0.0, 0.0, 0.0);
            }
         }
      }
   }
}
