package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FlintAndSteelItem extends Item {
   public FlintAndSteelItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity lv = context.getPlayer();
      World lv2 = context.getWorld();
      BlockPos lv3 = context.getBlockPos();
      BlockState lv4 = lv2.getBlockState(lv3);
      if (!CampfireBlock.canBeLit(lv4) && !CandleBlock.canBeLit(lv4) && !CandleCakeBlock.canBeLit(lv4)) {
         BlockPos lv5 = lv3.offset(context.getSide());
         if (AbstractFireBlock.canPlaceAt(lv2, lv5, context.getHorizontalPlayerFacing())) {
            lv2.playSound(lv, lv5, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, lv2.getRandom().nextFloat() * 0.4F + 0.8F);
            BlockState lv6 = AbstractFireBlock.getState(lv2, lv5);
            lv2.setBlockState(lv5, lv6, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            lv2.emitGameEvent(lv, GameEvent.BLOCK_PLACE, lv3);
            ItemStack lv7 = context.getStack();
            if (lv instanceof ServerPlayerEntity) {
               Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)lv, lv5, lv7);
               lv7.damage(1, (LivingEntity)lv, (Consumer)((p) -> {
                  p.sendToolBreakStatus(context.getHand());
               }));
            }

            return ActionResult.success(lv2.isClient());
         } else {
            return ActionResult.FAIL;
         }
      } else {
         lv2.playSound(lv, lv3, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, lv2.getRandom().nextFloat() * 0.4F + 0.8F);
         lv2.setBlockState(lv3, (BlockState)lv4.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         lv2.emitGameEvent(lv, GameEvent.BLOCK_CHANGE, lv3);
         if (lv != null) {
            context.getStack().damage(1, (LivingEntity)lv, (Consumer)((p) -> {
               p.sendToolBreakStatus(context.getHand());
            }));
         }

         return ActionResult.success(lv2.isClient());
      }
   }
}
