package net.minecraft.entity.passive;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class PufferfishEntity extends FishEntity {
   private static final TrackedData PUFF_STATE;
   int inflateTicks;
   int deflateTicks;
   private static final Predicate BLOW_UP_FILTER;
   static final TargetPredicate BLOW_UP_TARGET_PREDICATE;
   public static final int NOT_PUFFED = 0;
   public static final int SEMI_PUFFED = 1;
   public static final int FULLY_PUFFED = 2;

   public PufferfishEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.calculateDimensions();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(PUFF_STATE, 0);
   }

   public int getPuffState() {
      return (Integer)this.dataTracker.get(PUFF_STATE);
   }

   public void setPuffState(int puffState) {
      this.dataTracker.set(PUFF_STATE, puffState);
   }

   public void onTrackedDataSet(TrackedData data) {
      if (PUFF_STATE.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("PuffState", this.getPuffState());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setPuffState(Math.min(nbt.getInt("PuffState"), 2));
   }

   public ItemStack getBucketItem() {
      return new ItemStack(Items.PUFFERFISH_BUCKET);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new InflateGoal(this));
   }

   public void tick() {
      if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
         if (this.inflateTicks > 0) {
            if (this.getPuffState() == 0) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(SEMI_PUFFED);
            } else if (this.inflateTicks > 40 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(FULLY_PUFFED);
            }

            ++this.inflateTicks;
         } else if (this.getPuffState() != 0) {
            if (this.deflateTicks > 60 && this.getPuffState() == 2) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(SEMI_PUFFED);
            } else if (this.deflateTicks > 100 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getSoundPitch());
               this.setPuffState(NOT_PUFFED);
            }

            ++this.deflateTicks;
         }
      }

      super.tick();
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isAlive() && this.getPuffState() > 0) {
         List list = this.world.getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(0.3), (entity) -> {
            return BLOW_UP_TARGET_PREDICATE.test(this, entity);
         });
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            MobEntity lv = (MobEntity)var2.next();
            if (lv.isAlive()) {
               this.sting(lv);
            }
         }
      }

   }

   private void sting(MobEntity mob) {
      int i = this.getPuffState();
      if (mob.damage(this.getDamageSources().mobAttack(this), (float)(1 + i))) {
         mob.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0), this);
         this.playSound(SoundEvents.ENTITY_PUFFER_FISH_STING, 1.0F, 1.0F);
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      int i = this.getPuffState();
      if (player instanceof ServerPlayerEntity && i > 0 && player.damage(this.getDamageSources().mobAttack(this), (float)(1 + i))) {
         if (!this.isSilent()) {
            ((ServerPlayerEntity)player).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PUFFERFISH_STING, GameStateChangeS2CPacket.field_33328));
         }

         player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 * i, 0), this);
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PUFFER_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_PUFFER_FISH_FLOP;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return super.getDimensions(pose).scaled(getScaleForPuffState(this.getPuffState()));
   }

   private static float getScaleForPuffState(int puffState) {
      switch (puffState) {
         case 0:
            return 0.5F;
         case 1:
            return 0.7F;
         default:
            return 1.0F;
      }
   }

   static {
      PUFF_STATE = DataTracker.registerData(PufferfishEntity.class, TrackedDataHandlerRegistry.INTEGER);
      BLOW_UP_FILTER = (entity) -> {
         if (entity instanceof PlayerEntity && ((PlayerEntity)entity).isCreative()) {
            return false;
         } else {
            return entity.getType() == EntityType.AXOLOTL || entity.getGroup() != EntityGroup.AQUATIC;
         }
      };
      BLOW_UP_TARGET_PREDICATE = TargetPredicate.createNonAttackable().ignoreDistanceScalingFactor().ignoreVisibility().setPredicate(BLOW_UP_FILTER);
   }

   private static class InflateGoal extends Goal {
      private final PufferfishEntity pufferfish;

      public InflateGoal(PufferfishEntity pufferfish) {
         this.pufferfish = pufferfish;
      }

      public boolean canStart() {
         List list = this.pufferfish.world.getEntitiesByClass(LivingEntity.class, this.pufferfish.getBoundingBox().expand(2.0), (arg) -> {
            return PufferfishEntity.BLOW_UP_TARGET_PREDICATE.test(this.pufferfish, arg);
         });
         return !list.isEmpty();
      }

      public void start() {
         this.pufferfish.inflateTicks = 1;
         this.pufferfish.deflateTicks = 0;
      }

      public void stop() {
         this.pufferfish.inflateTicks = 0;
      }
   }
}
