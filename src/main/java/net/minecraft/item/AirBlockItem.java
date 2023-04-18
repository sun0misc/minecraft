package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AirBlockItem extends Item {
   private final Block block;

   public AirBlockItem(Block block, Item.Settings settings) {
      super(settings);
      this.block = block;
   }

   public String getTranslationKey() {
      return this.block.getTranslationKey();
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      this.block.appendTooltip(stack, world, tooltip, context);
   }
}
