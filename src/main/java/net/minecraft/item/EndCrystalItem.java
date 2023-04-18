package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class EndCrystalItem extends Item {
   public EndCrystalItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (!lv3.isOf(Blocks.OBSIDIAN) && !lv3.isOf(Blocks.BEDROCK)) {
         return ActionResult.FAIL;
      } else {
         BlockPos lv4 = lv2.up();
         if (!lv.isAir(lv4)) {
            return ActionResult.FAIL;
         } else {
            double d = (double)lv4.getX();
            double e = (double)lv4.getY();
            double f = (double)lv4.getZ();
            List list = lv.getOtherEntities((Entity)null, new Box(d, e, f, d + 1.0, e + 2.0, f + 1.0));
            if (!list.isEmpty()) {
               return ActionResult.FAIL;
            } else {
               if (lv instanceof ServerWorld) {
                  EndCrystalEntity lv5 = new EndCrystalEntity(lv, d + 0.5, e, f + 0.5);
                  lv5.setShowBottom(false);
                  lv.spawnEntity(lv5);
                  lv.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, lv4);
                  EnderDragonFight lv6 = ((ServerWorld)lv).getEnderDragonFight();
                  if (lv6 != null) {
                     lv6.respawnDragon();
                  }
               }

               context.getStack().decrement(1);
               return ActionResult.success(lv.isClient);
            }
         }
      }
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }
}
