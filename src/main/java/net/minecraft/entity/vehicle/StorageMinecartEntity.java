package net.minecraft.entity.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class StorageMinecartEntity extends AbstractMinecartEntity implements VehicleInventory {
   private DefaultedList inventory;
   @Nullable
   private Identifier lootTableId;
   private long lootSeed;

   protected StorageMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
   }

   protected StorageMinecartEntity(EntityType type, double x, double y, double z, World world) {
      super(type, world, x, y, z);
      this.inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
   }

   public void dropItems(DamageSource damageSource) {
      super.dropItems(damageSource);
      this.onBroken(damageSource, this.world, this);
   }

   public ItemStack getStack(int slot) {
      return this.getInventoryStack(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      return this.removeInventoryStack(slot, amount);
   }

   public ItemStack removeStack(int slot) {
      return this.removeInventoryStack(slot);
   }

   public void setStack(int slot, ItemStack stack) {
      this.setInventoryStack(slot, stack);
   }

   public StackReference getStackReference(int mappedIndex) {
      return this.getInventoryStackReference(mappedIndex);
   }

   public void markDirty() {
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.canPlayerAccess(player);
   }

   public void remove(Entity.RemovalReason reason) {
      if (!this.world.isClient && reason.shouldDestroy()) {
         ItemScatterer.spawn(this.world, (Entity)this, (Inventory)this);
      }

      super.remove(reason);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeInventoryToNbt(nbt);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readInventoryFromNbt(nbt);
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      return this.open(player);
   }

   protected void applySlowdown() {
      float f = 0.98F;
      if (this.lootTableId == null) {
         int i = 15 - ScreenHandler.calculateComparatorOutput((Inventory)this);
         f += (float)i * 0.001F;
      }

      if (this.isTouchingWater()) {
         f *= 0.95F;
      }

      this.setVelocity(this.getVelocity().multiply((double)f, 0.0, (double)f));
   }

   public void clear() {
      this.clearInventory();
   }

   public void setLootTable(Identifier id, long lootSeed) {
      this.lootTableId = id;
      this.lootSeed = lootSeed;
   }

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
      if (this.lootTableId != null && arg2.isSpectator()) {
         return null;
      } else {
         this.generateInventoryLoot(arg.player);
         return this.getScreenHandler(i, arg);
      }
   }

   protected abstract ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory);

   @Nullable
   public Identifier getLootTableId() {
      return this.lootTableId;
   }

   public void setLootTableId(@Nullable Identifier lootTableId) {
      this.lootTableId = lootTableId;
   }

   public long getLootTableSeed() {
      return this.lootSeed;
   }

   public void setLootTableSeed(long lootTableSeed) {
      this.lootSeed = lootTableSeed;
   }

   public DefaultedList getInventory() {
      return this.inventory;
   }

   public void resetInventory() {
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
   }
}
