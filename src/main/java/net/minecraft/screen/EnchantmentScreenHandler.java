package net.minecraft.screen;

import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class EnchantmentScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final ScreenHandlerContext context;
   private final Random random;
   private final Property seed;
   public final int[] enchantmentPower;
   public final int[] enchantmentId;
   public final int[] enchantmentLevel;

   public EnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public EnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(ScreenHandlerType.ENCHANTMENT, syncId);
      this.inventory = new SimpleInventory(2) {
         public void markDirty() {
            super.markDirty();
            EnchantmentScreenHandler.this.onContentChanged(this);
         }
      };
      this.random = Random.create();
      this.seed = Property.create();
      this.enchantmentPower = new int[3];
      this.enchantmentId = new int[]{-1, -1, -1};
      this.enchantmentLevel = new int[]{-1, -1, -1};
      this.context = context;
      this.addSlot(new Slot(this.inventory, 0, 15, 47) {
         public boolean canInsert(ItemStack stack) {
            return true;
         }

         public int getMaxItemCount() {
            return 1;
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 35, 47) {
         public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.LAPIS_LAZULI);
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

      this.addProperty(Property.create((int[])this.enchantmentPower, 0));
      this.addProperty(Property.create((int[])this.enchantmentPower, 1));
      this.addProperty(Property.create((int[])this.enchantmentPower, 2));
      this.addProperty(this.seed).set(playerInventory.player.getEnchantmentTableSeed());
      this.addProperty(Property.create((int[])this.enchantmentId, 0));
      this.addProperty(Property.create((int[])this.enchantmentId, 1));
      this.addProperty(Property.create((int[])this.enchantmentId, 2));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 0));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 1));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 2));
   }

   public void onContentChanged(Inventory inventory) {
      if (inventory == this.inventory) {
         ItemStack lv = inventory.getStack(0);
         if (!lv.isEmpty() && lv.isEnchantable()) {
            this.context.run((world, pos) -> {
               int i = 0;
               Iterator var5 = EnchantingTableBlock.POWER_PROVIDER_OFFSETS.iterator();

               while(var5.hasNext()) {
                  BlockPos lvx = (BlockPos)var5.next();
                  if (EnchantingTableBlock.canAccessPowerProvider(world, pos, lvx)) {
                     ++i;
                  }
               }

               this.random.setSeed((long)this.seed.get());

               int j;
               for(j = 0; j < 3; ++j) {
                  this.enchantmentPower[j] = EnchantmentHelper.calculateRequiredExperienceLevel(this.random, j, i, lv);
                  this.enchantmentId[j] = -1;
                  this.enchantmentLevel[j] = -1;
                  if (this.enchantmentPower[j] < j + 1) {
                     this.enchantmentPower[j] = 0;
                  }
               }

               for(j = 0; j < 3; ++j) {
                  if (this.enchantmentPower[j] > 0) {
                     List list = this.generateEnchantments(lv, j, this.enchantmentPower[j]);
                     if (list != null && !list.isEmpty()) {
                        EnchantmentLevelEntry lv2 = (EnchantmentLevelEntry)list.get(this.random.nextInt(list.size()));
                        this.enchantmentId[j] = Registries.ENCHANTMENT.getRawId(lv2.enchantment);
                        this.enchantmentLevel[j] = lv2.level;
                     }
                  }
               }

               this.sendContentUpdates();
            });
         } else {
            for(int i = 0; i < 3; ++i) {
               this.enchantmentPower[i] = 0;
               this.enchantmentId[i] = -1;
               this.enchantmentLevel[i] = -1;
            }
         }
      }

   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      if (id >= 0 && id < this.enchantmentPower.length) {
         ItemStack lv = this.inventory.getStack(0);
         ItemStack lv2 = this.inventory.getStack(1);
         int j = id + 1;
         if ((lv2.isEmpty() || lv2.getCount() < j) && !player.getAbilities().creativeMode) {
            return false;
         } else if (this.enchantmentPower[id] <= 0 || lv.isEmpty() || (player.experienceLevel < j || player.experienceLevel < this.enchantmentPower[id]) && !player.getAbilities().creativeMode) {
            return false;
         } else {
            this.context.run((world, pos) -> {
               ItemStack lvx = lv;
               List list = this.generateEnchantments(lv, id, this.enchantmentPower[id]);
               if (!list.isEmpty()) {
                  player.applyEnchantmentCosts(lv, j);
                  boolean bl = lv.isOf(Items.BOOK);
                  if (bl) {
                     lvx = new ItemStack(Items.ENCHANTED_BOOK);
                     NbtCompound lv2x = lv.getNbt();
                     if (lv2x != null) {
                        lvx.setNbt(lv2x.copy());
                     }

                     this.inventory.setStack(0, lvx);
                  }

                  for(int k = 0; k < list.size(); ++k) {
                     EnchantmentLevelEntry lv3 = (EnchantmentLevelEntry)list.get(k);
                     if (bl) {
                        EnchantedBookItem.addEnchantment(lvx, lv3);
                     } else {
                        lvx.addEnchantment(lv3.enchantment, lv3.level);
                     }
                  }

                  if (!player.getAbilities().creativeMode) {
                     lv2.decrement(j);
                     if (lv2.isEmpty()) {
                        this.inventory.setStack(1, ItemStack.EMPTY);
                     }
                  }

                  player.incrementStat(Stats.ENCHANT_ITEM);
                  if (player instanceof ServerPlayerEntity) {
                     Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity)player, lvx, j);
                  }

                  this.inventory.markDirty();
                  this.seed.set(player.getEnchantmentTableSeed());
                  this.onContentChanged(this.inventory);
                  world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
               }

            });
            return true;
         }
      } else {
         Text var10000 = player.getName();
         Util.error("" + var10000 + " pressed invalid button id: " + id);
         return false;
      }
   }

   private List generateEnchantments(ItemStack stack, int slot, int level) {
      this.random.setSeed((long)(this.seed.get() + slot));
      List list = EnchantmentHelper.generateEnchantments(this.random, stack, level, false);
      if (stack.isOf(Items.BOOK) && list.size() > 1) {
         list.remove(this.random.nextInt(list.size()));
      }

      return list;
   }

   public int getLapisCount() {
      ItemStack lv = this.inventory.getStack(1);
      return lv.isEmpty() ? 0 : lv.getCount();
   }

   public int getSeed() {
      return this.seed.get();
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.inventory);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.ENCHANTING_TABLE);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == 0) {
            if (!this.insertItem(lv3, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (slot == 1) {
            if (!this.insertItem(lv3, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (lv3.isOf(Items.LAPIS_LAZULI)) {
            if (!this.insertItem(lv3, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (((Slot)this.slots.get(0)).hasStack() || !((Slot)this.slots.get(0)).canInsert(lv3)) {
               return ItemStack.EMPTY;
            }

            ItemStack lv4 = lv3.copyWithCount(1);
            lv3.decrement(1);
            ((Slot)this.slots.get(0)).setStack(lv4);
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
