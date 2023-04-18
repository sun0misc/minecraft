package net.minecraft.item;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FireChargeItem extends Item {
   public FireChargeItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      boolean bl = false;
      if (!CampfireBlock.canBeLit(lv3) && !CandleBlock.canBeLit(lv3) && !CandleCakeBlock.canBeLit(lv3)) {
         lv2 = lv2.offset(context.getSide());
         if (AbstractFireBlock.canPlaceAt(lv, lv2, context.getHorizontalPlayerFacing())) {
            this.playUseSound(lv, lv2);
            lv.setBlockState(lv2, AbstractFireBlock.getState(lv, lv2));
            lv.emitGameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, lv2);
            bl = true;
         }
      } else {
         this.playUseSound(lv, lv2);
         lv.setBlockState(lv2, (BlockState)lv3.with(Properties.LIT, true));
         lv.emitGameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, lv2);
         bl = true;
      }

      if (bl) {
         context.getStack().decrement(1);
         return ActionResult.success(lv.isClient);
      } else {
         return ActionResult.FAIL;
      }
   }

   private void playUseSound(World world, BlockPos pos) {
      Random lv = world.getRandom();
      world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (lv.nextFloat() - lv.nextFloat()) * 0.2F + 1.0F);
   }
}
