package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BannerPatternItem extends Item {
   private final TagKey patternItemTag;

   public BannerPatternItem(TagKey patternItemTag, Item.Settings settings) {
      super(settings);
      this.patternItemTag = patternItemTag;
   }

   public TagKey getPattern() {
      return this.patternItemTag;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      tooltip.add(this.getDescription().formatted(Formatting.GRAY));
   }

   public MutableText getDescription() {
      return Text.translatable(this.getTranslationKey() + ".desc");
   }
}
