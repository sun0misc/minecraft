package net.minecraft.entity.passive;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HorseEntity extends AbstractHorseEntity implements VariantHolder {
   private static final UUID HORSE_ARMOR_BONUS_ID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
   private static final TrackedData VARIANT;

   public HorseEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initAttributes(Random random) {
      EntityAttributeInstance var10000 = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      Objects.requireNonNull(random);
      var10000.setBaseValue((double)getChildHealthBonus(random::nextInt));
      var10000 = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      Objects.requireNonNull(random);
      var10000.setBaseValue(getChildMovementSpeedBonus(random::nextDouble));
      var10000 = this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH);
      Objects.requireNonNull(random);
      var10000.setBaseValue(getChildJumpStrengthBonus(random::nextDouble));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, 0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Variant", this.getHorseVariant());
      if (!this.items.getStack(1).isEmpty()) {
         nbt.put("ArmorItem", this.items.getStack(1).writeNbt(new NbtCompound()));
      }

   }

   public ItemStack getArmorType() {
      return this.getEquippedStack(EquipmentSlot.CHEST);
   }

   private void equipArmor(ItemStack stack) {
      this.equipStack(EquipmentSlot.CHEST, stack);
      this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setHorseVariant(nbt.getInt("Variant"));
      if (nbt.contains("ArmorItem", NbtElement.COMPOUND_TYPE)) {
         ItemStack lv = ItemStack.fromNbt(nbt.getCompound("ArmorItem"));
         if (!lv.isEmpty() && this.isHorseArmor(lv)) {
            this.items.setStack(1, lv);
         }
      }

      this.updateSaddle();
   }

   private void setHorseVariant(int variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   private int getHorseVariant() {
      return (Integer)this.dataTracker.get(VARIANT);
   }

   private void setHorseVariant(HorseColor color, HorseMarking marking) {
      this.setHorseVariant(color.getId() & 255 | marking.getId() << 8 & '\uff00');
   }

   public HorseColor getVariant() {
      return HorseColor.byId(this.getHorseVariant() & 255);
   }

   public void setVariant(HorseColor arg) {
      this.setHorseVariant(arg.getId() & 255 | this.getHorseVariant() & -256);
   }

   public HorseMarking getMarking() {
      return HorseMarking.byIndex((this.getHorseVariant() & '\uff00') >> 8);
   }

   protected void updateSaddle() {
      if (!this.world.isClient) {
         super.updateSaddle();
         this.setArmorTypeFromStack(this.items.getStack(1));
         this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0F);
      }
   }

   private void setArmorTypeFromStack(ItemStack stack) {
      this.equipArmor(stack);
      if (!this.world.isClient) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(HORSE_ARMOR_BONUS_ID);
         if (this.isHorseArmor(stack)) {
            int i = ((HorseArmorItem)stack.getItem()).getBonus();
            if (i != 0) {
               this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addTemporaryModifier(new EntityAttributeModifier(HORSE_ARMOR_BONUS_ID, "Horse armor bonus", (double)i, EntityAttributeModifier.Operation.ADDITION));
            }
         }
      }

   }

   public void onInventoryChanged(Inventory sender) {
      ItemStack lv = this.getArmorType();
      super.onInventoryChanged(sender);
      ItemStack lv2 = this.getArmorType();
      if (this.age > 20 && this.isHorseArmor(lv2) && lv != lv2) {
         this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
      }

   }

   protected void playWalkSound(BlockSoundGroup group) {
      super.playWalkSound(group);
      if (this.random.nextInt(10) == 0) {
         this.playSound(SoundEvents.ENTITY_HORSE_BREATHE, group.getVolume() * 0.6F, group.getPitch());
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_HORSE_DEATH;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_HORSE_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_HORSE_HURT;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.ENTITY_HORSE_ANGRY;
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
         }

         return super.interactMob(player, hand);
      } else {
         return super.interactMob(player, hand);
      }
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (!(other instanceof DonkeyEntity) && !(other instanceof HorseEntity)) {
         return false;
      } else {
         return this.canBreed() && ((AbstractHorseEntity)other).canBreed();
      }
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      if (entity instanceof DonkeyEntity) {
         MuleEntity lv = (MuleEntity)EntityType.MULE.create(world);
         if (lv != null) {
            this.setChildAttributes(entity, lv);
         }

         return lv;
      } else {
         HorseEntity lv2 = (HorseEntity)entity;
         HorseEntity lv3 = (HorseEntity)EntityType.HORSE.create(world);
         if (lv3 != null) {
            int i = this.random.nextInt(9);
            HorseColor lv4;
            if (i < 4) {
               lv4 = this.getVariant();
            } else if (i < 8) {
               lv4 = lv2.getVariant();
            } else {
               lv4 = (HorseColor)Util.getRandom((Object[])HorseColor.values(), this.random);
            }

            int j = this.random.nextInt(5);
            HorseMarking lv5;
            if (j < 2) {
               lv5 = this.getMarking();
            } else if (j < 4) {
               lv5 = lv2.getMarking();
            } else {
               lv5 = (HorseMarking)Util.getRandom((Object[])HorseMarking.values(), this.random);
            }

            lv3.setHorseVariant(lv4, lv5);
            this.setChildAttributes(entity, lv3);
         }

         return lv3;
      }
   }

   public boolean hasArmorSlot() {
      return true;
   }

   public boolean isHorseArmor(ItemStack item) {
      return item.getItem() instanceof HorseArmorItem;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      HorseColor lv2;
      if (entityData instanceof HorseData) {
         lv2 = ((HorseData)entityData).color;
      } else {
         lv2 = (HorseColor)Util.getRandom((Object[])HorseColor.values(), lv);
         entityData = new HorseData(lv2);
      }

      this.setHorseVariant(lv2, (HorseMarking)Util.getRandom((Object[])HorseMarking.values(), lv));
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      VARIANT = DataTracker.registerData(HorseEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static class HorseData extends PassiveEntity.PassiveData {
      public final HorseColor color;

      public HorseData(HorseColor color) {
         super(true);
         this.color = color;
      }
   }
}
