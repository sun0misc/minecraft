package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TadpoleEntity extends FishEntity {
   @VisibleForTesting
   public static int MAX_TADPOLE_AGE = Math.abs(-24000);
   public static float WIDTH = 0.4F;
   public static float HEIGHT = 0.3F;
   private int tadpoleAge;
   protected static final ImmutableList SENSORS;
   protected static final ImmutableList MEMORY_MODULES;

   public TadpoleEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.lookControl = new YawAdjustingLookControl(this, 10);
   }

   protected EntityNavigation createNavigation(World world) {
      return new SwimNavigation(this, world);
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return TadpoleBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_TADPOLE_FLOP;
   }

   protected void mobTick() {
      this.world.getProfiler().push("tadpoleBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.world.getProfiler().push("tadpoleActivityUpdate");
      TadpoleBrain.updateActivities(this);
      this.world.getProfiler().pop();
      super.mobTick();
   }

   public static DefaultAttributeContainer.Builder createTadpoleAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0);
   }

   public void tickMovement() {
      super.tickMovement();
      if (!this.world.isClient) {
         this.setTadpoleAge(this.tadpoleAge + 1);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Age", this.tadpoleAge);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setTadpoleAge(nbt.getInt("Age"));
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_TADPOLE_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_TADPOLE_DEATH;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (this.isSlimeBall(lv)) {
         this.eatSlimeBall(player, lv);
         return ActionResult.success(this.world.isClient);
      } else {
         return (ActionResult)Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
      }
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public boolean isFromBucket() {
      return true;
   }

   public void setFromBucket(boolean fromBucket) {
   }

   public void copyDataToStack(ItemStack stack) {
      Bucketable.copyDataToStack(this, stack);
      NbtCompound lv = stack.getOrCreateNbt();
      lv.putInt("Age", this.getTadpoleAge());
   }

   public void copyDataFromNbt(NbtCompound nbt) {
      Bucketable.copyDataFromNbt(this, nbt);
      if (nbt.contains("Age")) {
         this.setTadpoleAge(nbt.getInt("Age"));
      }

   }

   public ItemStack getBucketItem() {
      return new ItemStack(Items.TADPOLE_BUCKET);
   }

   public SoundEvent getBucketFillSound() {
      return SoundEvents.ITEM_BUCKET_FILL_TADPOLE;
   }

   private boolean isSlimeBall(ItemStack stack) {
      return FrogEntity.SLIME_BALL.test(stack);
   }

   private void eatSlimeBall(PlayerEntity player, ItemStack stack) {
      this.decrementItem(player, stack);
      this.increaseAge(PassiveEntity.toGrowUpAge(this.getTicksUntilGrowth()));
      this.world.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
   }

   private void decrementItem(PlayerEntity player, ItemStack stack) {
      if (!player.getAbilities().creativeMode) {
         stack.decrement(1);
      }

   }

   private int getTadpoleAge() {
      return this.tadpoleAge;
   }

   private void increaseAge(int seconds) {
      this.setTadpoleAge(this.tadpoleAge + seconds * 20);
   }

   private void setTadpoleAge(int tadpoleAge) {
      this.tadpoleAge = tadpoleAge;
      if (this.tadpoleAge >= MAX_TADPOLE_AGE) {
         this.growUp();
      }

   }

   private void growUp() {
      World var2 = this.world;
      if (var2 instanceof ServerWorld lv) {
         FrogEntity lv2 = (FrogEntity)EntityType.FROG.create(this.world);
         if (lv2 != null) {
            lv2.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            lv2.initialize(lv, this.world.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.CONVERSION, (EntityData)null, (NbtCompound)null);
            lv2.setAiDisabled(this.isAiDisabled());
            if (this.hasCustomName()) {
               lv2.setCustomName(this.getCustomName());
               lv2.setCustomNameVisible(this.isCustomNameVisible());
            }

            lv2.setPersistent();
            this.playSound(SoundEvents.ENTITY_TADPOLE_GROW_UP, 0.15F, 1.0F);
            lv.spawnEntityAndPassengers(lv2);
            this.discard();
         }
      }

   }

   private int getTicksUntilGrowth() {
      return Math.max(0, MAX_TADPOLE_AGE - this.tadpoleAge);
   }

   public boolean shouldDropXp() {
      return false;
   }

   static {
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.FROG_TEMPTATIONS);
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING);
   }
}
