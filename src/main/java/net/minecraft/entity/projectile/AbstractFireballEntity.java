package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public abstract class AbstractFireballEntity extends ExplosiveProjectileEntity implements FlyingItemEntity {
   private static final TrackedData ITEM;

   public AbstractFireballEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public AbstractFireballEntity(EntityType arg, double d, double e, double f, double g, double h, double i, World arg2) {
      super(arg, d, e, f, g, h, i, arg2);
   }

   public AbstractFireballEntity(EntityType arg, LivingEntity arg2, double d, double e, double f, World arg3) {
      super(arg, arg2, d, e, f, arg3);
   }

   public void setItem(ItemStack stack) {
      if (!stack.isOf(Items.FIRE_CHARGE) || stack.hasNbt()) {
         this.getDataTracker().set(ITEM, stack.copyWithCount(1));
      }

   }

   protected ItemStack getItem() {
      return (ItemStack)this.getDataTracker().get(ITEM);
   }

   public ItemStack getStack() {
      ItemStack lv = this.getItem();
      return lv.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : lv;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      ItemStack lv = this.getItem();
      if (!lv.isEmpty()) {
         nbt.put("Item", lv.writeNbt(new NbtCompound()));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      ItemStack lv = ItemStack.fromNbt(nbt.getCompound("Item"));
      this.setItem(lv);
   }

   static {
      ITEM = DataTracker.registerData(AbstractFireballEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
