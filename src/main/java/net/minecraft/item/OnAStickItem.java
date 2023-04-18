package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class OnAStickItem extends Item {
   private final EntityType target;
   private final int damagePerUse;

   public OnAStickItem(Item.Settings settings, EntityType target, int damagePerUse) {
      super(settings);
      this.target = target;
      this.damagePerUse = damagePerUse;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (world.isClient) {
         return TypedActionResult.pass(lv);
      } else {
         Entity lv2 = user.getControllingVehicle();
         if (user.hasVehicle() && lv2 instanceof ItemSteerable) {
            ItemSteerable lv3 = (ItemSteerable)lv2;
            if (lv2.getType() == this.target && lv3.consumeOnAStickItem()) {
               lv.damage(this.damagePerUse, (LivingEntity)user, (Consumer)((p) -> {
                  p.sendToolBreakStatus(hand);
               }));
               if (lv.isEmpty()) {
                  ItemStack lv4 = new ItemStack(Items.FISHING_ROD);
                  lv4.setNbt(lv.getNbt());
                  return TypedActionResult.success(lv4);
               }

               return TypedActionResult.success(lv);
            }
         }

         user.incrementStat(Stats.USED.getOrCreateStat(this));
         return TypedActionResult.pass(lv);
      }
   }
}
