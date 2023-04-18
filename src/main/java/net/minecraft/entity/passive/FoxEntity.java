package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CaveVines;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.DiveJumpingGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
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
import org.jetbrains.annotations.Nullable;

public class FoxEntity extends AnimalEntity implements VariantHolder {
   private static final TrackedData TYPE;
   private static final TrackedData FOX_FLAGS;
   private static final int SITTING_FLAG = 1;
   public static final int CROUCHING_FLAG = 4;
   public static final int ROLLING_HEAD_FLAG = 8;
   public static final int CHASING_FLAG = 16;
   private static final int SLEEPING_FLAG = 32;
   private static final int WALKING_FLAG = 64;
   private static final int AGGRESSIVE_FLAG = 128;
   private static final TrackedData OWNER;
   private static final TrackedData OTHER_TRUSTED;
   static final Predicate PICKABLE_DROP_FILTER;
   private static final Predicate JUST_ATTACKED_SOMETHING_FILTER;
   static final Predicate CHICKEN_AND_RABBIT_FILTER;
   private static final Predicate NOTICEABLE_PLAYER_FILTER;
   private static final int EATING_DURATION = 600;
   private Goal followChickenAndRabbitGoal;
   private Goal followBabyTurtleGoal;
   private Goal followFishGoal;
   private float headRollProgress;
   private float lastHeadRollProgress;
   float extraRollingHeight;
   float lastExtraRollingHeight;
   private int eatingTime;

