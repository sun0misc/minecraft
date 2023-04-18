package net.minecraft.block.entity;

import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class BrewingStandBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
   private static final int INPUT_SLOT_INDEX = 3;
   private static final int FUEL_SLOT_INDEX = 4;
   private static final int[] TOP_SLOTS = new int[]{3};
   private static final int[] BOTTOM_SLOTS = new int[]{0, 1, 2, 3};
   private static final int[] SIDE_SLOTS = new int[]{0, 1, 2, 4};
   public static final int MAX_FUEL_USES = 20;
   public static final int BREW_TIME_PROPERTY_INDEX = 0;
   public static final int FUEL_PROPERTY_INDEX = 1;
   public static final int PROPERTY_COUNT = 2;
   private DefaultedList inventory;
   int brewTime;
   private boolean[] slotsEmptyLastTick;
   private Item itemBrewing;
   int fuel;
   protected final PropertyDelegate propertyDelegate;

   public BrewingStandBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.BREWING_STAND, pos, state);
      this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
      this.propertyDelegate = new PropertyDelegate() {
         public int get(int index) {
            switch (index) {
               case 0:
                  return BrewingStandBlockEntity.this.brewTime;
               case 1:
                  return BrewingStandBlockEntity.this.fuel;
               default:
                  return 0;
            }
         }

         public void set(int index, int value) {
            switch (index) {
               case 0:
                  BrewingStandBlockEntity.this.brewTime = value;
                  break;
               case 1:
                  BrewingStandBlockEntity.this.fuel = value;
            }

         }

         public int size() {
            return 2;
         }
      };
   }

   protected Text getContainerName() {
      return Text.translatable("container.brewing");
   }

   public int size() {
      return this.inventory.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.inventory.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }

   public static void tick(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity) {
      ItemStack lv = (ItemStack)blockEntity.inventory.get(4);
      if (blockEntity.fuel <= 0 && lv.isOf(Items.BLAZE_POWDER)) {
         blockEntity.fuel = 20;
         lv.decrement(1);
         markDirty(world, pos, state);
      }

      boolean bl = canCraft(blockEntity.inventory);
      boolean bl2 = blockEntity.brewTime > 0;
      ItemStack lv2 = (ItemStack)blockEntity.inventory.get(3);
      if (bl2) {
         --blockEntity.brewTime;
         boolean bl3 = blockEntity.brewTime == 0;
         if (bl3 && bl) {
            craft(world, pos, blockEntity.inventory);
            markDirty(world, pos, state);
         } else if (!bl || !lv2.isOf(blockEntity.itemBrewing)) {
            blockEntity.brewTime = 0;
            markDirty(world, pos, state);
         }
      } else if (bl && blockEntity.fuel > 0) {
         --blockEntity.fuel;
         blockEntity.brewTime = 400;
         blockEntity.itemBrewing = lv2.getItem();
         markDirty(world, pos, state);
      }

      boolean[] bls = blockEntity.getSlotsEmpty();
      if (!Arrays.equals(bls, blockEntity.slotsEmptyLastTick)) {
         blockEntity.slotsEmptyLastTick = bls;
         BlockState lv3 = state;
         if (!(state.getBlock() instanceof BrewingStandBlock)) {
            return;
         }

         for(int i = 0; i < BrewingStandBlock.BOTTLE_PROPERTIES.length; ++i) {
            lv3 = (BlockState)lv3.with(BrewingStandBlock.BOTTLE_PROPERTIES[i], bls[i]);
         }

         world.setBlockState(pos, lv3, Block.NOTIFY_LISTENERS);
      }

   }

   private boolean[] getSlotsEmpty() {
      boolean[] bls = new boolean[3];

      for(int i = 0; i < 3; ++i) {
         if (!((ItemStack)this.inventory.get(i)).isEmpty()) {
            bls[i] = true;
         }
      }

      return bls;
   }

   private static boolean canCraft(DefaultedList slots) {
      ItemStack lv = (ItemStack)slots.get(3);
      if (lv.isEmpty()) {
         return false;
      } else if (!BrewingRecipeRegistry.isValidIngredient(lv)) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            ItemStack lv2 = (ItemStack)slots.get(i);
            if (!lv2.isEmpty() && BrewingRecipeRegistry.hasRecipe(lv2, lv)) {
               return true;
            }
         }

         return false;
      }
   }

   private static void craft(World world, BlockPos pos, DefaultedList slots) {
      ItemStack lv = (ItemStack)slots.get(3);

      for(int i = 0; i < 3; ++i) {
         slots.set(i, BrewingRecipeRegistry.craft(lv, (ItemStack)slots.get(i)));
      }

      lv.decrement(1);
      if (lv.getItem().hasRecipeRemainder()) {
         ItemStack lv2 = new ItemStack(lv.getItem().getRecipeRemainder());
         if (lv.isEmpty()) {
            lv = lv2;
         } else {
            ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv2);
         }
      }

      slots.set(3, lv);
      world.syncWorldEvent(WorldEvents.BREWING_STAND_BREWS, pos, 0);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      Inventories.readNbt(nbt, this.inventory);
      this.brewTime = nbt.getShort("BrewTime");
      this.fuel = nbt.getByte("Fuel");
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putShort("BrewTime", (short)this.brewTime);
      Inventories.writeNbt(nbt, this.inventory);
      nbt.putByte("Fuel", (byte)this.fuel);
   }

   public ItemStack getStack(int slot) {
      return slot >= 0 && slot < this.inventory.size() ? (ItemStack)this.inventory.get(slot) : ItemStack.EMPTY;
   }

   public ItemStack removeStack(int slot, int amount) {
      return Inventories.splitStack(this.inventory, slot, amount);
   }

   public ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.inventory, slot);
   }

   public void setStack(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.inventory.size()) {
         this.inventory.set(slot, stack);
      }

   }

   public boolean canPlayerUse(PlayerEntity player) {
      return Inventory.canPlayerUse(this, player);
   }

   public boolean isValid(int slot, ItemStack stack) {
      if (slot == 3) {
         return BrewingRecipeRegistry.isValidIngredient(stack);
      } else if (slot == 4) {
         return stack.isOf(Items.BLAZE_POWDER);
      } else {
         return (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION) || stack.isOf(Items.GLASS_BOTTLE)) && this.getStack(slot).isEmpty();
      }
   }

   public int[] getAvailableSlots(Direction side) {
      if (side == Direction.UP) {
         return TOP_SLOTS;
      } else {
         return side == Direction.DOWN ? BOTTOM_SLOTS : SIDE_SLOTS;
      }
   }

   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
      return this.isValid(slot, stack);
   }

   public boolean canExtract(int slot, ItemStack stack, Direction dir) {
      return slot == 3 ? stack.isOf(Items.GLASS_BOTTLE) : true;
   }

   public void clear() {
      this.inventory.clear();
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new BrewingStandScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }
}
