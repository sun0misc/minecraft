package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class StriderEntity extends AnimalEntity implements ItemSteerable, Saddleable {
   private static final UUID SUFFOCATING_MODIFIER_ID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
   private static final EntityAttributeModifier SUFFOCATING_MODIFIER;
   private static final float COLD_SADDLED_SPEED = 0.35F;
   private static final float DEFAULT_SADDLED_SPEED = 0.55F;
   private static final Ingredient BREEDING_INGREDIENT;
   private static final Ingredient ATTRACTING_INGREDIENT;
   private static final TrackedData BOOST_TIME;
   private static final TrackedData COLD;
   private static final TrackedData SADDLED;
   private final SaddledComponent saddledComponent;
   @Nullable
   private TemptGoal temptGoal;
   @Nullable
   private EscapeDangerGoal escapeDangerGoal;

   public StriderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
      this.intersectionChecked = true;
      this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
      this.setPathfindingPenalty(PathNodeType.LAVA, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 0.0F);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      BlockPos.Mutable lv = pos.mutableCopy();

      do {
         lv.move(Direction.UP);
      } while(world.getFluidState(lv).isIn(FluidTags.LAVA));

      return world.getBlockState(lv).isAir();
   }

   public void onTrackedDataSet(TrackedData data) {
      if (BOOST_TIME.equals(data) && this.world.isClient) {
         this.saddledComponent.boost();
      }

      super.onTrackedDataSet(data);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(BOOST_TIME, 0);
      this.dataTracker.startTracking(COLD, false);
      this.dataTracker.startTracking(SADDLED, false);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.saddledComponent.writeNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.saddledComponent.readNbt(nbt);
   }

   public boolean isSaddled() {
      return this.saddledComponent.isSaddled();
   }

   public boolean canBeSaddled() {
      return this.isAlive() && !this.isBaby();
   }

   public void saddle(@Nullable SoundCategory sound) {
      this.saddledComponent.setSaddled(true);
      if (sound != null) {
         this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5F, 1.0F);
      }

   }

   protected void initGoals() {
      this.escapeDangerGoal = new EscapeDangerGoal(this, 1.65);
      this.goalSelector.add(1, this.escapeDangerGoal);
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
      this.temptGoal = new TemptGoal(this, 1.4, ATTRACTING_INGREDIENT, false);
      this.goalSelector.add(3, this.temptGoal);
      this.goalSelector.add(4, new GoBackToLavaGoal(this, 1.0));
      this.goalSelector.add(5, new FollowParentGoal(this, 1.0));
      this.goalSelector.add(7, new WanderAroundGoal(this, 1.0, 60));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.goalSelector.add(9, new LookAtEntityGoal(this, StriderEntity.class, 8.0F));
   }

   public void setCold(boolean cold) {
      this.dataTracker.set(COLD, cold);
      EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (lv != null) {
         lv.removeModifier(SUFFOCATING_MODIFIER_ID);
         if (cold) {
            lv.addTemporaryModifier(SUFFOCATING_MODIFIER);
         }
      }

   }

   public boolean isCold() {
      return (Boolean)this.dataTracker.get(COLD);
   }

   public boolean canWalkOnFluid(FluidState state) {
      return state.isIn(FluidTags.LAVA);
   }

   public double getMountedHeightOffset() {
      float f = Math.min(0.25F, this.limbAnimator.getSpeed());
      float g = this.limbAnimator.getPos();
      return (double)this.getHeight() - 0.19 + (double)(0.12F * MathHelper.cos(g * 1.5F) * 2.0F * f);
   }

   public boolean canSpawn(WorldView world) {
      return world.doesNotIntersectEntities(this);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var2 = this.getFirstPassenger();
      if (var2 instanceof PlayerEntity lv) {
         if (lv.getMainHandStack().isOf(Items.WARPED_FUNGUS_ON_A_STICK) || lv.getOffHandStack().isOf(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return lv;
         }
      }

      return null;
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Vec3d[] lvs = new Vec3d[]{getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.getYaw()), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.getYaw() - 22.5F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.getYaw() + 22.5F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.getYaw() - 45.0F), getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), passenger.getYaw() + 45.0F)};
      Set set = Sets.newLinkedHashSet();
      double d = this.getBoundingBox().maxY;
      double e = this.getBoundingBox().minY - 0.5;
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Vec3d[] var9 = lvs;
      int var10 = lvs.length;

      for(int var11 = 0; var11 < var10; ++var11) {
         Vec3d lv2 = var9[var11];
         lv.set(this.getX() + lv2.x, d, this.getZ() + lv2.z);

         for(double f = d; f > e; --f) {
            set.add(lv.toImmutable());
            lv.move(Direction.DOWN);
         }
      }

      Iterator var17 = set.iterator();

      while(true) {
         BlockPos lv3;
         double g;
         do {
            do {
               if (!var17.hasNext()) {
                  return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
               }

               lv3 = (BlockPos)var17.next();
            } while(this.world.getFluidState(lv3).isIn(FluidTags.LAVA));

            g = this.world.getDismountHeight(lv3);
         } while(!Dismounting.canDismountInBlock(g));

         Vec3d lv4 = Vec3d.ofCenter(lv3, g);
         UnmodifiableIterator var14 = passenger.getPoses().iterator();

         while(var14.hasNext()) {
            EntityPose lv5 = (EntityPose)var14.next();
            Box lv6 = passenger.getBoundingBox(lv5);
            if (Dismounting.canPlaceEntityAt(this.world, passenger, lv6.offset(lv4))) {
               passenger.setPose(lv5);
               return lv4;
            }
         }
      }
   }

   protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
      this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5F);
      this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
      this.saddledComponent.tickBoost();
      super.tickControlled(controllingPlayer, movementInput);
   }

   protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
      return new Vec3d(0.0, 0.0, 1.0);
   }

   protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
      return (float)(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (double)(this.isCold() ? 0.35F : 0.55F) * (double)this.saddledComponent.getMovementSpeedMultiplier());
   }

   protected float calculateNextStepSoundDistance() {
      return this.distanceTraveled + 0.6F;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.isInLava() ? SoundEvents.ENTITY_STRIDER_STEP_LAVA : SoundEvents.ENTITY_STRIDER_STEP, 1.0F, 1.0F);
   }

   public boolean consumeOnAStickItem() {
      return this.saddledComponent.boost(this.getRandom());
   }

   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
      this.checkBlockCollision();
      if (this.isInLava()) {
         this.onLanding();
      } else {
         super.fall(heightDifference, onGround, state, landedPosition);
      }
   }

   public void tick() {
      if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
         this.playSound(SoundEvents.ENTITY_STRIDER_HAPPY, 1.0F, this.getSoundPitch());
      } else if (this.isEscapingDanger() && this.random.nextInt(60) == 0) {
         this.playSound(SoundEvents.ENTITY_STRIDER_RETREAT, 1.0F, this.getSoundPitch());
      }

      if (!this.isAiDisabled()) {
         boolean var10000;
         boolean bl;
         label36: {
            BlockState lv = this.world.getBlockState(this.getBlockPos());
            BlockState lv2 = this.getLandingBlockState();
            bl = lv.isIn(BlockTags.STRIDER_WARM_BLOCKS) || lv2.isIn(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
            Entity var6 = this.getVehicle();
            if (var6 instanceof StriderEntity) {
               StriderEntity lv3 = (StriderEntity)var6;
               if (lv3.isCold()) {
                  var10000 = true;
                  break label36;
               }
            }

            var10000 = false;
         }

         boolean bl2 = var10000;
         this.setCold(!bl || bl2);
      }

      super.tick();
      this.updateFloating();
      this.checkBlockCollision();
   }

   private boolean isEscapingDanger() {
      return this.escapeDangerGoal != null && this.escapeDangerGoal.isActive();
   }

   private boolean isBeingTempted() {
      return this.temptGoal != null && this.temptGoal.isActive();
   }

   protected boolean movesIndependently() {
      return true;
   }

   private void updateFloating() {
      if (this.isInLava()) {
         ShapeContext lv = ShapeContext.of(this);
         if (lv.isAbove(FluidBlock.COLLISION_SHAPE, this.getBlockPos(), true) && !this.world.getFluidState(this.getBlockPos().up()).isIn(FluidTags.LAVA)) {
            this.onGround = true;
         } else {
            this.setVelocity(this.getVelocity().multiply(0.5).add(0.0, 0.05, 0.0));
         }
      }

   }

   public static DefaultAttributeContainer.Builder createStriderAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17499999701976776).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
   }

   protected SoundEvent getAmbientSound() {
      return !this.isEscapingDanger() && !this.isBeingTempted() ? SoundEvents.ENTITY_STRIDER_AMBIENT : null;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_STRIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_STRIDER_DEATH;
   }

   protected boolean canAddPassenger(Entity passenger) {
      return !this.hasPassengers() && !this.isSubmergedIn(FluidTags.LAVA);
   }

   public boolean hurtByWater() {
      return true;
   }

   public boolean isOnFire() {
      return false;
   }

   protected EntityNavigation createNavigation(World world) {
      return new Navigation(this, world);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      if (world.getBlockState(pos).getFluidState().isIn(FluidTags.LAVA)) {
         return 10.0F;
      } else {
         return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
      }
   }

   @Nullable
   public StriderEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return (StriderEntity)EntityType.STRIDER.create(arg);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   protected void dropInventory() {
      super.dropInventory();
      if (this.isSaddled()) {
         this.dropItem(Items.SADDLE);
      }

   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      boolean bl = this.isBreedingItem(player.getStackInHand(hand));
      if (!bl && this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
         if (!this.world.isClient) {
            player.startRiding(this);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         ActionResult lv = super.interactMob(player, hand);
         if (!lv.isAccepted()) {
            ItemStack lv2 = player.getStackInHand(hand);
            return lv2.isOf(Items.SADDLE) ? lv2.useOnEntity(player, this, hand) : ActionResult.PASS;
         } else {
            if (bl && !this.isSilent()) {
               this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            }

            return lv;
         }
      }
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.6F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (this.isBaby()) {
         return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
      } else {
         Random lv = world.getRandom();
         if (lv.nextInt(30) == 0) {
            MobEntity lv2 = (MobEntity)EntityType.ZOMBIFIED_PIGLIN.create(world.toServerWorld());
            if (lv2 != null) {
               entityData = this.initializeRider(world, difficulty, lv2, new ZombieEntity.ZombieData(ZombieEntity.shouldBeBaby(lv), false));
               lv2.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
               this.saddle((SoundCategory)null);
            }
         } else if (lv.nextInt(10) == 0) {
            PassiveEntity lv3 = (PassiveEntity)EntityType.STRIDER.create(world.toServerWorld());
            if (lv3 != null) {
               lv3.setBreedingAge(-24000);
               entityData = this.initializeRider(world, difficulty, lv3, (EntityData)null);
            }
         } else {
            entityData = new PassiveEntity.PassiveData(0.5F);
         }

         return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
      }
   }

   private EntityData initializeRider(ServerWorldAccess world, LocalDifficulty difficulty, MobEntity rider, @Nullable EntityData entityData) {
      rider.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
      rider.initialize(world, difficulty, SpawnReason.JOCKEY, entityData, (NbtCompound)null);
      rider.startRiding(this, true);
      return new PassiveEntity.PassiveData(0.0F);
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      SUFFOCATING_MODIFIER = new EntityAttributeModifier(SUFFOCATING_MODIFIER_ID, "Strider suffocating modifier", -0.3400000035762787, EntityAttributeModifier.Operation.MULTIPLY_BASE);
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS);
      ATTRACTING_INGREDIENT = Ingredient.ofItems(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
      BOOST_TIME = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
      COLD = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      SADDLED = DataTracker.registerData(StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   private static class GoBackToLavaGoal extends MoveToTargetPosGoal {
      private final StriderEntity strider;

      GoBackToLavaGoal(StriderEntity strider, double speed) {
         super(strider, speed, 8, 2);
         this.strider = strider;
      }

      public BlockPos getTargetPos() {
         return this.targetPos;
      }

      public boolean shouldContinue() {
         return !this.strider.isInLava() && this.isTargetPos(this.strider.world, this.targetPos);
      }

      public boolean canStart() {
         return !this.strider.isInLava() && super.canStart();
      }

      public boolean shouldResetPath() {
         return this.tryingTime % 20 == 0;
      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         return world.getBlockState(pos).isOf(Blocks.LAVA) && world.getBlockState(pos.up()).canPathfindThrough(world, pos, NavigationType.LAND);
      }
   }

   static class Navigation extends MobNavigation {
      Navigation(StriderEntity entity, World world) {
         super(entity, world);
      }

      protected PathNodeNavigator createPathNodeNavigator(int range) {
         this.nodeMaker = new LandPathNodeMaker();
         this.nodeMaker.setCanEnterOpenDoors(true);
         return new PathNodeNavigator(this.nodeMaker, range);
      }

      protected boolean canWalkOnPath(PathNodeType pathType) {
         return pathType != PathNodeType.LAVA && pathType != PathNodeType.DAMAGE_FIRE && pathType != PathNodeType.DANGER_FIRE ? super.canWalkOnPath(pathType) : true;
      }

      public boolean isValidPosition(BlockPos pos) {
         return this.world.getBlockState(pos).isOf(Blocks.LAVA) || super.isValidPosition(pos);
      }
   }
}
