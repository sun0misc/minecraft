package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class BoatDispenserBehavior extends ItemDispenserBehavior {
   private final ItemDispenserBehavior itemDispenser;
   private final BoatEntity.Type boatType;
   private final boolean chest;

   public BoatDispenserBehavior(BoatEntity.Type type) {
      this(type, false);
   }

   public BoatDispenserBehavior(BoatEntity.Type boatType, boolean chest) {
      this.itemDispenser = new ItemDispenserBehavior();
      this.boatType = boatType;
      this.chest = chest;
   }

   public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
      World lv2 = pointer.getWorld();
      double d = pointer.getX() + (double)((float)lv.getOffsetX() * 1.125F);
      double e = pointer.getY() + (double)((float)lv.getOffsetY() * 1.125F);
      double f = pointer.getZ() + (double)((float)lv.getOffsetZ() * 1.125F);
      BlockPos lv3 = pointer.getPos().offset(lv);
      double g;
      if (lv2.getFluidState(lv3).isIn(FluidTags.WATER)) {
         g = 1.0;
      } else {
         if (!lv2.getBlockState(lv3).isAir() || !lv2.getFluidState(lv3.down()).isIn(FluidTags.WATER)) {
            return this.itemDispenser.dispense(pointer, stack);
         }

         g = 0.0;
      }

      BoatEntity lv4 = this.chest ? new ChestBoatEntity(lv2, d, e + g, f) : new BoatEntity(lv2, d, e + g, f);
      ((BoatEntity)lv4).setVariant(this.boatType);
      ((BoatEntity)lv4).setYaw(lv.asRotation());
      lv2.spawnEntity((Entity)lv4);
      stack.decrement(1);
      return stack;
   }

   protected void playSound(BlockPointer pointer) {
      pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
   }
}
