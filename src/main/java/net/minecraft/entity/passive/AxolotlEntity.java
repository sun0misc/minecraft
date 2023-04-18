package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.entity.AngledModelEntity;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
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
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class AxolotlEntity extends AnimalEntity implements AngledModelEntity, VariantHolder, Bucketable {
   public static final int PLAY_DEAD_TICKS = 200;
   protected static final ImmutableList SENSORS;
   protected static final ImmutableList MEMORY_MODULES;
   private static final TrackedData VARIANT;
   private static final TrackedData PLAYING_DEAD;
   private static final TrackedData FROM_BUCKET;
   public static final double BUFF_RANGE = 20.0;
   public static final int BLUE_BABY_CHANCE = 1200;
   private static final int MAX_AIR = 6000;
   public static final String VARIANT_KEY = "Variant";
   private static final int HYDRATION_BY_POTION = 1800;
   private static final int MAX_REGENERATION_BUFF_DURATION = 2400;
   private final Map modelAngles = Maps.newHashMap();
   private static final int BUFF_DURATION = 100;

   public AxolotlEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.moveControl = new AxolotlMoveControl(this);
      this.lookControl = new AxolotlLookControl(this, 20);
      this.setStepHeight(1.0F);
   }

   public Map getModelAngles() {
      return this.modelAngles;
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return 0.0F;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, 0);
      this.dataTracker.startTracking(PLAYING_DEAD, false);
      this.dataTracker.startTracking(FROM_BUCKET, false);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Variant", this.getVariant().getId());
      nbt.putBoolean("FromBucket", this.isFromBucket());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setVariant(AxolotlEntity.Variant.byId(nbt.getInt("Variant")));
      this.setFromBucket(nbt.getBoolean("FromBucket"));
   }

   public void playAmbientSound() {
      if (!this.isPlayingDead()) {
         super.playAmbientSound();
      }
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      boolean bl = false;
      if (spawnReason == SpawnReason.BUCKET) {
         return (EntityData)entityData;
      } else {
         Random lv = world.getRandom();
         if (entityData instanceof AxolotlData) {
            if (((AxolotlData)entityData).getSpawnedCount() >= 2) {
               bl = true;
            }
         } else {
            entityData = new AxolotlData(new Variant[]{AxolotlEntity.Variant.getRandomNatural(lv), AxolotlEntity.Variant.getRandomNatural(lv)});
         }

         this.setVariant(((AxolotlData)entityData).getRandomVariant(lv));
         if (bl) {
            this.setBreedingAge(-24000);
         }

         return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
      }
   }

   public void baseTick() {
      int i = this.getAir();
      super.baseTick();
      if (!this.isAiDisabled()) {
         this.tickAir(i);
      }

   }

   protected void tickAir(int air) {
      if (this.isAlive() && !this.isWet()) {
         this.setAir(air - 1);
         if (this.getAir() == -20) {
            this.setAir(0);
            this.damage(this.getDamageSources().dryOut(), 2.0F);
         }
      } else {
         this.setAir(this.getMaxAir());
      }

   }

   public void hydrateFromPotion() {
      int i = this.getAir() + 1800;
      this.setAir(Math.min(i, this.getMaxAir()));
   }

   public int getMaxAir() {
      return 6000;
   }

   public Variant getVariant() {
      return AxolotlEntity.Variant.byId((Integer)this.dataTracker.get(VARIANT));
   }

   public void setVariant(Variant variant) {
      this.dataTracker.set(VARIANT, variant.getId());
   }

   private static boolean shouldBabyBeDifferent(Random random) {
      return random.nextInt(1200) == 0;
   }

   public boolean canSpawn(WorldView world) {
      return world.doesNotIntersectEntities(this);
   }

   public boolean canBreatheInWater() {
      return true;
   }

   public boolean isPushedByFluids() {
      return false;
   }

   public EntityGroup getGroup() {
      return EntityGroup.AQUATIC;
   }

   public void setPlayingDead(boolean playingDead) {
      this.dataTracker.set(PLAYING_DEAD, playingDead);
   }

   public boolean isPlayingDead() {
      return (Boolean)this.dataTracker.get(PLAYING_DEAD);
   }

   public boolean isFromBucket() {
      return (Boolean)this.dataTracker.get(FROM_BUCKET);
   }

   public void setFromBucket(boolean fromBucket) {
      this.dataTracker.set(FROM_BUCKET, fromBucket);
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      AxolotlEntity lv = (AxolotlEntity)EntityType.AXOLOTL.create(world);
      if (lv != null) {
         Variant lv2;
         if (shouldBabyBeDifferent(this.random)) {
            lv2 = AxolotlEntity.Variant.getRandomUnnatural(this.random);
         } else {
            lv2 = this.random.nextBoolean() ? this.getVariant() : ((AxolotlEntity)entity).getVariant();
         }

         lv.setVariant(lv2);
         lv.setPersistent();
      }

      return lv;
   }

   public double squaredAttackRange(LivingEntity target) {
      return 1.5 + (double)target.getWidth() * 2.0;
   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.isIn(ItemTags.AXOLOTL_TEMPT_ITEMS);
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return true;
   }

   protected void mobTick() {
      this.world.getProfiler().push("axolotlBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.world.getProfiler().push("axolotlActivityUpdate");
      AxolotlBrain.updateActivities(this);
      this.world.getProfiler().pop();
      if (!this.isAiDisabled()) {
         Optional optional = this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.PLAY_DEAD_TICKS);
         this.setPlayingDead(optional.isPresent() && (Integer)optional.get() > 0);
      }

   }

   public static DefaultAttributeContainer.Builder createAxolotlAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   protected EntityNavigation createNavigation(World world) {
      return new AmphibiousSwimNavigation(this, world);
   }

   public boolean tryAttack(Entity target) {
      boolean bl = target.damage(this.getDamageSources().mobAttack(this), (float)((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
      if (bl) {
         this.applyDamageEffects(this, target);
         this.playSound(SoundEvents.ENTITY_AXOLOTL_ATTACK, 1.0F, 1.0F);
      }

      return bl;
   }

   public boolean damage(DamageSource source, float amount) {
      float g = this.getHealth();
      if (!this.world.isClient && !this.isAiDisabled() && this.world.random.nextInt(3) == 0 && ((float)this.world.random.nextInt(3) < amount || g / this.getMaxHealth() < 0.5F) && amount < g && this.isTouchingWater() && (source.getAttacker() != null || source.getSource() != null) && !this.isPlayingDead()) {
         this.brain.remember(MemoryModuleType.PLAY_DEAD_TICKS, (int)200);
      }

      return super.damage(source, amount);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.655F;
   }

   public int getMaxLookPitchChange() {
      return 1;
   }

   public int getMaxHeadRotation() {
      return 1;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      return (ActionResult)Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
   }

   public void copyDataToStack(ItemStack stack) {
      Bucketable.copyDataToStack(this, stack);
      NbtCompound lv = stack.getOrCreateNbt();
      lv.putInt("Variant", this.getVariant().getId());
      lv.putInt("Age", this.getBreedingAge());
      Brain lv2 = this.getBrain();
      if (lv2.hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
         lv.putLong("HuntingCooldown", lv2.getMemoryExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
      }

   }

   public void copyDataFromNbt(NbtCompound nbt) {
      Bucketable.copyDataFromNbt(this, nbt);
      this.setVariant(AxolotlEntity.Variant.byId(nbt.getInt("Variant")));
      if (nbt.contains("Age")) {
         this.setBreedingAge(nbt.getInt("Age"));
      }

      if (nbt.contains("HuntingCooldown")) {
         this.getBrain().remember(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, nbt.getLong("HuntingCooldown"));
      }

   }

   public ItemStack getBucketItem() {
      return new ItemStack(Items.AXOLOTL_BUCKET);
   }

   public SoundEvent getBucketFillSound() {
      return SoundEvents.ITEM_BUCKET_FILL_AXOLOTL;
   }

   public boolean canTakeDamage() {
      return !this.isPlayingDead() && super.canTakeDamage();
   }

   public static void appreciatePlayer(AxolotlEntity axolotl, LivingEntity entity) {
      World lv = axolotl.world;
      if (entity.isDead()) {
         DamageSource lv2 = entity.getRecentDamageSource();
         if (lv2 != null) {
            Entity lv3 = lv2.getAttacker();
            if (lv3 != null && lv3.getType() == EntityType.PLAYER) {
               PlayerEntity lv4 = (PlayerEntity)lv3;
               List list = lv.getNonSpectatingEntities(PlayerEntity.class, axolotl.getBoundingBox().expand(20.0));
               if (list.contains(lv4)) {
                  axolotl.buffPlayer(lv4);
               }
            }
         }
      }

   }

   public void buffPlayer(PlayerEntity player) {
      StatusEffectInstance lv = player.getStatusEffect(StatusEffects.REGENERATION);
      if (lv == null || lv.isDurationBelow(2399)) {
         int i = lv != null ? lv.getDuration() : 0;
         int j = Math.min(2400, 100 + i);
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, j, 0), this);
      }

      player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
   }

   public boolean cannotDespawn() {
      return super.cannotDespawn() || this.isFromBucket();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_AXOLOTL_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_AXOLOTL_DEATH;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return this.isTouchingWater() ? SoundEvents.ENTITY_AXOLOTL_IDLE_WATER : SoundEvents.ENTITY_AXOLOTL_IDLE_AIR;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_AXOLOTL_SPLASH;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_AXOLOTL_SWIM;
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return AxolotlBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public void travel(Vec3d movementInput) {
      if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
         this.updateVelocity(this.getMovementSpeed(), movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.9));
      } else {
         super.travel(movementInput);
      }

   }

   protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
      if (stack.isOf(Items.TROPICAL_FISH_BUCKET)) {
         player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
      } else {
         super.eat(player, hand, stack);
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.isFromBucket() && !this.hasCustomName();
   }

   public static boolean canSpawn(EntityType type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.AXOLOTLS_SPAWNABLE_ON);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.AXOLOTL_TEMPTATIONS);
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT, new MemoryModuleType[]{MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryModuleType.IS_PANICKING});
      VARIANT = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.INTEGER);
      PLAYING_DEAD = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      FROM_BUCKET = DataTracker.registerData(AxolotlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static class AxolotlMoveControl extends AquaticMoveControl {
      private final AxolotlEntity axolotl;

      public AxolotlMoveControl(AxolotlEntity axolotl) {
         super(axolotl, 85, 10, 0.1F, 0.5F, false);
         this.axolotl = axolotl;
      }

      public void tick() {
         if (!this.axolotl.isPlayingDead()) {
            super.tick();
         }

      }
   }

   class AxolotlLookControl extends YawAdjustingLookControl {
      public AxolotlLookControl(AxolotlEntity axolotl, int yawAdjustThreshold) {
         super(axolotl, yawAdjustThreshold);
      }

      public void tick() {
         if (!AxolotlEntity.this.isPlayingDead()) {
            super.tick();
         }

      }
   }

   public static enum Variant implements StringIdentifiable {
      LUCY(0, "lucy", true),
      WILD(1, "wild", true),
      GOLD(2, "gold", true),
      CYAN(3, "cyan", true),
      BLUE(4, "blue", false);

      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Variant::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
      public static final Codec CODEC = StringIdentifiable.createCodec(Variant::values);
      private final int id;
      private final String name;
      private final boolean natural;

      private Variant(int id, String name, boolean natural) {
         this.id = id;
         this.name = name;
         this.natural = natural;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public String asString() {
         return this.name;
      }

      public static Variant byId(int id) {
         return (Variant)BY_ID.apply(id);
      }

      public static Variant getRandomNatural(Random random) {
         return getRandom(random, true);
      }

      public static Variant getRandomUnnatural(Random random) {
         return getRandom(random, false);
      }

      private static Variant getRandom(Random random, boolean natural) {
         Variant[] lvs = (Variant[])Arrays.stream(values()).filter((variant) -> {
            return variant.natural == natural;
         }).toArray((i) -> {
            return new Variant[i];
         });
         return (Variant)Util.getRandom((Object[])lvs, random);
      }

      // $FF: synthetic method
      private static Variant[] method_36644() {
         return new Variant[]{LUCY, WILD, GOLD, CYAN, BLUE};
      }
   }

   public static class AxolotlData extends PassiveEntity.PassiveData {
      public final Variant[] variants;

      public AxolotlData(Variant... variants) {
         super(false);
         this.variants = variants;
      }

      public Variant getRandomVariant(Random random) {
         return this.variants[random.nextInt(this.variants.length)];
      }
   }
}
