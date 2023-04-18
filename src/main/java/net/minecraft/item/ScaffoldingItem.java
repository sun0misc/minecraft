package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ScaffoldingItem extends BlockItem {
   public ScaffoldingItem(Block arg, Item.Settings arg2) {
      super(arg, arg2);
   }

   @Nullable
   public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
      BlockPos lv = context.getBlockPos();
      World lv2 = context.getWorld();
      BlockState lv3 = lv2.getBlockState(lv);
      Block lv4 = this.getBlock();
      if (!lv3.isOf(lv4)) {
         return ScaffoldingBlock.calculateDistance(lv2, lv) == 7 ? null : context;
      } else {
         Direction lv5;
         if (context.shouldCancelInteraction()) {
            lv5 = context.hitsInsideBlock() ? context.getSide().getOpposite() : context.getSide();
         } else {
            lv5 = context.getSide() == Direction.UP ? context.getHorizontalPlayerFacing() : Direction.UP;
         }

         int i = 0;
         BlockPos.Mutable lv6 = lv.mutableCopy().move(lv5);

         while(i < 7) {
            if (!lv2.isClient && !lv2.isInBuildLimit(lv6)) {
               PlayerEntity lv7 = context.getPlayer();
               int j = lv2.getTopY();
               if (lv7 instanceof ServerPlayerEntity && lv6.getY() >= j) {
                  ((ServerPlayerEntity)lv7).sendMessageToClient(Text.translatable("build.tooHigh", j - 1).formatted(Formatting.RED), true);
               }
               break;
            }

            lv3 = lv2.getBlockState(lv6);
            if (!lv3.isOf(this.getBlock())) {
               if (lv3.canReplace(context)) {
                  return ItemPlacementContext.offset(context, lv6, lv5);
               }
               break;
            }

            lv6.move(lv5);
            if (lv5.getAxis().isHorizontal()) {
               ++i;
            }
         }

         return null;
      }
   }

   protected boolean checkStatePlacement() {
      return false;
   }
}
