package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public abstract class ProjectileDispenserBehavior extends ItemDispenserBehavior {
   public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      World lv = pointer.getWorld();
      Position lv2 = DispenserBlock.getOutputLocation(pointer);
      Direction lv3 = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
      ProjectileEntity lv4 = this.createProjectile(lv, lv2, stack);
      lv4.setVelocity((double)lv3.getOffsetX(), (double)((float)lv3.getOffsetY() + 0.1F), (double)lv3.getOffsetZ(), this.getForce(), this.getVariation());
      lv.spawnEntity(lv4);
      stack.decrement(1);
      return stack;
   }

   protected void playSound(BlockPointer pointer) {
      pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_LAUNCHES_PROJECTILE, pointer.getPos(), 0);
   }

   protected abstract ProjectileEntity createProjectile(World world, Position position, ItemStack stack);

   protected float getVariation() {
      return 6.0F;
   }

   protected float getForce() {
      return 1.1F;
   }
}
