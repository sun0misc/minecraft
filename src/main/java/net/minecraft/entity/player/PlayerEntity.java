package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerEntity extends LivingEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int field_30643 = 16;
   public static final int field_30644 = 20;
   public static final int field_30645 = 100;
   public static final int field_30646 = 10;
   public static final int field_30647 = 200;
   public static final float field_30648 = 1.5F;
   public static final float field_30649 = 0.6F;
   public static final float field_30650 = 0.6F;
   public static final float DEFAULT_EYE_HEIGHT = 1.62F;
   public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F);
   private static final Map POSE_DIMENSIONS;
   private static final int field_30652 = 25;
   private static final TrackedData ABSORPTION_AMOUNT;
   private static final TrackedData SCORE;
   protected static final TrackedData PLAYER_MODEL_PARTS;
   protected static final TrackedData MAIN_ARM;
   protected static final TrackedData LEFT_SHOULDER_ENTITY;
   protected static final TrackedData RIGHT_SHOULDER_ENTITY;
   private long shoulderEntityAddedTime;
   private final PlayerInventory inventory = new PlayerInventory(this);
   protected EnderChestInventory enderChestInventory = new EnderChestInventory();
   public final PlayerScreenHandler playerScreenHandler;
   public ScreenHandler currentScreenHandler;
   protected HungerManager hungerManager = new HungerManager();
   protected int abilityResyncCountdown;
   public float prevStrideDistance;
   public float strideDistance;
   public int experiencePickUpDelay;
   public double prevCapeX;
   public double prevCapeY;
   public double prevCapeZ;
   public double capeX;
   public double capeY;
   public double capeZ;
   private int sleepTimer;
   protected boolean isSubmergedInWater;
   private final PlayerAbilities abilities = new PlayerAbilities();
   public int experienceLevel;
   public int totalExperience;
   public float experienceProgress;
   protected int enchantmentTableSeed;
   protected final float field_7509 = 0.02F;
   private int lastPlayedLevelUpSoundTime;
   private final GameProfile gameProfile;
   private boolean reducedDebugInfo;
   private ItemStack selectedItem;
   private final ItemCooldownManager itemCooldownManager;
   private Optional lastDeathPos;
   @Nullable
   public FishingBobberEntity fishHook;
   protected float damageTiltYaw;

   public PlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
      super(EntityType.PLAYER, world);
      this.selectedItem = ItemStack.EMPTY;
      this.itemCooldownManager = this.createCooldownManager();
      this.lastDeathPos = Optional.empty();
      this.setUuid(Uuids.getUuidFromProfile(gameProfile));
      this.gameProfile = gameProfile;
      this.playerScreenHandler = new PlayerScreenHandler(this.inventory, !world.isClient, this);
      this.currentScreenHandler = this.playerScreenHandler;
      this.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5, yaw, 0.0F);
      this.field_6215 = 180.0F;
   }

   public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
      if (!gameMode.isBlockBreakingRestricted()) {
         return false;
      } else if (gameMode == GameMode.SPECTATOR) {
         return true;
      } else if (this.canModifyBlocks()) {
         return false;
      } else {
         ItemStack lv = this.getMainHandStack();
         return lv.isEmpty() || !lv.canDestroy(world.getRegistryManager().get(RegistryKeys.BLOCK), new CachedBlockPosition(world, pos, false));
      }
   }

   public static DefaultAttributeContainer.Builder createPlayerAttributes() {
      return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10000000149011612).add(EntityAttributes.GENERIC_ATTACK_SPEED).add(EntityAttributes.GENERIC_LUCK);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(ABSORPTION_AMOUNT, 0.0F);
      this.dataTracker.startTracking(SCORE, 0);
      this.dataTracker.startTracking(PLAYER_MODEL_PARTS, (byte)0);
      this.dataTracker.startTracking(MAIN_ARM, (byte)1);
      this.dataTracker.startTracking(LEFT_SHOULDER_ENTITY, new NbtCompound());
      this.dataTracker.startTracking(RIGHT_SHOULDER_ENTITY, new NbtCompound());
   }

   public void tick() {
      this.noClip = this.isSpectator();
      if (this.isSpectator()) {
         this.onGround = false;
      }

      if (this.experiencePickUpDelay > 0) {
         --this.experiencePickUpDelay;
      }

      if (this.isSleeping()) {
         ++this.sleepTimer;
         if (this.sleepTimer > 100) {
            this.sleepTimer = 100;
         }

         if (!this.world.isClient && this.world.isDay()) {
            this.wakeUp(false, true);
         }
      } else if (this.sleepTimer > 0) {
         ++this.sleepTimer;
         if (this.sleepTimer >= 110) {
            this.sleepTimer = 0;
         }
      }

      this.updateWaterSubmersionState();
      super.tick();
      if (!this.world.isClient && this.currentScreenHandler != null && !this.currentScreenHandler.canUse(this)) {
         this.closeHandledScreen();
         this.currentScreenHandler = this.playerScreenHandler;
      }

      this.updateCapeAngles();
      if (!this.world.isClient) {
         this.hungerManager.update(this);
         this.incrementStat(Stats.PLAY_TIME);
         this.incrementStat(Stats.TOTAL_WORLD_TIME);
         if (this.isAlive()) {
            this.incrementStat(Stats.TIME_SINCE_DEATH);
         }

         if (this.isSneaky()) {
            this.incrementStat(Stats.SNEAK_TIME);
         }

         if (!this.isSleeping()) {
            this.incrementStat(Stats.TIME_SINCE_REST);
         }
      }

      int i = 29999999;
      double d = MathHelper.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
      double e = MathHelper.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
      if (d != this.getX() || e != this.getZ()) {
         this.setPosition(d, this.getY(), e);
      }

      ++this.lastAttackedTicks;
      ItemStack lv = this.getMainHandStack();
      if (!ItemStack.areEqual(this.selectedItem, lv)) {
         if (!ItemStack.areItemsEqual(this.selectedItem, lv)) {
            this.resetLastAttackedTicks();
         }

         this.selectedItem = lv.copy();
      }

      this.updateTurtleHelmet();
      this.itemCooldownManager.update();
      this.updatePose();
   }

   public boolean shouldCancelInteraction() {
      return this.isSneaking();
   }

   protected boolean shouldDismount() {
      return this.isSneaking();
   }

   protected boolean clipAtLedge() {
      return this.isSneaking();
   }

   protected boolean updateWaterSubmersionState() {
      this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
      return this.isSubmergedInWater;
   }

   private void updateTurtleHelmet() {
      ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
      if (lv.isOf(Items.TURTLE_HELMET) && !this.isSubmergedIn(FluidTags.WATER)) {
         this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, 0, false, false, true));
      }

   }

   protected ItemCooldownManager createCooldownManager() {
      return new ItemCooldownManager();
   }

   private void updateCapeAngles() {
      this.prevCapeX = this.capeX;
      this.prevCapeY = this.capeY;
      this.prevCapeZ = this.capeZ;
      double d = this.getX() - this.capeX;
      double e = this.getY() - this.capeY;
      double f = this.getZ() - this.capeZ;
      double g = 10.0;
      if (d > 10.0) {
         this.capeX = this.getX();
         this.prevCapeX = this.capeX;
      }

      if (f > 10.0) {
         this.capeZ = this.getZ();
         this.prevCapeZ = this.capeZ;
      }

      if (e > 10.0) {
         this.capeY = this.getY();
         this.prevCapeY = this.capeY;
      }

      if (d < -10.0) {
         this.capeX = this.getX();
         this.prevCapeX = this.capeX;
      }

      if (f < -10.0) {
         this.capeZ = this.getZ();
         this.prevCapeZ = this.capeZ;
      }

      if (e < -10.0) {
         this.capeY = this.getY();
         this.prevCapeY = this.capeY;
      }

      this.capeX += d * 0.25;
      this.capeZ += f * 0.25;
      this.capeY += e * 0.25;
   }

   protected void updatePose() {
      if (this.wouldPoseNotCollide(EntityPose.SWIMMING)) {
         EntityPose lv;
         if (this.isFallFlying()) {
            lv = EntityPose.FALL_FLYING;
         } else if (this.isSleeping()) {
            lv = EntityPose.SLEEPING;
         } else if (this.isSwimming()) {
            lv = EntityPose.SWIMMING;
         } else if (this.isUsingRiptide()) {
            lv = EntityPose.SPIN_ATTACK;
         } else if (this.isSneaking() && !this.abilities.flying) {
            lv = EntityPose.CROUCHING;
         } else {
            lv = EntityPose.STANDING;
         }

         EntityPose lv2;
         if (!this.isSpectator() && !this.hasVehicle() && !this.wouldPoseNotCollide(lv)) {
            if (this.wouldPoseNotCollide(EntityPose.CROUCHING)) {
               lv2 = EntityPose.CROUCHING;
            } else {
               lv2 = EntityPose.SWIMMING;
            }
         } else {
            lv2 = lv;
         }

         this.setPose(lv2);
      }
   }

   public int getMaxNetherPortalTime() {
      return this.abilities.invulnerable ? 1 : 80;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_PLAYER_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_PLAYER_SPLASH;
   }

   protected SoundEvent getHighSpeedSplashSound() {
      return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
   }

   public int getDefaultPortalCooldown() {
      return 10;
   }

   public void playSound(SoundEvent sound, float volume, float pitch) {
      this.world.playSound(this, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
   }

   public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.PLAYERS;
   }

   protected int getBurningDuration() {
      return 20;
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.CONSUME_ITEM) {
         this.consumeItem();
      } else if (status == EntityStatuses.USE_FULL_DEBUG_INFO) {
         this.reducedDebugInfo = false;
      } else if (status == EntityStatuses.USE_REDUCED_DEBUG_INFO) {
         this.reducedDebugInfo = true;
      } else if (status == EntityStatuses.ADD_CLOUD_PARTICLES) {
         this.spawnParticles(ParticleTypes.CLOUD);
      } else {
         super.handleStatus(status);
      }

   }

   private void spawnParticles(ParticleEffect parameters) {
      for(int i = 0; i < 5; ++i) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.world.addParticle(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), d, e, f);
      }

   }

   protected void closeHandledScreen() {
      this.currentScreenHandler = this.playerScreenHandler;
   }

   protected void onHandledScreenClosed() {
   }

   public void tickRiding() {
      if (!this.world.isClient && this.shouldDismount() && this.hasVehicle()) {
         this.stopRiding();
         this.setSneaking(false);
      } else {
         double d = this.getX();
         double e = this.getY();
         double f = this.getZ();
         super.tickRiding();
         this.prevStrideDistance = this.strideDistance;
         this.strideDistance = 0.0F;
         this.increaseRidingMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
      }
   }

   protected void tickNewAi() {
      super.tickNewAi();
      this.tickHandSwing();
      this.headYaw = this.getYaw();
   }

   public void tickMovement() {
      if (this.abilityResyncCountdown > 0) {
         --this.abilityResyncCountdown;
      }

      if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
         if (this.getHealth() < this.getMaxHealth() && this.age % 20 == 0) {
            this.heal(1.0F);
         }

         if (this.hungerManager.isNotFull() && this.age % 10 == 0) {
            this.hungerManager.setFoodLevel(this.hungerManager.getFoodLevel() + 1);
         }
      }

      this.inventory.updateItems();
      this.prevStrideDistance = this.strideDistance;
      super.tickMovement();
      this.setMovementSpeed((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
      float f;
      if (this.onGround && !this.isDead() && !this.isSwimming()) {
         f = Math.min(0.1F, (float)this.getVelocity().horizontalLength());
      } else {
         f = 0.0F;
      }

      this.strideDistance += (f - this.strideDistance) * 0.4F;
      if (this.getHealth() > 0.0F && !this.isSpectator()) {
         Box lv;
         if (this.hasVehicle() && !this.getVehicle().isRemoved()) {
            lv = this.getBoundingBox().union(this.getVehicle().getBoundingBox()).expand(1.0, 0.0, 1.0);
         } else {
            lv = this.getBoundingBox().expand(1.0, 0.5, 1.0);
         }

         List list = this.world.getOtherEntities(this, lv);
         List list2 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            Entity lv2 = (Entity)list.get(i);
            if (lv2.getType() == EntityType.EXPERIENCE_ORB) {
               list2.add(lv2);
            } else if (!lv2.isRemoved()) {
               this.collideWithEntity(lv2);
            }
         }

         if (!list2.isEmpty()) {
            this.collideWithEntity((Entity)Util.getRandom((List)list2, this.random));
         }
      }

      this.updateShoulderEntity(this.getShoulderEntityLeft());
      this.updateShoulderEntity(this.getShoulderEntityRight());
      if (!this.world.isClient && (this.fallDistance > 0.5F || this.isTouchingWater()) || this.abilities.flying || this.isSleeping() || this.inPowderSnow) {
         this.dropShoulderEntities();
      }

   }

   private void updateShoulderEntity(@Nullable NbtCompound entityNbt) {
      if (entityNbt != null && (!entityNbt.contains("Silent") || !entityNbt.getBoolean("Silent")) && this.world.random.nextInt(200) == 0) {
         String string = entityNbt.getString("id");
         EntityType.get(string).filter((entityType) -> {
            return entityType == EntityType.PARROT;
         }).ifPresent((arg) -> {
            if (!ParrotEntity.imitateNearbyMob(this.world, this)) {
               this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getRandomSound(this.world, this.world.random), this.getSoundCategory(), 1.0F, ParrotEntity.getSoundPitch(this.world.random));
            }

         });
      }

   }

   private void collideWithEntity(Entity entity) {
      entity.onPlayerCollision(this);
   }

   public int getScore() {
      return (Integer)this.dataTracker.get(SCORE);
   }

   public void setScore(int score) {
      this.dataTracker.set(SCORE, score);
   }

   public void addScore(int score) {
      int j = this.getScore();
      this.dataTracker.set(SCORE, j + score);
   }

   public void useRiptide(int riptideTicks) {
      this.riptideTicks = riptideTicks;
      if (!this.world.isClient) {
         this.dropShoulderEntities();
         this.setLivingFlag(LivingEntity.USING_RIPTIDE_FLAG, true);
      }

   }

   public void onDeath(DamageSource damageSource) {
      super.onDeath(damageSource);
      this.refreshPosition();
      if (!this.isSpectator()) {
         this.drop(damageSource);
      }

      if (damageSource != null) {
         this.setVelocity((double)(-MathHelper.cos((this.getDamageTiltYaw() + this.getYaw()) * 0.017453292F) * 0.1F), 0.10000000149011612, (double)(-MathHelper.sin((this.getDamageTiltYaw() + this.getYaw()) * 0.017453292F) * 0.1F));
      } else {
         this.setVelocity(0.0, 0.1, 0.0);
      }

      this.incrementStat(Stats.DEATHS);
      this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
      this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
      this.extinguish();
      this.setOnFire(false);
      this.setLastDeathPos(Optional.of(GlobalPos.create(this.world.getRegistryKey(), this.getBlockPos())));
   }

   protected void dropInventory() {
      super.dropInventory();
      if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
         this.vanishCursedItems();
         this.inventory.dropAll();
      }

   }

   protected void vanishCursedItems() {
      for(int i = 0; i < this.inventory.size(); ++i) {
         ItemStack lv = this.inventory.getStack(i);
         if (!lv.isEmpty() && EnchantmentHelper.hasVanishingCurse(lv)) {
            this.inventory.removeStack(i);
         }
      }

   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return source.getType().effects().getSound();
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PLAYER_DEATH;
   }

   @Nullable
   public ItemEntity dropItem(ItemStack stack, boolean retainOwnership) {
      return this.dropItem(stack, false, retainOwnership);
   }

   @Nullable
   public ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
      if (stack.isEmpty()) {
         return null;
      } else {
         if (this.world.isClient) {
            this.swingHand(Hand.MAIN_HAND);
         }

         double d = this.getEyeY() - 0.30000001192092896;
         ItemEntity lv = new ItemEntity(this.world, this.getX(), d, this.getZ(), stack);
         lv.setPickupDelay(40);
         if (retainOwnership) {
            lv.setThrower(this.getUuid());
         }

         float f;
         float g;
         if (throwRandomly) {
            f = this.random.nextFloat() * 0.5F;
            g = this.random.nextFloat() * 6.2831855F;
            lv.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
         } else {
            f = 0.3F;
            g = MathHelper.sin(this.getPitch() * 0.017453292F);
            float h = MathHelper.cos(this.getPitch() * 0.017453292F);
            float i = MathHelper.sin(this.getYaw() * 0.017453292F);
            float j = MathHelper.cos(this.getYaw() * 0.017453292F);
            float k = this.random.nextFloat() * 6.2831855F;
            float l = 0.02F * this.random.nextFloat();
            lv.setVelocity((double)(-i * h * 0.3F) + Math.cos((double)k) * (double)l, (double)(-g * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(j * h * 0.3F) + Math.sin((double)k) * (double)l);
         }

         return lv;
      }
   }

   public float getBlockBreakingSpeed(BlockState block) {
      float f = this.inventory.getBlockBreakingSpeed(block);
      if (f > 1.0F) {
         int i = EnchantmentHelper.getEfficiency(this);
         ItemStack lv = this.getMainHandStack();
         if (i > 0 && !lv.isEmpty()) {
            f += (float)(i * i + 1);
         }
      }

      if (StatusEffectUtil.hasHaste(this)) {
         f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
      }

      if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
         float g;
         switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
            case 0:
               g = 0.3F;
               break;
            case 1:
               g = 0.09F;
               break;
            case 2:
               g = 0.0027F;
               break;
            case 3:
            default:
               g = 8.1E-4F;
         }

         f *= g;
      }

      if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
         f /= 5.0F;
      }

      if (!this.onGround) {
         f /= 5.0F;
      }

      return f;
   }

   public boolean canHarvest(BlockState state) {
      return !state.isToolRequired() || this.inventory.getMainHandStack().isSuitableFor(state);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setUuid(Uuids.getUuidFromProfile(this.gameProfile));
      NbtList lv = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
      this.inventory.readNbt(lv);
      this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
      this.sleepTimer = nbt.getShort("SleepTimer");
      this.experienceProgress = nbt.getFloat("XpP");
      this.experienceLevel = nbt.getInt("XpLevel");
      this.totalExperience = nbt.getInt("XpTotal");
      this.enchantmentTableSeed = nbt.getInt("XpSeed");
      if (this.enchantmentTableSeed == 0) {
         this.enchantmentTableSeed = this.random.nextInt();
      }

      this.setScore(nbt.getInt("Score"));
      this.hungerManager.readNbt(nbt);
      this.abilities.readNbt(nbt);
      this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue((double)this.abilities.getWalkSpeed());
      if (nbt.contains("EnderItems", NbtElement.LIST_TYPE)) {
         this.enderChestInventory.readNbtList(nbt.getList("EnderItems", NbtElement.COMPOUND_TYPE));
      }

      if (nbt.contains("ShoulderEntityLeft", NbtElement.COMPOUND_TYPE)) {
         this.setShoulderEntityLeft(nbt.getCompound("ShoulderEntityLeft"));
      }

      if (nbt.contains("ShoulderEntityRight", NbtElement.COMPOUND_TYPE)) {
         this.setShoulderEntityRight(nbt.getCompound("ShoulderEntityRight"));
      }

      if (nbt.contains("LastDeathLocation", NbtElement.COMPOUND_TYPE)) {
         DataResult var10001 = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("LastDeathLocation"));
         Logger var10002 = LOGGER;
         Objects.requireNonNull(var10002);
         this.setLastDeathPos(var10001.resultOrPartial(var10002::error));
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      NbtHelper.putDataVersion(nbt);
      nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
      nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
      nbt.putShort("SleepTimer", (short)this.sleepTimer);
      nbt.putFloat("XpP", this.experienceProgress);
      nbt.putInt("XpLevel", this.experienceLevel);
      nbt.putInt("XpTotal", this.totalExperience);
      nbt.putInt("XpSeed", this.enchantmentTableSeed);
      nbt.putInt("Score", this.getScore());
      this.hungerManager.writeNbt(nbt);
      this.abilities.writeNbt(nbt);
      nbt.put("EnderItems", this.enderChestInventory.toNbtList());
      if (!this.getShoulderEntityLeft().isEmpty()) {
         nbt.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
      }

      if (!this.getShoulderEntityRight().isEmpty()) {
         nbt.put("ShoulderEntityRight", this.getShoulderEntityRight());
      }

      this.getLastDeathPos().flatMap((arg) -> {
         DataResult var10000 = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, arg);
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         return var10000.resultOrPartial(var10001::error);
      }).ifPresent((arg2) -> {
         nbt.put("LastDeathLocation", arg2);
      });
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      if (super.isInvulnerableTo(damageSource)) {
         return true;
      } else if (damageSource.isIn(DamageTypeTags.IS_DROWNING)) {
         return !this.world.getGameRules().getBoolean(GameRules.DROWNING_DAMAGE);
      } else if (damageSource.isIn(DamageTypeTags.IS_FALL)) {
         return !this.world.getGameRules().getBoolean(GameRules.FALL_DAMAGE);
      } else if (damageSource.isIn(DamageTypeTags.IS_FIRE)) {
         return !this.world.getGameRules().getBoolean(GameRules.FIRE_DAMAGE);
      } else if (damageSource.isIn(DamageTypeTags.IS_FREEZING)) {
         return !this.world.getGameRules().getBoolean(GameRules.FREEZE_DAMAGE);
      } else {
         return false;
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (this.abilities.invulnerable && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      } else {
         this.despawnCounter = 0;
         if (this.isDead()) {
            return false;
         } else {
            if (!this.world.isClient) {
               this.dropShoulderEntities();
            }

            if (source.isScaledWithDifficulty()) {
               if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                  amount = 0.0F;
               }

               if (this.world.getDifficulty() == Difficulty.EASY) {
                  amount = Math.min(amount / 2.0F + 1.0F, amount);
               }

               if (this.world.getDifficulty() == Difficulty.HARD) {
                  amount = amount * 3.0F / 2.0F;
               }
            }

            return amount == 0.0F ? false : super.damage(source, amount);
         }
      }
   }

   protected void takeShieldHit(LivingEntity attacker) {
      super.takeShieldHit(attacker);
      if (attacker.disablesShield()) {
         this.disableShield(true);
      }

   }

   public boolean canTakeDamage() {
      return !this.getAbilities().invulnerable && super.canTakeDamage();
   }

   public boolean shouldDamagePlayer(PlayerEntity player) {
      AbstractTeam lv = this.getScoreboardTeam();
      AbstractTeam lv2 = player.getScoreboardTeam();
      if (lv == null) {
         return true;
      } else {
         return !lv.isEqual(lv2) ? true : lv.isFriendlyFireAllowed();
      }
   }

   protected void damageArmor(DamageSource source, float amount) {
      this.inventory.damageArmor(source, amount, PlayerInventory.ARMOR_SLOTS);
   }

   protected void damageHelmet(DamageSource source, float amount) {
      this.inventory.damageArmor(source, amount, PlayerInventory.HELMET_SLOTS);
   }

   protected void damageShield(float amount) {
      if (this.activeItemStack.isOf(Items.SHIELD)) {
         if (!this.world.isClient) {
            this.incrementStat(Stats.USED.getOrCreateStat(this.activeItemStack.getItem()));
         }

         if (amount >= 3.0F) {
            int i = 1 + MathHelper.floor(amount);
            Hand lv = this.getActiveHand();
            this.activeItemStack.damage(i, (LivingEntity)this, (Consumer)((player) -> {
               player.sendToolBreakStatus(lv);
            }));
            if (this.activeItemStack.isEmpty()) {
               if (lv == Hand.MAIN_HAND) {
                  this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
               } else {
                  this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
               }

               this.activeItemStack = ItemStack.EMPTY;
               this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.random.nextFloat() * 0.4F);
            }
         }

      }
   }

   protected void applyDamage(DamageSource source, float amount) {
      if (!this.isInvulnerableTo(source)) {
         amount = this.applyArmorToDamage(source, amount);
         amount = this.modifyAppliedDamage(source, amount);
         float g = amount;
         amount = Math.max(amount - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
         float h = g - amount;
         if (h > 0.0F && h < 3.4028235E37F) {
            this.increaseStat(Stats.DAMAGE_ABSORBED, Math.round(h * 10.0F));
         }

         if (amount != 0.0F) {
            this.addExhaustion(source.getExhaustion());
            float i = this.getHealth();
            this.getDamageTracker().onDamage(source, i, amount);
            this.setHealth(this.getHealth() - amount);
            if (amount < 3.4028235E37F) {
               this.increaseStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0F));
            }

            this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
         }
      }
   }

   protected boolean isOnSoulSpeedBlock() {
      return !this.abilities.flying && super.isOnSoulSpeedBlock();
   }

   public boolean shouldFilterText() {
      return false;
   }

   public void openEditSignScreen(SignBlockEntity sign, boolean front) {
   }

   public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
   }

   public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
   }

   public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
   }

   public void openJigsawScreen(JigsawBlockEntity jigsaw) {
   }

   public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
   }

   public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
      return OptionalInt.empty();
   }

   public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
   }

   public void useBook(ItemStack book, Hand hand) {
   }

   public ActionResult interact(Entity entity, Hand hand) {
      if (this.isSpectator()) {
         if (entity instanceof NamedScreenHandlerFactory) {
            this.openHandledScreen((NamedScreenHandlerFactory)entity);
         }

         return ActionResult.PASS;
      } else {
         ItemStack lv = this.getStackInHand(hand);
         ItemStack lv2 = lv.copy();
         ActionResult lv3 = entity.interact(this, hand);
         if (lv3.isAccepted()) {
            if (this.abilities.creativeMode && lv == this.getStackInHand(hand) && lv.getCount() < lv2.getCount()) {
               lv.setCount(lv2.getCount());
            }

            return lv3;
         } else {
            if (!lv.isEmpty() && entity instanceof LivingEntity) {
               if (this.abilities.creativeMode) {
                  lv = lv2;
               }

               ActionResult lv4 = lv.useOnEntity(this, (LivingEntity)entity, hand);
               if (lv4.isAccepted()) {
                  this.world.emitGameEvent(GameEvent.ENTITY_INTERACT, entity.getPos(), GameEvent.Emitter.of((Entity)this));
                  if (lv.isEmpty() && !this.abilities.creativeMode) {
                     this.setStackInHand(hand, ItemStack.EMPTY);
                  }

                  return lv4;
               }
            }

            return ActionResult.PASS;
         }
      }
   }

   public double getHeightOffset() {
      return -0.35;
   }

   public void dismountVehicle() {
      super.dismountVehicle();
      this.ridingCooldown = 0;
   }

   protected boolean isImmobile() {
      return super.isImmobile() || this.isSleeping();
   }

   public boolean shouldSwimInFluids() {
      return !this.abilities.flying;
   }

   protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
      if (!this.abilities.flying && movement.y <= 0.0 && (type == MovementType.SELF || type == MovementType.PLAYER) && this.clipAtLedge() && this.method_30263()) {
         double d = movement.x;
         double e = movement.z;
         double f = 0.05;

         while(true) {
            while(d != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(d, (double)(-this.getStepHeight()), 0.0))) {
               if (d < 0.05 && d >= -0.05) {
                  d = 0.0;
               } else if (d > 0.0) {
                  d -= 0.05;
               } else {
                  d += 0.05;
               }
            }

            while(true) {
               while(e != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(0.0, (double)(-this.getStepHeight()), e))) {
                  if (e < 0.05 && e >= -0.05) {
                     e = 0.0;
                  } else if (e > 0.0) {
                     e -= 0.05;
                  } else {
                     e += 0.05;
                  }
               }

               while(true) {
                  while(d != 0.0 && e != 0.0 && this.world.isSpaceEmpty(this, this.getBoundingBox().offset(d, (double)(-this.getStepHeight()), e))) {
                     if (d < 0.05 && d >= -0.05) {
                        d = 0.0;
                     } else if (d > 0.0) {
                        d -= 0.05;
                     } else {
                        d += 0.05;
                     }

                     if (e < 0.05 && e >= -0.05) {
                        e = 0.0;
                     } else if (e > 0.0) {
                        e -= 0.05;
                     } else {
                        e += 0.05;
                     }
                  }

                  movement = new Vec3d(d, movement.y, e);
                  return movement;
               }
            }
         }
      } else {
         return movement;
      }
   }

   private boolean method_30263() {
      return this.onGround || this.fallDistance < this.getStepHeight() && !this.world.isSpaceEmpty(this, this.getBoundingBox().offset(0.0, (double)(this.fallDistance - this.getStepHeight()), 0.0));
   }

   public void attack(Entity target) {
      if (target.isAttackable()) {
         if (!target.handleAttack(this)) {
            float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float g;
            if (target instanceof LivingEntity) {
               g = EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            } else {
               g = EnchantmentHelper.getAttackDamage(this.getMainHandStack(), EntityGroup.DEFAULT);
            }

            float h = this.getAttackCooldownProgress(0.5F);
            f *= 0.2F + h * h * 0.8F;
            g *= h;
            this.resetLastAttackedTicks();
            if (f > 0.0F || g > 0.0F) {
               boolean bl = h > 0.9F;
               boolean bl2 = false;
               int i = 0;
               i += EnchantmentHelper.getKnockback(this);
               if (this.isSprinting() && bl) {
                  this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
                  ++i;
                  bl2 = true;
               }

               boolean bl3 = bl && this.fallDistance > 0.0F && !this.onGround && !this.isClimbing() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && !this.hasVehicle() && target instanceof LivingEntity;
               bl3 = bl3 && !this.isSprinting();
               if (bl3) {
                  f *= 1.5F;
               }

               f += g;
               boolean bl4 = false;
               double d = (double)(this.horizontalSpeed - this.prevHorizontalSpeed);
               if (bl && !bl3 && !bl2 && this.onGround && d < (double)this.getMovementSpeed()) {
                  ItemStack lv = this.getStackInHand(Hand.MAIN_HAND);
                  if (lv.getItem() instanceof SwordItem) {
                     bl4 = true;
                  }
               }

               float j = 0.0F;
               boolean bl5 = false;
               int k = EnchantmentHelper.getFireAspect(this);
               if (target instanceof LivingEntity) {
                  j = ((LivingEntity)target).getHealth();
                  if (k > 0 && !target.isOnFire()) {
                     bl5 = true;
                     target.setOnFireFor(1);
                  }
               }

               Vec3d lv2 = target.getVelocity();
               boolean bl6 = target.damage(this.getDamageSources().playerAttack(this), f);
               if (bl6) {
                  if (i > 0) {
                     if (target instanceof LivingEntity) {
                        ((LivingEntity)target).takeKnockback((double)((float)i * 0.5F), (double)MathHelper.sin(this.getYaw() * 0.017453292F), (double)(-MathHelper.cos(this.getYaw() * 0.017453292F)));
                     } else {
                        target.addVelocity((double)(-MathHelper.sin(this.getYaw() * 0.017453292F) * (float)i * 0.5F), 0.1, (double)(MathHelper.cos(this.getYaw() * 0.017453292F) * (float)i * 0.5F));
                     }

                     this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
                     this.setSprinting(false);
                  }

                  if (bl4) {
                     float l = 1.0F + EnchantmentHelper.getSweepingMultiplier(this) * f;
                     List list = this.world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0));
                     Iterator var19 = list.iterator();

                     label166:
                     while(true) {
                        LivingEntity lv3;
                        do {
                           do {
                              do {
                                 do {
                                    if (!var19.hasNext()) {
                                       this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                                       this.spawnSweepAttackParticles();
                                       break label166;
                                    }

                                    lv3 = (LivingEntity)var19.next();
                                 } while(lv3 == this);
                              } while(lv3 == target);
                           } while(this.isTeammate(lv3));
                        } while(lv3 instanceof ArmorStandEntity && ((ArmorStandEntity)lv3).isMarker());

                        if (this.squaredDistanceTo(lv3) < 9.0) {
                           lv3.takeKnockback(0.4000000059604645, (double)MathHelper.sin(this.getYaw() * 0.017453292F), (double)(-MathHelper.cos(this.getYaw() * 0.017453292F)));
                           lv3.damage(this.getDamageSources().playerAttack(this), l);
                        }
                     }
                  }

                  if (target instanceof ServerPlayerEntity && target.velocityModified) {
                     ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                     target.velocityModified = false;
                     target.setVelocity(lv2);
                  }

                  if (bl3) {
                     this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                     this.addCritParticles(target);
                  }

                  if (!bl3 && !bl4) {
                     if (bl) {
                        this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                     } else {
                        this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0F, 1.0F);
                     }
                  }

                  if (g > 0.0F) {
                     this.addEnchantedHitParticles(target);
                  }

                  this.onAttacking(target);
                  if (target instanceof LivingEntity) {
                     EnchantmentHelper.onUserDamaged((LivingEntity)target, this);
                  }

                  EnchantmentHelper.onTargetDamaged(this, target);
                  ItemStack lv4 = this.getMainHandStack();
                  Entity lv5 = target;
                  if (target instanceof EnderDragonPart) {
                     lv5 = ((EnderDragonPart)target).owner;
                  }

                  if (!this.world.isClient && !lv4.isEmpty() && lv5 instanceof LivingEntity) {
                     lv4.postHit((LivingEntity)lv5, this);
                     if (lv4.isEmpty()) {
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                     }
                  }

                  if (target instanceof LivingEntity) {
                     float m = j - ((LivingEntity)target).getHealth();
                     this.increaseStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
                     if (k > 0) {
                        target.setOnFireFor(k * 4);
                     }

                     if (this.world instanceof ServerWorld && m > 2.0F) {
                        int n = (int)((double)m * 0.5);
                        ((ServerWorld)this.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                     }
                  }

                  this.addExhaustion(0.1F);
               } else {
                  this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
                  if (bl5) {
                     target.extinguish();
                  }
               }
            }

         }
      }
   }

   protected void attackLivingEntity(LivingEntity target) {
      this.attack(target);
   }

   public void disableShield(boolean sprinting) {
      float f = 0.25F + (float)EnchantmentHelper.getEfficiency(this) * 0.05F;
      if (sprinting) {
         f += 0.75F;
      }

      if (this.random.nextFloat() < f) {
         this.getItemCooldownManager().set(Items.SHIELD, 100);
         this.clearActiveItem();
         this.world.sendEntityStatus(this, EntityStatuses.BREAK_SHIELD);
      }

   }

   public void addCritParticles(Entity target) {
   }

   public void addEnchantedHitParticles(Entity target) {
   }

   public void spawnSweepAttackParticles() {
      double d = (double)(-MathHelper.sin(this.getYaw() * 0.017453292F));
      double e = (double)MathHelper.cos(this.getYaw() * 0.017453292F);
      if (this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
      }

   }

   public void requestRespawn() {
   }

   public void remove(Entity.RemovalReason reason) {
      super.remove(reason);
      this.playerScreenHandler.onClosed(this);
      if (this.currentScreenHandler != null && this.shouldCloseHandledScreenOnRespawn()) {
         this.onHandledScreenClosed();
      }

   }

   public boolean isMainPlayer() {
      return false;
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public PlayerInventory getInventory() {
      return this.inventory;
   }

   public PlayerAbilities getAbilities() {
      return this.abilities;
   }

   public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
   }

   public boolean shouldCloseHandledScreenOnRespawn() {
      return this.currentScreenHandler != this.playerScreenHandler;
   }

   public Either trySleep(BlockPos pos) {
      this.sleep(pos);
      this.sleepTimer = 0;
      return Either.right(Unit.INSTANCE);
   }

   public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
      super.wakeUp();
      if (this.world instanceof ServerWorld && updateSleepingPlayers) {
         ((ServerWorld)this.world).updateSleepingPlayers();
      }

      this.sleepTimer = skipSleepTimer ? 0 : 100;
   }

   public void wakeUp() {
      this.wakeUp(true, true);
   }

   public static Optional findRespawnPosition(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive) {
      BlockState lv = world.getBlockState(pos);
      Block lv2 = lv.getBlock();
      if (lv2 instanceof RespawnAnchorBlock && (forced || (Integer)lv.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
         Optional optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
         if (!forced && !alive && optional.isPresent()) {
            world.setBlockState(pos, (BlockState)lv.with(RespawnAnchorBlock.CHARGES, (Integer)lv.get(RespawnAnchorBlock.CHARGES) - 1), Block.NOTIFY_ALL);
         }

         return optional;
      } else if (lv2 instanceof BedBlock && BedBlock.isBedWorking(world)) {
         return BedBlock.findWakeUpPosition(EntityType.PLAYER, world, pos, (Direction)lv.get(BedBlock.FACING), angle);
      } else if (!forced) {
         return Optional.empty();
      } else {
         boolean bl3 = lv2.canMobSpawnInside(lv);
         BlockState lv3 = world.getBlockState(pos.up());
         boolean bl4 = lv3.getBlock().canMobSpawnInside(lv3);
         return bl3 && bl4 ? Optional.of(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5)) : Optional.empty();
      }
   }

   public boolean canResetTimeBySleeping() {
      return this.isSleeping() && this.sleepTimer >= 100;
   }

   public int getSleepTimer() {
      return this.sleepTimer;
   }

   public void sendMessage(Text message, boolean overlay) {
   }

   public void incrementStat(Identifier stat) {
      this.incrementStat(Stats.CUSTOM.getOrCreateStat(stat));
   }

   public void increaseStat(Identifier stat, int amount) {
      this.increaseStat(Stats.CUSTOM.getOrCreateStat(stat), amount);
   }

   public void incrementStat(Stat stat) {
      this.increaseStat((Stat)stat, 1);
   }

   public void increaseStat(Stat stat, int amount) {
   }

   public void resetStat(Stat stat) {
   }

   public int unlockRecipes(Collection recipes) {
      return 0;
   }

   public void unlockRecipes(Identifier[] ids) {
   }

   public int lockRecipes(Collection recipes) {
      return 0;
   }

   public void jump() {
      super.jump();
      this.incrementStat(Stats.JUMP);
      if (this.isSprinting()) {
         this.addExhaustion(0.2F);
      } else {
         this.addExhaustion(0.05F);
      }

   }

   public void travel(Vec3d movementInput) {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      double g;
      if (this.isSwimming() && !this.hasVehicle()) {
         g = this.getRotationVector().y;
         double h = g < -0.2 ? 0.085 : 0.06;
         if (g <= 0.0 || this.jumping || !this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
            Vec3d lv = this.getVelocity();
            this.setVelocity(lv.add(0.0, (g - lv.y) * h, 0.0));
         }
      }

      if (this.abilities.flying && !this.hasVehicle()) {
         g = this.getVelocity().y;
         super.travel(movementInput);
         Vec3d lv2 = this.getVelocity();
         this.setVelocity(lv2.x, g * 0.6, lv2.z);
         this.onLanding();
         this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
      } else {
         super.travel(movementInput);
      }

      this.increaseTravelMotionStats(this.getX() - d, this.getY() - e, this.getZ() - f);
   }

   public void updateSwimming() {
      if (this.abilities.flying) {
         this.setSwimming(false);
      } else {
         super.updateSwimming();
      }

   }

   protected boolean doesNotSuffocate(BlockPos pos) {
      return !this.world.getBlockState(pos).shouldSuffocate(this.world, pos);
   }

   public float getMovementSpeed() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
   }

   public void increaseTravelMotionStats(double dx, double dy, double dz) {
      if (!this.hasVehicle()) {
         int i;
         if (this.isSwimming()) {
            i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            if (i > 0) {
               this.increaseStat(Stats.SWIM_ONE_CM, i);
               this.addExhaustion(0.01F * (float)i * 0.01F);
            }
         } else if (this.isSubmergedIn(FluidTags.WATER)) {
            i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            if (i > 0) {
               this.increaseStat(Stats.WALK_UNDER_WATER_ONE_CM, i);
               this.addExhaustion(0.01F * (float)i * 0.01F);
            }
         } else if (this.isTouchingWater()) {
            i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
            if (i > 0) {
               this.increaseStat(Stats.WALK_ON_WATER_ONE_CM, i);
               this.addExhaustion(0.01F * (float)i * 0.01F);
            }
         } else if (this.isClimbing()) {
            if (dy > 0.0) {
               this.increaseStat(Stats.CLIMB_ONE_CM, (int)Math.round(dy * 100.0));
            }
         } else if (this.onGround) {
            i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
            if (i > 0) {
               if (this.isSprinting()) {
                  this.increaseStat(Stats.SPRINT_ONE_CM, i);
                  this.addExhaustion(0.1F * (float)i * 0.01F);
               } else if (this.isInSneakingPose()) {
                  this.increaseStat(Stats.CROUCH_ONE_CM, i);
                  this.addExhaustion(0.0F * (float)i * 0.01F);
               } else {
                  this.increaseStat(Stats.WALK_ONE_CM, i);
                  this.addExhaustion(0.0F * (float)i * 0.01F);
               }
            }
         } else if (this.isFallFlying()) {
            i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            this.increaseStat(Stats.AVIATE_ONE_CM, i);
         } else {
            i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
            if (i > 25) {
               this.increaseStat(Stats.FLY_ONE_CM, i);
            }
         }

      }
   }

   private void increaseRidingMotionStats(double dx, double dy, double dz) {
      if (this.hasVehicle()) {
         int i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
         if (i > 0) {
            Entity lv = this.getVehicle();
            if (lv instanceof AbstractMinecartEntity) {
               this.increaseStat(Stats.MINECART_ONE_CM, i);
            } else if (lv instanceof BoatEntity) {
               this.increaseStat(Stats.BOAT_ONE_CM, i);
            } else if (lv instanceof PigEntity) {
               this.increaseStat(Stats.PIG_ONE_CM, i);
            } else if (lv instanceof AbstractHorseEntity) {
               this.increaseStat(Stats.HORSE_ONE_CM, i);
            } else if (lv instanceof StriderEntity) {
               this.increaseStat(Stats.STRIDER_ONE_CM, i);
            }
         }
      }

   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      if (this.abilities.allowFlying) {
         return false;
      } else {
         if (fallDistance >= 2.0F) {
            this.increaseStat(Stats.FALL_ONE_CM, (int)Math.round((double)fallDistance * 100.0));
         }

         return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
      }
   }

   public boolean checkFallFlying() {
      if (!this.onGround && !this.isFallFlying() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
         ItemStack lv = this.getEquippedStack(EquipmentSlot.CHEST);
         if (lv.isOf(Items.ELYTRA) && ElytraItem.isUsable(lv)) {
            this.startFallFlying();
            return true;
         }
      }

      return false;
   }

   public void startFallFlying() {
      this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
   }

   public void stopFallFlying() {
      this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
      this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
   }

   protected void onSwimmingStart() {
      if (!this.isSpectator()) {
         super.onSwimmingStart();
      }

   }

   public LivingEntity.FallSounds getFallSounds() {
      return new LivingEntity.FallSounds(SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundEvents.ENTITY_PLAYER_BIG_FALL);
   }

   public boolean onKilledOther(ServerWorld world, LivingEntity other) {
      this.incrementStat(Stats.KILLED.getOrCreateStat(other.getType()));
      return true;
   }

   public void slowMovement(BlockState state, Vec3d multiplier) {
      if (!this.abilities.flying) {
         super.slowMovement(state, multiplier);
      }

   }

   public void addExperience(int experience) {
      this.addScore(experience);
      this.experienceProgress += (float)experience / (float)this.getNextLevelExperience();
      this.totalExperience = MathHelper.clamp(this.totalExperience + experience, 0, Integer.MAX_VALUE);

      while(this.experienceProgress < 0.0F) {
         float f = this.experienceProgress * (float)this.getNextLevelExperience();
         if (this.experienceLevel > 0) {
            this.addExperienceLevels(-1);
            this.experienceProgress = 1.0F + f / (float)this.getNextLevelExperience();
         } else {
            this.addExperienceLevels(-1);
            this.experienceProgress = 0.0F;
         }
      }

      while(this.experienceProgress >= 1.0F) {
         this.experienceProgress = (this.experienceProgress - 1.0F) * (float)this.getNextLevelExperience();
         this.addExperienceLevels(1);
         this.experienceProgress /= (float)this.getNextLevelExperience();
      }

   }

   public int getEnchantmentTableSeed() {
      return this.enchantmentTableSeed;
   }

   public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
      this.experienceLevel -= experienceLevels;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      this.enchantmentTableSeed = this.random.nextInt();
   }

   public void addExperienceLevels(int levels) {
      this.experienceLevel += levels;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastPlayedLevelUpSoundTime < (float)this.age - 100.0F) {
         float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75F, 1.0F);
         this.lastPlayedLevelUpSoundTime = this.age;
      }

   }

   public int getNextLevelExperience() {
      if (this.experienceLevel >= 30) {
         return 112 + (this.experienceLevel - 30) * 9;
      } else {
         return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
      }
   }

   public void addExhaustion(float exhaustion) {
      if (!this.abilities.invulnerable) {
         if (!this.world.isClient) {
            this.hungerManager.addExhaustion(exhaustion);
         }

      }
   }

   public Optional getSculkShriekerWarningManager() {
      return Optional.empty();
   }

   public HungerManager getHungerManager() {
      return this.hungerManager;
   }

   public boolean canConsume(boolean ignoreHunger) {
      return this.abilities.invulnerable || ignoreHunger || this.hungerManager.isNotFull();
   }

   public boolean canFoodHeal() {
      return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
   }

   public boolean canModifyBlocks() {
      return this.abilities.allowModifyWorld;
   }

   public boolean canPlaceOn(BlockPos pos, Direction facing, ItemStack stack) {
      if (this.abilities.allowModifyWorld) {
         return true;
      } else {
         BlockPos lv = pos.offset(facing.getOpposite());
         CachedBlockPosition lv2 = new CachedBlockPosition(this.world, lv, false);
         return stack.canPlaceOn(this.world.getRegistryManager().get(RegistryKeys.BLOCK), lv2);
      }
   }

   public int getXpToDrop() {
      if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !this.isSpectator()) {
         int i = this.experienceLevel * 7;
         return i > 100 ? 100 : i;
      } else {
         return 0;
      }
   }

   protected boolean shouldAlwaysDropXp() {
      return true;
   }

   public boolean shouldRenderName() {
      return true;
   }

   protected Entity.MoveEffect getMoveEffect() {
      return this.abilities.flying || this.onGround && this.isSneaky() ? Entity.MoveEffect.NONE : Entity.MoveEffect.ALL;
   }

   public void sendAbilitiesUpdate() {
   }

   public Text getName() {
      return Text.literal(this.gameProfile.getName());
   }

   public EnderChestInventory getEnderChestInventory() {
      return this.enderChestInventory;
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      if (slot == EquipmentSlot.MAINHAND) {
         return this.inventory.getMainHandStack();
      } else if (slot == EquipmentSlot.OFFHAND) {
         return (ItemStack)this.inventory.offHand.get(0);
      } else {
         return slot.getType() == EquipmentSlot.Type.ARMOR ? (ItemStack)this.inventory.armor.get(slot.getEntitySlotId()) : ItemStack.EMPTY;
      }
   }

   protected boolean isArmorSlot(EquipmentSlot slot) {
      return slot.getType() == EquipmentSlot.Type.ARMOR;
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      this.processEquippedStack(stack);
      if (slot == EquipmentSlot.MAINHAND) {
         this.onEquipStack(slot, (ItemStack)this.inventory.main.set(this.inventory.selectedSlot, stack), stack);
      } else if (slot == EquipmentSlot.OFFHAND) {
         this.onEquipStack(slot, (ItemStack)this.inventory.offHand.set(0, stack), stack);
      } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
         this.onEquipStack(slot, (ItemStack)this.inventory.armor.set(slot.getEntitySlotId(), stack), stack);
      }

   }

   public boolean giveItemStack(ItemStack stack) {
      return this.inventory.insertStack(stack);
   }

   public Iterable getHandItems() {
      return Lists.newArrayList(new ItemStack[]{this.getMainHandStack(), this.getOffHandStack()});
   }

   public Iterable getArmorItems() {
      return this.inventory.armor;
   }

   public boolean addShoulderEntity(NbtCompound entityNbt) {
      if (!this.hasVehicle() && this.onGround && !this.isTouchingWater() && !this.inPowderSnow) {
         if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(entityNbt);
            this.shoulderEntityAddedTime = this.world.getTime();
            return true;
         } else if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(entityNbt);
            this.shoulderEntityAddedTime = this.world.getTime();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected void dropShoulderEntities() {
      if (this.shoulderEntityAddedTime + 20L < this.world.getTime()) {
         this.dropShoulderEntity(this.getShoulderEntityLeft());
         this.setShoulderEntityLeft(new NbtCompound());
         this.dropShoulderEntity(this.getShoulderEntityRight());
         this.setShoulderEntityRight(new NbtCompound());
      }

   }

   private void dropShoulderEntity(NbtCompound entityNbt) {
      if (!this.world.isClient && !entityNbt.isEmpty()) {
         EntityType.getEntityFromNbt(entityNbt, this.world).ifPresent((entity) -> {
            if (entity instanceof TameableEntity) {
               ((TameableEntity)entity).setOwnerUuid(this.uuid);
            }

            entity.setPosition(this.getX(), this.getY() + 0.699999988079071, this.getZ());
            ((ServerWorld)this.world).tryLoadEntity(entity);
         });
      }

   }

   public abstract boolean isSpectator();

   public boolean canBeHitByProjectile() {
      return !this.isSpectator() && super.canBeHitByProjectile();
   }

   public boolean isSwimming() {
      return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
   }

   public abstract boolean isCreative();

   public boolean isPushedByFluids() {
      return !this.abilities.flying;
   }

   public Scoreboard getScoreboard() {
      return this.world.getScoreboard();
   }

   public Text getDisplayName() {
      MutableText lv = Team.decorateName(this.getScoreboardTeam(), this.getName());
      return this.addTellClickEvent(lv);
   }

   private MutableText addTellClickEvent(MutableText component) {
      String string = this.getGameProfile().getName();
      return component.styled((style) -> {
         return style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(this.getHoverEvent()).withInsertion(string);
      });
   }

   public String getEntityName() {
      return this.getGameProfile().getName();
   }

   public float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      switch (pose) {
         case SWIMMING:
         case FALL_FLYING:
         case SPIN_ATTACK:
            return 0.4F;
         case CROUCHING:
            return 1.27F;
         default:
            return 1.62F;
      }
   }

   public void setAbsorptionAmount(float amount) {
      if (amount < 0.0F) {
         amount = 0.0F;
      }

      this.getDataTracker().set(ABSORPTION_AMOUNT, amount);
   }

   public float getAbsorptionAmount() {
      return (Float)this.getDataTracker().get(ABSORPTION_AMOUNT);
   }

   public boolean isPartVisible(PlayerModelPart modelPart) {
      return ((Byte)this.getDataTracker().get(PLAYER_MODEL_PARTS) & modelPart.getBitFlag()) == modelPart.getBitFlag();
   }

   public StackReference getStackReference(int mappedIndex) {
      if (mappedIndex >= 0 && mappedIndex < this.inventory.main.size()) {
         return StackReference.of(this.inventory, mappedIndex);
      } else {
         int j = mappedIndex - 200;
         return j >= 0 && j < this.enderChestInventory.size() ? StackReference.of(this.enderChestInventory, j) : super.getStackReference(mappedIndex);
      }
   }

   public boolean hasReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public void setReducedDebugInfo(boolean reducedDebugInfo) {
      this.reducedDebugInfo = reducedDebugInfo;
   }

   public void setFireTicks(int fireTicks) {
      super.setFireTicks(this.abilities.invulnerable ? Math.min(fireTicks, 1) : fireTicks);
   }

   public Arm getMainArm() {
      return (Byte)this.dataTracker.get(MAIN_ARM) == 0 ? Arm.LEFT : Arm.RIGHT;
   }

   public void setMainArm(Arm arm) {
      this.dataTracker.set(MAIN_ARM, (byte)(arm == Arm.LEFT ? 0 : 1));
   }

   public NbtCompound getShoulderEntityLeft() {
      return (NbtCompound)this.dataTracker.get(LEFT_SHOULDER_ENTITY);
   }

   protected void setShoulderEntityLeft(NbtCompound entityNbt) {
      this.dataTracker.set(LEFT_SHOULDER_ENTITY, entityNbt);
   }

   public NbtCompound getShoulderEntityRight() {
      return (NbtCompound)this.dataTracker.get(RIGHT_SHOULDER_ENTITY);
   }

   protected void setShoulderEntityRight(NbtCompound entityNbt) {
      this.dataTracker.set(RIGHT_SHOULDER_ENTITY, entityNbt);
   }

   public float getAttackCooldownProgressPerTick() {
      return (float)(1.0 / this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
   }

   public float getAttackCooldownProgress(float baseTime) {
      return MathHelper.clamp(((float)this.lastAttackedTicks + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
   }

   public void resetLastAttackedTicks() {
      this.lastAttackedTicks = 0;
   }

   public ItemCooldownManager getItemCooldownManager() {
      return this.itemCooldownManager;
   }

   protected float getVelocityMultiplier() {
      return !this.abilities.flying && !this.isFallFlying() ? super.getVelocityMultiplier() : 1.0F;
   }

   public float getLuck() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_LUCK);
   }

   public boolean isCreativeLevelTwoOp() {
      return this.abilities.creativeMode && this.getPermissionLevel() >= 2;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot lv = MobEntity.getPreferredEquipmentSlot(stack);
      return this.getEquippedStack(lv).isEmpty();
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return (EntityDimensions)POSE_DIMENSIONS.getOrDefault(pose, STANDING_DIMENSIONS);
   }

   public ImmutableList getPoses() {
      return ImmutableList.of(EntityPose.STANDING, EntityPose.CROUCHING, EntityPose.SWIMMING);
   }

   public ItemStack getProjectileType(ItemStack stack) {
      if (!(stack.getItem() instanceof RangedWeaponItem)) {
         return ItemStack.EMPTY;
      } else {
         Predicate predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
         ItemStack lv = RangedWeaponItem.getHeldProjectile(this, predicate);
         if (!lv.isEmpty()) {
            return lv;
         } else {
            predicate = ((RangedWeaponItem)stack.getItem()).getProjectiles();

            for(int i = 0; i < this.inventory.size(); ++i) {
               ItemStack lv2 = this.inventory.getStack(i);
               if (predicate.test(lv2)) {
                  return lv2;
               }
            }

            return this.abilities.creativeMode ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
         }
      }
   }

   public ItemStack eatFood(World world, ItemStack stack) {
      this.getHungerManager().eat(stack.getItem(), stack);
      this.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
      world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
      if (this instanceof ServerPlayerEntity) {
         Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)this, stack);
      }

      return super.eatFood(world, stack);
   }

   protected boolean shouldRemoveSoulSpeedBoost(BlockState landingState) {
      return this.abilities.flying || super.shouldRemoveSoulSpeedBoost(landingState);
   }

   public Vec3d getLeashPos(float delta) {
      double d = 0.22 * (this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0);
      float g = MathHelper.lerp(delta * 0.5F, this.getPitch(), this.prevPitch) * 0.017453292F;
      float h = MathHelper.lerp(delta, this.prevBodyYaw, this.bodyYaw) * 0.017453292F;
      double e;
      if (!this.isFallFlying() && !this.isUsingRiptide()) {
         if (this.isInSwimmingPose()) {
            return this.getLerpedPos(delta).add((new Vec3d(d, 0.2, -0.15)).rotateX(-g).rotateY(-h));
         } else {
            double m = this.getBoundingBox().getYLength() - 1.0;
            e = this.isInSneakingPose() ? -0.2 : 0.07;
            return this.getLerpedPos(delta).add((new Vec3d(d, m, e)).rotateY(-h));
         }
      } else {
         Vec3d lv = this.getRotationVec(delta);
         Vec3d lv2 = this.getVelocity();
         e = lv2.horizontalLengthSquared();
         double i = lv.horizontalLengthSquared();
         float l;
         if (e > 0.0 && i > 0.0) {
            double j = (lv2.x * lv.x + lv2.z * lv.z) / Math.sqrt(e * i);
            double k = lv2.x * lv.z - lv2.z * lv.x;
            l = (float)(Math.signum(k) * Math.acos(j));
         } else {
            l = 0.0F;
         }

         return this.getLerpedPos(delta).add((new Vec3d(d, -0.11, 0.85)).rotateZ(-l).rotateX(-g).rotateY(-h));
      }
   }

   public boolean isPlayer() {
      return true;
   }

   public boolean isUsingSpyglass() {
      return this.isUsingItem() && this.getActiveItem().isOf(Items.SPYGLASS);
   }

   public boolean shouldSave() {
      return false;
   }

   public Optional getLastDeathPos() {
      return this.lastDeathPos;
   }

   public void setLastDeathPos(Optional lastDeathPos) {
      this.lastDeathPos = lastDeathPos;
   }

   public float getDamageTiltYaw() {
      return this.damageTiltYaw;
   }

   public void animateDamage(float yaw) {
      super.animateDamage(yaw);
      this.damageTiltYaw = yaw;
   }

   public boolean canSprintAsVehicle() {
      return true;
   }

   protected float getOffGroundSpeed() {
      if (this.abilities.flying && !this.hasVehicle()) {
         return this.isSprinting() ? this.abilities.getFlySpeed() * 2.0F : this.abilities.getFlySpeed();
      } else {
         return this.isSprinting() ? 0.025999999F : 0.02F;
      }
   }

   static {
      POSE_DIMENSIONS = ImmutableMap.builder().put(EntityPose.STANDING, STANDING_DIMENSIONS).put(EntityPose.SLEEPING, SLEEPING_DIMENSIONS).put(EntityPose.FALL_FLYING, EntityDimensions.changing(0.6F, 0.6F)).put(EntityPose.SWIMMING, EntityDimensions.changing(0.6F, 0.6F)).put(EntityPose.SPIN_ATTACK, EntityDimensions.changing(0.6F, 0.6F)).put(EntityPose.CROUCHING, EntityDimensions.changing(0.6F, 1.5F)).put(EntityPose.DYING, EntityDimensions.fixed(0.2F, 0.2F)).build();
      ABSORPTION_AMOUNT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
      SCORE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
      PLAYER_MODEL_PARTS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
      MAIN_ARM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
      LEFT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
      RIGHT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
   }

   public static enum SleepFailureReason {
      NOT_POSSIBLE_HERE,
      NOT_POSSIBLE_NOW(Text.translatable("block.minecraft.bed.no_sleep")),
      TOO_FAR_AWAY(Text.translatable("block.minecraft.bed.too_far_away")),
      OBSTRUCTED(Text.translatable("block.minecraft.bed.obstructed")),
      OTHER_PROBLEM,
      NOT_SAFE(Text.translatable("block.minecraft.bed.not_safe"));

      @Nullable
      private final Text message;

      private SleepFailureReason() {
         this.message = null;
      }

      private SleepFailureReason(Text message) {
         this.message = message;
      }

      @Nullable
      public Text getMessage() {
         return this.message;
      }

      // $FF: synthetic method
      private static SleepFailureReason[] method_36661() {
         return new SleepFailureReason[]{NOT_POSSIBLE_HERE, NOT_POSSIBLE_NOW, TOO_FAR_AWAY, OBSTRUCTED, OTHER_PROBLEM, NOT_SAFE};
      }
   }
}
