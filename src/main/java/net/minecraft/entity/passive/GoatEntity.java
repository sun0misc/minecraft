package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.InstrumentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class GoatEntity extends AnimalEntity {
   public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.changing(0.9F, 1.3F).scaled(0.7F);
   private static final int DEFAULT_ATTACK_DAMAGE = 2;
   private static final int BABY_ATTACK_DAMAGE = 1;
   protected static final ImmutableList SENSORS;
   protected static final ImmutableList MEMORY_MODULES;
   public static final int FALL_DAMAGE_SUBTRACTOR = 10;
   public static final double SCREAMING_CHANCE = 0.02;
   public static final double field_39046 = 0.10000000149011612;
   private static final TrackedData SCREAMING;
   private static final TrackedData LEFT_HORN;
   private static final TrackedData RIGHT_HORN;
   private boolean preparingRam;
   private int headPitch;

   public GoatEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.getNavigation().setCanSwim(true);
      this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0F);
      this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0F);
   }

   public ItemStack getGoatHornStack() {
      Random lv = Random.create((long)this.getUuid().hashCode());
      TagKey lv2 = this.isScreaming() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
      RegistryEntryList lv3 = Registries.INSTRUMENT.getOrCreateEntryList(lv2);
      return GoatHornItem.getStackForInstrument(Items.GOAT_HORN, (RegistryEntry)lv3.getRandom(lv).get());
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return GoatBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public static DefaultAttributeContainer.Builder createGoatAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   protected void onGrowUp() {
      if (this.isBaby()) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
         this.removeHorns();
      } else {
         this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(2.0);
         this.addHorns();
      }

   }

   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
      return super.computeFallDamage(fallDistance, damageMultiplier) - 10;
   }

   protected SoundEvent getAmbientSound() {
      return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT : SoundEvents.ENTITY_GOAT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_HURT : SoundEvents.ENTITY_GOAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_DEATH : SoundEvents.ENTITY_GOAT_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_GOAT_STEP, 0.15F, 1.0F);
   }

   protected SoundEvent getMilkingSound() {
      return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_MILK : SoundEvents.ENTITY_GOAT_MILK;
   }

   @Nullable
   public GoatEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      GoatEntity lv = (GoatEntity)EntityType.GOAT.create(arg);
      if (lv != null) {
         boolean var10000;
         label22: {
            label21: {
               GoatBrain.resetLongJumpCooldown(lv, arg.getRandom());
               PassiveEntity lv2 = arg.getRandom().nextBoolean() ? this : arg2;
               if (lv2 instanceof GoatEntity) {
                  GoatEntity lv3 = (GoatEntity)lv2;
                  if (lv3.isScreaming()) {
                     break label21;
                  }
               }

               if (!(arg.getRandom().nextDouble() < 0.02)) {
                  var10000 = false;
                  break label22;
               }
            }

            var10000 = true;
         }

         boolean bl = var10000;
         lv.setScreaming(bl);
      }

      return lv;
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected void mobTick() {
      this.world.getProfiler().push("goatBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.world.getProfiler().push("goatActivityUpdate");
      GoatBrain.updateActivities(this);
      this.world.getProfiler().pop();
      super.mobTick();
   }

   public int getMaxHeadRotation() {
      return 15;
   }

   public void setHeadYaw(float headYaw) {
      int i = this.getMaxHeadRotation();
      float g = MathHelper.subtractAngles(this.bodyYaw, headYaw);
      float h = MathHelper.clamp(g, (float)(-i), (float)i);
      super.setHeadYaw(this.bodyYaw + h);
   }

   public SoundEvent getEatSound(ItemStack stack) {
      return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_EAT : SoundEvents.ENTITY_GOAT_EAT;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.BUCKET) && !this.isBaby()) {
         player.playSound(this.getMilkingSound(), 1.0F, 1.0F);
         ItemStack lv2 = ItemUsage.exchangeStack(lv, player, Items.MILK_BUCKET.getDefaultStack());
         player.setStackInHand(hand, lv2);
         return ActionResult.success(this.world.isClient);
      } else {
         ActionResult lv3 = super.interactMob(player, hand);
         if (lv3.isAccepted() && this.isBreedingItem(lv)) {
            this.world.playSoundFromEntity((PlayerEntity)null, this, this.getEatSound(lv), SoundCategory.NEUTRAL, 1.0F, MathHelper.nextBetween(this.world.random, 0.8F, 1.2F));
         }

         return lv3;
      }
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      GoatBrain.resetLongJumpCooldown(this, lv);
      this.setScreaming(lv.nextDouble() < 0.02);
      this.onGrowUp();
      if (!this.isBaby() && (double)lv.nextFloat() < 0.10000000149011612) {
         TrackedData lv2 = lv.nextBoolean() ? LEFT_HORN : RIGHT_HORN;
         this.dataTracker.set(lv2, false);
      }

      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return pose == EntityPose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scaled(this.getScaleFactor()) : super.getDimensions(pose);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsScreamingGoat", this.isScreaming());
      nbt.putBoolean("HasLeftHorn", this.hasLeftHorn());
      nbt.putBoolean("HasRightHorn", this.hasRightHorn());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setScreaming(nbt.getBoolean("IsScreamingGoat"));
      this.dataTracker.set(LEFT_HORN, nbt.getBoolean("HasLeftHorn"));
      this.dataTracker.set(RIGHT_HORN, nbt.getBoolean("HasRightHorn"));
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PREPARE_RAM) {
         this.preparingRam = true;
      } else if (status == EntityStatuses.FINISH_RAM) {
         this.preparingRam = false;
      } else {
         super.handleStatus(status);
      }

   }

   public void tickMovement() {
      if (this.preparingRam) {
         ++this.headPitch;
      } else {
         this.headPitch -= 2;
      }

      this.headPitch = MathHelper.clamp(this.headPitch, 0, 20);
      super.tickMovement();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SCREAMING, false);
      this.dataTracker.startTracking(LEFT_HORN, true);
      this.dataTracker.startTracking(RIGHT_HORN, true);
   }

   public boolean hasLeftHorn() {
      return (Boolean)this.dataTracker.get(LEFT_HORN);
   }

   public boolean hasRightHorn() {
      return (Boolean)this.dataTracker.get(RIGHT_HORN);
   }

   public boolean dropHorn() {
      boolean bl = this.hasLeftHorn();
      boolean bl2 = this.hasRightHorn();
      if (!bl && !bl2) {
         return false;
      } else {
         TrackedData lv;
         if (!bl) {
            lv = RIGHT_HORN;
         } else if (!bl2) {
            lv = LEFT_HORN;
         } else {
            lv = this.random.nextBoolean() ? LEFT_HORN : RIGHT_HORN;
         }

         this.dataTracker.set(lv, false);
         Vec3d lv2 = this.getPos();
         ItemStack lv3 = this.getGoatHornStack();
         double d = (double)MathHelper.nextBetween(this.random, -0.2F, 0.2F);
         double e = (double)MathHelper.nextBetween(this.random, 0.3F, 0.7F);
         double f = (double)MathHelper.nextBetween(this.random, -0.2F, 0.2F);
         ItemEntity lv4 = new ItemEntity(this.world, lv2.getX(), lv2.getY(), lv2.getZ(), lv3, d, e, f);
         this.world.spawnEntity(lv4);
         return true;
      }
   }

   public void addHorns() {
      this.dataTracker.set(LEFT_HORN, true);
      this.dataTracker.set(RIGHT_HORN, true);
   }

   public void removeHorns() {
      this.dataTracker.set(LEFT_HORN, false);
      this.dataTracker.set(RIGHT_HORN, false);
   }

   public boolean isScreaming() {
      return (Boolean)this.dataTracker.get(SCREAMING);
   }

   public void setScreaming(boolean screaming) {
      this.dataTracker.set(SCREAMING, screaming);
   }

   public float getHeadPitch() {
      return (float)this.headPitch / 20.0F * 30.0F * 0.017453292F;
   }

   public static boolean canSpawn(EntityType entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.GOATS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING});
      SCREAMING = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      LEFT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      RIGHT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
