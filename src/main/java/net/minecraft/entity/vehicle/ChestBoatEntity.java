package net.minecraft.entity.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ChestBoatEntity extends BoatEntity implements RideableInventory, VehicleInventory {
   private static final int INVENTORY_SIZE = 27;
   private DefaultedList inventory;
   @Nullable
   private Identifier lootTableId;
   private long lootTableSeed;

   public ChestBoatEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
   }

   public ChestBoatEntity(World arg, double d, double e, double f) {
      this(EntityType.CHEST_BOAT, arg);
      this.setPosition(d, e, f);
      this.prevX = d;
      this.prevY = e;
      this.prevZ = f;
   }

   protected float getPassengerHorizontalOffset() {
      return 0.15F;
   }

   protected int getMaxPassengers() {
      return 1;
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeInventoryToNbt(nbt);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readInventoryFromNbt(nbt);
   }

   public void dropItems(DamageSource source) {
      super.dropItems(source);
      this.onBroken(source, this.world, this);
   }

   public void remove(Entity.RemovalReason reason) {
      if (!this.world.isClient && reason.shouldDestroy()) {
         ItemScatterer.spawn(this.world, (Entity)this, (Inventory)this);
      }

      super.remove(reason);
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (this.canAddPassenger(player) && !player.shouldCancelInteraction()) {
         return super.interact(player, hand);
      } else {
         ActionResult lv = this.open(player);
         if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(player, true);
         }

         return lv;
      }
   }

   public void openInventory(PlayerEntity player) {
      player.openHandledScreen(this);
      if (!player.world.isClient) {
         this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
         PiglinBrain.onGuardedBlockInteracted(player, true);
      }

   }

   public Item asItem() {
      Item var10000;
      switch (this.getVariant()) {
         case SPRUCE:
            var10000 = Items.SPRUCE_CHEST_BOAT;
            break;
         case BIRCH:
            var10000 = Items.BIRCH_CHEST_BOAT;
            break;
         case JUNGLE:
            var10000 = Items.JUNGLE_CHEST_BOAT;
            break;
         case ACACIA:
            var10000 = Items.ACACIA_CHEST_BOAT;
            break;
         case CHERRY:
            var10000 = Items.CHERRY_CHEST_BOAT;
            break;
         case DARK_OAK:
            var10000 = Items.DARK_OAK_CHEST_BOAT;
            break;
         case MANGROVE:
            var10000 = Items.MANGROVE_CHEST_BOAT;
            break;
         case BAMBOO:
            var10000 = Items.BAMBOO_CHEST_RAFT;
            break;
         default:
            var10000 = Items.OAK_CHEST_BOAT;
      }

      return var10000;
   }

   public void clear() {
      this.clearInventory();
   }

   public int size() {
      return 27;
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

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
      if (this.lootTableId != null && arg2.isSpectator()) {
         return null;
      } else {
         this.generateLoot(arg.player);
         return GenericContainerScreenHandler.createGeneric9x3(i, arg, this);
      }
   }

   public void generateLoot(@Nullable PlayerEntity player) {
      this.generateInventoryLoot(player);
   }

   @Nullable
   public Identifier getLootTableId() {
      return this.lootTableId;
   }

   public void setLootTableId(@Nullable Identifier lootTableId) {
      this.lootTableId = lootTableId;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long lootTableSeed) {
      this.lootTableSeed = lootTableSeed;
   }

   public DefaultedList getInventory() {
      return this.inventory;
   }

   public void resetInventory() {
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
   }

   public void onClose(PlayerEntity player) {
      this.world.emitGameEvent(GameEvent.CONTAINER_CLOSE, this.getPos(), GameEvent.Emitter.of((Entity)player));
   }
}
