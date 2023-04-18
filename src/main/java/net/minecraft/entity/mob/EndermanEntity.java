package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EndermanEntity extends HostileEntity implements Angerable {
   private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
   private static final EntityAttributeModifier ATTACKING_SPEED_BOOST;
   private static final int field_30462 = 400;
   private static final int field_30461 = 600;
   private static final TrackedData CARRIED_BLOCK;
   private static final TrackedData ANGRY;
   private static final TrackedData PROVOKED;
   private int lastAngrySoundAge = Integer.MIN_VALUE;
   private int ageWhenTargetSet;
   private static final UniformIntProvider ANGER_TIME_RANGE;
   private int angerTime;
   @Nullable
   private UUID angryAt;

   public EndermanEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setStepHeight(1.0F);
      this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new ChasePlayerGoal(this));
      this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0, 0.0F));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.goalSelector.add(10, new PlaceBlockGoal(this));
      this.goalSelector.add(11, new PickUpBlockGoal(this));
      this.targetSelector.add(1, new TeleportTowardsPlayerGoal(this, this::shouldAngerAt));
      this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
      this.targetSelector.add(3, new ActiveTargetGoal(this, EndermiteEntity.class, true, false));
      this.targetSelector.add(4, new UniversalAngerGoal(this, false));
   }

   public static DefaultAttributeContainer.Builder createEndermanAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0);
   }

   public void setTarget(@Nullable LivingEntity target) {
      super.setTarget(target);
      EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (target == null) {
         this.ageWhenTargetSet = 0;
         this.dataTracker.set(ANGRY, false);
         this.dataTracker.set(PROVOKED, false);
         lv.removeModifier(ATTACKING_SPEED_BOOST);
      } else {
         this.ageWhenTargetSet = this.age;
         this.dataTracker.set(ANGRY, true);
         if (!lv.hasModifier(ATTACKING_SPEED_BOOST)) {
            lv.addTemporaryModifier(ATTACKING_SPEED_BOOST);
         }
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CARRIED_BLOCK, Optional.empty());
      this.dataTracker.startTracking(ANGRY, false);
      this.dataTracker.startTracking(PROVOKED, false);
   }

   public void chooseRandomAngerTime() {
      this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
   }

   public void setAngerTime(int angerTime) {
      this.angerTime = angerTime;
   }

   public int getAngerTime() {
      return this.angerTime;
   }

   public void setAngryAt(@Nullable UUID angryAt) {
      this.angryAt = angryAt;
   }

   @Nullable
   public UUID getAngryAt() {
      return this.angryAt;
   }

   public void playAngrySound() {
      if (this.age >= this.lastAngrySoundAge + 400) {
         this.lastAngrySoundAge = this.age;
         if (!this.isSilent()) {
            this.world.playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ENDERMAN_STARE, this.getSoundCategory(), 2.5F, 1.0F, false);
         }
      }

   }

   public void onTrackedDataSet(TrackedData data) {
      if (ANGRY.equals(data) && this.isProvoked() && this.world.isClient) {
         this.playAngrySound();
      }

      super.onTrackedDataSet(data);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      BlockState lv = this.getCarriedBlock();
      if (lv != null) {
         nbt.put("carriedBlockState", NbtHelper.fromBlockState(lv));
      }

      this.writeAngerToNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      BlockState lv = null;
      if (nbt.contains("carriedBlockState", NbtElement.COMPOUND_TYPE)) {
         lv = NbtHelper.toBlockState(this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("carriedBlockState"));
         if (lv.isAir()) {
            lv = null;
         }
      }

      this.setCarriedBlock(lv);
      this.readAngerFromNbt(this.world, nbt);
   }

   boolean isPlayerStaring(PlayerEntity player) {
      ItemStack lv = (ItemStack)player.getInventory().armor.get(3);
      if (lv.isOf(Blocks.CARVED_PUMPKIN.asItem())) {
         return false;
      } else {
         Vec3d lv2 = player.getRotationVec(1.0F).normalize();
         Vec3d lv3 = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
         double d = lv3.length();
         lv3 = lv3.normalize();
         double e = lv2.dotProduct(lv3);
         return e > 1.0 - 0.025 / d ? player.canSee(this) : false;
      }
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 2.55F;
   }

   public void tickMovement() {
      if (this.world.isClient) {
         for(int i = 0; i < 2; ++i) {
            this.world.addParticle(ParticleTypes.PORTAL, this.getParticleX(0.5), this.getRandomBodyY() - 0.25, this.getParticleZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
         }
      }

      this.jumping = false;
      if (!this.world.isClient) {
         this.tickAngerLogic((ServerWorld)this.world, true);
      }

      super.tickMovement();
   }

   public boolean hurtByWater() {
      return true;
   }

   protected void mobTick() {
      if (this.world.isDay() && this.age >= this.ageWhenTargetSet + 600) {
         float f = this.getBrightnessAtEyes();
         if (f > 0.5F && this.world.isSkyVisible(this.getBlockPos()) && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
            this.setTarget((LivingEntity)null);
            this.teleportRandomly();
         }
      }

      super.mobTick();
   }

   protected boolean teleportRandomly() {
      if (!this.world.isClient() && this.isAlive()) {
         double d = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
         double e = this.getY() + (double)(this.random.nextInt(64) - 32);
         double f = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
         return this.teleportTo(d, e, f);
      } else {
         return false;
      }
   }

   boolean teleportTo(Entity entity) {
      Vec3d lv = new Vec3d(this.getX() - entity.getX(), this.getBodyY(0.5) - entity.getEyeY(), this.getZ() - entity.getZ());
      lv = lv.normalize();
      double d = 16.0;
      double e = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - lv.x * 16.0;
      double f = this.getY() + (double)(this.random.nextInt(16) - 8) - lv.y * 16.0;
      double g = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - lv.z * 16.0;
      return this.teleportTo(e, f, g);
   }

   private boolean teleportTo(double x, double y, double z) {
      BlockPos.Mutable lv = new BlockPos.Mutable(x, y, z);

      while(lv.getY() > this.world.getBottomY() && !this.world.getBlockState(lv).getMaterial().blocksMovement()) {
         lv.move(Direction.DOWN);
      }

      BlockState lv2 = this.world.getBlockState(lv);
      boolean bl = lv2.getMaterial().blocksMovement();
      boolean bl2 = lv2.getFluidState().isIn(FluidTags.WATER);
      if (bl && !bl2) {
         Vec3d lv3 = this.getPos();
         boolean bl3 = this.teleport(x, y, z, true);
         if (bl3) {
            this.world.emitGameEvent(GameEvent.TELEPORT, lv3, GameEvent.Emitter.of((Entity)this));
            if (!this.isSilent()) {
               this.world.playSound((PlayerEntity)null, this.prevX, this.prevY, this.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
               this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
         }

         return bl3;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isAngry() ? SoundEvents.ENTITY_ENDERMAN_SCREAM : SoundEvents.ENTITY_ENDERMAN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ENDERMAN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ENDERMAN_DEATH;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      BlockState lv = this.getCarriedBlock();
      if (lv != null) {
         ItemStack lv2 = new ItemStack(Items.DIAMOND_AXE);
         lv2.addEnchantment(Enchantments.SILK_TOUCH, 1);
         LootContext.Builder lv3 = (new LootContext.Builder((ServerWorld)this.world)).random(this.world.getRandom()).parameter(LootContextParameters.ORIGIN, this.getPos()).parameter(LootContextParameters.TOOL, lv2).optionalParameter(LootContextParameters.THIS_ENTITY, this);
         List list = lv.getDroppedStacks(lv3);
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            ItemStack lv4 = (ItemStack)var8.next();
            this.dropStack(lv4);
         }
      }

   }

   public void setCarriedBlock(@Nullable BlockState state) {
      this.dataTracker.set(CARRIED_BLOCK, Optional.ofNullable(state));
   }

   @Nullable
   public BlockState getCarriedBlock() {
      return (BlockState)((Optional)this.dataTracker.get(CARRIED_BLOCK)).orElse((Object)null);
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         boolean bl = source.getSource() instanceof PotionEntity;
         boolean bl2;
         if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && !bl) {
            bl2 = super.damage(source, amount);
            if (!this.world.isClient() && !(source.getAttacker() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
               this.teleportRandomly();
            }

            return bl2;
         } else {
            bl2 = bl && this.damageFromPotion(source, (PotionEntity)source.getSource(), amount);

            for(int i = 0; i < 64; ++i) {
               if (this.teleportRandomly()) {
                  return true;
               }
            }

            return bl2;
         }
      }
   }

   private boolean damageFromPotion(DamageSource source, PotionEntity potion, float amount) {
      ItemStack lv = potion.getStack();
      Potion lv2 = PotionUtil.getPotion(lv);
      List list = PotionUtil.getPotionEffects(lv);
      boolean bl = lv2 == Potions.WATER && list.isEmpty();
      return bl ? super.damage(source, amount) : false;
   }

   public boolean isAngry() {
      return (Boolean)this.dataTracker.get(ANGRY);
   }

   public boolean isProvoked() {
      return (Boolean)this.dataTracker.get(PROVOKED);
   }

   public void setProvoked() {
      this.dataTracker.set(PROVOKED, true);
   }

   public boolean cannotDespawn() {
      return super.cannotDespawn() || this.getCarriedBlock() != null;
   }

   static {
      ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.15000000596046448, EntityAttributeModifier.Operation.ADDITION);
      CARRIED_BLOCK = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
      ANGRY = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      PROVOKED = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
   }

   static class ChasePlayerGoal extends Goal {
      private final EndermanEntity enderman;
      @Nullable
      private LivingEntity target;

      public ChasePlayerGoal(EndermanEntity enderman) {
         this.enderman = enderman;
         this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
      }

      public boolean canStart() {
         this.target = this.enderman.getTarget();
         if (!(this.target instanceof PlayerEntity)) {
            return false;
         } else {
            double d = this.target.squaredDistanceTo(this.enderman);
            return d > 256.0 ? false : this.enderman.isPlayerStaring((PlayerEntity)this.target);
         }
      }

      public void start() {
         this.enderman.getNavigation().stop();
      }

      public void tick() {
         this.enderman.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
      }
   }

   static class PlaceBlockGoal extends Goal {
      private final EndermanEntity enderman;

      public PlaceBlockGoal(EndermanEntity enderman) {
         this.enderman = enderman;
      }

      public boolean canStart() {
         if (this.enderman.getCarriedBlock() == null) {
            return false;
         } else if (!this.enderman.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(toGoalTicks(2000)) == 0;
         }
      }

      public void tick() {
         Random lv = this.enderman.getRandom();
         World lv2 = this.enderman.world;
         int i = MathHelper.floor(this.enderman.getX() - 1.0 + lv.nextDouble() * 2.0);
         int j = MathHelper.floor(this.enderman.getY() + lv.nextDouble() * 2.0);
         int k = MathHelper.floor(this.enderman.getZ() - 1.0 + lv.nextDouble() * 2.0);
         BlockPos lv3 = new BlockPos(i, j, k);
         BlockState lv4 = lv2.getBlockState(lv3);
         BlockPos lv5 = lv3.down();
         BlockState lv6 = lv2.getBlockState(lv5);
         BlockState lv7 = this.enderman.getCarriedBlock();
         if (lv7 != null) {
            lv7 = Block.postProcessState(lv7, this.enderman.world, lv3);
            if (this.canPlaceOn(lv2, lv3, lv7, lv4, lv6, lv5)) {
               lv2.setBlockState(lv3, lv7, Block.NOTIFY_ALL);
               lv2.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(this.enderman, lv7));
               this.enderman.setCarriedBlock((BlockState)null);
            }

         }
      }

      private boolean canPlaceOn(World world, BlockPos posAbove, BlockState carriedState, BlockState stateAbove, BlockState state, BlockPos pos) {
         return stateAbove.isAir() && !state.isAir() && !state.isOf(Blocks.BEDROCK) && state.isFullCube(world, pos) && carriedState.canPlaceAt(world, posAbove) && world.getOtherEntities(this.enderman, Box.from(Vec3d.of(posAbove))).isEmpty();
      }
   }

   static class PickUpBlockGoal extends Goal {
      private final EndermanEntity enderman;

      public PickUpBlockGoal(EndermanEntity enderman) {
         this.enderman = enderman;
      }

      public boolean canStart() {
         if (this.enderman.getCarriedBlock() != null) {
            return false;
         } else if (!this.enderman.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(toGoalTicks(20)) == 0;
         }
      }

      public void tick() {
         Random lv = this.enderman.getRandom();
         World lv2 = this.enderman.world;
         int i = MathHelper.floor(this.enderman.getX() - 2.0 + lv.nextDouble() * 4.0);
         int j = MathHelper.floor(this.enderman.getY() + lv.nextDouble() * 3.0);
         int k = MathHelper.floor(this.enderman.getZ() - 2.0 + lv.nextDouble() * 4.0);
         BlockPos lv3 = new BlockPos(i, j, k);
         BlockState lv4 = lv2.getBlockState(lv3);
         Vec3d lv5 = new Vec3d((double)this.enderman.getBlockX() + 0.5, (double)j + 0.5, (double)this.enderman.getBlockZ() + 0.5);
         Vec3d lv6 = new Vec3d((double)i + 0.5, (double)j + 0.5, (double)k + 0.5);
         BlockHitResult lv7 = lv2.raycast(new RaycastContext(lv5, lv6, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this.enderman));
         boolean bl = lv7.getBlockPos().equals(lv3);
         if (lv4.isIn(BlockTags.ENDERMAN_HOLDABLE) && bl) {
            lv2.removeBlock(lv3, false);
            lv2.emitGameEvent(GameEvent.BLOCK_DESTROY, lv3, GameEvent.Emitter.of(this.enderman, lv4));
            this.enderman.setCarriedBlock(lv4.getBlock().getDefaultState());
         }

      }
   }

   static class TeleportTowardsPlayerGoal extends ActiveTargetGoal {
      private final EndermanEntity enderman;
      @Nullable
      private PlayerEntity targetPlayer;
      private int lookAtPlayerWarmup;
      private int ticksSinceUnseenTeleport;
      private final TargetPredicate staringPlayerPredicate;
      private final TargetPredicate validTargetPredicate = TargetPredicate.createAttackable().ignoreVisibility();
      private final Predicate angerPredicate;

      public TeleportTowardsPlayerGoal(EndermanEntity enderman, @Nullable Predicate targetPredicate) {
         super(enderman, PlayerEntity.class, 10, false, false, targetPredicate);
         this.enderman = enderman;
         this.angerPredicate = (playerEntity) -> {
            return (enderman.isPlayerStaring((PlayerEntity)playerEntity) || enderman.shouldAngerAt(playerEntity)) && !enderman.hasPassengerDeep(playerEntity);
         };
         this.staringPlayerPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(this.angerPredicate);
      }

      public boolean canStart() {
         this.targetPlayer = this.enderman.world.getClosestPlayer(this.staringPlayerPredicate, this.enderman);
         return this.targetPlayer != null;
      }

      public void start() {
         this.lookAtPlayerWarmup = this.getTickCount(5);
         this.ticksSinceUnseenTeleport = 0;
         this.enderman.setProvoked();
      }

      public void stop() {
         this.targetPlayer = null;
         super.stop();
      }

      public boolean shouldContinue() {
         if (this.targetPlayer != null) {
            if (!this.angerPredicate.test(this.targetPlayer)) {
               return false;
            } else {
               this.enderman.lookAtEntity(this.targetPlayer, 10.0F, 10.0F);
               return true;
            }
         } else {
            if (this.targetEntity != null) {
               if (this.enderman.hasPassengerDeep(this.targetEntity)) {
                  return false;
               }

               if (this.validTargetPredicate.test(this.enderman, this.targetEntity)) {
                  return true;
               }
            }

            return super.shouldContinue();
         }
      }

      public void tick() {
         if (this.enderman.getTarget() == null) {
            super.setTargetEntity((LivingEntity)null);
         }

         if (this.targetPlayer != null) {
            if (--this.lookAtPlayerWarmup <= 0) {
               this.targetEntity = this.targetPlayer;
               this.targetPlayer = null;
               super.start();
            }
         } else {
            if (this.targetEntity != null && !this.enderman.hasVehicle()) {
               if (this.enderman.isPlayerStaring((PlayerEntity)this.targetEntity)) {
                  if (this.targetEntity.squaredDistanceTo(this.enderman) < 16.0) {
                     this.enderman.teleportRandomly();
                  }

                  this.ticksSinceUnseenTeleport = 0;
               } else if (this.targetEntity.squaredDistanceTo(this.enderman) > 256.0 && this.ticksSinceUnseenTeleport++ >= this.getTickCount(30) && this.enderman.teleportTo(this.targetEntity)) {
                  this.ticksSinceUnseenTeleport = 0;
               }
            }

            super.tick();
         }

      }
   }
}