   public FoxEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.lookControl = new FoxLookControl();
      this.moveControl = new FoxMoveControl();
      this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0F);
      this.setCanPickUpLoot(true);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(OWNER, Optional.empty());
      this.dataTracker.startTracking(OTHER_TRUSTED, Optional.empty());
      this.dataTracker.startTracking(TYPE, 0);
      this.dataTracker.startTracking(FOX_FLAGS, (byte)0);
   }

   protected void initGoals() {
      this.followChickenAndRabbitGoal = new ActiveTargetGoal(this, AnimalEntity.class, 10, false, false, (entity) -> {
         return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
      });
      this.followBabyTurtleGoal = new ActiveTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
      this.followFishGoal = new ActiveTargetGoal(this, FishEntity.class, 20, false, false, (entity) -> {
         return entity instanceof SchoolingFishEntity;
      });
      this.goalSelector.add(0, new FoxSwimGoal());
      this.goalSelector.add(0, new PowderSnowJumpGoal(this, this.world));
      this.goalSelector.add(1, new StopWanderingGoal());
      this.goalSelector.add(2, new EscapeWhenNotAggressiveGoal(2.2));
      this.goalSelector.add(3, new MateGoal(1.0));
      this.goalSelector.add(4, new FleeEntityGoal(this, PlayerEntity.class, 16.0F, 1.6, 1.4, (entity) -> {
         return NOTICEABLE_PLAYER_FILTER.test(entity) && !this.canTrust(entity.getUuid()) && !this.isAggressive();
      }));
      this.goalSelector.add(4, new FleeEntityGoal(this, WolfEntity.class, 8.0F, 1.6, 1.4, (entity) -> {
         return !((WolfEntity)entity).isTamed() && !this.isAggressive();
      }));
      this.goalSelector.add(4, new FleeEntityGoal(this, PolarBearEntity.class, 8.0F, 1.6, 1.4, (entity) -> {
         return !this.isAggressive();
      }));
      this.goalSelector.add(5, new MoveToHuntGoal());
      this.goalSelector.add(6, new JumpChasingGoal());
      this.goalSelector.add(6, new AvoidDaylightGoal(1.25));
      this.goalSelector.add(7, new AttackGoal(1.2000000476837158, true));
      this.goalSelector.add(7, new DelayedCalmDownGoal());
      this.goalSelector.add(8, new FollowParentGoal(this, 1.25));
      this.goalSelector.add(9, new GoToVillageGoal(32, 200));
      this.goalSelector.add(10, new EatBerriesGoal(1.2000000476837158, 12, 1));
      this.goalSelector.add(10, new PounceAtTargetGoal(this, 0.4F));
      this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(11, new PickupItemGoal());
      this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 24.0F));
      this.goalSelector.add(13, new SitDownAndLookAroundGoal());
      this.targetSelector.add(3, new DefendFriendGoal(LivingEntity.class, false, false, (entity) -> {
         return JUST_ATTACKED_SOMETHING_FILTER.test(entity) && !this.canTrust(entity.getUuid());
      }));
   }

   public SoundEvent getEatSound(ItemStack stack) {
      return SoundEvents.ENTITY_FOX_EAT;
   }

   public void tickMovement() {
      if (!this.world.isClient && this.isAlive() && this.canMoveVoluntarily()) {
         ++this.eatingTime;
         ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (this.canEat(lv)) {
            if (this.eatingTime > 600) {
               ItemStack lv2 = lv.finishUsing(this.world, this);
               if (!lv2.isEmpty()) {
                  this.equipStack(EquipmentSlot.MAINHAND, lv2);
               }

               this.eatingTime = 0;
            } else if (this.eatingTime > 560 && this.random.nextFloat() < 0.1F) {
               this.playSound(this.getEatSound(lv), 1.0F, 1.0F);
               this.world.sendEntityStatus(this, EntityStatuses.CREATE_EATING_PARTICLES);
            }
         }

         LivingEntity lv3 = this.getTarget();
         if (lv3 == null || !lv3.isAlive()) {
            this.setCrouching(false);
            this.setRollingHead(false);
         }
      }

      if (this.isSleeping() || this.isImmobile()) {
         this.jumping = false;
         this.sidewaysSpeed = 0.0F;
         this.forwardSpeed = 0.0F;
      }

      super.tickMovement();
      if (this.isAggressive() && this.random.nextFloat() < 0.05F) {
         this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
      }

   }

   protected boolean isImmobile() {
      return this.isDead();
   }

   private boolean canEat(ItemStack stack) {
      return stack.getItem().isFood() && this.getTarget() == null && this.onGround && !this.isSleeping();
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      if (random.nextFloat() < 0.2F) {
         float f = random.nextFloat();
         ItemStack lv;
         if (f < 0.05F) {
            lv = new ItemStack(Items.EMERALD);
         } else if (f < 0.2F) {
            lv = new ItemStack(Items.EGG);
         } else if (f < 0.4F) {
            lv = random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
         } else if (f < 0.6F) {
            lv = new ItemStack(Items.WHEAT);
         } else if (f < 0.8F) {
            lv = new ItemStack(Items.LEATHER);
         } else {
            lv = new ItemStack(Items.FEATHER);
         }

         this.equipStack(EquipmentSlot.MAINHAND, lv);
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.CREATE_EATING_PARTICLES) {
         ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (!lv.isEmpty()) {
            for(int i = 0; i < 8; ++i) {
               Vec3d lv2 = (new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)).rotateX(-this.getPitch() * 0.017453292F).rotateY(-this.getYaw() * 0.017453292F);
               this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, lv), this.getX() + this.getRotationVector().x / 2.0, this.getY(), this.getZ() + this.getRotationVector().z / 2.0, lv2.x, lv2.y + 0.05, lv2.z);
            }
         }
      } else {
         super.handleStatus(status);
      }

   }

   public static DefaultAttributeContainer.Builder createFoxAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   @Nullable
   public FoxEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      FoxEntity lv = (FoxEntity)EntityType.FOX.create(arg);
      if (lv != null) {
         lv.setVariant(this.random.nextBoolean() ? this.getVariant() : ((FoxEntity)arg2).getVariant());
      }

      return lv;
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.FOXES_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      RegistryEntry lv = world.getBiome(this.getBlockPos());
      Type lv2 = FoxEntity.Type.fromBiome(lv);
      boolean bl = false;
      if (entityData instanceof FoxData lv3) {
         lv2 = lv3.type;
         if (lv3.getSpawnedCount() >= 2) {
            bl = true;
         }
      } else {
         entityData = new FoxData(lv2);
      }

      this.setVariant(lv2);
      if (bl) {
         this.setBreedingAge(-24000);
      }

      if (world instanceof ServerWorld) {
         this.addTypeSpecificGoals();
      }

      this.initEquipment(world.getRandom(), difficulty);
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   private void addTypeSpecificGoals() {
      if (this.getVariant() == FoxEntity.Type.RED) {
         this.targetSelector.add(4, this.followChickenAndRabbitGoal);
         this.targetSelector.add(4, this.followBabyTurtleGoal);
         this.targetSelector.add(6, this.followFishGoal);
      } else {
         this.targetSelector.add(4, this.followFishGoal);
         this.targetSelector.add(6, this.followChickenAndRabbitGoal);
         this.targetSelector.add(6, this.followBabyTurtleGoal);
      }

   }

   protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
      if (this.isBreedingItem(stack)) {
         this.playSound(this.getEatSound(stack), 1.0F, 1.0F);
      }

      super.eat(player, hand, stack);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? dimensions.height * 0.85F : 0.4F;
   }

   public Type getVariant() {
      return FoxEntity.Type.fromId((Integer)this.dataTracker.get(TYPE));
   }

   public void setVariant(Type arg) {
      this.dataTracker.set(TYPE, arg.getId());
   }

   List getTrustedUuids() {
      List list = Lists.newArrayList();
      list.add((UUID)((Optional)this.dataTracker.get(OWNER)).orElse((Object)null));
      list.add((UUID)((Optional)this.dataTracker.get(OTHER_TRUSTED)).orElse((Object)null));
      return list;
   }

   void addTrustedUuid(@Nullable UUID uuid) {
      if (((Optional)this.dataTracker.get(OWNER)).isPresent()) {
         this.dataTracker.set(OTHER_TRUSTED, Optional.ofNullable(uuid));
      } else {
         this.dataTracker.set(OWNER, Optional.ofNullable(uuid));
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      List list = this.getTrustedUuids();
      NbtList lv = new NbtList();
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         UUID uUID = (UUID)var4.next();
         if (uUID != null) {
            lv.add(NbtHelper.fromUuid(uUID));
         }
      }

      nbt.put("Trusted", lv);
      nbt.putBoolean("Sleeping", this.isSleeping());
      nbt.putString("Type", this.getVariant().asString());
      nbt.putBoolean("Sitting", this.isSitting());
      nbt.putBoolean("Crouching", this.isInSneakingPose());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      NbtList lv = nbt.getList("Trusted", NbtElement.INT_ARRAY_TYPE);

      for(int i = 0; i < lv.size(); ++i) {
         this.addTrustedUuid(NbtHelper.toUuid(lv.get(i)));
      }

      this.setSleeping(nbt.getBoolean("Sleeping"));
      this.setVariant(FoxEntity.Type.byName(nbt.getString("Type")));
      this.setSitting(nbt.getBoolean("Sitting"));
      this.setCrouching(nbt.getBoolean("Crouching"));
      if (this.world instanceof ServerWorld) {
         this.addTypeSpecificGoals();
      }

   }

   public boolean isSitting() {
      return this.getFoxFlag(SITTING_FLAG);
   }

   public void setSitting(boolean sitting) {
      this.setFoxFlag(SITTING_FLAG, sitting);
   }

   public boolean isWalking() {
      return this.getFoxFlag(WALKING_FLAG);
   }

   void setWalking(boolean walking) {
      this.setFoxFlag(WALKING_FLAG, walking);
   }

   boolean isAggressive() {
      return this.getFoxFlag(AGGRESSIVE_FLAG);
   }

   void setAggressive(boolean aggressive) {
      this.setFoxFlag(AGGRESSIVE_FLAG, aggressive);
   }

   public boolean isSleeping() {
      return this.getFoxFlag(SLEEPING_FLAG);
   }

   void setSleeping(boolean sleeping) {
      this.setFoxFlag(SLEEPING_FLAG, sleeping);
   }

   private void setFoxFlag(int mask, boolean value) {
      if (value) {
         this.dataTracker.set(FOX_FLAGS, (byte)((Byte)this.dataTracker.get(FOX_FLAGS) | mask));
      } else {
         this.dataTracker.set(FOX_FLAGS, (byte)((Byte)this.dataTracker.get(FOX_FLAGS) & ~mask));
      }

   }

   private boolean getFoxFlag(int bitmask) {
      return ((Byte)this.dataTracker.get(FOX_FLAGS) & bitmask) != 0;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot lv = MobEntity.getPreferredEquipmentSlot(stack);
      if (!this.getEquippedStack(lv).isEmpty()) {
         return false;
      } else {
         return lv == EquipmentSlot.MAINHAND && super.canEquip(stack);
      }
   }

   public boolean canPickupItem(ItemStack stack) {
      Item lv = stack.getItem();
      ItemStack lv2 = this.getEquippedStack(EquipmentSlot.MAINHAND);
      return lv2.isEmpty() || this.eatingTime > 0 && lv.isFood() && !lv2.getItem().isFood();
   }

   private void spit(ItemStack stack) {
      if (!stack.isEmpty() && !this.world.isClient) {
         ItemEntity lv = new ItemEntity(this.world, this.getX() + this.getRotationVector().x, this.getY() + 1.0, this.getZ() + this.getRotationVector().z, stack);
         lv.setPickupDelay(40);
         lv.setThrower(this.getUuid());
         this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
         this.world.spawnEntity(lv);
      }
   }

   private void dropItem(ItemStack stack) {
      ItemEntity lv = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), stack);
      this.world.spawnEntity(lv);
   }

   protected void loot(ItemEntity item) {
      ItemStack lv = item.getStack();
      if (this.canPickupItem(lv)) {
         int i = lv.getCount();
         if (i > 1) {
            this.dropItem(lv.split(i - 1));
         }

         this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
         this.triggerItemPickedUpByEntityCriteria(item);
         this.equipStack(EquipmentSlot.MAINHAND, lv.split(1));
         this.updateDropChances(EquipmentSlot.MAINHAND);
         this.sendPickup(item, lv.getCount());
         item.discard();
         this.eatingTime = 0;
      }

   }

   public void tick() {
      super.tick();
      if (this.canMoveVoluntarily()) {
         boolean bl = this.isTouchingWater();
         if (bl || this.getTarget() != null || this.world.isThundering()) {
            this.stopSleeping();
         }

         if (bl || this.isSleeping()) {
            this.setSitting(false);
         }

         if (this.isWalking() && this.world.random.nextFloat() < 0.2F) {
            BlockPos lv = this.getBlockPos();
            BlockState lv2 = this.world.getBlockState(lv);
            this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv, Block.getRawIdFromState(lv2));
         }
      }

      this.lastHeadRollProgress = this.headRollProgress;
      if (this.isRollingHead()) {
         this.headRollProgress += (1.0F - this.headRollProgress) * 0.4F;
      } else {
         this.headRollProgress += (0.0F - this.headRollProgress) * 0.4F;
      }

      this.lastExtraRollingHeight = this.extraRollingHeight;
      if (this.isInSneakingPose()) {
         this.extraRollingHeight += 0.2F;
         if (this.extraRollingHeight > 3.0F) {
            this.extraRollingHeight = 3.0F;
         }
      } else {
         this.extraRollingHeight = 0.0F;
      }

   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.isIn(ItemTags.FOX_FOOD);
   }

   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
      ((FoxEntity)child).addTrustedUuid(player.getUuid());
   }

   public boolean isChasing() {
      return this.getFoxFlag(CHASING_FLAG);
   }

   public void setChasing(boolean chasing) {
      this.setFoxFlag(CHASING_FLAG, chasing);
   }

   public boolean isJumping() {
      return this.jumping;
   }

   public boolean isFullyCrouched() {
      return this.extraRollingHeight == 3.0F;
   }

   public void setCrouching(boolean crouching) {
      this.setFoxFlag(CROUCHING_FLAG, crouching);
   }

   public boolean isInSneakingPose() {
      return this.getFoxFlag(CROUCHING_FLAG);
   }

   public void setRollingHead(boolean rollingHead) {
      this.setFoxFlag(ROLLING_HEAD_FLAG, rollingHead);
   }

   public boolean isRollingHead() {
      return this.getFoxFlag(ROLLING_HEAD_FLAG);
   }

   public float getHeadRoll(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastHeadRollProgress, this.headRollProgress) * 0.11F * 3.1415927F;
   }

   public float getBodyRotationHeightOffset(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastExtraRollingHeight, this.extraRollingHeight);
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (this.isAggressive() && target == null) {
         this.setAggressive(false);
      }

      super.setTarget(target);
   }

   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
      return MathHelper.ceil((fallDistance - 5.0F) * damageMultiplier);
   }

   void stopSleeping() {
      this.setSleeping(false);
   }

   void stopActions() {
      this.setRollingHead(false);
      this.setCrouching(false);
      this.setSitting(false);
      this.setSleeping(false);
      this.setAggressive(false);
      this.setWalking(false);
   }

   boolean wantsToPickupItem() {
      return !this.isSleeping() && !this.isSitting() && !this.isWalking();
   }

   public void playAmbientSound() {
      SoundEvent lv = this.getAmbientSound();
      if (lv == SoundEvents.ENTITY_FOX_SCREECH) {
         this.playSound(lv, 2.0F, this.getSoundPitch());
      } else {
         super.playAmbientSound();
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return SoundEvents.ENTITY_FOX_SLEEP;
      } else {
         if (!this.world.isDay() && this.random.nextFloat() < 0.1F) {
            List list = this.world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR);
            if (list.isEmpty()) {
               return SoundEvents.ENTITY_FOX_SCREECH;
            }
         }

         return SoundEvents.ENTITY_FOX_AMBIENT;
      }
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_FOX_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_FOX_DEATH;
   }

   boolean canTrust(UUID uuid) {
      return this.getTrustedUuids().contains(uuid);
   }

   protected void drop(DamageSource source) {
      ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
      if (!lv.isEmpty()) {
         this.dropStack(lv);
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

      super.drop(source);
   }

   public static boolean canJumpChase(FoxEntity fox, LivingEntity chasedEntity) {
      double d = chasedEntity.getZ() - fox.getZ();
      double e = chasedEntity.getX() - fox.getX();
      double f = d / e;
      int i = true;

      for(int j = 0; j < 6; ++j) {
         double g = f == 0.0 ? 0.0 : d * (double)((float)j / 6.0F);
         double h = f == 0.0 ? e * (double)((float)j / 6.0F) : g / f;

         for(int k = 1; k < 4; ++k) {
            if (!fox.world.getBlockState(BlockPos.ofFloored(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).isReplaceable()) {
               return false;
            }
         }
      }

      return true;
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      TYPE = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.INTEGER);
      FOX_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
      OWNER = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
      OTHER_TRUSTED = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
      PICKABLE_DROP_FILTER = (item) -> {
         return !item.cannotPickup() && item.isAlive();
      };
      JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
         if (!(entity instanceof LivingEntity lv)) {
            return false;
         } else {
            return lv.getAttacking() != null && lv.getLastAttackTime() < lv.age + 600;
         }
      };
      CHICKEN_AND_RABBIT_FILTER = (entity) -> {
         return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
      };
      NOTICEABLE_PLAYER_FILTER = (entity) -> {
         return !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
      };
   }

   public class FoxLookControl extends LookControl {
      public FoxLookControl() {
         super(FoxEntity.this);
      }

      public void tick() {
         if (!FoxEntity.this.isSleeping()) {
            super.tick();
         }

      }

      protected boolean shouldStayHorizontal() {
         return !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.isWalking();
      }
   }

   class FoxMoveControl extends MoveControl {
      public FoxMoveControl() {
         super(FoxEntity.this);
      }

      public void tick() {
         if (FoxEntity.this.wantsToPickupItem()) {
            super.tick();
         }

      }
   }

   class FoxSwimGoal extends SwimGoal {
      public FoxSwimGoal() {
         super(FoxEntity.this);
      }

      public void start() {
         super.start();
         FoxEntity.this.stopActions();
      }

      public boolean canStart() {
         return FoxEntity.this.isTouchingWater() && FoxEntity.this.getFluidHeight(FluidTags.WATER) > 0.25 || FoxEntity.this.isInLava();
      }
   }

   private class StopWanderingGoal extends Goal {
      int timer;

      public StopWanderingGoal() {
         this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.JUMP, Goal.Control.MOVE));
      }

      public boolean canStart() {
         return FoxEntity.this.isWalking();
      }

      public boolean shouldContinue() {
         return this.canStart() && this.timer > 0;
      }

      public void start() {
         this.timer = this.getTickCount(40);
      }

      public void stop() {
         FoxEntity.this.setWalking(false);
      }

      public void tick() {
         --this.timer;
      }
   }

   private class EscapeWhenNotAggressiveGoal extends EscapeDangerGoal {
      public EscapeWhenNotAggressiveGoal(double speed) {
         super(FoxEntity.this, speed);
      }

      public boolean isInDanger() {
         return !FoxEntity.this.isAggressive() && super.isInDanger();
      }
   }

   private class MateGoal extends AnimalMateGoal {
      public MateGoal(double chance) {
         super(FoxEntity.this, chance);
      }

      public void start() {
         ((FoxEntity)this.animal).stopActions();
         ((FoxEntity)this.mate).stopActions();
         super.start();
      }

      protected void breed() {
         ServerWorld lv = (ServerWorld)this.world;
         FoxEntity lv2 = (FoxEntity)this.animal.createChild(lv, this.mate);
         if (lv2 != null) {
            ServerPlayerEntity lv3 = this.animal.getLovingPlayer();
            ServerPlayerEntity lv4 = this.mate.getLovingPlayer();
            ServerPlayerEntity lv5 = lv3;
            if (lv3 != null) {
               lv2.addTrustedUuid(lv3.getUuid());
            } else {
               lv5 = lv4;
            }

            if (lv4 != null && lv3 != lv4) {
               lv2.addTrustedUuid(lv4.getUuid());
            }

            if (lv5 != null) {
               lv5.incrementStat(Stats.ANIMALS_BRED);
               Criteria.BRED_ANIMALS.trigger(lv5, this.animal, this.mate, lv2);
            }

            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            lv2.setBreedingAge(-24000);
            lv2.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
            lv.spawnEntityAndPassengers(lv2);
            this.world.sendEntityStatus(this.animal, EntityStatuses.ADD_BREEDING_PARTICLES);
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
               this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }

         }
      }
   }

   private class MoveToHuntGoal extends Goal {
      public MoveToHuntGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         if (FoxEntity.this.isSleeping()) {
            return false;
         } else {
            LivingEntity lv = FoxEntity.this.getTarget();
            return lv != null && lv.isAlive() && FoxEntity.CHICKEN_AND_RABBIT_FILTER.test(lv) && FoxEntity.this.squaredDistanceTo(lv) > 36.0 && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.jumping;
         }
      }

      public void start() {
         FoxEntity.this.setSitting(false);
         FoxEntity.this.setWalking(false);
      }

      public void stop() {
         LivingEntity lv = FoxEntity.this.getTarget();
         if (lv != null && FoxEntity.canJumpChase(FoxEntity.this, lv)) {
            FoxEntity.this.setRollingHead(true);
            FoxEntity.this.setCrouching(true);
            FoxEntity.this.getNavigation().stop();
            FoxEntity.this.getLookControl().lookAt(lv, (float)FoxEntity.this.getMaxHeadRotation(), (float)FoxEntity.this.getMaxLookPitchChange());
         } else {
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setCrouching(false);
         }

      }

      public void tick() {
         LivingEntity lv = FoxEntity.this.getTarget();
         if (lv != null) {
            FoxEntity.this.getLookControl().lookAt(lv, (float)FoxEntity.this.getMaxHeadRotation(), (float)FoxEntity.this.getMaxLookPitchChange());
            if (FoxEntity.this.squaredDistanceTo(lv) <= 36.0) {
               FoxEntity.this.setRollingHead(true);
               FoxEntity.this.setCrouching(true);
               FoxEntity.this.getNavigation().stop();
            } else {
               FoxEntity.this.getNavigation().startMovingTo(lv, 1.5);
            }

         }
      }
   }

   public class JumpChasingGoal extends DiveJumpingGoal {
      public boolean canStart() {
         if (!FoxEntity.this.isFullyCrouched()) {
            return false;
         } else {
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv != null && lv.isAlive()) {
               if (lv.getMovementDirection() != lv.getHorizontalFacing()) {
                  return false;
               } else {
                  boolean bl = FoxEntity.canJumpChase(FoxEntity.this, lv);
                  if (!bl) {
                     FoxEntity.this.getNavigation().findPathTo((Entity)lv, 0);
                     FoxEntity.this.setCrouching(false);
                     FoxEntity.this.setRollingHead(false);
                  }

                  return bl;
               }
            } else {
               return false;
            }
         }
      }

      public boolean shouldContinue() {
         LivingEntity lv = FoxEntity.this.getTarget();
         if (lv != null && lv.isAlive()) {
            double d = FoxEntity.this.getVelocity().y;
            return (!(d * d < 0.05000000074505806) || !(Math.abs(FoxEntity.this.getPitch()) < 15.0F) || !FoxEntity.this.onGround) && !FoxEntity.this.isWalking();
         } else {
            return false;
         }
      }

      public boolean canStop() {
         return false;
      }

      public void start() {
         FoxEntity.this.setJumping(true);
         FoxEntity.this.setChasing(true);
         FoxEntity.this.setRollingHead(false);
         LivingEntity lv = FoxEntity.this.getTarget();
         if (lv != null) {
            FoxEntity.this.getLookControl().lookAt(lv, 60.0F, 30.0F);
            Vec3d lv2 = (new Vec3d(lv.getX() - FoxEntity.this.getX(), lv.getY() - FoxEntity.this.getY(), lv.getZ() - FoxEntity.this.getZ())).normalize();
            FoxEntity.this.setVelocity(FoxEntity.this.getVelocity().add(lv2.x * 0.8, 0.9, lv2.z * 0.8));
         }

         FoxEntity.this.getNavigation().stop();
      }

      public void stop() {
         FoxEntity.this.setCrouching(false);
         FoxEntity.this.extraRollingHeight = 0.0F;
         FoxEntity.this.lastExtraRollingHeight = 0.0F;
         FoxEntity.this.setRollingHead(false);
         FoxEntity.this.setChasing(false);
      }

      public void tick() {
         LivingEntity lv = FoxEntity.this.getTarget();
         if (lv != null) {
            FoxEntity.this.getLookControl().lookAt(lv, 60.0F, 30.0F);
         }

         if (!FoxEntity.this.isWalking()) {
            Vec3d lv2 = FoxEntity.this.getVelocity();
            if (lv2.y * lv2.y < 0.029999999329447746 && FoxEntity.this.getPitch() != 0.0F) {
               FoxEntity.this.setPitch(MathHelper.lerpAngleDegrees(0.2F, FoxEntity.this.getPitch(), 0.0F));
            } else {
               double d = lv2.horizontalLength();
               double e = Math.signum(-lv2.y) * Math.acos(d / lv2.length()) * 57.2957763671875;
               FoxEntity.this.setPitch((float)e);
            }
         }

         if (lv != null && FoxEntity.this.distanceTo(lv) <= 2.0F) {
            FoxEntity.this.tryAttack(lv);
         } else if (FoxEntity.this.getPitch() > 0.0F && FoxEntity.this.onGround && (float)FoxEntity.this.getVelocity().y != 0.0F && FoxEntity.this.world.getBlockState(FoxEntity.this.getBlockPos()).isOf(Blocks.SNOW)) {
            FoxEntity.this.setPitch(60.0F);
            FoxEntity.this.setTarget((LivingEntity)null);
            FoxEntity.this.setWalking(true);
         }

      }
   }

   private class AvoidDaylightGoal extends EscapeSunlightGoal {
      private int timer = toGoalTicks(100);

      public AvoidDaylightGoal(double speed) {
         super(FoxEntity.this, speed);
      }

      public boolean canStart() {
         if (!FoxEntity.this.isSleeping() && this.mob.getTarget() == null) {
            if (FoxEntity.this.world.isThundering() && FoxEntity.this.world.isSkyVisible(this.mob.getBlockPos())) {
               return this.targetShadedPos();
            } else if (this.timer > 0) {
               --this.timer;
               return false;
            } else {
               this.timer = 100;
               BlockPos lv = this.mob.getBlockPos();
               return FoxEntity.this.world.isDay() && FoxEntity.this.world.isSkyVisible(lv) && !((ServerWorld)FoxEntity.this.world).isNearOccupiedPointOfInterest(lv) && this.targetShadedPos();
            }
         } else {
            return false;
         }
      }

      public void start() {
         FoxEntity.this.stopActions();
         super.start();
      }
   }

   private class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(double speed, boolean pauseWhenIdle) {
         super(FoxEntity.this, speed, pauseWhenIdle);
      }

      protected void attack(LivingEntity target, double squaredDistance) {
         double e = this.getSquaredMaxAttackDistance(target);
         if (squaredDistance <= e && this.isCooledDown()) {
            this.resetCooldown();
            this.mob.tryAttack(target);
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_BITE, 1.0F, 1.0F);
         }

      }

      public void start() {
         FoxEntity.this.setRollingHead(false);
         super.start();
      }

      public boolean canStart() {
         return !FoxEntity.this.isSitting() && !FoxEntity.this.isSleeping() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isWalking() && super.canStart();
      }
   }

   class DelayedCalmDownGoal extends CalmDownGoal {
      private static final int MAX_CALM_DOWN_TIME = toGoalTicks(140);
      private int timer;

      public DelayedCalmDownGoal() {
         super();
         this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
      }

      public boolean canStart() {
         if (FoxEntity.this.sidewaysSpeed == 0.0F && FoxEntity.this.upwardSpeed == 0.0F && FoxEntity.this.forwardSpeed == 0.0F) {
            return this.canNotCalmDown() || FoxEntity.this.isSleeping();
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         return this.canNotCalmDown();
      }

      private boolean canNotCalmDown() {
         if (this.timer > 0) {
            --this.timer;
            return false;
         } else {
            return FoxEntity.this.world.isDay() && this.isAtFavoredLocation() && !this.canCalmDown() && !FoxEntity.this.inPowderSnow;
         }
      }

      public void stop() {
         this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
         FoxEntity.this.stopActions();
      }

      public void start() {
         FoxEntity.this.setSitting(false);
         FoxEntity.this.setCrouching(false);
         FoxEntity.this.setRollingHead(false);
         FoxEntity.this.setJumping(false);
         FoxEntity.this.setSleeping(true);
         FoxEntity.this.getNavigation().stop();
         FoxEntity.this.getMoveControl().moveTo(FoxEntity.this.getX(), FoxEntity.this.getY(), FoxEntity.this.getZ(), 0.0);
      }
   }

   private class FollowParentGoal extends net.minecraft.entity.ai.goal.FollowParentGoal {
      private final FoxEntity fox;

      public FollowParentGoal(FoxEntity fox, double speed) {
         super(fox, speed);
         this.fox = fox;
      }

      public boolean canStart() {
         return !this.fox.isAggressive() && super.canStart();
      }

      public boolean shouldContinue() {
         return !this.fox.isAggressive() && super.shouldContinue();
      }

      public void start() {
         this.fox.stopActions();
         super.start();
      }
   }

   private class GoToVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
      public GoToVillageGoal(int unused, int searchRange) {
         super(FoxEntity.this, searchRange);
      }

      public void start() {
         FoxEntity.this.stopActions();
         super.start();
      }

      public boolean canStart() {
         return super.canStart() && this.canGoToVillage();
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && this.canGoToVillage();
      }

      private boolean canGoToVillage() {
         return !FoxEntity.this.isSleeping() && !FoxEntity.this.isSitting() && !FoxEntity.this.isAggressive() && FoxEntity.this.getTarget() == null;
      }
   }

   public class EatBerriesGoal extends MoveToTargetPosGoal {
      private static final int EATING_TIME = 40;
      protected int timer;

      public EatBerriesGoal(double speed, int range, int maxYDifference) {
         super(FoxEntity.this, speed, range, maxYDifference);
      }

      public double getDesiredDistanceToTarget() {
         return 2.0;
      }

      public boolean shouldResetPath() {
         return this.tryingTime % 100 == 0;
      }

      protected boolean isTargetPos(WorldView world, BlockPos pos) {
         BlockState lv = world.getBlockState(pos);
         return lv.isOf(Blocks.SWEET_BERRY_BUSH) && (Integer)lv.get(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasBerries(lv);
      }

      public void tick() {
         if (this.hasReached()) {
            if (this.timer >= 40) {
               this.eatBerries();
            } else {
               ++this.timer;
            }
         } else if (!this.hasReached() && FoxEntity.this.random.nextFloat() < 0.05F) {
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
         }

         super.tick();
      }

      protected void eatBerries() {
         if (FoxEntity.this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            BlockState lv = FoxEntity.this.world.getBlockState(this.targetPos);
            if (lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
               this.pickSweetBerries(lv);
            } else if (CaveVines.hasBerries(lv)) {
               this.pickGlowBerries(lv);
            }

         }
      }

      private void pickGlowBerries(BlockState state) {
         CaveVines.pickBerries(FoxEntity.this, state, FoxEntity.this.world, this.targetPos);
      }

      private void pickSweetBerries(BlockState state) {
         int i = (Integer)state.get(SweetBerryBushBlock.AGE);
         state.with(SweetBerryBushBlock.AGE, 1);
         int j = 1 + FoxEntity.this.world.random.nextInt(2) + (i == 3 ? 1 : 0);
         ItemStack lv = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (lv.isEmpty()) {
            FoxEntity.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
            --j;
         }

         if (j > 0) {
            Block.dropStack(FoxEntity.this.world, this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
         }

         FoxEntity.this.playSound(SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
         FoxEntity.this.world.setBlockState(this.targetPos, (BlockState)state.with(SweetBerryBushBlock.AGE, 1), Block.NOTIFY_LISTENERS);
      }

      public boolean canStart() {
         return !FoxEntity.this.isSleeping() && super.canStart();
      }

      public void start() {
         this.timer = 0;
         FoxEntity.this.setSitting(false);
         super.start();
      }
   }

   class PickupItemGoal extends Goal {
      public PickupItemGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         if (!FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
         } else if (FoxEntity.this.getTarget() == null && FoxEntity.this.getAttacker() == null) {
            if (!FoxEntity.this.wantsToPickupItem()) {
               return false;
            } else if (FoxEntity.this.getRandom().nextInt(toGoalTicks(10)) != 0) {
               return false;
            } else {
               List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), FoxEntity.PICKABLE_DROP_FILTER);
               return !list.isEmpty() && FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
            }
         } else {
            return false;
         }
      }

      public void tick() {
         List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), FoxEntity.PICKABLE_DROP_FILTER);
         ItemStack lv = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (lv.isEmpty() && !list.isEmpty()) {
            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
         }

      }

      public void start() {
         List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), FoxEntity.PICKABLE_DROP_FILTER);
         if (!list.isEmpty()) {
            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
         }

      }
   }

   class LookAtEntityGoal extends net.minecraft.entity.ai.goal.LookAtEntityGoal {
      public LookAtEntityGoal(MobEntity fox, Class targetType, float range) {
         super(fox, targetType, range);
      }

      public boolean canStart() {
         return super.canStart() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
      }

      public boolean shouldContinue() {
         return super.shouldContinue() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
      }
   }

   class SitDownAndLookAroundGoal extends CalmDownGoal {
      private double lookX;
      private double lookZ;
      private int timer;
      private int counter;

      public SitDownAndLookAroundGoal() {
         super();
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         return FoxEntity.this.getAttacker() == null && FoxEntity.this.getRandom().nextFloat() < 0.02F && !FoxEntity.this.isSleeping() && FoxEntity.this.getTarget() == null && FoxEntity.this.getNavigation().isIdle() && !this.canCalmDown() && !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose();
      }

      public boolean shouldContinue() {
         return this.counter > 0;
      }

      public void start() {
         this.chooseNewAngle();
         this.counter = 2 + FoxEntity.this.getRandom().nextInt(3);
         FoxEntity.this.setSitting(true);
         FoxEntity.this.getNavigation().stop();
      }

      public void stop() {
         FoxEntity.this.setSitting(false);
      }

      public void tick() {
         --this.timer;
         if (this.timer <= 0) {
            --this.counter;
            this.chooseNewAngle();
         }

         FoxEntity.this.getLookControl().lookAt(FoxEntity.this.getX() + this.lookX, FoxEntity.this.getEyeY(), FoxEntity.this.getZ() + this.lookZ, (float)FoxEntity.this.getMaxHeadRotation(), (float)FoxEntity.this.getMaxLookPitchChange());
      }

      private void chooseNewAngle() {
         double d = 6.283185307179586 * FoxEntity.this.getRandom().nextDouble();
         this.lookX = Math.cos(d);
         this.lookZ = Math.sin(d);
         this.timer = this.getTickCount(80 + FoxEntity.this.getRandom().nextInt(20));
      }
   }

   private class DefendFriendGoal extends ActiveTargetGoal {
      @Nullable
      private LivingEntity offender;
      @Nullable
      private LivingEntity friend;
      private int lastAttackedTime;

      public DefendFriendGoal(Class targetEntityClass, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate targetPredicate) {
         super(FoxEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
      }

      public boolean canStart() {
         if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
         } else {
            Iterator var1 = FoxEntity.this.getTrustedUuids().iterator();

            while(var1.hasNext()) {
               UUID uUID = (UUID)var1.next();
               if (uUID != null && FoxEntity.this.world instanceof ServerWorld) {
                  Entity lv = ((ServerWorld)FoxEntity.this.world).getEntity(uUID);
                  if (lv instanceof LivingEntity) {
                     LivingEntity lv2 = (LivingEntity)lv;
                     this.friend = lv2;
                     this.offender = lv2.getAttacker();
                     int i = lv2.getLastAttackedTime();
                     return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
                  }
               }
            }

            return false;
         }
      }

      public void start() {
         this.setTargetEntity(this.offender);
         this.targetEntity = this.offender;
         if (this.friend != null) {
            this.lastAttackedTime = this.friend.getLastAttackedTime();
         }

         FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
         FoxEntity.this.setAggressive(true);
         FoxEntity.this.stopSleeping();
         super.start();
      }
   }

   public static enum Type implements StringIdentifiable {
      RED(0, "red"),
      SNOW(1, "snow");

      public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Type::values);
      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Type::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
      private final int id;
      private final String key;

      private Type(int id, String key) {
         this.id = id;
         this.key = key;
      }

      public String asString() {
         return this.key;
      }

      public int getId() {
         return this.id;
      }

      public static Type byName(String name) {
         return (Type)CODEC.byId(name, RED);
      }

      public static Type fromId(int id) {
         return (Type)BY_ID.apply(id);
      }

      public static Type fromBiome(RegistryEntry biome) {
         return biome.isIn(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
      }

      // $FF: synthetic method
      private static Type[] method_36637() {
         return new Type[]{RED, SNOW};
      }
   }

   public static class FoxData extends PassiveEntity.PassiveData {
      public final Type type;

      public FoxData(Type type) {
         super(false);
         this.type = type;
      }
   }

   private abstract class CalmDownGoal extends Goal {
      private final TargetPredicate WORRIABLE_ENTITY_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(12.0).ignoreVisibility().setPredicate(FoxEntity.this.new WorriableEntityFilter());

      CalmDownGoal() {
      }

      protected boolean isAtFavoredLocation() {
         BlockPos lv = BlockPos.ofFloored(FoxEntity.this.getX(), FoxEntity.this.getBoundingBox().maxY, FoxEntity.this.getZ());
         return !FoxEntity.this.world.isSkyVisible(lv) && FoxEntity.this.getPathfindingFavor(lv) >= 0.0F;
      }

      protected boolean canCalmDown() {
         return !FoxEntity.this.world.getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, FoxEntity.this, FoxEntity.this.getBoundingBox().expand(12.0, 6.0, 12.0)).isEmpty();
      }
   }

   public class WorriableEntityFilter implements Predicate {
      public boolean test(LivingEntity arg) {
         if (arg instanceof FoxEntity) {
            return false;
         } else if (!(arg instanceof ChickenEntity) && !(arg instanceof RabbitEntity) && !(arg instanceof HostileEntity)) {
            if (arg instanceof TameableEntity) {
               return !((TameableEntity)arg).isTamed();
            } else if (arg instanceof PlayerEntity && (arg.isSpectator() || ((PlayerEntity)arg).isCreative())) {
               return false;
            } else if (FoxEntity.this.canTrust(arg.getUuid())) {
               return false;
            } else {
               return !arg.isSleeping() && !arg.isSneaky();
            }
         } else {
            return true;
         }
      }

      // $FF: synthetic method
      public boolean test(Object entity) {
         return this.test((LivingEntity)entity);
      }
   }
}
