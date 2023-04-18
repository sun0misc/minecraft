package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TippedArrowItem extends ArrowItem {
   public TippedArrowItem(Item.Settings arg) {
      super(arg);
   }

   public ItemStack getDefaultStack() {
      return PotionUtil.setPotion(super.getDefaultStack(), Potions.POISON);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      PotionUtil.buildTooltip(stack, tooltip, 0.125F);
   }

   public String getTranslationKey(ItemStack stack) {
      return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
   }
}
