package net.minecraft.entity.passive;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.AmbientStandGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHorseEntity extends AnimalEntity implements InventoryChangedListener, RideableInventory, Tameable, JumpingMount, Saddleable {
   public static final int field_30413 = 400;
   public static final int field_30414 = 499;
   public static final int field_30415 = 500;
   public static final double field_42647 = 0.15;
   private static final float MIN_MOVEMENT_SPEED_BONUS = (float)getChildMovementSpeedBonus(() -> {
      return 0.0;
   });
   private static final float MAX_MOVEMENT_SPEED_BONUS = (float)getChildMovementSpeedBonus(() -> {
      return 1.0;
   });
   private static final float MIN_JUMP_STRENGTH_BONUS = (float)getChildJumpStrengthBonus(() -> {
      return 0.0;
   });
   private static final float MAX_JUMP_STRENGTH_BONUS = (float)getChildJumpStrengthBonus(() -> {
      return 1.0;
   });
   private static final float MIN_HEALTH_BONUS = getChildHealthBonus((max) -> {
      return 0;
   });
   private static final float MAX_HEALTH_BONUS = getChildHealthBonus((max) -> {
      return max - 1;
   });
   private static final float field_42979 = 0.25F;
   private static final float field_42980 = 0.5F;
   private static final Predicate IS_BRED_HORSE = (entity) -> {
      return entity instanceof AbstractHorseEntity && ((AbstractHorseEntity)entity).isBred();
   };
   private static final TargetPredicate PARENT_HORSE_PREDICATE;
   private static final Ingredient BREEDING_INGREDIENT;
   private static final TrackedData HORSE_FLAGS;
   private static final int TAMED_FLAG = 2;
   private static final int SADDLED_FLAG = 4;
   private static final int BRED_FLAG = 8;
   private static final int EATING_GRASS_FLAG = 16;
   private static final int ANGRY_FLAG = 32;
   private static final int EATING_FLAG = 64;
   public static final int field_30416 = 0;
   public static final int field_30417 = 1;
   public static final int field_30418 = 2;
   private int eatingGrassTicks;
   private int eatingTicks;
   private int angryTicks;
   public int tailWagTicks;
   public int field_6958;
   protected boolean inAir;
   protected SimpleInventory items;
   protected int temper;
   protected float jumpStrength;
   protected boolean jumping;
   private float eatingGrassAnimationProgress;
   private float lastEatingGrassAnimationProgress;
   private float angryAnimationProgress;
   private float lastAngryAnimationProgress;
   private float eatingAnimationProgress;
   private float lastEatingAnimationProgress;
   protected boolean playExtraHorseSounds = true;
   protected int soundTicks;
   @Nullable
   private UUID ownerUuid;

   protected AbstractHorseEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setStepHeight(1.0F);
      this.onChestedStatusChanged();
   }

   protected void initGoals() {
      this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2));
      this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0, AbstractHorseEntity.class));
      this.goalSelector.add(4, new FollowParentGoal(this, 1.0));
      this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      if (this.shouldAmbientStand()) {
         this.goalSelector.add(9, new AmbientStandGoal(this));
      }

      this.initCustomGoals();
   }

   protected void initCustomGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(3, new TemptGoal(this, 1.25, Ingredient.ofItems(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE), false));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(HORSE_FLAGS, (byte)0);
   }

   protected boolean getHorseFlag(int bitmask) {
      return ((Byte)this.dataTracker.get(HORSE_FLAGS) & bitmask) != 0;
   }

   protected void setHorseFlag(int bitmask, boolean flag) {
      byte b = (Byte)this.dataTracker.get(HORSE_FLAGS);
      if (flag) {
         this.dataTracker.set(HORSE_FLAGS, (byte)(b | bitmask));
      } else {
         this.dataTracker.set(HORSE_FLAGS, (byte)(b & ~bitmask));
      }

   }

   public boolean isTame() {
      return this.getHorseFlag(TAMED_FLAG);
   }

   @Nullable
   public UUID getOwnerUuid() {
      return this.ownerUuid;
   }

   public void setOwnerUuid(@Nullable UUID ownerUuid) {
      this.ownerUuid = ownerUuid;
   }

   public boolean isInAir() {
      return this.inAir;
   }

   public void setTame(boolean tame) {
      this.setHorseFlag(TAMED_FLAG, tame);
   }

   public void setInAir(boolean inAir) {
      this.inAir = inAir;
   }

   protected void updateForLeashLength(float leashLength) {
      if (leashLength > 6.0F && this.isEatingGrass()) {
         this.setEatingGrass(false);
      }

   }

   public boolean isEatingGrass() {
      return this.getHorseFlag(EATING_GRASS_FLAG);
   }

   public boolean isAngry() {
      return this.getHorseFlag(ANGRY_FLAG);
   }

   public boolean isBred() {
      return this.getHorseFlag(BRED_FLAG);
   }

   public void setBred(boolean bred) {
      this.setHorseFlag(BRED_FLAG, bred);
   }

   public boolean canBeSaddled() {
      return this.isAlive() && !this.isBaby() && this.isTame();
   }

   public void saddle(@Nullable SoundCategory sound) {
      this.items.setStack(0, new ItemStack(Items.SADDLE));
   }

   public void equipHorseArmor(PlayerEntity player, ItemStack stack) {
      if (this.isHorseArmor(stack)) {
         this.items.setStack(1, new ItemStack(stack.getItem()));
         if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
         }
      }

   }

   public boolean isSaddled() {
      return this.getHorseFlag(SADDLED_FLAG);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int temper) {
      this.temper = temper;
   }

   public int addTemper(int difference) {
      int j = MathHelper.clamp(this.getTemper() + difference, 0, this.getMaxTemper());
      this.setTemper(j);
      return j;
   }

   public boolean isPushable() {
      return !this.hasPassengers();
   }

   private void playEatingAnimation() {
      this.setEating();
      if (!this.isSilent()) {
         SoundEvent lv = this.getEatSound();
         if (lv != null) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), lv, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      if (fallDistance > 1.0F) {
         this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4F, 1.0F);
      }

      int i = this.computeFallDamage(fallDistance, damageMultiplier);
      if (i <= 0) {
         return false;
      } else {
         this.damage(damageSource, (float)i);
         if (this.hasPassengers()) {
            Iterator var5 = this.getPassengersDeep().iterator();

            while(var5.hasNext()) {
               Entity lv = (Entity)var5.next();
               lv.damage(damageSource, (float)i);
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
      return MathHelper.ceil((fallDistance * 0.5F - 3.0F) * damageMultiplier);
   }

   protected int getInventorySize() {
      return 2;
   }

   protected void onChestedStatusChanged() {
      SimpleInventory lv = this.items;
      this.items = new SimpleInventory(this.getInventorySize());
      if (lv != null) {
         lv.removeListener(this);
         int i = Math.min(lv.size(), this.items.size());

         for(int j = 0; j < i; ++j) {
            ItemStack lv2 = lv.getStack(j);
            if (!lv2.isEmpty()) {
               this.items.setStack(j, lv2.copy());
            }
         }
      }

      this.items.addListener(this);
      this.updateSaddle();
   }

   protected void updateSaddle() {
      if (!this.world.isClient) {
         this.setHorseFlag(SADDLED_FLAG, !this.items.getStack(0).isEmpty());
      }
   }

   public void onInventoryChanged(Inventory sender) {
      boolean bl = this.isSaddled();
      this.updateSaddle();
      if (this.age > 20 && !bl && this.isSaddled()) {
         this.playSound(this.getSaddleSound(), 0.5F, 1.0F);
      }

   }

   public double getJumpStrength() {
      return this.getAttributeValue(EntityAttributes.HORSE_JUMP_STRENGTH);
   }

   public boolean damage(DamageSource source, float amount) {
      boolean bl = super.damage(source, amount);
      if (bl && this.random.nextInt(3) == 0) {
         this.updateAnger();
      }

      return bl;
   }

   protected boolean shouldAmbientStand() {
      return true;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getAngrySound() {
      return null;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (!state.isLiquid()) {
         BlockState lv = this.world.getBlockState(pos.up());
         BlockSoundGroup lv2 = state.getSoundGroup();
         if (lv.isOf(Blocks.SNOW)) {
            lv2 = lv.getSoundGroup();
         }

         if (this.hasPassengers() && this.playExtraHorseSounds) {
            ++this.soundTicks;
            if (this.soundTicks > 5 && this.soundTicks % 3 == 0) {
               this.playWalkSound(lv2);
            } else if (this.soundTicks <= 5) {
               this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, lv2.getVolume() * 0.15F, lv2.getPitch());
            }
         } else if (this.isWooden(lv2)) {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, lv2.getVolume() * 0.15F, lv2.getPitch());
         } else {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP, lv2.getVolume() * 0.15F, lv2.getPitch());
         }

      }
   }

   private boolean isWooden(BlockSoundGroup soundGroup) {
      return soundGroup == BlockSoundGroup.WOOD || soundGroup == BlockSoundGroup.NETHER_WOOD || soundGroup == BlockSoundGroup.NETHER_STEM || soundGroup == BlockSoundGroup.CHERRY_WOOD || soundGroup == BlockSoundGroup.BAMBOO_WOOD;
   }

   protected void playWalkSound(BlockSoundGroup group) {
      this.playSound(SoundEvents.ENTITY_HORSE_GALLOP, group.getVolume() * 0.15F, group.getPitch());
   }

   public static DefaultAttributeContainer.Builder createBaseHorseAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.HORSE_JUMP_STRENGTH).add(EntityAttributes.GENERIC_MAX_HEALTH, 53.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22499999403953552);
   }

   public int getLimitPerChunk() {
      return 6;
   }

   public int getMaxTemper() {
      return 100;
   }

   protected float getSoundVolume() {
      return 0.8F;
   }

   public int getMinAmbientSoundDelay() {
      return 400;
   }

   public void openInventory(PlayerEntity player) {
      if (!this.world.isClient && (!this.hasPassengers() || this.hasPassenger(player)) && this.isTame()) {
         player.openHorseInventory(this, this.items);
      }

   }

   public ActionResult interactHorse(PlayerEntity player, ItemStack stack) {
      boolean bl = this.receiveFood(player, stack);
      if (!player.getAbilities().creativeMode) {
         stack.decrement(1);
      }

      if (this.world.isClient) {
         return ActionResult.CONSUME;
      } else {
         return bl ? ActionResult.SUCCESS : ActionResult.PASS;
      }
   }

   protected boolean receiveFood(PlayerEntity player, ItemStack item) {
      boolean bl = false;
      float f = 0.0F;
      int i = 0;
      int j = 0;
      if (item.isOf(Items.WHEAT)) {
         f = 2.0F;
         i = 20;
         j = 3;
      } else if (item.isOf(Items.SUGAR)) {
         f = 1.0F;
         i = 30;
         j = 3;
      } else if (item.isOf(Blocks.HAY_BLOCK.asItem())) {
         f = 20.0F;
         i = 180;
      } else if (item.isOf(Items.APPLE)) {
         f = 3.0F;
         i = 60;
         j = 3;
      } else if (item.isOf(Items.GOLDEN_CARROT)) {
         f = 4.0F;
         i = 60;
         j = 5;
         if (!this.world.isClient && this.isTame() && this.getBreedingAge() == 0 && !this.isInLove()) {
            bl = true;
            this.lovePlayer(player);
         }
      } else if (item.isOf(Items.GOLDEN_APPLE) || item.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
         f = 10.0F;
         i = 240;
         j = 10;
         if (!this.world.isClient && this.isTame() && this.getBreedingAge() == 0 && !this.isInLove()) {
            bl = true;
            this.lovePlayer(player);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         bl = true;
      }

      if (this.isBaby() && i > 0) {
         this.world.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
         if (!this.world.isClient) {
            this.growUp(i);
         }

         bl = true;
      }

      if (j > 0 && (bl || !this.isTame()) && this.getTemper() < this.getMaxTemper()) {
         bl = true;
         if (!this.world.isClient) {
            this.addTemper(j);
         }
      }

      if (bl) {
         this.playEatingAnimation();
         this.emitGameEvent(GameEvent.EAT);
      }

      return bl;
   }

   protected void putPlayerOnBack(PlayerEntity player) {
      this.setEatingGrass(false);
      this.setAngry(false);
      if (!this.world.isClient) {
         player.setYaw(this.getYaw());
         player.setPitch(this.getPitch());
         player.startRiding(this);
      }

   }

   public boolean isImmobile() {
      return super.isImmobile() && this.hasPassengers() && this.isSaddled() || this.isEatingGrass() || this.isAngry();
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   private void wagTail() {
      this.tailWagTicks = 1;
   }

   protected void dropInventory() {
      super.dropInventory();
      if (this.items != null) {
         for(int i = 0; i < this.items.size(); ++i) {
            ItemStack lv = this.items.getStack(i);
            if (!lv.isEmpty() && !EnchantmentHelper.hasVanishingCurse(lv)) {
               this.dropStack(lv);
            }
         }

      }
   }

   public void tickMovement() {
      if (this.random.nextInt(200) == 0) {
         this.wagTail();
      }

      super.tickMovement();
      if (!this.world.isClient && this.isAlive()) {
         if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F);
         }

         if (this.eatsGrass()) {
            if (!this.isEatingGrass() && !this.hasPassengers() && this.random.nextInt(300) == 0 && this.world.getBlockState(this.getBlockPos().down()).isOf(Blocks.GRASS_BLOCK)) {
               this.setEatingGrass(true);
            }

            if (this.isEatingGrass() && ++this.eatingGrassTicks > 50) {
               this.eatingGrassTicks = 0;
               this.setEatingGrass(false);
            }
         }

         this.walkToParent();
      }
   }

   protected void walkToParent() {
      if (this.isBred() && this.isBaby() && !this.isEatingGrass()) {
         LivingEntity lv = this.world.getClosestEntity(AbstractHorseEntity.class, PARENT_HORSE_PREDICATE, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().expand(16.0));
         if (lv != null && this.squaredDistanceTo(lv) > 4.0) {
            this.navigation.findPathTo((Entity)lv, 0);
         }
      }

   }

   public boolean eatsGrass() {
      return true;
   }

   public void tick() {
      super.tick();
      if (this.eatingTicks > 0 && ++this.eatingTicks > 30) {
         this.eatingTicks = 0;
         this.setHorseFlag(EATING_FLAG, false);
      }

      if (this.canMoveVoluntarily() && this.angryTicks > 0 && ++this.angryTicks > 20) {
         this.angryTicks = 0;
         this.setAngry(false);
      }

      if (this.tailWagTicks > 0 && ++this.tailWagTicks > 8) {
         this.tailWagTicks = 0;
      }

      if (this.field_6958 > 0) {
         ++this.field_6958;
         if (this.field_6958 > 300) {
            this.field_6958 = 0;
         }
      }

      this.lastEatingGrassAnimationProgress = this.eatingGrassAnimationProgress;
      if (this.isEatingGrass()) {
         this.eatingGrassAnimationProgress += (1.0F - this.eatingGrassAnimationProgress) * 0.4F + 0.05F;
         if (this.eatingGrassAnimationProgress > 1.0F) {
            this.eatingGrassAnimationProgress = 1.0F;
         }
      } else {
         this.eatingGrassAnimationProgress += (0.0F - this.eatingGrassAnimationProgress) * 0.4F - 0.05F;
         if (this.eatingGrassAnimationProgress < 0.0F) {
            this.eatingGrassAnimationProgress = 0.0F;
         }
      }

      this.lastAngryAnimationProgress = this.angryAnimationProgress;
      if (this.isAngry()) {
         this.eatingGrassAnimationProgress = 0.0F;
         this.lastEatingGrassAnimationProgress = this.eatingGrassAnimationProgress;
         this.angryAnimationProgress += (1.0F - this.angryAnimationProgress) * 0.4F + 0.05F;
         if (this.angryAnimationProgress > 1.0F) {
            this.angryAnimationProgress = 1.0F;
         }
      } else {
         this.jumping = false;
         this.angryAnimationProgress += (0.8F * this.angryAnimationProgress * this.angryAnimationProgress * this.angryAnimationProgress - this.angryAnimationProgress) * 0.6F - 0.05F;
         if (this.angryAnimationProgress < 0.0F) {
            this.angryAnimationProgress = 0.0F;
         }
      }

      this.lastEatingAnimationProgress = this.eatingAnimationProgress;
      if (this.getHorseFlag(EATING_FLAG)) {
         this.eatingAnimationProgress += (1.0F - this.eatingAnimationProgress) * 0.7F + 0.05F;
         if (this.eatingAnimationProgress > 1.0F) {
            this.eatingAnimationProgress = 1.0F;
         }
      } else {
         this.eatingAnimationProgress += (0.0F - this.eatingAnimationProgress) * 0.7F - 0.05F;
         if (this.eatingAnimationProgress < 0.0F) {
            this.eatingAnimationProgress = 0.0F;
         }
      }

   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      if (!this.hasPassengers() && !this.isBaby()) {
         if (this.isTame() && player.shouldCancelInteraction()) {
            this.openInventory(player);
            return ActionResult.success(this.world.isClient);
         } else {
            ItemStack lv = player.getStackInHand(hand);
            if (!lv.isEmpty()) {
               ActionResult lv2 = lv.useOnEntity(player, this, hand);
               if (lv2.isAccepted()) {
                  return lv2;
               }

               if (this.hasArmorSlot() && this.isHorseArmor(lv) && !this.hasArmorInSlot()) {
                  this.equipHorseArmor(player, lv);
                  return ActionResult.success(this.world.isClient);
               }
            }

            this.putPlayerOnBack(player);
            return ActionResult.success(this.world.isClient);
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   private void setEating() {
      if (!this.world.isClient) {
         this.eatingTicks = 1;
         this.setHorseFlag(EATING_FLAG, true);
      }

   }

   public void setEatingGrass(boolean eatingGrass) {
      this.setHorseFlag(EATING_GRASS_FLAG, eatingGrass);
   }

   public void setAngry(boolean angry) {
      if (angry) {
         this.setEatingGrass(false);
      }

      this.setHorseFlag(ANGRY_FLAG, angry);
   }

   @Nullable
   public SoundEvent getAmbientStandSound() {
      return this.getAmbientSound();
   }

   public void updateAnger() {
      if (this.shouldAmbientStand() && this.canMoveVoluntarily()) {
         this.angryTicks = 1;
         this.setAngry(true);
      }

   }

   public void playAngrySound() {
      if (!this.isAngry()) {
         this.updateAnger();
         SoundEvent lv = this.getAngrySound();
         if (lv != null) {
            this.playSound(lv, this.getSoundVolume(), this.getSoundPitch());
         }
      }

   }

   public boolean bondWithPlayer(PlayerEntity player) {
      this.setOwnerUuid(player.getUuid());
      this.setTame(true);
      if (player instanceof ServerPlayerEntity) {
         Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, this);
      }

      this.world.sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
      return true;
   }

   protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
      super.tickControlled(controllingPlayer, movementInput);
      Vec2f lv = this.getControlledRotation(controllingPlayer);
      this.setRotation(lv.y, lv.x);
      this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
      if (this.isLogicalSideForUpdatingMovement()) {
         if (movementInput.z <= 0.0) {
            this.soundTicks = 0;
         }

         if (this.onGround) {
            this.setInAir(false);
            if (this.jumpStrength > 0.0F && !this.isInAir()) {
               this.jump(this.jumpStrength, movementInput);
            }

            this.jumpStrength = 0.0F;
         }
      }

   }

   protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
      return new Vec2f(controllingPassenger.getPitch() * 0.5F, controllingPassenger.getYaw());
   }

   protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
      if (this.onGround && this.jumpStrength == 0.0F && this.isAngry() && !this.jumping) {
         return Vec3d.ZERO;
      } else {
         float f = controllingPlayer.sidewaysSpeed * 0.5F;
         float g = controllingPlayer.forwardSpeed;
         if (g <= 0.0F) {
            g *= 0.25F;
         }

         return new Vec3d((double)f, 0.0, (double)g);
      }
   }

   protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
   }

   protected void jump(float strength, Vec3d movementInput) {
      double d = this.getJumpStrength() * (double)strength * (double)this.getJumpVelocityMultiplier();
      double e = d + this.getJumpBoostVelocityModifier();
      Vec3d lv = this.getVelocity();
      this.setVelocity(lv.x, e, lv.z);
      this.setInAir(true);
      this.velocityDirty = true;
      if (movementInput.z > 0.0) {
         float g = MathHelper.sin(this.getYaw() * 0.017453292F);
         float h = MathHelper.cos(this.getYaw() * 0.017453292F);
         this.setVelocity(this.getVelocity().add((double)(-0.4F * g * strength), 0.0, (double)(0.4F * h * strength)));
      }

   }

   protected void playJumpSound() {
      this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4F, 1.0F);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("EatingHaystack", this.isEatingGrass());
      nbt.putBoolean("Bred", this.isBred());
      nbt.putInt("Temper", this.getTemper());
      nbt.putBoolean("Tame", this.isTame());
      if (this.getOwnerUuid() != null) {
         nbt.putUuid("Owner", this.getOwnerUuid());
      }

      if (!this.items.getStack(0).isEmpty()) {
         nbt.put("SaddleItem", this.items.getStack(0).writeNbt(new NbtCompound()));
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setEatingGrass(nbt.getBoolean("EatingHaystack"));
      this.setBred(nbt.getBoolean("Bred"));
      this.setTemper(nbt.getInt("Temper"));
      this.setTame(nbt.getBoolean("Tame"));
      UUID uUID;
      if (nbt.containsUuid("Owner")) {
         uUID = nbt.getUuid("Owner");
      } else {
         String string = nbt.getString("Owner");
         uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
      }

      if (uUID != null) {
         this.setOwnerUuid(uUID);
      }

      if (nbt.contains("SaddleItem", NbtElement.COMPOUND_TYPE)) {
         ItemStack lv = ItemStack.fromNbt(nbt.getCompound("SaddleItem"));
         if (lv.isOf(Items.SADDLE)) {
            this.items.setStack(0, lv);
         }
      }

      this.updateSaddle();
   }

   public boolean canBreedWith(AnimalEntity other) {
      return false;
   }

   protected boolean canBreed() {
      return !this.hasPassengers() && !this.hasVehicle() && this.isTame() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return null;
   }

   protected void setChildAttributes(PassiveEntity other, AbstractHorseEntity child) {
      this.setChildAttribute(other, child, EntityAttributes.GENERIC_MAX_HEALTH, (double)MIN_HEALTH_BONUS, (double)MAX_HEALTH_BONUS);
      this.setChildAttribute(other, child, EntityAttributes.HORSE_JUMP_STRENGTH, (double)MIN_JUMP_STRENGTH_BONUS, (double)MAX_JUMP_STRENGTH_BONUS);
      this.setChildAttribute(other, child, EntityAttributes.GENERIC_MOVEMENT_SPEED, (double)MIN_MOVEMENT_SPEED_BONUS, (double)MAX_MOVEMENT_SPEED_BONUS);
   }

   private void setChildAttribute(PassiveEntity other, AbstractHorseEntity child, EntityAttribute attribute, double min, double max) {
      double f = calculateAttributeBaseValue(this.getAttributeBaseValue(attribute), other.getAttributeBaseValue(attribute), min, max, this.random);
      child.getAttributeInstance(attribute).setBaseValue(f);
   }

   static double calculateAttributeBaseValue(double parentBase, double otherParentBase, double min, double max, Random random) {
      if (max <= min) {
         throw new IllegalArgumentException("Incorrect range for an attribute");
      } else {
         parentBase = MathHelper.clamp(parentBase, min, max);
         otherParentBase = MathHelper.clamp(otherParentBase, min, max);
         double h = 0.15 * (max - min);
         double i = Math.abs(parentBase - otherParentBase) + h * 2.0;
         double j = (parentBase + otherParentBase) / 2.0;
         double k = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0 - 0.5;
         double l = j + i * k;
         double m;
         if (l > max) {
            m = l - max;
            return max - m;
         } else if (l < min) {
            m = min - l;
            return min + m;
         } else {
            return l;
         }
      }
   }

   public float getEatingGrassAnimationProgress(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastEatingGrassAnimationProgress, this.eatingGrassAnimationProgress);
   }

   public float getAngryAnimationProgress(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastAngryAnimationProgress, this.angryAnimationProgress);
   }

   public float getEatingAnimationProgress(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastEatingAnimationProgress, this.eatingAnimationProgress);
   }

   public void setJumpStrength(int strength) {
      if (this.isSaddled()) {
         if (strength < 0) {
            strength = 0;
         } else {
            this.jumping = true;
            this.updateAnger();
         }

         if (strength >= 90) {
            this.jumpStrength = 1.0F;
         } else {
            this.jumpStrength = 0.4F + 0.4F * (float)strength / 90.0F;
         }

      }
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void startJumping(int height) {
      this.jumping = true;
      this.updateAnger();
      this.playJumpSound();
   }

   public void stopJumping() {
   }

   protected void spawnPlayerReactionParticles(boolean positive) {
      ParticleEffect lv = positive ? ParticleTypes.HEART : ParticleTypes.SMOKE;

      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.world.addParticle(lv, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
         this.spawnPlayerReactionParticles(true);
      } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
         this.spawnPlayerReactionParticles(false);
      } else {
         super.handleStatus(status);
      }

   }

   public void updatePassengerPosition(Entity passenger) {
      super.updatePassengerPosition(passenger);
      if (this.lastAngryAnimationProgress > 0.0F) {
         float f = MathHelper.sin(this.bodyYaw * 0.017453292F);
         float g = MathHelper.cos(this.bodyYaw * 0.017453292F);
         float h = 0.7F * this.lastAngryAnimationProgress;
         float i = 0.15F * this.lastAngryAnimationProgress;
         passenger.setPosition(this.getX() + (double)(h * f), this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset() + (double)i, this.getZ() - (double)(h * g));
         if (passenger instanceof LivingEntity) {
            ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
         }
      }

   }

   protected static float getChildHealthBonus(IntUnaryOperator randomIntGetter) {
      return 15.0F + (float)randomIntGetter.applyAsInt(8) + (float)randomIntGetter.applyAsInt(9);
   }

   protected static double getChildJumpStrengthBonus(DoubleSupplier randomDoubleGetter) {
      return 0.4000000059604645 + randomDoubleGetter.getAsDouble() * 0.2 + randomDoubleGetter.getAsDouble() * 0.2 + randomDoubleGetter.getAsDouble() * 0.2;
   }

   protected static double getChildMovementSpeedBonus(DoubleSupplier randomDoubleGetter) {
      return (0.44999998807907104 + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3) * 0.25;
   }

   public boolean isClimbing() {
      return false;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.95F;
   }

   public boolean hasArmorSlot() {
      return false;
   }

   public boolean hasArmorInSlot() {
      return !this.getEquippedStack(EquipmentSlot.CHEST).isEmpty();
   }

   public boolean isHorseArmor(ItemStack item) {
      return false;
   }

   private StackReference createInventoryStackReference(final int slot, final Predicate predicate) {
      return new StackReference() {
         public ItemStack get() {
            return AbstractHorseEntity.this.items.getStack(slot);
         }

         public boolean set(ItemStack stack) {
            if (!predicate.test(stack)) {
               return false;
            } else {
               AbstractHorseEntity.this.items.setStack(slot, stack);
               AbstractHorseEntity.this.updateSaddle();
               return true;
            }
         }
      };
   }

   public StackReference getStackReference(int mappedIndex) {
      int j = mappedIndex - 400;
      if (j >= 0 && j < 2 && j < this.items.size()) {
         if (j == 0) {
            return this.createInventoryStackReference(j, (stack) -> {
               return stack.isEmpty() || stack.isOf(Items.SADDLE);
            });
         }

         if (j == 1) {
            if (!this.hasArmorSlot()) {
               return StackReference.EMPTY;
            }

            return this.createInventoryStackReference(j, (stack) -> {
               return stack.isEmpty() || this.isHorseArmor(stack);
            });
         }
      }

      int k = mappedIndex - 500 + 2;
      return k >= 2 && k < this.items.size() ? StackReference.of(this.items, k) : super.getStackReference(mappedIndex);
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var3 = this.getFirstPassenger();
      if (var3 instanceof MobEntity lv) {
         return lv;
      } else {
         if (this.isSaddled()) {
            var3 = this.getFirstPassenger();
            if (var3 instanceof PlayerEntity) {
               PlayerEntity lv2 = (PlayerEntity)var3;
               return lv2;
            }
         }

         return null;
      }
   }

   @Nullable
   private Vec3d locateSafeDismountingPos(Vec3d offset, LivingEntity passenger) {
      double d = this.getX() + offset.x;
      double e = this.getBoundingBox().minY;
      double f = this.getZ() + offset.z;
      BlockPos.Mutable lv = new BlockPos.Mutable();
      UnmodifiableIterator var10 = passenger.getPoses().iterator();

      while(var10.hasNext()) {
         EntityPose lv2 = (EntityPose)var10.next();
         lv.set(d, e, f);
         double g = this.getBoundingBox().maxY + 0.75;

         while(true) {
            double h = this.world.getDismountHeight(lv);
            if ((double)lv.getY() + h > g) {
               break;
            }

            if (Dismounting.canDismountInBlock(h)) {
               Box lv3 = passenger.getBoundingBox(lv2);
               Vec3d lv4 = new Vec3d(d, (double)lv.getY() + h, f);
               if (Dismounting.canPlaceEntityAt(this.world, passenger, lv3.offset(lv4))) {
                  passenger.setPose(lv2);
                  return lv4;
               }
            }

            lv.move(Direction.UP);
            if (!((double)lv.getY() < g)) {
               break;
            }
         }
      }

      return null;
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Vec3d lv = getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.0F : -90.0F));
      Vec3d lv2 = this.locateSafeDismountingPos(lv, passenger);
      if (lv2 != null) {
         return lv2;
      } else {
         Vec3d lv3 = getPassengerDismountOffset((double)this.getWidth(), (double)passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.0F : -90.0F));
         Vec3d lv4 = this.locateSafeDismountingPos(lv3, passenger);
         return lv4 != null ? lv4 : this.getPos();
      }
   }

   protected void initAttributes(Random random) {
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(0.2F);
      }

      this.initAttributes(world.getRandom());
      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   public boolean areInventoriesDifferent(Inventory inventory) {
      return this.items != inventory;
   }

   public int getMinAmbientStandDelay() {
      return this.getMinAmbientSoundDelay();
   }

   // $FF: synthetic method
   public EntityView method_48926() {
      return super.getWorld();
   }

   static {
      PARENT_HORSE_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(16.0).ignoreVisibility().setPredicate(IS_BRED_HORSE);
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
      HORSE_FLAGS = DataTracker.registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.BYTE);
   }
}
