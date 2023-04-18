package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EndPortalBlock extends BlockWithEntity {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

   protected EndPortalBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new EndPortalBlockEntity(pos, state);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (world instanceof ServerWorld && entity.canUsePortals() && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()))), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {
         RegistryKey lv = world.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
         ServerWorld lv2 = ((ServerWorld)world).getServer().getWorld(lv);
         if (lv2 == null) {
            return;
         }

         entity.moveToWorld(lv2);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX() + random.nextDouble();
      double e = (double)pos.getY() + 0.8;
      double f = (double)pos.getZ() + random.nextDouble();
      world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   public boolean canBucketPlace(BlockState state, Fluid fluid) {
      return false;
   }
}
