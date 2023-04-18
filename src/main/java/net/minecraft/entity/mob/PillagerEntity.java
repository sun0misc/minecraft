package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PillagerEntity extends IllagerEntity implements CrossbowUser, InventoryOwner {
   private static final TrackedData CHARGING;
   private static final int field_30478 = 5;
   private static final int field_30476 = 300;
   private static final float field_30477 = 1.6F;
   private final SimpleInventory inventory = new SimpleInventory(5);

   public PillagerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(2, new RaiderEntity.PatrolApproachGoal(this, 10.0F));
      this.goalSelector.add(3, new CrossbowAttackGoal(this, 1.0, 8.0F));
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 15.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 15.0F));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new ActiveTargetGoal(this, MerchantEntity.class, false));
      this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
   }

   public static DefaultAttributeContainer.Builder createPillagerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355).add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CHARGING, false);
   }

   public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
      return weapon == Items.CROSSBOW;
   }

   public boolean isCharging() {
      return (Boolean)this.dataTracker.get(CHARGING);
   }

   public void setCharging(boolean charging) {
      this.dataTracker.set(CHARGING, charging);
   }

   public void postShoot() {
      this.despawnCounter = 0;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeInventory(nbt);
   }

   public IllagerEntity.State getState() {
      if (this.isCharging()) {
         return IllagerEntity.State.CROSSBOW_CHARGE;
      } else if (this.isHolding(Items.CROSSBOW)) {
         return IllagerEntity.State.CROSSBOW_HOLD;
      } else {
         return this.isAttacking() ? IllagerEntity.State.ATTACKING : IllagerEntity.State.NEUTRAL;
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readInventory(nbt);
      this.setCanPickUpLoot(true);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return 0.0F;
   }

   public int getLimitPerChunk() {
      return 1;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      this.initEquipment(lv, difficulty);
      this.updateEnchantments(lv, difficulty);
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
   }

   protected void enchantMainHandItem(Random random, float power) {
      super.enchantMainHandItem(random, power);
      if (random.nextInt(300) == 0) {
         ItemStack lv = this.getMainHandStack();
         if (lv.isOf(Items.CROSSBOW)) {
            Map map = EnchantmentHelper.get(lv);
            map.putIfAbsent(Enchantments.PIERCING, 1);
            EnchantmentHelper.set(map, lv);
            this.equipStack(EquipmentSlot.MAINHAND, lv);
         }
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

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PILLAGER_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PILLAGER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_PILLAGER_HURT;
   }

   public void attack(LivingEntity target, float pullProgress) {
      this.shoot(this, 1.6F);
   }

   public void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray) {
      this.shoot(this, target, projectile, multiShotSpray, 1.6F);
   }

   public SimpleInventory getInventory() {
      return this.inventory;
   }

   protected void loot(ItemEntity item) {
      ItemStack lv = item.getStack();
      if (lv.getItem() instanceof BannerItem) {
         super.loot(item);
      } else if (this.isRaidCaptain(lv)) {
         this.triggerItemPickedUpByEntityCriteria(item);
         ItemStack lv2 = this.inventory.addStack(lv);
         if (lv2.isEmpty()) {
            item.discard();
         } else {
            lv.setCount(lv2.getCount());
         }
      }

   }

   private boolean isRaidCaptain(ItemStack stack) {
      return this.hasActiveRaid() && stack.isOf(Items.WHITE_BANNER);
   }

   public StackReference getStackReference(int mappedIndex) {
      int j = mappedIndex - 300;
      return j >= 0 && j < this.inventory.size() ? StackReference.of(this.inventory, j) : super.getStackReference(mappedIndex);
   }

   public void addBonusForWave(int wave, boolean unused) {
      Raid lv = this.getRaid();
      boolean bl2 = this.random.nextFloat() <= lv.getEnchantmentChance();
      if (bl2) {
         ItemStack lv2 = new ItemStack(Items.CROSSBOW);
         Map map = Maps.newHashMap();
         if (wave > lv.getMaxWaves(Difficulty.NORMAL)) {
            map.put(Enchantments.QUICK_CHARGE, 2);
         } else if (wave > lv.getMaxWaves(Difficulty.EASY)) {
            map.put(Enchantments.QUICK_CHARGE, 1);
         }

         map.put(Enchantments.MULTISHOT, 1);
         EnchantmentHelper.set(map, lv2);
         this.equipStack(EquipmentSlot.MAINHAND, lv2);
      }

   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_PILLAGER_CELEBRATE;
   }

   static {
      CHARGING = DataTracker.registerData(PillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
