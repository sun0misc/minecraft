package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EndGatewayBlock extends BlockWithEntity {
   protected EndGatewayBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new EndGatewayBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.END_GATEWAY, world.isClient ? EndGatewayBlockEntity::clientTick : EndGatewayBlockEntity::serverTick);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof EndGatewayBlockEntity) {
         int i = ((EndGatewayBlockEntity)lv).getDrawnSidesCount();

         for(int j = 0; j < i; ++j) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = (random.nextDouble() - 0.5) * 0.5;
            double h = (random.nextDouble() - 0.5) * 0.5;
            double k = (random.nextDouble() - 0.5) * 0.5;
            int l = random.nextInt(2) * 2 - 1;
            if (random.nextBoolean()) {
               f = (double)pos.getZ() + 0.5 + 0.25 * (double)l;
               k = (double)(random.nextFloat() * 2.0F * (float)l);
            } else {
               d = (double)pos.getX() + 0.5 + 0.25 * (double)l;
               g = (double)(random.nextFloat() * 2.0F * (float)l);
            }

            world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, k);
         }

      }
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   public boolean canBucketPlace(BlockState state, Fluid fluid) {
      return false;
   }
}
