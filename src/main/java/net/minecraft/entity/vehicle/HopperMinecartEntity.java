package net.minecraft.entity.vehicle;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;

public class HopperMinecartEntity extends StorageMinecartEntity implements Hopper {
   private boolean enabled = true;

   public HopperMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public HopperMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.HOPPER_MINECART, x, y, z, world);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.HOPPER;
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.HOPPER.getDefaultState();
   }

   public int getDefaultBlockOffset() {
      return 1;
   }

   public int size() {
      return 5;
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
      boolean bl2 = !powered;
      if (bl2 != this.isEnabled()) {
         this.setEnabled(bl2);
      }

   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public double getHopperX() {
      return this.getX();
   }

   public double getHopperY() {
      return this.getY() + 0.5;
   }

   public double getHopperZ() {
      return this.getZ();
   }

   public void tick() {
      super.tick();
      if (!this.world.isClient && this.isAlive() && this.isEnabled() && this.canOperate()) {
         this.markDirty();
      }

   }

   public boolean canOperate() {
      if (HopperBlockEntity.extract((World)this.world, (Hopper)this)) {
         return true;
      } else {
         List list = this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.25, 0.0, 0.25), EntityPredicates.VALID_ENTITY);
         Iterator var2 = list.iterator();

         ItemEntity lv;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            lv = (ItemEntity)var2.next();
         } while(!HopperBlockEntity.extract((Inventory)this, (ItemEntity)lv));

         return true;
      }
   }

   protected Item getItem() {
      return Items.HOPPER_MINECART;
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("Enabled", this.enabled);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.enabled = nbt.contains("Enabled") ? nbt.getBoolean("Enabled") : true;
   }

   public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new HopperScreenHandler(syncId, playerInventory, this);
   }
}
