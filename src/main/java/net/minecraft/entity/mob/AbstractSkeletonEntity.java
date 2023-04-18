package net.minecraft.entity.mob;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSkeletonEntity extends HostileEntity implements RangedAttackMob {
   private final BowAttackGoal bowAttackGoal = new BowAttackGoal(this, 1.0, 20, 15.0F);
   private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.2, false) {
      public void stop() {
         super.stop();
         AbstractSkeletonEntity.this.setAttacking(false);
      }

      public void start() {
         super.start();
         AbstractSkeletonEntity.this.setAttacking(true);
      }
   };

   protected AbstractSkeletonEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.updateAttackType();
   }

   protected void initGoals() {
      this.goalSelector.add(2, new AvoidSunlightGoal(this));
      this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
      this.goalSelector.add(3, new FleeEntityGoal(this, WolfEntity.class, 6.0F, 1.0, 1.2));
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(6, new LookAroundGoal(this));
      this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
   }

   public static DefaultAttributeContainer.Builder createAbstractSkeletonAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   abstract SoundEvent getStepSound();

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   public void tickMovement() {
      boolean bl = this.isAffectedByDaylight();
      if (bl) {
         ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
         if (!lv.isEmpty()) {
            if (lv.isDamageable()) {
               lv.setDamage(lv.getDamage() + this.random.nextInt(2));
               if (lv.getDamage() >= lv.getMaxDamage()) {
                  this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                  this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
               }
            }

            bl = false;
         }

         if (bl) {
            this.setOnFireFor(8);
         }
      }

      super.tickMovement();
   }

   public void tickRiding() {
      super.tickRiding();
      Entity var2 = this.getControllingVehicle();
      if (var2 instanceof PathAwareEntity lv) {
         this.bodyYaw = lv.bodyYaw;
      }

   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      super.initEquipment(random, localDifficulty);
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
      Random lv = world.getRandom();
      this.initEquipment(lv, difficulty);
      this.updateEnchantments(lv, difficulty);
      this.updateAttackType();
      this.setCanPickUpLoot(lv.nextFloat() < 0.55F * difficulty.getClampedLocalDifficulty());
      if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
         LocalDate localDate = LocalDate.now();
         int i = localDate.get(ChronoField.DAY_OF_MONTH);
         int j = localDate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && lv.nextFloat() < 0.25F) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(lv.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
         }
      }

      return entityData;
   }

   public void updateAttackType() {
      if (this.world != null && !this.world.isClient) {
         this.goalSelector.remove(this.meleeAttackGoal);
         this.goalSelector.remove(this.bowAttackGoal);
         ItemStack lv = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
         if (lv.isOf(Items.BOW)) {
            int i = 20;
            if (this.world.getDifficulty() != Difficulty.HARD) {
               i = 40;
            }

            this.bowAttackGoal.setAttackInterval(i);
            this.goalSelector.add(4, this.bowAttackGoal);
         } else {
            this.goalSelector.add(4, this.meleeAttackGoal);
         }

      }
   }

   public void attack(LivingEntity target, float pullProgress) {
      ItemStack lv = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
      PersistentProjectileEntity lv2 = this.createArrowProjectile(lv, pullProgress);
      double d = target.getX() - this.getX();
      double e = target.getBodyY(0.3333333333333333) - lv2.getY();
      double g = target.getZ() - this.getZ();
      double h = Math.sqrt(d * d + g * g);
      lv2.setVelocity(d, e + h * 0.20000000298023224, g, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
      this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(lv2);
   }

   protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
      return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
   }

   public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
      return weapon == Items.BOW;
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.updateAttackType();
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      super.equipStack(slot, stack);
      if (!this.world.isClient) {
         this.updateAttackType();
      }

   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 1.74F;
   }

   public double getHeightOffset() {
      return -0.6;
   }

   public boolean isShaking() {
      return this.isFrozen();
   }
}
