package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class ZoglinEntity extends HostileEntity implements Monster, Hoglin {
   private static final TrackedData BABY;
   private static final int field_30514 = 40;
   private static final int field_30505 = 1;
   private static final float field_30506 = 0.6F;
   private static final int field_30507 = 6;
   private static final float field_30508 = 0.5F;
   private static final int field_30509 = 40;
   private static final int field_30510 = 15;
   private static final int ATTACK_TARGET_DURATION = 200;
   private static final float field_30512 = 0.3F;
   private static final float field_30513 = 0.4F;
   private int movementCooldownTicks;
   protected static final ImmutableList USED_SENSORS;
   protected static final ImmutableList USED_MEMORY_MODULES;

   public ZoglinEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.experiencePoints = 5;
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(USED_MEMORY_MODULES, USED_SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      Brain lv = this.createBrainProfile().deserialize(dynamic);
      addCoreTasks(lv);
      addIdleTasks(lv);
      addFightTasks(lv);
      lv.setCoreActivities(ImmutableSet.of(Activity.CORE));
      lv.setDefaultActivity(Activity.IDLE);
      lv.resetPossibleActivities();
      return lv;
   }

   private static void addCoreTasks(Brain brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
   }

   private static void addIdleTasks(Brain brain) {
      brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(UpdateAttackTargetTask.create(ZoglinEntity::getHoglinTarget), LookAtMobWithIntervalTask.follow(8.0F, UniformIntProvider.create(30, 60)), new RandomTask(ImmutableList.of(Pair.of(StrollTask.create(0.4F), 2), Pair.of(GoTowardsLookTargetTask.create(0.4F, 3), 2), Pair.of(new WaitTask(30, 60), 1)))));
   }

   private static void addFightTasks(Brain brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(RangedApproachTask.create(1.0F), TaskTriggerer.runIf(ZoglinEntity::isAdult, MeleeAttackTask.create(40)), TaskTriggerer.runIf(ZoglinEntity::isBaby, MeleeAttackTask.create(15)), ForgetAttackTargetTask.create()), MemoryModuleType.ATTACK_TARGET);
   }

   private Optional getHoglinTarget() {
      return ((LivingTargetCache)this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty())).findFirst(this::shouldAttack);
   }

   private boolean shouldAttack(LivingEntity entity) {
      EntityType lv = entity.getType();
      return lv != EntityType.ZOGLIN && lv != EntityType.CREEPER && Sensor.testAttackableTargetPredicate(this, entity);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(BABY, false);
   }

   public void onTrackedDataSet(TrackedData data) {
      super.onTrackedDataSet(data);
      if (BABY.equals(data)) {
         this.calculateDimensions();
      }

   }

   public static DefaultAttributeContainer.Builder createZoglinAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6000000238418579).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   public boolean tryAttack(Entity target) {
      if (!(target instanceof LivingEntity)) {
         return false;
      } else {
         this.movementCooldownTicks = 10;
         this.world.sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
         this.playSound(SoundEvents.ENTITY_ZOGLIN_ATTACK, 1.0F, this.getSoundPitch());
         return Hoglin.tryAttack(this, (LivingEntity)target);
      }
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   protected void knockback(LivingEntity target) {
      if (!this.isBaby()) {
         Hoglin.knockback(this, target);
      }

   }

   public double getMountedHeightOffset() {
      return (double)this.getHeight() - (this.isBaby() ? 0.2 : 0.15);
   }

   public boolean damage(DamageSource source, float amount) {
      boolean bl = super.damage(source, amount);
      if (this.world.isClient) {
         return false;
      } else if (bl && source.getAttacker() instanceof LivingEntity) {
         LivingEntity lv = (LivingEntity)source.getAttacker();
         if (this.canTarget(lv) && !LookTargetUtil.isNewTargetTooFar(this, lv, 4.0)) {
            this.setAttackTarget(lv);
         }

         return bl;
      } else {
         return bl;
      }
   }

   private void setAttackTarget(LivingEntity entity) {
      this.brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      this.brain.remember(MemoryModuleType.ATTACK_TARGET, entity, 200L);
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected void tickBrain() {
      Activity lv = (Activity)this.brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      this.brain.resetPossibleActivities((List)ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity lv2 = (Activity)this.brain.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (lv2 == Activity.FIGHT && lv != Activity.FIGHT) {
         this.playAngrySound();
      }

      this.setAttacking(this.brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
   }

   protected void mobTick() {
      this.world.getProfiler().push("zoglinBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.tickBrain();
   }

   public void setBaby(boolean baby) {
      this.getDataTracker().set(BABY, baby);
      if (!this.world.isClient && baby) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(0.5);
      }

   }

   public boolean isBaby() {
      return (Boolean)this.getDataTracker().get(BABY);
   }

   public void tickMovement() {
      if (this.movementCooldownTicks > 0) {
         --this.movementCooldownTicks;
      }

      super.tickMovement();
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
         this.movementCooldownTicks = 10;
         this.playSound(SoundEvents.ENTITY_ZOGLIN_ATTACK, 1.0F, this.getSoundPitch());
      } else {
         super.handleStatus(status);
      }

   }

   public int getMovementCooldownTicks() {
      return this.movementCooldownTicks;
   }

   protected SoundEvent getAmbientSound() {
      if (this.world.isClient) {
         return null;
      } else {
         return this.brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ENTITY_ZOGLIN_ANGRY : SoundEvents.ENTITY_ZOGLIN_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOGLIN_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_ZOGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playAngrySound() {
      this.playSound(SoundEvents.ENTITY_ZOGLIN_ANGRY, 1.0F, this.getSoundPitch());
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.isBaby()) {
         nbt.putBoolean("IsBaby", true);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.getBoolean("IsBaby")) {
         this.setBaby(true);
      }

   }

   static {
      BABY = DataTracker.registerData(ZoglinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      USED_SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
      USED_MEMORY_MODULES = ImmutableList.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);
   }
}
