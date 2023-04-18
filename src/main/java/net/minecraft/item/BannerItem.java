package net.minecraft.item;

import java.util.List;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class BannerItem extends VerticallyAttachableBlockItem {
   private static final String TRANSLATION_KEY_PREFIX = "block.minecraft.banner.";

   public BannerItem(Block bannerBlock, Block wallBannerBlock, Item.Settings settings) {
      super(bannerBlock, wallBannerBlock, settings, Direction.DOWN);
      Validate.isInstanceOf(AbstractBannerBlock.class, bannerBlock);
      Validate.isInstanceOf(AbstractBannerBlock.class, wallBannerBlock);
   }

   public static void appendBannerTooltip(ItemStack stack, List tooltip) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv != null && lv.contains("Patterns")) {
         NbtList lv2 = lv.getList("Patterns", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv2.size() && i < 6; ++i) {
            NbtCompound lv3 = lv2.getCompound(i);
            DyeColor lv4 = DyeColor.byId(lv3.getInt("Color"));
            RegistryEntry lv5 = BannerPattern.byId(lv3.getString("Pattern"));
            if (lv5 != null) {
               lv5.getKey().map((key) -> {
                  return key.getValue().toShortTranslationKey();
               }).ifPresent((translationKey) -> {
                  tooltip.add(Text.translatable("block.minecraft.banner." + translationKey + "." + lv4.getName()).formatted(Formatting.GRAY));
               });
            }
         }

      }
   }

   public DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      appendBannerTooltip(stack, tooltip);
   }
}
