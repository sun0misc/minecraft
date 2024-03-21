package net.minecraft.core.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior {
   public final ItemStack dispense(BlockSource p_123391_, ItemStack p_123392_) {
      ItemStack itemstack = this.execute(p_123391_, p_123392_);
      this.playSound(p_123391_);
      this.playAnimation(p_123391_, p_123391_.state().getValue(DispenserBlock.FACING));
      return itemstack;
   }

   protected ItemStack execute(BlockSource p_301824_, ItemStack p_123386_) {
      Direction direction = p_301824_.state().getValue(DispenserBlock.FACING);
      Position position = DispenserBlock.getDispensePosition(p_301824_);
      ItemStack itemstack = p_123386_.split(1);
      spawnItem(p_301824_.level(), itemstack, 6, direction, position);
      return p_123386_;
   }

   public static void spawnItem(Level p_123379_, ItemStack p_123380_, int p_123381_, Direction p_123382_, Position p_123383_) {
      double d0 = p_123383_.x();
      double d1 = p_123383_.y();
      double d2 = p_123383_.z();
      if (p_123382_.getAxis() == Direction.Axis.Y) {
         d1 -= 0.125D;
      } else {
         d1 -= 0.15625D;
      }

      ItemEntity itementity = new ItemEntity(p_123379_, d0, d1, d2, p_123380_);
      double d3 = p_123379_.random.nextDouble() * 0.1D + 0.2D;
      itementity.setDeltaMovement(p_123379_.random.triangle((double)p_123382_.getStepX() * d3, 0.0172275D * (double)p_123381_), p_123379_.random.triangle(0.2D, 0.0172275D * (double)p_123381_), p_123379_.random.triangle((double)p_123382_.getStepZ() * d3, 0.0172275D * (double)p_123381_));
      p_123379_.addFreshEntity(itementity);
   }

   protected void playSound(BlockSource p_123384_) {
      p_123384_.level().levelEvent(1000, p_123384_.pos(), 0);
   }

   protected void playAnimation(BlockSource p_123388_, Direction p_123389_) {
      p_123388_.level().levelEvent(2000, p_123388_.pos(), p_123389_.get3DDataValue());
   }
}