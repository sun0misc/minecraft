package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public interface DyeableItem {
   String COLOR_KEY = "color";
   String DISPLAY_KEY = "display";
   int DEFAULT_COLOR = 10511680;

   default boolean hasColor(ItemStack stack) {
      NbtCompound lv = stack.getSubNbt("display");
      return lv != null && lv.contains("color", NbtElement.NUMBER_TYPE);
   }

   default int getColor(ItemStack stack) {
      NbtCompound lv = stack.getSubNbt("display");
      return lv != null && lv.contains("color", NbtElement.NUMBER_TYPE) ? lv.getInt("color") : 10511680;
   }

   default void removeColor(ItemStack stack) {
      NbtCompound lv = stack.getSubNbt("display");
      if (lv != null && lv.contains("color")) {
         lv.remove("color");
      }

   }

   default void setColor(ItemStack stack, int color) {
      stack.getOrCreateSubNbt("display").putInt("color", color);
   }

   static ItemStack blendAndSetColor(ItemStack stack, List colors) {
      ItemStack lv = ItemStack.EMPTY;
      int[] is = new int[3];
      int i = 0;
      int j = 0;
      Item lv3 = stack.getItem();
      int k;
      float h;
      int n;
      if (lv3 instanceof DyeableItem lv2) {
         lv = stack.copyWithCount(1);
         if (lv2.hasColor(stack)) {
            k = lv2.getColor(lv);
            float f = (float)(k >> 16 & 255) / 255.0F;
            float g = (float)(k >> 8 & 255) / 255.0F;
            h = (float)(k & 255) / 255.0F;
            i += (int)(Math.max(f, Math.max(g, h)) * 255.0F);
            is[0] += (int)(f * 255.0F);
            is[1] += (int)(g * 255.0F);
            is[2] += (int)(h * 255.0F);
            ++j;
         }

         for(Iterator var14 = colors.iterator(); var14.hasNext(); ++j) {
            DyeItem lv4 = (DyeItem)var14.next();
            float[] fs = lv4.getColor().getColorComponents();
            int l = (int)(fs[0] * 255.0F);
            int m = (int)(fs[1] * 255.0F);
            n = (int)(fs[2] * 255.0F);
            i += Math.max(l, Math.max(m, n));
            is[0] += l;
            is[1] += m;
            is[2] += n;
         }
      }

      if (lv2 == null) {
         return ItemStack.EMPTY;
      } else {
         k = is[0] / j;
         int o = is[1] / j;
         int p = is[2] / j;
         h = (float)i / (float)j;
         float q = (float)Math.max(k, Math.max(o, p));
         k = (int)((float)k * h / q);
         o = (int)((float)o * h / q);
         p = (int)((float)p * h / q);
         n = (k << 8) + o;
         n = (n << 8) + p;
         lv2.setColor(lv, n);
         return lv;
      }
   }
}
