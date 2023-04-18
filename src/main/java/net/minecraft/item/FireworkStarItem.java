package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireworkStarItem extends Item {
   public FireworkStarItem(Item.Settings arg) {
      super(arg);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      NbtCompound lv = stack.getSubNbt("Explosion");
      if (lv != null) {
         appendFireworkTooltip(lv, tooltip);
      }

   }

   public static void appendFireworkTooltip(NbtCompound nbt, List tooltip) {
      FireworkRocketItem.Type lv = FireworkRocketItem.Type.byId(nbt.getByte("Type"));
      tooltip.add(Text.translatable("item.minecraft.firework_star.shape." + lv.getName()).formatted(Formatting.GRAY));
      int[] is = nbt.getIntArray("Colors");
      if (is.length > 0) {
         tooltip.add(appendColors(Text.empty().formatted(Formatting.GRAY), is));
      }

      int[] js = nbt.getIntArray("FadeColors");
      if (js.length > 0) {
         tooltip.add(appendColors(Text.translatable("item.minecraft.firework_star.fade_to").append(ScreenTexts.SPACE).formatted(Formatting.GRAY), js));
      }

      if (nbt.getBoolean("Trail")) {
         tooltip.add(Text.translatable("item.minecraft.firework_star.trail").formatted(Formatting.GRAY));
      }

      if (nbt.getBoolean("Flicker")) {
         tooltip.add(Text.translatable("item.minecraft.firework_star.flicker").formatted(Formatting.GRAY));
      }

   }

   private static Text appendColors(MutableText line, int[] colors) {
      for(int i = 0; i < colors.length; ++i) {
         if (i > 0) {
            line.append(", ");
         }

         line.append(getColorText(colors[i]));
      }

      return line;
   }

   private static Text getColorText(int color) {
      DyeColor lv = DyeColor.byFireworkColor(color);
      return lv == null ? Text.translatable("item.minecraft.firework_star.custom_color") : Text.translatable("item.minecraft.firework_star." + lv.getName());
   }
}
