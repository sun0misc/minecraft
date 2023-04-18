package net.minecraft.entity;

import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface Bucketable {
   boolean isFromBucket();

   void setFromBucket(boolean fromBucket);

   void copyDataToStack(ItemStack stack);

   void copyDataFromNbt(NbtCompound nbt);

   ItemStack getBucketItem();

   SoundEvent getBucketFillSound();

   /** @deprecated */
   @Deprecated
   static void copyDataToStack(MobEntity entity, ItemStack stack) {
      NbtCompound lv = stack.getOrCreateNbt();
      if (entity.hasCustomName()) {
         stack.setCustomName(entity.getCustomName());
      }

      if (entity.isAiDisabled()) {
         lv.putBoolean("NoAI", entity.isAiDisabled());
      }

      if (entity.isSilent()) {
         lv.putBoolean("Silent", entity.isSilent());
      }

      if (entity.hasNoGravity()) {
         lv.putBoolean("NoGravity", entity.hasNoGravity());
      }

      if (entity.isGlowingLocal()) {
         lv.putBoolean("Glowing", entity.isGlowingLocal());
      }

      if (entity.isInvulnerable()) {
         lv.putBoolean("Invulnerable", entity.isInvulnerable());
      }

      lv.putFloat("Health", entity.getHealth());
   }

   /** @deprecated */
   @Deprecated
   static void copyDataFromNbt(MobEntity entity, NbtCompound nbt) {
      if (nbt.contains("NoAI")) {
         entity.setAiDisabled(nbt.getBoolean("NoAI"));
      }

      if (nbt.contains("Silent")) {
         entity.setSilent(nbt.getBoolean("Silent"));
      }

      if (nbt.contains("NoGravity")) {
         entity.setNoGravity(nbt.getBoolean("NoGravity"));
      }

      if (nbt.contains("Glowing")) {
         entity.setGlowing(nbt.getBoolean("Glowing"));
      }

      if (nbt.contains("Invulnerable")) {
         entity.setInvulnerable(nbt.getBoolean("Invulnerable"));
      }

      if (nbt.contains("Health", NbtElement.NUMBER_TYPE)) {
         entity.setHealth(nbt.getFloat("Health"));
      }

   }

   static Optional tryBucket(PlayerEntity player, Hand hand, LivingEntity entity) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.getItem() == Items.WATER_BUCKET && entity.isAlive()) {
         entity.playSound(((Bucketable)entity).getBucketFillSound(), 1.0F, 1.0F);
         ItemStack lv2 = ((Bucketable)entity).getBucketItem();
         ((Bucketable)entity).copyDataToStack(lv2);
         ItemStack lv3 = ItemUsage.exchangeStack(lv, player, lv2, false);
         player.setStackInHand(hand, lv3);
         World lv4 = entity.world;
         if (!lv4.isClient) {
            Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)player, lv2);
         }

         entity.discard();
         return Optional.of(ActionResult.success(lv4.isClient));
      } else {
         return Optional.empty();
      }
   }
}
