package net.minecraft.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToRaidCenterGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

public abstract class RaiderEntity extends PatrolEntity {
   protected static final TrackedData CELEBRATING;
   static final Predicate OBTAINABLE_OMINOUS_BANNER_PREDICATE;
   @Nullable
   protected Raid raid;
   private int wave;
   private boolean ableToJoinRaid;
   private int outOfRaidCounter;

   protected RaiderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new PickupBannerAsLeaderGoal(this));
      this.goalSelector.add(3, new MoveToRaidCenterGoal(this));
      this.goalSelector.add(4, new AttackHomeGoal(this, 1.0499999523162842, 1));
      this.goalSelector.add(5, new CelebrateGoal(this));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CELEBRATING, false);
   }

   public abstract void addBonusForWave(int wave, boolean unused);

   public boolean canJoinRaid() {
      return this.ableToJoinRaid;
   }

   public void setAbleToJoinRaid(boolean ableToJoinRaid) {
      this.ableToJoinRaid = ableToJoinRaid;
   }

   public void tickMovement() {
      if (this.world instanceof ServerWorld && this.isAlive()) {
         Raid lv = this.getRaid();
         if (this.canJoinRaid()) {
            if (lv == null) {
               if (this.world.getTime() % 20L == 0L) {
                  Raid lv2 = ((ServerWorld)this.world).getRaidAt(this.getBlockPos());
                  if (lv2 != null && RaidManager.isValidRaiderFor(this, lv2)) {
                     lv2.addRaider(lv2.getGroupsSpawned(), this, (BlockPos)null, true);
                  }
               }
            } else {
               LivingEntity lv3 = this.getTarget();
               if (lv3 != null && (lv3.getType() == EntityType.PLAYER || lv3.getType() == EntityType.IRON_GOLEM)) {
                  this.despawnCounter = 0;
               }
            }
         }
      }

      super.tickMovement();
   }

   protected void updateDespawnCounter() {
      this.despawnCounter += 2;
   }

   public void onDeath(DamageSource damageSource) {
      if (this.world instanceof ServerWorld) {
         Entity lv = damageSource.getAttacker();
         Raid lv2 = this.getRaid();
         if (lv2 != null) {
            if (this.isPatrolLeader()) {
               lv2.removeLeader(this.getWave());
            }

            if (lv != null && lv.getType() == EntityType.PLAYER) {
               lv2.addHero(lv);
            }

            lv2.removeFromWave(this, false);
         }

         if (this.isPatrolLeader() && lv2 == null && ((ServerWorld)this.world).getRaidAt(this.getBlockPos()) == null) {
            ItemStack lv3 = this.getEquippedStack(EquipmentSlot.HEAD);
            PlayerEntity lv4 = null;
            if (lv instanceof PlayerEntity) {
               lv4 = (PlayerEntity)lv;
            } else if (lv instanceof WolfEntity) {
               WolfEntity lv6 = (WolfEntity)lv;
               LivingEntity lv7 = lv6.getOwner();
               if (lv6.isTamed() && lv7 instanceof PlayerEntity) {
                  lv4 = (PlayerEntity)lv7;
               }
            }

            if (!lv3.isEmpty() && ItemStack.areEqual(lv3, Raid.getOminousBanner()) && lv4 != null) {
               StatusEffectInstance lv8 = lv4.getStatusEffect(StatusEffects.BAD_OMEN);
               int i = 1;
               if (lv8 != null) {
                  i += lv8.getAmplifier();
                  lv4.removeStatusEffectInternal(StatusEffects.BAD_OMEN);
               } else {
                  --i;
               }

               i = MathHelper.clamp(i, 0, 4);
               StatusEffectInstance lv9 = new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, i, false, false, true);
               if (!this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
                  lv4.addStatusEffect(lv9);
               }
            }
         }
      }

      super.onDeath(damageSource);
   }

   public boolean hasNoRaid() {
      return !this.hasActiveRaid();
   }

   public void setRaid(@Nullable Raid raid) {
      this.raid = raid;
   }

   @Nullable
   public Raid getRaid() {
      return this.raid;
   }

   public boolean hasActiveRaid() {
      return this.getRaid() != null && this.getRaid().isActive();
   }

   public void setWave(int wave) {
      this.wave = wave;
   }

   public int getWave() {
      return this.wave;
   }

   public boolean isCelebrating() {
      return (Boolean)this.dataTracker.get(CELEBRATING);
   }

   public void setCelebrating(boolean celebrating) {
      this.dataTracker.set(CELEBRATING, celebrating);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Wave", this.wave);
      nbt.putBoolean("CanJoinRaid", this.ableToJoinRaid);
      if (this.raid != null) {
         nbt.putInt("RaidId", this.raid.getRaidId());
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.wave = nbt.getInt("Wave");
      this.ableToJoinRaid = nbt.getBoolean("CanJoinRaid");
      if (nbt.contains("RaidId", NbtElement.INT_TYPE)) {
         if (this.world instanceof ServerWorld) {
            this.raid = ((ServerWorld)this.world).getRaidManager().getRaid(nbt.getInt("RaidId"));
         }

         if (this.raid != null) {
            this.raid.addToWave(this.wave, this, false);
            if (this.isPatrolLeader()) {
               this.raid.setWaveCaptain(this.wave, this);
            }
         }
      }

   }

   protected void loot(ItemEntity item) {
      ItemStack lv = item.getStack();
      boolean bl = this.hasActiveRaid() && this.getRaid().getCaptain(this.getWave()) != null;
      if (this.hasActiveRaid() && !bl && ItemStack.areEqual(lv, Raid.getOminousBanner())) {
         EquipmentSlot lv2 = EquipmentSlot.HEAD;
         ItemStack lv3 = this.getEquippedStack(lv2);
         double d = (double)this.getDropChance(lv2);
         if (!lv3.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
            this.dropStack(lv3);
         }

         this.triggerItemPickedUpByEntityCriteria(item);
         this.equipStack(lv2, lv);
         this.sendPickup(item, lv.getCount());
         item.discard();
         this.getRaid().setWaveCaptain(this.getWave(), this);
         this.setPatrolLeader(true);
      } else {
         super.loot(item);
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return this.getRaid() == null ? super.canImmediatelyDespawn(distanceSquared) : false;
   }

   public boolean cannotDespawn() {
      return super.cannotDespawn() || this.getRaid() != null;
   }

   public int getOutOfRaidCounter() {
      return this.outOfRaidCounter;
   }

   public void setOutOfRaidCounter(int outOfRaidCounter) {
      this.outOfRaidCounter = outOfRaidCounter;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.hasActiveRaid()) {
         this.getRaid().updateBar();
      }

      return super.damage(source, amount);
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setAbleToJoinRaid(this.getType() != EntityType.WITCH || spawnReason != SpawnReason.NATURAL);
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public abstract SoundEvent getCelebratingSound();

   static {
      CELEBRATING = DataTracker.registerData(RaiderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      OBTAINABLE_OMINOUS_BANNER_PREDICATE = (itemEntity) -> {
         return !itemEntity.cannotPickup() && itemEntity.isAlive() && ItemStack.areEqual(itemEntity.getStack(), Raid.getOminousBanner());
      };
   }

   public class PickupBannerAsLeaderGoal extends Goal {
      private final RaiderEntity actor;

      public PickupBannerAsLeaderGoal(RaiderEntity actor) {
         this.actor = actor;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         Raid lv = this.actor.getRaid();
         if (this.actor.hasActiveRaid() && !this.actor.getRaid().isFinished() && this.actor.canLead() && !ItemStack.areEqual(this.actor.getEquippedStack(EquipmentSlot.HEAD), Raid.getOminousBanner())) {
            RaiderEntity lv2 = lv.getCaptain(this.actor.getWave());
            if (lv2 == null || !lv2.isAlive()) {
               List list = this.actor.world.getEntitiesByClass(ItemEntity.class, this.actor.getBoundingBox().expand(16.0, 8.0, 16.0), RaiderEntity.OBTAINABLE_OMINOUS_BANNER_PREDICATE);
               if (!list.isEmpty()) {
                  return this.actor.getNavigation().startMovingTo((Entity)list.get(0), 1.149999976158142);
               }
            }

            return false;
         } else {
            return false;
         }
      }

      public void tick() {
         if (this.actor.getNavigation().getTargetPos().isWithinDistance(this.actor.getPos(), 1.414)) {
            List list = this.actor.world.getEntitiesByClass(ItemEntity.class, this.actor.getBoundingBox().expand(4.0, 4.0, 4.0), RaiderEntity.OBTAINABLE_OMINOUS_BANNER_PREDICATE);
            if (!list.isEmpty()) {
               this.actor.loot((ItemEntity)list.get(0));
            }
         }

      }
   }

   private static class AttackHomeGoal extends Goal {
      private final RaiderEntity raider;
      private final double speed;
      private BlockPos home;
      private final List lastHomes = Lists.newArrayList();
      private final int distance;
      private boolean finished;

      public AttackHomeGoal(RaiderEntity raider, double speed, int distance) {
         this.raider = raider;
         this.speed = speed;
         this.distance = distance;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         this.purgeMemory();
         return this.isRaiding() && this.tryFindHome() && this.raider.getTarget() == null;
      }

      private boolean isRaiding() {
         return this.raider.hasActiveRaid() && !this.raider.getRaid().isFinished();
      }

      private boolean tryFindHome() {
         ServerWorld lv = (ServerWorld)this.raider.world;
         BlockPos lv2 = this.raider.getBlockPos();
         Optional optional = lv.getPointOfInterestStorage().getPosition((arg) -> {
            return arg.matchesKey(PointOfInterestTypes.HOME);
         }, this::canLootHome, PointOfInterestStorage.OccupationStatus.ANY, lv2, 48, this.raider.random);
         if (!optional.isPresent()) {
            return false;
         } else {
            this.home = ((BlockPos)optional.get()).toImmutable();
            return true;
         }
      }

      public boolean shouldContinue() {
         if (this.raider.getNavigation().isIdle()) {
            return false;
         } else {
            return this.raider.getTarget() == null && !this.home.isWithinDistance(this.raider.getPos(), (double)(this.raider.getWidth() + (float)this.distance)) && !this.finished;
         }
      }

      public void stop() {
         if (this.home.isWithinDistance(this.raider.getPos(), (double)this.distance)) {
            this.lastHomes.add(this.home);
         }

      }

      public void start() {
         super.start();
         this.raider.setDespawnCounter(0);
         this.raider.getNavigation().startMovingTo((double)this.home.getX(), (double)this.home.getY(), (double)this.home.getZ(), this.speed);
         this.finished = false;
      }

      public void tick() {
         if (this.raider.getNavigation().isIdle()) {
            Vec3d lv = Vec3d.ofBottomCenter(this.home);
            Vec3d lv2 = NoPenaltyTargeting.findTo(this.raider, 16, 7, lv, 0.3141592741012573);
            if (lv2 == null) {
               lv2 = NoPenaltyTargeting.findTo(this.raider, 8, 7, lv, 1.5707963705062866);
            }

            if (lv2 == null) {
               this.finished = true;
               return;
            }

            this.raider.getNavigation().startMovingTo(lv2.x, lv2.y, lv2.z, this.speed);
         }

      }

      private boolean canLootHome(BlockPos pos) {
         Iterator var2 = this.lastHomes.iterator();

         BlockPos lv;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            lv = (BlockPos)var2.next();
         } while(!Objects.equals(pos, lv));

         return false;
      }

      private void purgeMemory() {
         if (this.lastHomes.size() > 2) {
            this.lastHomes.remove(0);
         }

      }
   }

   public class CelebrateGoal extends Goal {
      private final RaiderEntity raider;

      CelebrateGoal(RaiderEntity raider) {
         this.raider = raider;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         Raid lv = this.raider.getRaid();
         return this.raider.isAlive() && this.raider.getTarget() == null && lv != null && lv.hasLost();
      }

      public void start() {
         this.raider.setCelebrating(true);
         super.start();
      }

      public void stop() {
         this.raider.setCelebrating(false);
         super.stop();
      }

      public void tick() {
         if (!this.raider.isSilent() && this.raider.random.nextInt(this.getTickCount(100)) == 0) {
            RaiderEntity.this.playSound(RaiderEntity.this.getCelebratingSound(), RaiderEntity.this.getSoundVolume(), RaiderEntity.this.getSoundPitch());
         }

         if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0) {
            this.raider.getJumpControl().setActive();
         }

         super.tick();
      }
   }

   protected class PatrolApproachGoal extends Goal {
      private final RaiderEntity raider;
      private final float squaredDistance;
      public final TargetPredicate closeRaiderPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0).ignoreVisibility().ignoreDistanceScalingFactor();

      public PatrolApproachGoal(IllagerEntity illager, float distance) {
         this.raider = illager;
         this.squaredDistance = distance * distance;
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         LivingEntity lv = this.raider.getAttacker();
         return this.raider.getRaid() == null && this.raider.isRaidCenterSet() && this.raider.getTarget() != null && !this.raider.isAttacking() && (lv == null || lv.getType() != EntityType.PLAYER);
      }

      public void start() {
         super.start();
         this.raider.getNavigation().stop();
         List list = this.raider.world.getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            RaiderEntity lv = (RaiderEntity)var2.next();
            lv.setTarget(this.raider.getTarget());
         }

      }

      public void stop() {
         super.stop();
         LivingEntity lv = this.raider.getTarget();
         if (lv != null) {
            List list = this.raider.world.getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
               RaiderEntity lv2 = (RaiderEntity)var3.next();
               lv2.setTarget(lv);
               lv2.setAttacking(true);
            }

            this.raider.setAttacking(true);
         }

      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         LivingEntity lv = this.raider.getTarget();
         if (lv != null) {
            if (this.raider.squaredDistanceTo(lv) > (double)this.squaredDistance) {
               this.raider.getLookControl().lookAt(lv, 30.0F, 30.0F);
               if (this.raider.random.nextInt(50) == 0) {
                  this.raider.playAmbientSound();
               }
            } else {
               this.raider.setAttacking(true);
            }

            super.tick();
         }
      }
   }
}
