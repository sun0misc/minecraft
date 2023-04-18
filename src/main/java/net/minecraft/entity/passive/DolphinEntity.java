package net.minecraft.entity.passive;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.goal.BreatheAirGoal;
import net.minecraft.entity.ai.goal.ChaseBoatGoal;
import net.minecraft.entity.ai.goal.DolphinJumpGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveIntoWaterGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DolphinEntity extends WaterCreatureEntity {
   private static final TrackedData TREASURE_POS;
   private static final TrackedData HAS_FISH;
   private static final TrackedData MOISTNESS;
   static final TargetPredicate CLOSE_PLAYER_PREDICATE;
   public static final int MAX_AIR = 4800;
   private static final int MAX_MOISTNESS = 2400;
   public static final Predicate CAN_TAKE;

   public DolphinEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.lookControl = new YawAdjustingLookControl(this, 10);
      this.setCanPickUpLoot(true);
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setAir(this.getMaxAir());
      this.setPitch(0.0F);
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public boolean canBreatheInWater() {
      return false;
   }

   protected void tickWaterBreathingAir(int air) {
   }

   public void setTreasurePos(BlockPos treasurePos) {
      this.dataTracker.set(TREASURE_POS, treasurePos);
   }

   public BlockPos getTreasurePos() {
      return (BlockPos)this.dataTracker.get(TREASURE_POS);
   }

   public boolean hasFish() {
      return (Boolean)this.dataTracker.get(HAS_FISH);
   }

   public void setHasFish(boolean hasFish) {
      this.dataTracker.set(HAS_FISH, hasFish);
   }

   public int getMoistness() {
      return (Integer)this.dataTracker.get(MOISTNESS);
   }

   public void setMoistness(int moistness) {
      this.dataTracker.set(MOISTNESS, moistness);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TREASURE_POS, BlockPos.ORIGIN);
      this.dataTracker.startTracking(HAS_FISH, false);
      this.dataTracker.startTracking(MOISTNESS, 2400);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("TreasurePosX", this.getTreasurePos().getX());
      nbt.putInt("TreasurePosY", this.getTreasurePos().getY());
      nbt.putInt("TreasurePosZ", this.getTreasurePos().getZ());
      nbt.putBoolean("GotFish", this.hasFish());
      nbt.putInt("Moistness", this.getMoistness());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      int i = nbt.getInt("TreasurePosX");
      int j = nbt.getInt("TreasurePosY");
      int k = nbt.getInt("TreasurePosZ");
      this.setTreasurePos(new BlockPos(i, j, k));
      super.readCustomDataFromNbt(nbt);
      this.setHasFish(nbt.getBoolean("GotFish"));
      this.setMoistness(nbt.getInt("Moistness"));
   }

   protected void initGoals() {
      this.goalSelector.add(0, new BreatheAirGoal(this));
      this.goalSelector.add(0, new MoveIntoWaterGoal(this));
      this.goalSelector.add(1, new LeadToNearbyTreasureGoal(this));
      this.goalSelector.add(2, new SwimWithPlayerGoal(this, 4.0));
      this.goalSelector.add(4, new SwimAroundGoal(this, 1.0, 10));
      this.goalSelector.add(4, new LookAroundGoal(this));
      this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(5, new DolphinJumpGoal(this, 10));
      this.goalSelector.add(6, new MeleeAttackGoal(this, 1.2000000476837158, true));
      this.goalSelector.add(8, new PlayWithItemsGoal());
      this.goalSelector.add(8, new ChaseBoatGoal(this));
      this.goalSelector.add(9, new FleeEntityGoal(this, GuardianEntity.class, 8.0F, 1.0, 1.0));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{GuardianEntity.class})).setGroupRevenge());
   }

   public static DefaultAttributeContainer.Builder createDolphinAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.2000000476837158).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
   }

   protected EntityNavigation createNavigation(World world) {
      return new SwimNavigation(this, world);
   }

   public boolean tryAttack(Entity target) {
      boolean bl = target.damage(this.getDamageSources().mobAttack(this), (float)((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
      if (bl) {
         this.applyDamageEffects(this, target);
         this.playSound(SoundEvents.ENTITY_DOLPHIN_ATTACK, 1.0F, 1.0F);
      }

      return bl;
   }

   public int getMaxAir() {
      return 4800;
   }

   protected int getNextAirOnLand(int air) {
      return this.getMaxAir();
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.3F;
   }

   public int getMaxLookPitchChange() {
      return 1;
   }

   public int getMaxHeadRotation() {
      return 1;
   }

   protected boolean canStartRiding(Entity entity) {
      return true;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot lv = MobEntity.getPreferredEquipmentSlot(stack);
      if (!this.getEquippedStack(lv).isEmpty()) {
         return false;
      } else {
         return lv == EquipmentSlot.MAINHAND && super.canEquip(stack);
      }
   }

   protected void loot(ItemEntity item) {
      if (this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
         ItemStack lv = item.getStack();
         if (this.canPickupItem(lv)) {
            this.triggerItemPickedUpByEntityCriteria(item);
            this.equipStack(EquipmentSlot.MAINHAND, lv);
            this.updateDropChances(EquipmentSlot.MAINHAND);
            this.sendPickup(item, lv.getCount());
            item.discard();
         }
      }

   }

   public void tick() {
      super.tick();
      if (this.isAiDisabled()) {
         this.setAir(this.getMaxAir());
      } else {
         if (this.isWet()) {
            this.setMoistness(2400);
         } else {
            this.setMoistness(this.getMoistness() - 1);
            if (this.getMoistness() <= 0) {
               this.damage(this.getDamageSources().dryOut(), 1.0F);
            }

            if (this.onGround) {
               this.setVelocity(this.getVelocity().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.2F), 0.5, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.2F)));
               this.setYaw(this.random.nextFloat() * 360.0F);
               this.onGround = false;
               this.velocityDirty = true;
            }
         }

         if (this.world.isClient && this.isTouchingWater() && this.getVelocity().lengthSquared() > 0.03) {
            Vec3d lv = this.getRotationVec(0.0F);
            float f = MathHelper.cos(this.getYaw() * 0.017453292F) * 0.3F;
            float g = MathHelper.sin(this.getYaw() * 0.017453292F) * 0.3F;
            float h = 1.2F - this.random.nextFloat() * 0.7F;

            for(int i = 0; i < 2; ++i) {
               this.world.addParticle(ParticleTypes.DOLPHIN, this.getX() - lv.x * (double)h + (double)f, this.getY() - lv.y, this.getZ() - lv.z * (double)h + (double)g, 0.0, 0.0, 0.0);
               this.world.addParticle(ParticleTypes.DOLPHIN, this.getX() - lv.x * (double)h - (double)f, this.getY() - lv.y, this.getZ() - lv.z * (double)h - (double)g, 0.0, 0.0, 0.0);
            }
         }

      }
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_DOLPHIN_HAPPY_VILLAGER_PARTICLES) {
         this.spawnParticlesAround(ParticleTypes.HAPPY_VILLAGER);
      } else {
         super.handleStatus(status);
      }

   }

   private void spawnParticlesAround(ParticleEffect parameters) {
      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.01;
         double e = this.random.nextGaussian() * 0.01;
         double f = this.random.nextGaussian() * 0.01;
         this.world.addParticle(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 0.2, this.getParticleZ(1.0), d, e, f);
      }

   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (!lv.isEmpty() && lv.isIn(ItemTags.FISHES)) {
         if (!this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_DOLPHIN_EAT, 1.0F, 1.0F);
         }

         this.setHasFish(true);
         if (!player.getAbilities().creativeMode) {
            lv.decrement(1);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_DOLPHIN_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_DOLPHIN_DEATH;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return this.isTouchingWater() ? SoundEvents.ENTITY_DOLPHIN_AMBIENT_WATER : SoundEvents.ENTITY_DOLPHIN_AMBIENT;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_DOLPHIN_SPLASH;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_DOLPHIN_SWIM;
   }

   protected boolean isNearTarget() {
      BlockPos lv = this.getNavigation().getTargetPos();
      return lv != null ? lv.isWithinDistance(this.getPos(), 12.0) : false;
   }

   public void travel(Vec3d movementInput) {
      if (this.canMoveVoluntarily() && this.isTouchingWater()) {
         this.updateVelocity(this.getMovementSpeed(), movementInput);
         this.move(MovementType.SELF, this.getVelocity());
         this.setVelocity(this.getVelocity().multiply(0.9));
         if (this.getTarget() == null) {
            this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
         }
      } else {
         super.travel(movementInput);
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return true;
   }

   static {
      TREASURE_POS = DataTracker.registerData(DolphinEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
      HAS_FISH = DataTracker.registerData(DolphinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      MOISTNESS = DataTracker.registerData(DolphinEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CLOSE_PLAYER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(10.0).ignoreVisibility();
      CAN_TAKE = (item) -> {
         return !item.cannotPickup() && item.isAlive() && item.isTouchingWater();
      };
   }

   static class LeadToNearbyTreasureGoal extends Goal {
      private final DolphinEntity dolphin;
      private boolean noPathToStructure;

      LeadToNearbyTreasureGoal(DolphinEntity dolphin) {
         this.dolphin = dolphin;
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStop() {
         return false;
      }

      public boolean canStart() {
         return this.dolphin.hasFish() && this.dolphin.getAir() >= 100;
      }

      public boolean shouldContinue() {
         BlockPos lv = this.dolphin.getTreasurePos();
         return !BlockPos.ofFloored((double)lv.getX(), this.dolphin.getY(), (double)lv.getZ()).isWithinDistance(this.dolphin.getPos(), 4.0) && !this.noPathToStructure && this.dolphin.getAir() >= 100;
      }

      public void start() {
         if (this.dolphin.world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)this.dolphin.world;
            this.noPathToStructure = false;
            this.dolphin.getNavigation().stop();
            BlockPos lv2 = this.dolphin.getBlockPos();
            BlockPos lv3 = lv.locateStructure(StructureTags.DOLPHIN_LOCATED, lv2, 50, false);
            if (lv3 != null) {
               this.dolphin.setTreasurePos(lv3);
               lv.sendEntityStatus(this.dolphin, EntityStatuses.ADD_DOLPHIN_HAPPY_VILLAGER_PARTICLES);
            } else {
               this.noPathToStructure = true;
            }
         }
      }

      public void stop() {
         BlockPos lv = this.dolphin.getTreasurePos();
         if (BlockPos.ofFloored((double)lv.getX(), this.dolphin.getY(), (double)lv.getZ()).isWithinDistance(this.dolphin.getPos(), 4.0) || this.noPathToStructure) {
            this.dolphin.setHasFish(false);
         }

      }

      public void tick() {
         World lv = this.dolphin.world;
         if (this.dolphin.isNearTarget() || this.dolphin.getNavigation().isIdle()) {
            Vec3d lv2 = Vec3d.ofCenter(this.dolphin.getTreasurePos());
            Vec3d lv3 = NoPenaltyTargeting.findTo(this.dolphin, 16, 1, lv2, 0.39269909262657166);
            if (lv3 == null) {
               lv3 = NoPenaltyTargeting.findTo(this.dolphin, 8, 4, lv2, 1.5707963705062866);
            }

            if (lv3 != null) {
               BlockPos lv4 = BlockPos.ofFloored(lv3);
               if (!lv.getFluidState(lv4).isIn(FluidTags.WATER) || !lv.getBlockState(lv4).canPathfindThrough(lv, lv4, NavigationType.WATER)) {
                  lv3 = NoPenaltyTargeting.findTo(this.dolphin, 8, 5, lv2, 1.5707963705062866);
               }
            }

            if (lv3 == null) {
               this.noPathToStructure = true;
               return;
            }

            this.dolphin.getLookControl().lookAt(lv3.x, lv3.y, lv3.z, (float)(this.dolphin.getMaxHeadRotation() + 20), (float)this.dolphin.getMaxLookPitchChange());
            this.dolphin.getNavigation().startMovingTo(lv3.x, lv3.y, lv3.z, 1.3);
            if (lv.random.nextInt(this.getTickCount(80)) == 0) {
               lv.sendEntityStatus(this.dolphin, EntityStatuses.ADD_DOLPHIN_HAPPY_VILLAGER_PARTICLES);
            }
         }

      }
   }

   static class SwimWithPlayerGoal extends Goal {
      private final DolphinEntity dolphin;
      private final double speed;
      @Nullable
      private PlayerEntity closestPlayer;

      SwimWithPlayerGoal(DolphinEntity dolphin, double speed) {
         this.dolphin = dolphin;
         this.speed = speed;
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         this.closestPlayer = this.dolphin.world.getClosestPlayer(DolphinEntity.CLOSE_PLAYER_PREDICATE, this.dolphin);
         if (this.closestPlayer == null) {
            return false;
         } else {
            return this.closestPlayer.isSwimming() && this.dolphin.getTarget() != this.closestPlayer;
         }
      }

      public boolean shouldContinue() {
         return this.closestPlayer != null && this.closestPlayer.isSwimming() && this.dolphin.squaredDistanceTo(this.closestPlayer) < 256.0;
      }

      public void start() {
         this.closestPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 100), this.dolphin);
      }

      public void stop() {
         this.closestPlayer = null;
         this.dolphin.getNavigation().stop();
      }

      public void tick() {
         this.dolphin.getLookControl().lookAt(this.closestPlayer, (float)(this.dolphin.getMaxHeadRotation() + 20), (float)this.dolphin.getMaxLookPitchChange());
         if (this.dolphin.squaredDistanceTo(this.closestPlayer) < 6.25) {
            this.dolphin.getNavigation().stop();
         } else {
            this.dolphin.getNavigation().startMovingTo(this.closestPlayer, this.speed);
         }

         if (this.closestPlayer.isSwimming() && this.closestPlayer.world.random.nextInt(6) == 0) {
            this.closestPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 100), this.dolphin);
         }

      }
   }

   private class PlayWithItemsGoal extends Goal {
      private int nextPlayingTime;

      PlayWithItemsGoal() {
      }

      public boolean canStart() {
         if (this.nextPlayingTime > DolphinEntity.this.age) {
            return false;
         } else {
            List list = DolphinEntity.this.world.getEntitiesByClass(ItemEntity.class, DolphinEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), DolphinEntity.CAN_TAKE);
            return !list.isEmpty() || !DolphinEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
         }
      }

      public void start() {
         List list = DolphinEntity.this.world.getEntitiesByClass(ItemEntity.class, DolphinEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), DolphinEntity.CAN_TAKE);
         if (!list.isEmpty()) {
            DolphinEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
            DolphinEntity.this.playSound(SoundEvents.ENTITY_DOLPHIN_PLAY, 1.0F, 1.0F);
         }

         this.nextPlayingTime = 0;
      }

      public void stop() {
         ItemStack lv = DolphinEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (!lv.isEmpty()) {
            this.spitOutItem(lv);
            DolphinEntity.this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.nextPlayingTime = DolphinEntity.this.age + DolphinEntity.this.random.nextInt(100);
         }

      }

      public void tick() {
         List list = DolphinEntity.this.world.getEntitiesByClass(ItemEntity.class, DolphinEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), DolphinEntity.CAN_TAKE);
         ItemStack lv = DolphinEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
         if (!lv.isEmpty()) {
            this.spitOutItem(lv);
            DolphinEntity.this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         } else if (!list.isEmpty()) {
            DolphinEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158);
         }

      }

      private void spitOutItem(ItemStack stack) {
         if (!stack.isEmpty()) {
            double d = DolphinEntity.this.getEyeY() - 0.30000001192092896;
            ItemEntity lv = new ItemEntity(DolphinEntity.this.world, DolphinEntity.this.getX(), d, DolphinEntity.this.getZ(), stack);
            lv.setPickupDelay(40);
            lv.setThrower(DolphinEntity.this.getUuid());
            float f = 0.3F;
            float g = DolphinEntity.this.random.nextFloat() * 6.2831855F;
            float h = 0.02F * DolphinEntity.this.random.nextFloat();
            lv.setVelocity((double)(0.3F * -MathHelper.sin(DolphinEntity.this.getYaw() * 0.017453292F) * MathHelper.cos(DolphinEntity.this.getPitch() * 0.017453292F) + MathHelper.cos(g) * h), (double)(0.3F * MathHelper.sin(DolphinEntity.this.getPitch() * 0.017453292F) * 1.5F), (double)(0.3F * MathHelper.cos(DolphinEntity.this.getYaw() * 0.017453292F) * MathHelper.cos(DolphinEntity.this.getPitch() * 0.017453292F) + MathHelper.sin(g) * h));
            DolphinEntity.this.world.spawnEntity(lv);
         }
      }
   }
}
