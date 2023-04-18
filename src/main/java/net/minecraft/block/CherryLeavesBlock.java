package net.minecraft.block;

import net.minecraft.client.util.ParticleUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CherryLeavesBlock extends LeavesBlock {
   public CherryLeavesBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      super.randomDisplayTick(state, world, pos, random);
      if (random.nextInt(10) == 0) {
         BlockPos lv = pos.down();
         BlockState lv2 = world.getBlockState(lv);
         if (!isFaceFullSquare(lv2.getCollisionShape(world, lv), Direction.UP)) {
            ParticleUtil.spawnParticle(world, pos, (Random)random, (ParticleEffect)ParticleTypes.CHERRY_LEAVES);
         }
      }
   }
}
