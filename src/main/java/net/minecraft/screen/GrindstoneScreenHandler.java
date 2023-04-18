package net.minecraft.screen;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class GrindstoneScreenHandler extends ScreenHandler {
   public static final int field_30793 = 35;
   public static final int field_30794 = 0;
   public static final int field_30795 = 1;
   public static final int field_30796 = 2;
   private static final int field_30797 = 3;
   private static final int field_30798 = 30;
   private static final int field_30799 = 30;
   private static final int field_30800 = 39;
   private final Inventory result;
   final Inventory input;
   private final ScreenHandlerContext context;

   public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public GrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
      super(ScreenHandlerType.GRINDSTONE, syncId);
      this.result = new CraftingResultInventory();
      this.input = new SimpleInventory(2) {
         public void markDirty() {
            super.markDirty();
            GrindstoneScreenHandler.this.onContentChanged(this);
         }
      };
      this.context = context;
      this.addSlot(new Slot(this.input, 0, 49, 19) {
         public boolean canInsert(ItemStack stack) {
            return stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments();
         }
      });
      this.addSlot(new Slot(this.input, 1, 49, 40) {
         public boolean canInsert(ItemStack stack) {
            return stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments();
         }
      });
      this.addSlot(new Slot(this.result, 2, 129, 34) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            context.run((world, pos) -> {
               if (world instanceof ServerWorld) {
                  ExperienceOrbEntity.spawn((ServerWorld)world, Vec3d.ofCenter(pos), this.getExperience(world));
               }

               world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, pos, 0);
            });
            GrindstoneScreenHandler.this.input.setStack(0, ItemStack.EMPTY);
            GrindstoneScreenHandler.this.input.setStack(1, ItemStack.EMPTY);
         }

         private int getExperience(World world) {
            int i = 0;
            i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(0));
            i += this.getExperience(GrindstoneScreenHandler.this.input.getStack(1));
            if (i > 0) {
               int j = (int)Math.ceil((double)i / 2.0);
               return j + world.random.nextInt(j);
            } else {
               return 0;
            }
         }

         private int getExperience(ItemStack stack) {
            int i = 0;
            Map map = EnchantmentHelper.get(stack);
            Iterator var4 = map.entrySet().iterator();

            while(var4.hasNext()) {
               Map.Entry entry = (Map.Entry)var4.next();
               Enchantment lv = (Enchantment)entry.getKey();
               Integer integer = (Integer)entry.getValue();
               if (!lv.isCursed()) {
                  i += lv.getMinPower(integer);
               }
            }

            return i;
         }
      });

      int j;
      for(j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

   }

   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      if (inventory == this.input) {
         this.updateResult();
      }

   }

   private void updateResult() {
      ItemStack lv = this.input.getStack(0);
      ItemStack lv2 = this.input.getStack(1);
      boolean bl = !lv.isEmpty() || !lv2.isEmpty();
      boolean bl2 = !lv.isEmpty() && !lv2.isEmpty();
      if (!bl) {
         this.result.setStack(0, ItemStack.EMPTY);
      } else {
         boolean bl3 = !lv.isEmpty() && !lv.isOf(Items.ENCHANTED_BOOK) && !lv.hasEnchantments() || !lv2.isEmpty() && !lv2.isOf(Items.ENCHANTED_BOOK) && !lv2.hasEnchantments();
         if (lv.getCount() > 1 || lv2.getCount() > 1 || !bl2 && bl3) {
            this.result.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            return;
         }

         int i = 1;
         int m;
         ItemStack lv4;
         if (bl2) {
            if (!lv.isOf(lv2.getItem())) {
               this.result.setStack(0, ItemStack.EMPTY);
               this.sendContentUpdates();
               return;
            }

            Item lv3 = lv.getItem();
            int j = lv3.getMaxDamage() - lv.getDamage();
            int k = lv3.getMaxDamage() - lv2.getDamage();
            int l = j + k + lv3.getMaxDamage() * 5 / 100;
            m = Math.max(lv3.getMaxDamage() - l, 0);
            lv4 = this.transferEnchantments(lv, lv2);
            if (!lv4.isDamageable()) {
               if (!ItemStack.areEqual(lv, lv2)) {
                  this.result.setStack(0, ItemStack.EMPTY);
                  this.sendContentUpdates();
                  return;
               }

               i = 2;
            }
         } else {
            boolean bl4 = !lv.isEmpty();
            m = bl4 ? lv.getDamage() : lv2.getDamage();
            lv4 = bl4 ? lv : lv2;
         }

         this.result.setStack(0, this.grind(lv4, m, i));
      }

      this.sendContentUpdates();
   }

   private ItemStack transferEnchantments(ItemStack target, ItemStack source) {
      ItemStack lv = target.copy();
      Map map = EnchantmentHelper.get(source);
      Iterator var5 = map.entrySet().iterator();

      while(true) {
         Map.Entry entry;
         Enchantment lv2;
         do {
            if (!var5.hasNext()) {
               return lv;
            }

            entry = (Map.Entry)var5.next();
            lv2 = (Enchantment)entry.getKey();
         } while(lv2.isCursed() && EnchantmentHelper.getLevel(lv2, lv) != 0);

         lv.addEnchantment(lv2, (Integer)entry.getValue());
      }
   }

   private ItemStack grind(ItemStack item, int damage, int amount) {
      ItemStack lv = item.copyWithCount(amount);
      lv.removeSubNbt("Enchantments");
      lv.removeSubNbt("StoredEnchantments");
      if (damage > 0) {
         lv.setDamage(damage);
      } else {
         lv.removeSubNbt("Damage");
      }

      Map map = (Map)EnchantmentHelper.get(item).entrySet().stream().filter((entry) -> {
         return ((Enchantment)entry.getKey()).isCursed();
      }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      EnchantmentHelper.set(map, lv);
      lv.setRepairCost(0);
      if (lv.isOf(Items.ENCHANTED_BOOK) && map.size() == 0) {
         lv = new ItemStack(Items.BOOK);
         if (item.hasCustomName()) {
            lv.setCustomName(item.getName());
         }
      }

      for(int k = 0; k < map.size(); ++k) {
         lv.setRepairCost(AnvilScreenHandler.getNextCost(lv.getRepairCost()));
      }

      return lv;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.input);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.GRINDSTONE);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         ItemStack lv4 = this.input.getStack(0);
         ItemStack lv5 = this.input.getStack(1);
         if (slot == 2) {
            if (!this.insertItem(lv3, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot != 0 && slot != 1) {
            if (!lv4.isEmpty() && !lv5.isEmpty()) {
               if (slot >= 3 && slot < 30) {
                  if (!this.insertItem(lv3, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(lv3, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         } else {
            lv2.markDirty();
         }

         if (lv3.getCount() == lv.getCount()) {
            return ItemStack.EMPTY;
         }

         lv2.onTakeItem(player, lv3);
      }

      return lv;
   }
}
