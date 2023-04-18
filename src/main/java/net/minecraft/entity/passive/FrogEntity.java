package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
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
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.pathing.AmphibiousPathNodeMaker;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class FrogEntity extends AnimalEntity implements VariantHolder {
   public static final Ingredient SLIME_BALL;
   protected static final ImmutableList SENSORS;
   protected static final ImmutableList MEMORY_MODULES;
   private static final TrackedData VARIANT;
   private static final TrackedData TARGET;
   private static final int field_37459 = 5;
   public static final String VARIANT_KEY = "variant";
   public final AnimationState longJumpingAnimationState = new AnimationState();
   public final AnimationState croakingAnimationState = new AnimationState();
   public final AnimationState usingTongueAnimationState = new AnimationState();
   public final AnimationState idlingInWaterAnimationState = new AnimationState();

   public FrogEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.lookControl = new FrogLookControl(this);
      this.setPathfindingPenalty(PathNodeType.WATER, 4.0F);
      this.setPathfindingPenalty(PathNodeType.TRAPDOOR, -1.0F);
      this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.setStepHeight(1.0F);
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return FrogBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, FrogVariant.TEMPERATE);
      this.dataTracker.startTracking(TARGET, OptionalInt.empty());
   }

   public void clearFrogTarget() {
      this.dataTracker.set(TARGET, OptionalInt.empty());
   }

   public Optional getFrogTarget() {
      IntStream var10000 = ((OptionalInt)this.dataTracker.get(TARGET)).stream();
      World var10001 = this.world;
      Objects.requireNonNull(var10001);
      return var10000.mapToObj(var10001::getEntityById).filter(Objects::nonNull).findFirst();
   }

   public void setFrogTarget(Entity entity) {
      this.dataTracker.set(TARGET, OptionalInt.of(entity.getId()));
   }

   public int getMaxLookYawChange() {
      return 35;
   }

   public int getMaxHeadRotation() {
      return 5;
   }

   public FrogVariant getVariant() {
      return (FrogVariant)this.dataTracker.get(VARIANT);
   }

   public void setVariant(FrogVariant variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putString("variant", Registries.FROG_VARIANT.getId(this.getVariant()).toString());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      FrogVariant lv = (FrogVariant)Registries.FROG_VARIANT.get(Identifier.tryParse(nbt.getString("variant")));
      if (lv != null) {
         this.setVariant(lv);
      }

   }

   public boolean canBreatheInWater() {
      return true;
   }

   protected void mobTick() {
      this.world.getProfiler().push("frogBrain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().pop();
      this.world.getProfiler().push("frogActivityUpdate");
      FrogBrain.updateActivities(this);
      this.world.getProfiler().pop();
      super.mobTick();
   }

   public void tick() {
      if (this.world.isClient()) {
         this.idlingInWaterAnimationState.setRunning(this.isInsideWaterOrBubbleColumn() && !this.limbAnimator.isLimbMoving(), this.age);
      }

      super.tick();
   }

   public void onTrackedDataSet(TrackedData data) {
      if (POSE.equals(data)) {
         EntityPose lv = this.getPose();
         if (lv == EntityPose.LONG_JUMPING) {
            this.longJumpingAnimationState.start(this.age);
         } else {
            this.longJumpingAnimationState.stop();
         }

         if (lv == EntityPose.CROAKING) {
            this.croakingAnimationState.start(this.age);
         } else {
            this.croakingAnimationState.stop();
         }

         if (lv == EntityPose.USING_TONGUE) {
            this.usingTongueAnimationState.start(this.age);
         } else {
            this.usingTongueAnimationState.stop();
         }
      }

      super.onTrackedDataSet(data);
   }

   protected void updateLimbs(float posDelta) {
      float g;
      if (this.longJumpingAnimationState.isRunning()) {
         g = 0.0F;
      } else {
         g = Math.min(posDelta * 25.0F, 1.0F);
      }

      this.limbAnimator.updateLimbs(g, 0.4F);
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      FrogEntity lv = (FrogEntity)EntityType.FROG.create(world);
      if (lv != null) {
         FrogBrain.coolDownLongJump(lv, world.getRandom());
      }

      return lv;
   }

   public boolean isBaby() {
      return false;
   }

   public void setBaby(boolean baby) {
   }

   public void breed(ServerWorld world, AnimalEntity other) {
      this.breed(world, other, (PassiveEntity)null);
      this.getBrain().remember(MemoryModuleType.IS_PREGNANT, (Object)Unit.INSTANCE);
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      RegistryEntry lv = world.getBiome(this.getBlockPos());
      if (lv.isIn(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
         this.setVariant(FrogVariant.COLD);
      } else if (lv.isIn(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
         this.setVariant(FrogVariant.WARM);
      } else {
         this.setVariant(FrogVariant.TEMPERATE);
      }

      FrogBrain.coolDownLongJump(this, world.getRandom());
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public static DefaultAttributeContainer.Builder createFrogAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_FROG_AMBIENT;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_FROG_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_FROG_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_FROG_STEP, 0.15F, 1.0F);
   }

   public boolean isPushedByFluids() {
      return false;
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
      return super.computeFallDamage(fallDistance, damageMultiplier) - 5;
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

   public static boolean isValidFrogFood(LivingEntity entity) {
      if (entity instanceof SlimeEntity lv) {
         if (lv.getSize() != 1) {
            return false;
         }
      }

      return entity.getType().isIn(EntityTypeTags.FROG_FOOD);
   }

   protected EntityNavigation createNavigation(World world) {
      return new FrogSwimNavigation(this, world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return SLIME_BALL.test(stack);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.FROGS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      SLIME_BALL = Ingredient.ofItems(Items.SLIME_BALL);
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER);
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.IS_IN_WATER, MemoryModuleType.IS_PREGNANT, MemoryModuleType.IS_PANICKING, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS});
      VARIANT = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.FROG_VARIANT);
      TARGET = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
   }

   class FrogLookControl extends LookControl {
      FrogLookControl(MobEntity entity) {
         super(entity);
      }

      protected boolean shouldStayHorizontal() {
         return FrogEntity.this.getFrogTarget().isEmpty();
      }
   }

   static class FrogSwimNavigation extends AmphibiousSwimNavigation {
      FrogSwimNavigation(FrogEntity frog, World world) {
         super(frog, world);
      }

      public boolean canJumpToNext(PathNodeType nodeType) {
         return nodeType != PathNodeType.WATER_BORDER && super.canJumpToNext(nodeType);
      }

      protected PathNodeNavigator createPathNodeNavigator(int range) {
         this.nodeMaker = new FrogSwimPathNodeMaker(true);
         this.nodeMaker.setCanEnterOpenDoors(true);
         return new PathNodeNavigator(this.nodeMaker, range);
      }
   }

   private static class FrogSwimPathNodeMaker extends AmphibiousPathNodeMaker {
      private final BlockPos.Mutable pos = new BlockPos.Mutable();

      public FrogSwimPathNodeMaker(boolean bl) {
         super(bl);
      }

      public PathNode getStart() {
         return !this.entity.isTouchingWater() ? super.getStart() : this.getStart(new BlockPos(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY), MathHelper.floor(this.entity.getBoundingBox().minZ)));
      }

      public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
         this.pos.set(x, y - 1, z);
         BlockState lv = world.getBlockState(this.pos);
         return lv.isIn(BlockTags.FROG_PREFER_JUMP_TO) ? PathNodeType.OPEN : super.getDefaultNodeType(world, x, y, z);
      }
   }
}
