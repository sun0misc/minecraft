package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class PlayerInventory implements Inventory, Nameable {
   public static final int ITEM_USAGE_COOLDOWN = 5;
   public static final int MAIN_SIZE = 36;
   private static final int HOTBAR_SIZE = 9;
   public static final int OFF_HAND_SLOT = 40;
   public static final int NOT_FOUND = -1;
   public static final int[] ARMOR_SLOTS = new int[]{0, 1, 2, 3};
   public static final int[] HELMET_SLOTS = new int[]{3};
   public final DefaultedList main;
   public final DefaultedList armor;
   public final DefaultedList offHand;
   private final List combinedInventory;
   public int selectedSlot;
   public final PlayerEntity player;
   private int changeCount;

   public PlayerInventory(PlayerEntity player) {
      this.main = DefaultedList.ofSize(36, ItemStack.EMPTY);
      this.armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
      this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
      this.player = player;
   }

   public ItemStack getMainHandStack() {
      return isValidHotbarIndex(this.selectedSlot) ? (ItemStack)this.main.get(this.selectedSlot) : ItemStack.EMPTY;
   }

   public static int getHotbarSize() {
      return 9;
   }

   private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
      return !existingStack.isEmpty() && ItemStack.canCombine(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < this.getMaxCountPerStack();
   }

   public int getEmptySlot() {
      for(int i = 0; i < this.main.size(); ++i) {
         if (((ItemStack)this.main.get(i)).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public void addPickBlock(ItemStack stack) {
      int i = this.getSlotWithStack(stack);
      if (isValidHotbarIndex(i)) {
         this.selectedSlot = i;
      } else {
         if (i == -1) {
            this.selectedSlot = this.getSwappableHotbarSlot();
            if (!((ItemStack)this.main.get(this.selectedSlot)).isEmpty()) {
               int j = this.getEmptySlot();
               if (j != -1) {
                  this.main.set(j, (ItemStack)this.main.get(this.selectedSlot));
               }
            }

            this.main.set(this.selectedSlot, stack);
         } else {
            this.swapSlotWithHotbar(i);
         }

      }
   }

   public void swapSlotWithHotbar(int slot) {
      this.selectedSlot = this.getSwappableHotbarSlot();
      ItemStack lv = (ItemStack)this.main.get(this.selectedSlot);
      this.main.set(this.selectedSlot, (ItemStack)this.main.get(slot));
      this.main.set(slot, lv);
   }

   public static boolean isValidHotbarIndex(int slot) {
      return slot >= 0 && slot < 9;
   }

   public int getSlotWithStack(ItemStack stack) {
      for(int i = 0; i < this.main.size(); ++i) {
         if (!((ItemStack)this.main.get(i)).isEmpty() && ItemStack.canCombine(stack, (ItemStack)this.main.get(i))) {
            return i;
         }
      }

      return -1;
   }

   public int indexOf(ItemStack stack) {
      for(int i = 0; i < this.main.size(); ++i) {
         ItemStack lv = (ItemStack)this.main.get(i);
         if (!((ItemStack)this.main.get(i)).isEmpty() && ItemStack.canCombine(stack, (ItemStack)this.main.get(i)) && !((ItemStack)this.main.get(i)).isDamaged() && !lv.hasEnchantments() && !lv.hasCustomName()) {
            return i;
         }
      }

      return -1;
   }

   public int getSwappableHotbarSlot() {
      int i;
      int j;
      for(i = 0; i < 9; ++i) {
         j = (this.selectedSlot + i) % 9;
         if (((ItemStack)this.main.get(j)).isEmpty()) {
            return j;
         }
      }

      for(i = 0; i < 9; ++i) {
         j = (this.selectedSlot + i) % 9;
         if (!((ItemStack)this.main.get(j)).hasEnchantments()) {
            return j;
         }
      }

      return this.selectedSlot;
   }

   public void scrollInHotbar(double scrollAmount) {
      int i = (int)Math.signum(scrollAmount);

      for(this.selectedSlot -= i; this.selectedSlot < 0; this.selectedSlot += 9) {
      }

      while(this.selectedSlot >= 9) {
         this.selectedSlot -= 9;
      }

   }

   public int remove(Predicate shouldRemove, int maxCount, Inventory craftingInventory) {
      int j = 0;
      boolean bl = maxCount == 0;
      j += Inventories.remove((Inventory)this, shouldRemove, maxCount - j, bl);
      j += Inventories.remove(craftingInventory, shouldRemove, maxCount - j, bl);
      ItemStack lv = this.player.currentScreenHandler.getCursorStack();
      j += Inventories.remove(lv, shouldRemove, maxCount - j, bl);
      if (lv.isEmpty()) {
         this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
      }

      return j;
   }

   private int addStack(ItemStack stack) {
      int i = this.getOccupiedSlotWithRoomForStack(stack);
      if (i == -1) {
         i = this.getEmptySlot();
      }

      return i == -1 ? stack.getCount() : this.addStack(i, stack);
   }

   private int addStack(int slot, ItemStack stack) {
      Item lv = stack.getItem();
      int j = stack.getCount();
      ItemStack lv2 = this.getStack(slot);
      if (lv2.isEmpty()) {
         lv2 = new ItemStack(lv, 0);
         if (stack.hasNbt()) {
            lv2.setNbt(stack.getNbt().copy());
         }

         this.setStack(slot, lv2);
      }

      int k = j;
      if (j > lv2.getMaxCount() - lv2.getCount()) {
         k = lv2.getMaxCount() - lv2.getCount();
      }

      if (k > this.getMaxCountPerStack() - lv2.getCount()) {
         k = this.getMaxCountPerStack() - lv2.getCount();
      }

      if (k == 0) {
         return j;
      } else {
         j -= k;
         lv2.increment(k);
         lv2.setBobbingAnimationTime(5);
         return j;
      }
   }

   public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
      if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
         return this.selectedSlot;
      } else if (this.canStackAddMore(this.getStack(40), stack)) {
         return 40;
      } else {
         for(int i = 0; i < this.main.size(); ++i) {
            if (this.canStackAddMore((ItemStack)this.main.get(i), stack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void updateItems() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         DefaultedList lv = (DefaultedList)var1.next();

         for(int i = 0; i < lv.size(); ++i) {
            if (!((ItemStack)lv.get(i)).isEmpty()) {
               ((ItemStack)lv.get(i)).inventoryTick(this.player.world, this.player, i, this.selectedSlot == i);
            }
         }
      }

   }

   public boolean insertStack(ItemStack stack) {
      return this.insertStack(-1, stack);
   }

   public boolean insertStack(int slot, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         try {
            if (stack.isDamaged()) {
               if (slot == -1) {
                  slot = this.getEmptySlot();
               }

               if (slot >= 0) {
                  this.main.set(slot, stack.copyAndEmpty());
                  ((ItemStack)this.main.get(slot)).setBobbingAnimationTime(5);
                  return true;
               } else if (this.player.getAbilities().creativeMode) {
                  stack.setCount(0);
                  return true;
               } else {
                  return false;
               }
            } else {
               int j;
               do {
                  j = stack.getCount();
                  if (slot == -1) {
                     stack.setCount(this.addStack(stack));
                  } else {
                     stack.setCount(this.addStack(slot, stack));
                  }
               } while(!stack.isEmpty() && stack.getCount() < j);

               if (stack.getCount() == j && this.player.getAbilities().creativeMode) {
                  stack.setCount(0);
                  return true;
               } else {
                  return stack.getCount() < j;
               }
            }
         } catch (Throwable var6) {
            CrashReport lv = CrashReport.create(var6, "Adding item to inventory");
            CrashReportSection lv2 = lv.addElement("Item being added");
            lv2.add("Item ID", (Object)Item.getRawId(stack.getItem()));
            lv2.add("Item data", (Object)stack.getDamage());
            lv2.add("Item name", () -> {
               return stack.getName().getString();
            });
            throw new CrashException(lv);
         }
      }
   }

   public void offerOrDrop(ItemStack stack) {
      this.offer(stack, true);
   }

   public void offer(ItemStack stack, boolean notifiesClient) {
      while(true) {
         if (!stack.isEmpty()) {
            int i = this.getOccupiedSlotWithRoomForStack(stack);
            if (i == -1) {
               i = this.getEmptySlot();
            }

            if (i != -1) {
               int j = stack.getMaxCount() - this.getStack(i).getCount();
               if (this.insertStack(i, stack.split(j)) && notifiesClient && this.player instanceof ServerPlayerEntity) {
                  ((ServerPlayerEntity)this.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, this.getStack(i)));
               }
               continue;
            }

            this.player.dropItem(stack, false);
         }

         return;
      }
   }

   public ItemStack removeStack(int slot, int amount) {
      List list = null;

      DefaultedList lv;
      for(Iterator var4 = this.combinedInventory.iterator(); var4.hasNext(); slot -= lv.size()) {
         lv = (DefaultedList)var4.next();
         if (slot < lv.size()) {
            list = lv;
            break;
         }
      }

      return list != null && !((ItemStack)list.get(slot)).isEmpty() ? Inventories.splitStack(list, slot, amount) : ItemStack.EMPTY;
   }

   public void removeOne(ItemStack stack) {
      Iterator var2 = this.combinedInventory.iterator();

      while(true) {
         while(var2.hasNext()) {
            DefaultedList lv = (DefaultedList)var2.next();

            for(int i = 0; i < lv.size(); ++i) {
               if (lv.get(i) == stack) {
                  lv.set(i, ItemStack.EMPTY);
                  break;
               }
            }
         }

         return;
      }
   }

   public ItemStack removeStack(int slot) {
      DefaultedList lv = null;

      DefaultedList lv2;
      for(Iterator var3 = this.combinedInventory.iterator(); var3.hasNext(); slot -= lv2.size()) {
         lv2 = (DefaultedList)var3.next();
         if (slot < lv2.size()) {
            lv = lv2;
            break;
         }
      }

      if (lv != null && !((ItemStack)lv.get(slot)).isEmpty()) {
         ItemStack lv3 = (ItemStack)lv.get(slot);
         lv.set(slot, ItemStack.EMPTY);
         return lv3;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setStack(int slot, ItemStack stack) {
      DefaultedList lv = null;

      DefaultedList lv2;
      for(Iterator var4 = this.combinedInventory.iterator(); var4.hasNext(); slot -= lv2.size()) {
         lv2 = (DefaultedList)var4.next();
         if (slot < lv2.size()) {
            lv = lv2;
            break;
         }
      }

      if (lv != null) {
         lv.set(slot, stack);
      }

   }

   public float getBlockBreakingSpeed(BlockState block) {
      return ((ItemStack)this.main.get(this.selectedSlot)).getMiningSpeedMultiplier(block);
   }

   public NbtList writeNbt(NbtList nbtList) {
      int i;
      NbtCompound lv;
      for(i = 0; i < this.main.size(); ++i) {
         if (!((ItemStack)this.main.get(i)).isEmpty()) {
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)i);
            ((ItemStack)this.main.get(i)).writeNbt(lv);
            nbtList.add(lv);
         }
      }

      for(i = 0; i < this.armor.size(); ++i) {
         if (!((ItemStack)this.armor.get(i)).isEmpty()) {
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)(i + 100));
            ((ItemStack)this.armor.get(i)).writeNbt(lv);
            nbtList.add(lv);
         }
      }

      for(i = 0; i < this.offHand.size(); ++i) {
         if (!((ItemStack)this.offHand.get(i)).isEmpty()) {
            lv = new NbtCompound();
            lv.putByte("Slot", (byte)(i + 150));
            ((ItemStack)this.offHand.get(i)).writeNbt(lv);
            nbtList.add(lv);
         }
      }

      return nbtList;
   }

   public void readNbt(NbtList nbtList) {
      this.main.clear();
      this.armor.clear();
      this.offHand.clear();

      for(int i = 0; i < nbtList.size(); ++i) {
         NbtCompound lv = nbtList.getCompound(i);
         int j = lv.getByte("Slot") & 255;
         ItemStack lv2 = ItemStack.fromNbt(lv);
         if (!lv2.isEmpty()) {
            if (j >= 0 && j < this.main.size()) {
               this.main.set(j, lv2);
            } else if (j >= 100 && j < this.armor.size() + 100) {
               this.armor.set(j - 100, lv2);
            } else if (j >= 150 && j < this.offHand.size() + 150) {
               this.offHand.set(j - 150, lv2);
            }
         }
      }

   }

   public int size() {
      return this.main.size() + this.armor.size() + this.offHand.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.main.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            var1 = this.armor.iterator();

            do {
               if (!var1.hasNext()) {
                  var1 = this.offHand.iterator();

                  do {
                     if (!var1.hasNext()) {
                        return true;
                     }

                     lv = (ItemStack)var1.next();
                  } while(lv.isEmpty());

                  return false;
               }

               lv = (ItemStack)var1.next();
            } while(lv.isEmpty());

            return false;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }

   public ItemStack getStack(int slot) {
      List list = null;

      DefaultedList lv;
      for(Iterator var3 = this.combinedInventory.iterator(); var3.hasNext(); slot -= lv.size()) {
         lv = (DefaultedList)var3.next();
         if (slot < lv.size()) {
            list = lv;
            break;
         }
      }

      return list == null ? ItemStack.EMPTY : (ItemStack)list.get(slot);
   }

   public Text getName() {
      return Text.translatable("container.inventory");
   }

   public ItemStack getArmorStack(int slot) {
      return (ItemStack)this.armor.get(slot);
   }

   public void damageArmor(DamageSource damageSource, float amount, int[] slots) {
      if (!(amount <= 0.0F)) {
         amount /= 4.0F;
         if (amount < 1.0F) {
            amount = 1.0F;
         }

         int[] var4 = slots;
         int var5 = slots.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];
            ItemStack lv = (ItemStack)this.armor.get(i);
            if ((!damageSource.isIn(DamageTypeTags.IS_FIRE) || !lv.getItem().isFireproof()) && lv.getItem() instanceof ArmorItem) {
               lv.damage((int)amount, (LivingEntity)this.player, (Consumer)((player) -> {
                  player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i));
               }));
            }
         }

      }
   }

   public void dropAll() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         List list = (List)var1.next();

         for(int i = 0; i < list.size(); ++i) {
            ItemStack lv = (ItemStack)list.get(i);
            if (!lv.isEmpty()) {
               this.player.dropItem(lv, true, false);
               list.set(i, ItemStack.EMPTY);
            }
         }
      }

   }

   public void markDirty() {
      ++this.changeCount;
   }

   public int getChangeCount() {
      return this.changeCount;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      if (this.player.isRemoved()) {
         return false;
      } else {
         return !(player.squaredDistanceTo(this.player) > 64.0);
      }
   }

   public boolean contains(ItemStack stack) {
      Iterator var2 = this.combinedInventory.iterator();

      while(var2.hasNext()) {
         List list = (List)var2.next();
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            ItemStack lv = (ItemStack)var4.next();
            if (!lv.isEmpty() && ItemStack.canCombine(lv, stack)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean contains(TagKey tag) {
      Iterator var2 = this.combinedInventory.iterator();

      while(var2.hasNext()) {
         List list = (List)var2.next();
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            ItemStack lv = (ItemStack)var4.next();
            if (!lv.isEmpty() && lv.isIn(tag)) {
               return true;
            }
         }
      }

      return false;
   }

   public void clone(PlayerInventory other) {
      for(int i = 0; i < this.size(); ++i) {
         this.setStack(i, other.getStack(i));
      }

      this.selectedSlot = other.selectedSlot;
   }

   public void clear() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         List list = (List)var1.next();
         list.clear();
      }

   }

   public void populateRecipeFinder(RecipeMatcher finder) {
      Iterator var2 = this.main.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         finder.addUnenchantedInput(lv);
      }

   }

   public ItemStack dropSelectedItem(boolean entireStack) {
      ItemStack lv = this.getMainHandStack();
      return lv.isEmpty() ? ItemStack.EMPTY : this.removeStack(this.selectedSlot, entireStack ? lv.getCount() : 1);
   }
}
