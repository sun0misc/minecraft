package net.minecraft.entity.passive;

import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TurtleEntity extends AnimalEntity {
   private static final TrackedData HOME_POS;
   private static final TrackedData HAS_EGG;
   private static final TrackedData DIGGING_SAND;
   private static final TrackedData TRAVEL_POS;
   private static final TrackedData LAND_BOUND;
   private static final TrackedData ACTIVELY_TRAVELING;
   public static final Ingredient BREEDING_ITEM;
   int sandDiggingCounter;
   public static final Predicate BABY_TURTLE_ON_LAND_FILTER;

   public TurtleEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DOOR_IRON_CLOSED, -1.0F);
      this.setPathfindingPenalty(PathNodeType.DOOR_WOOD_CLOSED, -1.0F);
      this.setPathfindingPenalty(PathNodeType.DOOR_OPEN, -1.0F);
      this.moveControl = new TurtleMoveControl(this);
      this.setStepHeight(1.0F);
   }

   public void setHomePos(BlockPos pos) {
      this.dataTracker.set(HOME_POS, pos);
   }

   BlockPos getHomePos() {
      return (BlockPos)this.dataTracker.get(HOME_POS);
   }

   void setTravelPos(BlockPos pos) {
      this.dataTracker.set(TRAVEL_POS, pos);
   }

   BlockPos getTravelPos() {
      return (BlockPos)this.dataTracker.get(TRAVEL_POS);
   }

   public boolean hasEgg() {
      return (Boolean)this.dataTracker.get(HAS_EGG);
   }

   void setHasEgg(boolean hasEgg) {
      this.dataTracker.set(HAS_EGG, hasEgg);
   }

   public boolean isDiggingSand() {
      return (Boolean)this.dataTracker.get(DIGGING_SAND);
   }

   void setDiggingSand(boolean diggingSand) {
      this.sandDiggingCounter = diggingSand ? 1 : 0;
      this.dataTracker.set(DIGGING_SAND, diggingSand);
   }

   boolean isLandBound() {
      return (Boolean)this.dataTracker.get(LAND_BOUND);
   }

   void setLandBound(boolean landBound) {
      this.dataTracker.set(LAND_BOUND, landBound);
   }

   boolean isActivelyTraveling() {
      return (Boolean)this.dataTracker.get(ACTIVELY_TRAVELING);
   }

   void setActivelyTraveling(boolean traveling) {
      this.dataTracker.set(ACTIVELY_TRAVELING, traveling);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(HOME_POS, BlockPos.ORIGIN);
      this.dataTracker.startTracking(HAS_EGG, false);
      this.dataTracker.startTracking(TRAVEL_POS, BlockPos.ORIGIN);
      this.dataTracker.startTracking(LAND_BOUND, false);
      this.dataTracker.startTracking(ACTIVELY_TRAVELING, false);
      this.dataTracker.startTracking(DIGGING_SAND, false);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("HomePosX", this.getHomePos().getX());
      nbt.putInt("HomePosY", this.getHomePos().getY());
      nbt.putInt("HomePosZ", this.getHomePos().getZ());
      nbt.putBoolean("HasEgg", this.hasEgg());
      nbt.putInt("TravelPosX", this.getTravelPos().getX());
      nbt.putInt("TravelPosY", this.getTravelPos().getY());
      nbt.putInt("TravelPosZ", this.getTravelPos().getZ());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      int i = nbt.getInt("HomePosX");
      int j = nbt.getInt("HomePosY");
      int k = nbt.getInt("HomePosZ");
      this.setHomePos(new BlockPos(i, j, k));
      super.readCustomDataFromNbt(nbt);
      this.setHasEgg(nbt.getBoolean("HasEgg"));
      int l = nbt.getInt("TravelPosX");
      int m = nbt.getInt("TravelPosY");
      int n = nbt.getInt("TravelPosZ");
      this.setTravelPos(new BlockPos(l, m, n));
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setHomePos(this.getBlockPos());
      this.setTravelPos(BlockPos.ORIGIN);
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return pos.getY() < world.getSeaLevel() + 4 && TurtleEggBlock.isSandBelow(world, pos) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new TurtleEscapeDangerGoal(this, 1.2));
      this.goalSelector.add(1, new MateGoal(this, 1.0));
      this.goalSelector.add(1, new LayEggGoal(this, 1.0));
      this.goalSelector.add(2, new TemptGoal(this, 1.1, BREEDING_ITEM, false));
      this.goalSelector.add(3, new WanderInWaterGoal(this, 1.0));
      this.goalSelector.add(4, new GoHomeGoal(this, 1.0));
      this.goalSelector.add(7, new TravelGoal(this, 1.0));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(9, new WanderOnLandGoal(this, 1.0, 100));
   }

   public static DefaultAttributeContainer.Builder createTurtleAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
   }

   public boolean isPushedByFluids() {
      return false;
   }

   public boolean canBreatheInWater() {
      return true;
   }

   public EntityGroup getGroup() {
      return EntityGroup.AQUATIC;
   }

   public int getMinAmbientSoundDelay() {
      return 200;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return !this.isTouchingWater() && this.onGround && !this.isBaby() ? SoundEvents.ENTITY_TURTLE_AMBIENT_LAND : super.getAmbientSound();
   }

   protected void playSwimSound(float volume) {
      super.playSwimSound(volume * 1.5F);
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_TURTLE_SWIM;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isBaby() ? SoundEvents.ENTITY_TURTLE_HURT_BABY : SoundEvents.ENTITY_TURTLE_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return this.isBaby() ? SoundEvents.ENTITY_TURTLE_DEATH_BABY : SoundEvents.ENTITY_TURTLE_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      SoundEvent lv = this.isBaby() ? SoundEvents.ENTITY_TURTLE_SHAMBLE_BABY : SoundEvents.ENTITY_TURTLE_SHAMBLE;
      this.playSound(lv, 0.15F, 1.0F);
   }

   public boolean canEat() {
      return super.canEat() && !this.hasEgg();
   }

   protected float calculateNextStepSoundDistance() {
      return this.distanceTraveled + 0.15F;
   }

   public float getScaleFactor() {
      return this.isBaby() ? 0.3F : 1.0F;
   }

   protected EntityNavigation createNavigation(World world) {
      return new TurtleSwimNavigation(this, world);
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return (PassiveEntity)EntityType.TURTLE.create(world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.isOf(Blocks.SEAGRASS.asItem());
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      if (!this.isLandBound() && world.getFluidState(pos).isIn(FluidTags.WATER)) {
         return 10.0F;
      } else {
         return TurtleEggBlock.isSandBelow(world, pos) ? 10.0F : world.getPhototaxisFavor(pos);
      }
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isAlive() && this.isDiggingSand() && this.sandDiggingCounter >= 1 && this.sandDiggingCounter % 5 == 0) {
         BlockPos lv = this.getBlockPos();
         if (TurtleEggBlock.isSandBelow(this.world, lv)) {
            this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv, Block.getRawIdFromState(this.world.getBlockState(lv.down())));
         }
      }

   }

   protected void onGrowUp() {
      super.onGrowUp();
      if (!this.isBaby() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
         this.dropItem(Items.SCUTE, 1);
      }

   }

   public void travel(Vec3d movementInput) {
      if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
         this.updateVelocity(0.1F, movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.9));
         if (this.getTarget() == null && (!this.isLandBound() || !this.getHomePos().isWithinDistance(this.getPos(), 20.0))) {
            this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
         }
      } else {
         super.travel(movementInput);
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      this.damage(this.getDamageSources().lightningBolt(), Float.MAX_VALUE);
   }

   static {
      HOME_POS = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
      HAS_EGG = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      DIGGING_SAND = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      TRAVEL_POS = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
      LAND_BOUND = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      ACTIVELY_TRAVELING = DataTracker.registerData(TurtleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      BREEDING_ITEM = Ingredient.ofItems(Blocks.SEAGRASS.asItem());
      BABY_TURTLE_ON_LAND_FILTER = (entity) -> {
         return entity.isBaby() && !entity.isTouchingWater();
      };
   }

   private static class TurtleMoveControl extends MoveControl {
      private final TurtleEntity turtle;

      TurtleMoveControl(TurtleEntity turtle) {
         super(turtle);
         this.turtle = turtle;
      }

      private void updateVelocity() {
         if (this.turtle.isTouchingWater()) {
            this.turtle.setVelocity(this.turtle.getVelocity().add(0.0, 0.005, 0.0));
            if (!this.turtle.getHomePos().isWithinDistance(this.turtle.getPos(), 16.0)) {
               this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 2.0F, 0.08F));
            }

            if (this.turtle.isBaby()) {
               this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 3.0F, 0.06F));
            }
         } else if (this.turtle.onGround) {
            this.turtle.setMovementSpeed(Math.max(this.turtle.getMovementSpeed() / 2.0F, 0.06F));
         }

      }

      public void tick() {
         this.updateVelocity();
         if (this.state == MoveControl.State.MOVE_TO && !this.turtle.getNavigation().isIdle()) {
            double d = this.targetX - this.turtle.getX();
            double e = this.targetY - this.turtle.getY();
            double f = this.targetZ - this.turtle.getZ();
            double g = Math.sqrt(d * d + e * e + f * f);
            if (g < 9.999999747378752E-6) {
               this.entity.setMovementSpeed(0.0F);
            } else {
               e /= g;
               float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F;
               this.turtle.setYaw(this.wrapDegrees(this.turtle.getYaw(), h, 90.0F));
               this.turtle.bodyYaw = this.turtle.getYaw();
               float i = (float)(this.speed * this.turtle.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
               this.turtle.setMovementSpeed(MathHelper.lerp(0.125F, this.turtle.getMovementSpeed(), i));
               this.turtle.setVelocity(this.turtle.getVelocity().add(0.0, (double)this.turtle.getMovementSpeed() * e * 0.1, 0.0));
            }
         } else {
            this.turtle.setMovementSpeed(0.0F);
         }
      }
   }

   private static class TurtleEscapeDangerGoal extends EscapeDangerGoal {
      TurtleEscapeDangerGoal(TurtleEntity turtle, double speed) {
         super(turtle, speed);
      }

      public boolean canStart() {
         if (!this.isInDanger()) {
            return false;
         } else {
            BlockPos lv = this.locateClosestWater(this.mob.world, this.mob, 7);
            if (lv != null) {
               this.targetX = (double)lv.getX();
               this.targetY = (double)lv.getY();
               this.targetZ = (double)lv.getZ();
               return true;
            } else {
               return this.findTarget();
            }
         }
      }
   }

   private static class MateGoal extends AnimalMateGoal {
      private final TurtleEntity turtle;

      MateGoal(TurtleEntity turtle, double speed) {
         super(turtle, speed);
         this.turtle = turtle;
      }

      public boolean canStart() {
         return super.canStart() && !this.turtle.hasEgg();
      }

      protected void breed() {
         ServerPlayerEntity lv = this.animal.getLovingPlayer();
         if (lv == null && this.mate.getLovingPlayer() != null) {
            lv = this.mate.getLovingPlayer();
         }

         if (lv != null) {
            lv.incrementStat(Stats.ANIMALS_BRED);
            Criteria.BRED_ANIMALS.trigger(lv, this.animal, this.mate, (PassiveEntity)null);
         }

         this.turtle.setHasEgg(true);
         this.animal.setBreedingAge(6000);
         this.mate.setBreedingAge(6000);
         this.animal.resetLoveTicks();
         this.mate.resetLoveTicks();
         Random lv2 = this.animal.getRandom();
         if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), lv2.nextInt(7) + 1));
         }

      }
   }

   private static class LayEggGoal extends MoveToTargetPosGoal {
      private final TurtleEntity turtle;

      LayEggGoal(TurtleEntity turtle, double speed) {
         super(turtle, speed, 16);
         this.turtle = turtle;
      }

      public boolean canStart() {
         return this.turtle.hasEgg() && this.turtle.getHomePos().isWithinDistance(this.turtle.getPos(), 9.0) ? super.canStart() : false;
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && this.turtle.hasEgg() && this.turtle.getHomePos().isWithinDistance(this.turtle.getPos(), 9.0);
      }

      public void tick() {
         super.tick();
         BlockPos lv = this.turtle.getBlockPos();
         if (!this.turtle.isTouchingWater() && this.hasReached()) {
            if (this.turtle.sandDiggingCounter < 1) {
               this.turtle.setDiggingSand(true);
            } else if (this.turtle.sandDiggingCounter > this.getTickCount(200)) {
               World lv2 = this.turtle.world;
               lv2.playSound((PlayerEntity)null, lv, SoundEvents.ENTITY_TURTLE_LAY_EGG, SoundCategory.BLOCKS, 0.3F, 0.9F + lv2.random.nextFloat() * 0.2F);
               BlockPos lv3 = this.targetPos.up();
               BlockState lv4 = (BlockState)Blocks.TURTLE_EGG.getDefaultState().with(TurtleEggBlock.EGGS, this.turtle.random.nextInt(4) + 1);
               lv2.setBlockState(lv3, lv4, Block.NOTIFY_ALL);
               lv2.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(this.turtle, lv4));
               this.turtle.setHasEgg(false);
               this.turtle.setDiggingSand(false);
               this.turtle.setLoveTicks(600);
            }

            if (this.turtle.isDiggingSand()) {
               ++this.turtle.sandDiggingCounter;
            }
         }

      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         return !world.isAir(pos.up()) ? false : TurtleEggBlock.isSand(world, pos);
      }
   }

   static class WanderInWaterGoal extends MoveToTargetPosGoal {
      private static final int field_30385 = 1200;
      private final TurtleEntity turtle;

      WanderInWaterGoal(TurtleEntity turtle, double speed) {
         super(turtle, turtle.isBaby() ? 2.0 : speed, 24);
         this.turtle = turtle;
         this.lowestY = -1;
      }

      public boolean shouldContinue() {
         return !this.turtle.isTouchingWater() && this.tryingTime <= 1200 && this.isTargetPos(this.turtle.world, this.targetPos);
      }

      public boolean canStart() {
         if (this.turtle.isBaby() && !this.turtle.isTouchingWater()) {
            return super.canStart();
         } else {
            return !this.turtle.isLandBound() && !this.turtle.isTouchingWater() && !this.turtle.hasEgg() ? super.canStart() : false;
         }
      }

      public boolean shouldResetPath() {
         return this.tryingTime % 160 == 0;
      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         return world.getBlockState(pos).isOf(Blocks.WATER);
      }
   }

   private static class GoHomeGoal extends Goal {
      private final TurtleEntity turtle;
      private final double speed;
      private boolean noPath;
      private int homeReachingTryTicks;
      private static final int MAX_TRY_TICKS = 600;

      GoHomeGoal(TurtleEntity turtle, double speed) {
         this.turtle = turtle;
         this.speed = speed;
      }

      public boolean canStart() {
         if (this.turtle.isBaby()) {
            return false;
         } else if (this.turtle.hasEgg()) {
            return true;
         } else if (this.turtle.getRandom().nextInt(toGoalTicks(700)) != 0) {
            return false;
         } else {
            return !this.turtle.getHomePos().isWithinDistance(this.turtle.getPos(), 64.0);
         }
      }

      public void start() {
         this.turtle.setLandBound(true);
         this.noPath = false;
         this.homeReachingTryTicks = 0;
      }

      public void stop() {
         this.turtle.setLandBound(false);
      }

      public boolean shouldContinue() {
         return !this.turtle.getHomePos().isWithinDistance(this.turtle.getPos(), 7.0) && !this.noPath && this.homeReachingTryTicks <= this.getTickCount(600);
      }

      public void tick() {
         BlockPos lv = this.turtle.getHomePos();
         boolean bl = lv.isWithinDistance(this.turtle.getPos(), 16.0);
         if (bl) {
            ++this.homeReachingTryTicks;
         }

         if (this.turtle.getNavigation().isIdle()) {
            Vec3d lv2 = Vec3d.ofBottomCenter(lv);
            Vec3d lv3 = NoPenaltyTargeting.findTo(this.turtle, 16, 3, lv2, 0.3141592741012573);
            if (lv3 == null) {
               lv3 = NoPenaltyTargeting.findTo(this.turtle, 8, 7, lv2, 1.5707963705062866);
            }

            if (lv3 != null && !bl && !this.turtle.world.getBlockState(BlockPos.ofFloored(lv3)).isOf(Blocks.WATER)) {
               lv3 = NoPenaltyTargeting.findTo(this.turtle, 16, 5, lv2, 1.5707963705062866);
            }

            if (lv3 == null) {
               this.noPath = true;
               return;
            }

            this.turtle.getNavigation().startMovingTo(lv3.x, lv3.y, lv3.z, this.speed);
         }

      }
   }

   private static class TravelGoal extends Goal {
      private final TurtleEntity turtle;
      private final double speed;
      private boolean noPath;

      TravelGoal(TurtleEntity turtle, double speed) {
         this.turtle = turtle;
         this.speed = speed;
      }

      public boolean canStart() {
         return !this.turtle.isLandBound() && !this.turtle.hasEgg() && this.turtle.isTouchingWater();
      }

      public void start() {
         int i = true;
         int j = true;
         Random lv = this.turtle.random;
         int k = lv.nextInt(1025) - 512;
         int l = lv.nextInt(9) - 4;
         int m = lv.nextInt(1025) - 512;
         if ((double)l + this.turtle.getY() > (double)(this.turtle.world.getSeaLevel() - 1)) {
            l = 0;
         }

         BlockPos lv2 = BlockPos.ofFloored((double)k + this.turtle.getX(), (double)l + this.turtle.getY(), (double)m + this.turtle.getZ());
         this.turtle.setTravelPos(lv2);
         this.turtle.setActivelyTraveling(true);
         this.noPath = false;
      }

      public void tick() {
         if (this.turtle.getNavigation().isIdle()) {
            Vec3d lv = Vec3d.ofBottomCenter(this.turtle.getTravelPos());
            Vec3d lv2 = NoPenaltyTargeting.findTo(this.turtle, 16, 3, lv, 0.3141592741012573);
            if (lv2 == null) {
               lv2 = NoPenaltyTargeting.findTo(this.turtle, 8, 7, lv, 1.5707963705062866);
            }

            if (lv2 != null) {
               int i = MathHelper.floor(lv2.x);
               int j = MathHelper.floor(lv2.z);
               int k = true;
               if (!this.turtle.world.isRegionLoaded(i - 34, j - 34, i + 34, j + 34)) {
                  lv2 = null;
               }
            }

            if (lv2 == null) {
               this.noPath = true;
               return;
            }

            this.turtle.getNavigation().startMovingTo(lv2.x, lv2.y, lv2.z, this.speed);
         }

      }

      public boolean shouldContinue() {
         return !this.turtle.getNavigation().isIdle() && !this.noPath && !this.turtle.isLandBound() && !this.turtle.isInLove() && !this.turtle.hasEgg();
      }

      public void stop() {
         this.turtle.setActivelyTraveling(false);
         super.stop();
      }
   }

   private static class WanderOnLandGoal extends WanderAroundGoal {
      private final TurtleEntity turtle;

      WanderOnLandGoal(TurtleEntity turtle, double speed, int chance) {
         super(turtle, speed, chance);
         this.turtle = turtle;
      }

      public boolean canStart() {
         return !this.mob.isTouchingWater() && !this.turtle.isLandBound() && !this.turtle.hasEgg() ? super.canStart() : false;
      }
   }

   static class TurtleSwimNavigation extends AmphibiousSwimNavigation {
      TurtleSwimNavigation(TurtleEntity owner, World world) {
         super(owner, world);
      }

      public boolean isValidPosition(BlockPos pos) {
         MobEntity var3 = this.entity;
         if (var3 instanceof TurtleEntity lv) {
            if (lv.isActivelyTraveling()) {
               return this.world.getBlockState(pos).isOf(Blocks.WATER);
            }
         }

         return !this.world.getBlockState(pos.down()).isAir();
      }
   }
}
