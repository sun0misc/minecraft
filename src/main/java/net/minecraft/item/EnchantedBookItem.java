package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookItem extends Item {
   public static final String STORED_ENCHANTMENTS_KEY = "StoredEnchantments";

   public EnchantedBookItem(Item.Settings arg) {
      super(arg);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public static NbtList getEnchantmentNbt(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null ? lv.getList("StoredEnchantments", NbtElement.COMPOUND_TYPE) : new NbtList();
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      ItemStack.appendEnchantments(tooltip, getEnchantmentNbt(stack));
   }

   public static void addEnchantment(ItemStack stack, EnchantmentLevelEntry entry) {
      NbtList lv = getEnchantmentNbt(stack);
      boolean bl = true;
      Identifier lv2 = EnchantmentHelper.getEnchantmentId(entry.enchantment);

      for(int i = 0; i < lv.size(); ++i) {
         NbtCompound lv3 = lv.getCompound(i);
         Identifier lv4 = EnchantmentHelper.getIdFromNbt(lv3);
         if (lv4 != null && lv4.equals(lv2)) {
            if (EnchantmentHelper.getLevelFromNbt(lv3) < entry.level) {
               EnchantmentHelper.writeLevelToNbt(lv3, entry.level);
            }

            bl = false;
            break;
         }
      }

      if (bl) {
         lv.add(EnchantmentHelper.createNbt(lv2, entry.level));
      }

      stack.getOrCreateNbt().put("StoredEnchantments", lv);
   }

   public static ItemStack forEnchantment(EnchantmentLevelEntry info) {
      ItemStack lv = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(lv, info);
      return lv;
   }
}
