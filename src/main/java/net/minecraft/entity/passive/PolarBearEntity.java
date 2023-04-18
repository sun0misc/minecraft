package net.minecraft.entity.passive;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class PolarBearEntity extends AnimalEntity implements Angerable {
   private static final TrackedData WARNING;
   private static final float field_30352 = 6.0F;
   private float lastWarningAnimationProgress;
   private float warningAnimationProgress;
   private int warningSoundCooldown;
   private static final UniformIntProvider ANGER_TIME_RANGE;
   private int angerTime;
   @Nullable
   private UUID angryAt;

   public PolarBearEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return (PassiveEntity)EntityType.POLAR_BEAR.create(world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return false;
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new AttackGoal());
      this.goalSelector.add(1, new PolarBearEscapeDangerGoal());
      this.goalSelector.add(4, new FollowParentGoal(this, 1.25));
      this.goalSelector.add(5, new WanderAroundGoal(this, 1.0));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(7, new LookAroundGoal(this));
      this.targetSelector.add(1, new PolarBearRevengeGoal());
      this.targetSelector.add(2, new ProtectBabiesGoal());
      this.targetSelector.add(3, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
      this.targetSelector.add(4, new ActiveTargetGoal(this, FoxEntity.class, 10, true, true, (Predicate)null));
      this.targetSelector.add(5, new UniversalAngerGoal(this, false));
   }

   public static DefaultAttributeContainer.Builder createPolarBearAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      RegistryEntry lv = world.getBiome(pos);
      if (!lv.isIn(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)) {
         return isValidNaturalSpawn(type, world, spawnReason, pos, random);
      } else {
         return isLightLevelValidForNaturalSpawn(world, pos) && world.getBlockState(pos.down()).isIn(BlockTags.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readAngerFromNbt(this.world, nbt);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeAngerToNbt(nbt);
   }

   public void chooseRandomAngerTime() {
      this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
   }

   public void setAngerTime(int angerTime) {
      this.angerTime = angerTime;
   }

   public int getAngerTime() {
      return this.angerTime;
   }

   public void setAngryAt(@Nullable UUID angryAt) {
      this.angryAt = angryAt;
   }

   @Nullable
   public UUID getAngryAt() {
      return this.angryAt;
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? SoundEvents.ENTITY_POLAR_BEAR_AMBIENT_BABY : SoundEvents.ENTITY_POLAR_BEAR_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_POLAR_BEAR_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
   }

   protected void playWarningSound() {
      if (this.warningSoundCooldown <= 0) {
         this.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 1.0F, this.getSoundPitch());
         this.warningSoundCooldown = 40;
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(WARNING, false);
   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.warningAnimationProgress != this.lastWarningAnimationProgress) {
            this.calculateDimensions();
         }

         this.lastWarningAnimationProgress = this.warningAnimationProgress;
         if (this.isWarning()) {
            this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress + 1.0F, 0.0F, 6.0F);
         } else {
            this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress - 1.0F, 0.0F, 6.0F);
         }
      }

      if (this.warningSoundCooldown > 0) {
         --this.warningSoundCooldown;
      }

      if (!this.world.isClient) {
         this.tickAngerLogic((ServerWorld)this.world, true);
      }

   }

   public EntityDimensions getDimensions(EntityPose pose) {
      if (this.warningAnimationProgress > 0.0F) {
         float f = this.warningAnimationProgress / 6.0F;
         float g = 1.0F + f;
         return super.getDimensions(pose).scaled(1.0F, g);
      } else {
         return super.getDimensions(pose);
      }
   }

   public boolean tryAttack(Entity target) {
      boolean bl = target.damage(this.getDamageSources().mobAttack(this), (float)((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
      if (bl) {
         this.applyDamageEffects(this, target);
      }

      return bl;
   }

   public boolean isWarning() {
      return (Boolean)this.dataTracker.get(WARNING);
   }

   public void setWarning(boolean warning) {
      this.dataTracker.set(WARNING, warning);
   }

   public float getWarningAnimationProgress(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastWarningAnimationProgress, this.warningAnimationProgress) / 6.0F;
   }

   protected float getBaseMovementSpeedMultiplier() {
      return 0.98F;
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(1.0F);
      }

      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   static {
      WARNING = DataTracker.registerData(PolarBearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
   }

   private class AttackGoal extends MeleeAttackGoal {
      public AttackGoal() {
         super(PolarBearEntity.this, 1.25, true);
      }

      protected void attack(LivingEntity target, double squaredDistance) {
         double e = this.getSquaredMaxAttackDistance(target);
         if (squaredDistance <= e && this.isCooledDown()) {
            this.resetCooldown();
            this.mob.tryAttack(target);
            PolarBearEntity.this.setWarning(false);
         } else if (squaredDistance <= e * 2.0) {
            if (this.isCooledDown()) {
               PolarBearEntity.this.setWarning(false);
               this.resetCooldown();
            }

            if (this.getCooldown() <= 10) {
               PolarBearEntity.this.setWarning(true);
               PolarBearEntity.this.playWarningSound();
            }
         } else {
            this.resetCooldown();
            PolarBearEntity.this.setWarning(false);
         }

      }

      public void stop() {
         PolarBearEntity.this.setWarning(false);
         super.stop();
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         return (double)(4.0F + entity.getWidth());
      }
   }

   private class PolarBearEscapeDangerGoal extends EscapeDangerGoal {
      public PolarBearEscapeDangerGoal() {
         super(PolarBearEntity.this, 2.0);
      }

      protected boolean isInDanger() {
         return this.mob.getAttacker() != null && this.mob.isBaby() || this.mob.isOnFire();
      }
   }

   class PolarBearRevengeGoal extends RevengeGoal {
      public PolarBearRevengeGoal() {
         super(PolarBearEntity.this);
      }

      public void start() {
         super.start();
         if (PolarBearEntity.this.isBaby()) {
            this.callSameTypeForRevenge();
            this.stop();
         }

      }

      protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
         if (mob instanceof PolarBearEntity && !mob.isBaby()) {
            super.setMobEntityTarget(mob, target);
         }

      }
   }

   private class ProtectBabiesGoal extends ActiveTargetGoal {
      public ProtectBabiesGoal() {
         super(PolarBearEntity.this, PlayerEntity.class, 20, true, true, (Predicate)null);
      }

      public boolean canStart() {
         if (PolarBearEntity.this.isBaby()) {
            return false;
         } else {
            if (super.canStart()) {
               List list = PolarBearEntity.this.world.getNonSpectatingEntities(PolarBearEntity.class, PolarBearEntity.this.getBoundingBox().expand(8.0, 4.0, 8.0));
               Iterator var2 = list.iterator();

               while(var2.hasNext()) {
                  PolarBearEntity lv = (PolarBearEntity)var2.next();
                  if (lv.isBaby()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected double getFollowRange() {
         return super.getFollowRange() * 0.5;
      }
   }
}
