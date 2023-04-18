package net.minecraft.client.color.item;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
public class ItemColors {
   private static final int NO_COLOR = -1;
   private final IdList providers = new IdList(32);

   public static ItemColors create(BlockColors blockColors) {
      ItemColors lv = new ItemColors();
      lv.register((stack, tintIndex) -> {
         return tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack);
      }, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
      lv.register((stack, tintIndex) -> {
         return GrassColors.getColor(0.5, 1.0);
      }, Blocks.TALL_GRASS, Blocks.LARGE_FERN);
      lv.register((stack, tintIndex) -> {
         if (tintIndex != 1) {
            return -1;
         } else {
            NbtCompound lv = stack.getSubNbt("Explosion");
            int[] is = lv != null && lv.contains("Colors", NbtElement.INT_ARRAY_TYPE) ? lv.getIntArray("Colors") : null;
            if (is != null && is.length != 0) {
               if (is.length == 1) {
                  return is[0];
               } else {
                  int j = 0;
                  int k = 0;
                  int l = 0;
                  int[] var7 = is;
                  int var8 = is.length;

                  for(int var9 = 0; var9 < var8; ++var9) {
                     int m = var7[var9];
                     j += (m & 16711680) >> 16;
                     k += (m & '\uff00') >> 8;
                     l += (m & 255) >> 0;
                  }

                  j /= is.length;
                  k /= is.length;
                  l /= is.length;
                  return j << 16 | k << 8 | l;
               }
            } else {
               return 9079434;
            }
         }
      }, Items.FIREWORK_STAR);
      lv.register((stack, tintIndex) -> {
         return tintIndex > 0 ? -1 : PotionUtil.getColor(stack);
      }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
      Iterator var2 = SpawnEggItem.getAll().iterator();

      while(var2.hasNext()) {
         SpawnEggItem lv2 = (SpawnEggItem)var2.next();
         lv.register((stack, tintIndex) -> {
            return lv2.getColor(tintIndex);
         }, lv2);
      }

      lv.register((stack, tintIndex) -> {
         BlockState lv = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
         return blockColors.getColor(lv, (BlockRenderView)null, (BlockPos)null, tintIndex);
      }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
      lv.register((stack, tintIndex) -> {
         return FoliageColors.getMangroveColor();
      }, Blocks.MANGROVE_LEAVES);
      lv.register((stack, tintIndex) -> {
         return tintIndex == 0 ? PotionUtil.getColor(stack) : -1;
      }, Items.TIPPED_ARROW);
      lv.register((stack, tintIndex) -> {
         return tintIndex == 0 ? -1 : FilledMapItem.getMapColor(stack);
      }, Items.FILLED_MAP);
      return lv;
   }

   public int getColor(ItemStack item, int tintIndex) {
      ItemColorProvider lv = (ItemColorProvider)this.providers.get(Registries.ITEM.getRawId(item.getItem()));
      return lv == null ? -1 : lv.getColor(item, tintIndex);
   }

   public void register(ItemColorProvider provider, ItemConvertible... items) {
      ItemConvertible[] var3 = items;
      int var4 = items.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemConvertible lv = var3[var5];
         this.providers.set(provider, Item.getRawId(lv.asItem()));
      }

   }
}
