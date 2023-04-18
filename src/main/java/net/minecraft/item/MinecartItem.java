package net.minecraft.item;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class MinecartItem extends Item {
   private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
      private final ItemDispenserBehavior defaultBehavior = new ItemDispenserBehavior();

      public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
         Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
         World lv2 = pointer.getWorld();
         double d = pointer.getX() + (double)lv.getOffsetX() * 1.125;
         double e = Math.floor(pointer.getY()) + (double)lv.getOffsetY();
         double f = pointer.getZ() + (double)lv.getOffsetZ() * 1.125;
         BlockPos lv3 = pointer.getPos().offset(lv);
         BlockState lv4 = lv2.getBlockState(lv3);
         RailShape lv5 = lv4.getBlock() instanceof AbstractRailBlock ? (RailShape)lv4.get(((AbstractRailBlock)lv4.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
         double g;
         if (lv4.isIn(BlockTags.RAILS)) {
            if (lv5.isAscending()) {
               g = 0.6;
            } else {
               g = 0.1;
            }
         } else {
            if (!lv4.isAir() || !lv2.getBlockState(lv3.down()).isIn(BlockTags.RAILS)) {
               return this.defaultBehavior.dispense(pointer, stack);
            }

            BlockState lv6 = lv2.getBlockState(lv3.down());
            RailShape lv7 = lv6.getBlock() instanceof AbstractRailBlock ? (RailShape)lv6.get(((AbstractRailBlock)lv6.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (lv != Direction.DOWN && lv7.isAscending()) {
               g = -0.4;
            } else {
               g = -0.9;
            }
         }

         AbstractMinecartEntity lv8 = AbstractMinecartEntity.create(lv2, d, e + g, f, ((MinecartItem)stack.getItem()).type);
         if (stack.hasCustomName()) {
            lv8.setCustomName(stack.getName());
         }

         lv2.spawnEntity(lv8);
         stack.decrement(1);
         return stack;
      }

      protected void playSound(BlockPointer pointer) {
         pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
      }
   };
   final AbstractMinecartEntity.Type type;

   public MinecartItem(AbstractMinecartEntity.Type type, Item.Settings settings) {
      super(settings);
      this.type = type;
      DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (!lv3.isIn(BlockTags.RAILS)) {
         return ActionResult.FAIL;
      } else {
         ItemStack lv4 = context.getStack();
         if (!lv.isClient) {
            RailShape lv5 = lv3.getBlock() instanceof AbstractRailBlock ? (RailShape)lv3.get(((AbstractRailBlock)lv3.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (lv5.isAscending()) {
               d = 0.5;
            }

            AbstractMinecartEntity lv6 = AbstractMinecartEntity.create(lv, (double)lv2.getX() + 0.5, (double)lv2.getY() + 0.0625 + d, (double)lv2.getZ() + 0.5, this.type);
            if (lv4.hasCustomName()) {
               lv6.setCustomName(lv4.getName());
            }

            lv.spawnEntity(lv6);
            lv.emitGameEvent(GameEvent.ENTITY_PLACE, lv2, GameEvent.Emitter.of(context.getPlayer(), lv.getBlockState(lv2.down())));
         }

         lv4.decrement(1);
         return ActionResult.success(lv.isClient);
      }
   }
}
