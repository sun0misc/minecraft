package net.minecraft.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WritableBookItem extends Item {
   public WritableBookItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (lv3.isOf(Blocks.LECTERN)) {
         return LecternBlock.putBookIfAbsent(context.getPlayer(), lv, lv2, lv3, context.getStack()) ? ActionResult.success(lv.isClient) : ActionResult.PASS;
      } else {
         return ActionResult.PASS;
      }
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      user.useBook(lv, hand);
      user.incrementStat(Stats.USED.getOrCreateStat(this));
      return TypedActionResult.success(lv, world.isClient());
   }

   public static boolean isValid(@Nullable NbtCompound nbt) {
      if (nbt == null) {
         return false;
      } else if (!nbt.contains("pages", NbtElement.LIST_TYPE)) {
         return false;
      } else {
         NbtList lv = nbt.getList("pages", NbtElement.STRING_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            String string = lv.getString(i);
            if (string.length() > 32767) {
               return false;
            }
         }

         return true;
      }
   }
}
