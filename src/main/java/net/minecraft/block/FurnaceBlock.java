package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FurnaceBlock extends AbstractFurnaceBlock {
   protected FurnaceBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new FurnaceBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(world, type, BlockEntityType.FURNACE);
   }

   protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof FurnaceBlockEntity) {
         player.openHandledScreen((NamedScreenHandlerFactory)lv);
         player.incrementStat(Stats.INTERACT_WITH_FURNACE);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         double d = (double)pos.getX() + 0.5;
         double e = (double)pos.getY();
         double f = (double)pos.getZ() + 0.5;
         if (random.nextDouble() < 0.1) {
            world.playSound(d, e, f, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         Direction lv = (Direction)state.get(FACING);
         Direction.Axis lv2 = lv.getAxis();
         double g = 0.52;
         double h = random.nextDouble() * 0.6 - 0.3;
         double i = lv2 == Direction.Axis.X ? (double)lv.getOffsetX() * 0.52 : h;
         double j = random.nextDouble() * 6.0 / 16.0;
         double k = lv2 == Direction.Axis.Z ? (double)lv.getOffsetZ() * 0.52 : h;
         world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
         world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0, 0.0, 0.0);
      }
   }
}
