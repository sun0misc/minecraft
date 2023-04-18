package net.minecraft.entity.passive;

import java.util.Objects;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class AbstractDonkeyEntity extends AbstractHorseEntity {
   private static final TrackedData CHEST;
   public static final int field_30412 = 15;

   protected AbstractDonkeyEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.playExtraHorseSounds = false;
   }

   protected void initAttributes(Random random) {
      EntityAttributeInstance var10000 = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      Objects.requireNonNull(random);
      var10000.setBaseValue((double)getChildHealthBonus(random::nextInt));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CHEST, false);
   }

   public static DefaultAttributeContainer.Builder createAbstractDonkeyAttributes() {
      return createBaseHorseAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17499999701976776).add(EntityAttributes.HORSE_JUMP_STRENGTH, 0.5);
   }

   public boolean hasChest() {
      return (Boolean)this.dataTracker.get(CHEST);
   }

   public void setHasChest(boolean hasChest) {
      this.dataTracker.set(CHEST, hasChest);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 17 : super.getInventorySize();
   }

   public double getMountedHeightOffset() {
      return super.getMountedHeightOffset() - 0.25;
   }

   protected void dropInventory() {
      super.dropInventory();
      if (this.hasChest()) {
         if (!this.world.isClient) {
            this.dropItem(Blocks.CHEST);
         }

         this.setHasChest(false);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("ChestedHorse", this.hasChest());
      if (this.hasChest()) {
         NbtList lv = new NbtList();

         for(int i = 2; i < this.items.size(); ++i) {
            ItemStack lv2 = this.items.getStack(i);
            if (!lv2.isEmpty()) {
               NbtCompound lv3 = new NbtCompound();
               lv3.putByte("Slot", (byte)i);
               lv2.writeNbt(lv3);
               lv.add(lv3);
            }
         }

         nbt.put("Items", lv);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setHasChest(nbt.getBoolean("ChestedHorse"));
      this.onChestedStatusChanged();
      if (this.hasChest()) {
         NbtList lv = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            int j = lv2.getByte("Slot") & 255;
            if (j >= 2 && j < this.items.size()) {
               this.items.setStack(j, ItemStack.fromNbt(lv2));
            }
         }
      }

      this.updateSaddle();
   }

   public StackReference getStackReference(int mappedIndex) {
      return mappedIndex == 499 ? new StackReference() {
         public ItemStack get() {
            return AbstractDonkeyEntity.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
         }

         public boolean set(ItemStack stack) {
            if (stack.isEmpty()) {
               if (AbstractDonkeyEntity.this.hasChest()) {
                  AbstractDonkeyEntity.this.setHasChest(false);
                  AbstractDonkeyEntity.this.onChestedStatusChanged();
               }

               return true;
            } else if (stack.isOf(Items.CHEST)) {
               if (!AbstractDonkeyEntity.this.hasChest()) {
                  AbstractDonkeyEntity.this.setHasChest(true);
                  AbstractDonkeyEntity.this.onChestedStatusChanged();
               }

               return true;
            } else {
               return false;
            }
         }
      } : super.getStackReference(mappedIndex);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      boolean bl = !this.isBaby() && this.isTame() && player.shouldCancelInteraction();
      if (!this.hasPassengers() && !bl) {
         ItemStack lv = player.getStackInHand(hand);
         if (!lv.isEmpty()) {
            if (this.isBreedingItem(lv)) {
               return this.interactHorse(player, lv);
            }

            if (!this.isTame()) {
               this.playAngrySound();
               return ActionResult.success(this.world.isClient);
            }

            if (!this.hasChest() && lv.isOf(Items.CHEST)) {
               this.addChest(player, lv);
               return ActionResult.success(this.world.isClient);
            }
         }

         return super.interactMob(player, hand);
      } else {
         return super.interactMob(player, hand);
      }
   }

   private void addChest(PlayerEntity player, ItemStack chest) {
      this.setHasChest(true);
      this.playAddChestSound();
      if (!player.getAbilities().creativeMode) {
         chest.decrement(1);
      }

      this.onChestedStatusChanged();
   }

   protected void playAddChestSound() {
      this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public int getInventoryColumns() {
      return 5;
   }

   static {
      CHEST = DataTracker.registerData(AbstractDonkeyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
