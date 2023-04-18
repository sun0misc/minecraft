package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VindicatorEntity extends IllagerEntity {
   private static final String JOHNNY_KEY = "Johnny";
   static final Predicate DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE = (difficulty) -> {
      return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
   };
   boolean johnny;

   public VindicatorEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new BreakDoorGoal(this));
      this.goalSelector.add(2, new IllagerEntity.LongDoorInteractGoal(this));
      this.goalSelector.add(3, new RaiderEntity.PatrolApproachGoal(this, 10.0F));
      this.goalSelector.add(4, new AttackGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, MerchantEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
      this.targetSelector.add(4, new TargetGoal(this));
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
   }

   protected void mobTick() {
      if (!this.isAiDisabled() && NavigationConditions.hasMobNavigation(this)) {
         boolean bl = ((ServerWorld)this.world).hasRaidAt(this.getBlockPos());
         ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(bl);
      }

      super.mobTick();
   }

   public static DefaultAttributeContainer.Builder createVindicatorAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.johnny) {
         nbt.putBoolean("Johnny", true);
      }

   }

   public IllagerEntity.State getState() {
      if (this.isAttacking()) {
         return IllagerEntity.State.ATTACKING;
      } else {
         return this.isCelebrating() ? IllagerEntity.State.CELEBRATING : IllagerEntity.State.CROSSED;
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Johnny", NbtElement.NUMBER_TYPE)) {
         this.johnny = nbt.getBoolean("Johnny");
      }

   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_VINDICATOR_CELEBRATE;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      EntityData lv = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
      ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(true);
      Random lv2 = world.getRandom();
      this.initEquipment(lv2, difficulty);
      this.updateEnchantments(lv2, difficulty);
      return lv;
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      if (this.getRaid() == null) {
         this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
      }

   }

   public boolean isTeammate(Entity other) {
      if (super.isTeammate(other)) {
         return true;
      } else if (other instanceof LivingEntity && ((LivingEntity)other).getGroup() == EntityGroup.ILLAGER) {
         return this.getScoreboardTeam() == null && other.getScoreboardTeam() == null;
      } else {
         return false;
      }
   }

   public void setCustomName(@Nullable Text name) {
      super.setCustomName(name);
      if (!this.johnny && name != null && name.getString().equals("Johnny")) {
         this.johnny = true;
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_VINDICATOR_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_VINDICATOR_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_VINDICATOR_HURT;
   }

   public void addBonusForWave(int wave, boolean unused) {
      ItemStack lv = new ItemStack(Items.IRON_AXE);
      Raid lv2 = this.getRaid();
      int j = 1;
      if (wave > lv2.getMaxWaves(Difficulty.NORMAL)) {
         j = 2;
      }

      boolean bl2 = this.random.nextFloat() <= lv2.getEnchantmentChance();
      if (bl2) {
         Map map = Maps.newHashMap();
         map.put(Enchantments.SHARPNESS, Integer.valueOf(j));
         EnchantmentHelper.set(map, lv);
      }

      this.equipStack(EquipmentSlot.MAINHAND, lv);
   }

   static class BreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal {
      public BreakDoorGoal(MobEntity arg) {
         super(arg, 6, VindicatorEntity.DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE);
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean shouldContinue() {
         VindicatorEntity lv = (VindicatorEntity)this.mob;
         return lv.hasActiveRaid() && super.shouldContinue();
      }

      public boolean canStart() {
         VindicatorEntity lv = (VindicatorEntity)this.mob;
         return lv.hasActiveRaid() && lv.random.nextInt(toGoalTicks(10)) == 0 && super.canStart();
      }

      public void start() {
         super.start();
         this.mob.setDespawnCounter(0);
      }
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(VindicatorEntity vindicator) {
         super(vindicator, 1.0, false);
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         if (this.mob.getVehicle() instanceof RavagerEntity) {
            float f = this.mob.getVehicle().getWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + entity.getWidth());
         } else {
            return super.getSquaredMaxAttackDistance(entity);
         }
      }
   }

   static class TargetGoal extends ActiveTargetGoal {
      public TargetGoal(VindicatorEntity vindicator) {
         super(vindicator, LivingEntity.class, 0, true, true, LivingEntity::isMobOrPlayer);
      }

      public boolean canStart() {
         return ((VindicatorEntity)this.mob).johnny && super.canStart();
      }

      public void start() {
         super.start();
         this.mob.setDespawnCounter(0);
      }
   }
}
