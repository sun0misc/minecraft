package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FishingRodItem extends Item implements Vanishable {
   public FishingRodItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      int i;
      if (user.fishHook != null) {
         if (!world.isClient) {
            i = user.fishHook.use(lv);
            lv.damage(i, (LivingEntity)user, (Consumer)((p) -> {
               p.sendToolBreakStatus(hand);
            }));
         }

         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
         user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
      } else {
         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
         if (!world.isClient) {
            i = EnchantmentHelper.getLure(lv);
            int j = EnchantmentHelper.getLuckOfTheSea(lv);
            world.spawnEntity(new FishingBobberEntity(user, world, j, i));
         }

         user.incrementStat(Stats.USED.getOrCreateStat(this));
         user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
      }

      return TypedActionResult.success(lv, world.isClient());
   }

   public int getEnchantability() {
      return 1;
   }
}
