package net.minecraft.screen;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.text.Text;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class AnvilScreenHandler extends ForgingScreenHandler {
   public static final int field_41898 = 0;
   public static final int field_41899 = 1;
   public static final int field_41900 = 2;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean field_30752 = false;
   public static final int field_30751 = 50;
   private int repairItemUsage;
   private String newItemName;
   private final Property levelCost;
   private static final int field_30753 = 0;
   private static final int field_30754 = 1;
   private static final int field_30755 = 1;
   private static final int field_30747 = 1;
   private static final int field_30748 = 2;
   private static final int field_30749 = 1;
   private static final int field_30750 = 1;
   private static final int field_41894 = 27;
   private static final int field_41895 = 76;
   private static final int field_41896 = 134;
   private static final int field_41897 = 47;

   public AnvilScreenHandler(int syncId, PlayerInventory inventory) {
      this(syncId, inventory, ScreenHandlerContext.EMPTY);
   }

   public AnvilScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
      super(ScreenHandlerType.ANVIL, syncId, inventory, context);
      this.levelCost = Property.create();
      this.addProperty(this.levelCost);
   }

   protected ForgingSlotsManager getForgingSlotsManager() {
      return ForgingSlotsManager.create().input(0, 27, 47, (stack) -> {
         return true;
      }).input(1, 76, 47, (stack) -> {
         return true;
      }).output(2, 134, 47).build();
   }

   protected boolean canUse(BlockState state) {
      return state.isIn(BlockTags.ANVIL);
   }

   protected boolean canTakeOutput(PlayerEntity player, boolean present) {
      return (player.getAbilities().creativeMode || player.experienceLevel >= this.levelCost.get()) && this.levelCost.get() > 0;
   }

   protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
      if (!player.getAbilities().creativeMode) {
         player.addExperienceLevels(-this.levelCost.get());
      }

      this.input.setStack(0, ItemStack.EMPTY);
      if (this.repairItemUsage > 0) {
         ItemStack lv = this.input.getStack(1);
         if (!lv.isEmpty() && lv.getCount() > this.repairItemUsage) {
            lv.decrement(this.repairItemUsage);
            this.input.setStack(1, lv);
         } else {
            this.input.setStack(1, ItemStack.EMPTY);
         }
      } else {
         this.input.setStack(1, ItemStack.EMPTY);
      }

      this.levelCost.set(0);
      this.context.run((world, pos) -> {
         BlockState lv = world.getBlockState(pos);
         if (!player.getAbilities().creativeMode && lv.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
            BlockState lv2 = AnvilBlock.getLandingState(lv);
            if (lv2 == null) {
               world.removeBlock(pos, false);
               world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
            } else {
               world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
               world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
            }
         } else {
            world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
         }

      });
   }

   public void updateResult() {
      ItemStack lv = this.input.getStack(0);
      this.levelCost.set(1);
      int i = 0;
      int j = 0;
      int k = 0;
      if (lv.isEmpty()) {
         this.output.setStack(0, ItemStack.EMPTY);
         this.levelCost.set(0);
      } else {
         ItemStack lv2 = lv.copy();
         ItemStack lv3 = this.input.getStack(1);
         Map map = EnchantmentHelper.get(lv2);
         j += lv.getRepairCost() + (lv3.isEmpty() ? 0 : lv3.getRepairCost());
         this.repairItemUsage = 0;
         if (!lv3.isEmpty()) {
            boolean bl = lv3.isOf(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantmentNbt(lv3).isEmpty();
            int l;
            int m;
            int n;
            if (lv2.isDamageable() && lv2.getItem().canRepair(lv, lv3)) {
               l = Math.min(lv2.getDamage(), lv2.getMaxDamage() / 4);
               if (l <= 0) {
                  this.output.setStack(0, ItemStack.EMPTY);
                  this.levelCost.set(0);
                  return;
               }

               for(m = 0; l > 0 && m < lv3.getCount(); ++m) {
                  n = lv2.getDamage() - l;
                  lv2.setDamage(n);
                  ++i;
                  l = Math.min(lv2.getDamage(), lv2.getMaxDamage() / 4);
               }

               this.repairItemUsage = m;
            } else {
               if (!bl && (!lv2.isOf(lv3.getItem()) || !lv2.isDamageable())) {
                  this.output.setStack(0, ItemStack.EMPTY);
                  this.levelCost.set(0);
                  return;
               }

               if (lv2.isDamageable() && !bl) {
                  l = lv.getMaxDamage() - lv.getDamage();
                  m = lv3.getMaxDamage() - lv3.getDamage();
                  n = m + lv2.getMaxDamage() * 12 / 100;
                  int o = l + n;
                  int p = lv2.getMaxDamage() - o;
                  if (p < 0) {
                     p = 0;
                  }

                  if (p < lv2.getDamage()) {
                     lv2.setDamage(p);
                     i += 2;
                  }
               }

               Map map2 = EnchantmentHelper.get(lv3);
               boolean bl2 = false;
               boolean bl3 = false;
               Iterator var23 = map2.keySet().iterator();

               label155:
               while(true) {
                  Enchantment lv4;
                  do {
                     if (!var23.hasNext()) {
                        if (bl3 && !bl2) {
                           this.output.setStack(0, ItemStack.EMPTY);
                           this.levelCost.set(0);
                           return;
                        }
                        break label155;
                     }

                     lv4 = (Enchantment)var23.next();
                  } while(lv4 == null);

                  int q = (Integer)map.getOrDefault(lv4, 0);
                  int r = (Integer)map2.get(lv4);
                  r = q == r ? r + 1 : Math.max(r, q);
                  boolean bl4 = lv4.isAcceptableItem(lv);
                  if (this.player.getAbilities().creativeMode || lv.isOf(Items.ENCHANTED_BOOK)) {
                     bl4 = true;
                  }

                  Iterator var17 = map.keySet().iterator();

                  while(var17.hasNext()) {
                     Enchantment lv5 = (Enchantment)var17.next();
                     if (lv5 != lv4 && !lv4.canCombine(lv5)) {
                        bl4 = false;
                        ++i;
                     }
                  }

                  if (!bl4) {
                     bl3 = true;
                  } else {
                     bl2 = true;
                     if (r > lv4.getMaxLevel()) {
                        r = lv4.getMaxLevel();
                     }

                     map.put(lv4, r);
                     int s = 0;
                     switch (lv4.getRarity()) {
                        case COMMON:
                           s = 1;
                           break;
                        case UNCOMMON:
                           s = 2;
                           break;
                        case RARE:
                           s = 4;
                           break;
                        case VERY_RARE:
                           s = 8;
                     }

                     if (bl) {
                        s = Math.max(1, s / 2);
                     }

                     i += s * r;
                     if (lv.getCount() > 1) {
                        i = 40;
                     }
                  }
               }
            }
         }

         if (StringUtils.isBlank(this.newItemName)) {
            if (lv.hasCustomName()) {
               k = 1;
               i += k;
               lv2.removeCustomName();
            }
         } else if (!this.newItemName.equals(lv.getName().getString())) {
            k = 1;
            i += k;
            lv2.setCustomName(Text.literal(this.newItemName));
         }

         this.levelCost.set(j + i);
         if (i <= 0) {
            lv2 = ItemStack.EMPTY;
         }

         if (k == i && k > 0 && this.levelCost.get() >= 40) {
            this.levelCost.set(39);
         }

         if (this.levelCost.get() >= 40 && !this.player.getAbilities().creativeMode) {
            lv2 = ItemStack.EMPTY;
         }

         if (!lv2.isEmpty()) {
            int t = lv2.getRepairCost();
            if (!lv3.isEmpty() && t < lv3.getRepairCost()) {
               t = lv3.getRepairCost();
            }

            if (k != i || k == 0) {
               t = getNextCost(t);
            }

            lv2.setRepairCost(t);
            EnchantmentHelper.set(map, lv2);
         }

         this.output.setStack(0, lv2);
         this.sendContentUpdates();
      }
   }

   public static int getNextCost(int cost) {
      return cost * 2 + 1;
   }

   public void setNewItemName(String newItemName) {
      this.newItemName = newItemName;
      if (this.getSlot(2).hasStack()) {
         ItemStack lv = this.getSlot(2).getStack();
         if (StringUtils.isBlank(newItemName)) {
            lv.removeCustomName();
         } else {
            lv.setCustomName(Text.literal(this.newItemName));
         }
      }

      this.updateResult();
   }

   public int getLevelCost() {
      return this.levelCost.get();
   }
}
