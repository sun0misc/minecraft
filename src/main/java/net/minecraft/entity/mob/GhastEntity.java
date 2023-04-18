package net.minecraft.entity.mob;

import java.util.EnumSet;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

public class GhastEntity extends FlyingEntity implements Monster {
   private static final TrackedData SHOOTING;
   private int fireballStrength = 1;

   public GhastEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.experiencePoints = 5;
      this.moveControl = new GhastMoveControl(this);
   }

   protected void initGoals() {
      this.goalSelector.add(5, new FlyRandomlyGoal(this));
      this.goalSelector.add(7, new LookAtTargetGoal(this));
      this.goalSelector.add(7, new ShootFireballGoal(this));
      this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, (entity) -> {
         return Math.abs(entity.getY() - this.getY()) <= 4.0;
      }));
   }

   public boolean isShooting() {
      return (Boolean)this.dataTracker.get(SHOOTING);
   }

   public void setShooting(boolean shooting) {
      this.dataTracker.set(SHOOTING, shooting);
   }

   public int getFireballStrength() {
      return this.fireballStrength;
   }

   protected boolean isDisallowedInPeaceful() {
      return true;
   }

   private static boolean isFireballFromPlayer(DamageSource damageSource) {
      return damageSource.getSource() instanceof FireballEntity && damageSource.getAttacker() instanceof PlayerEntity;
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return !isFireballFromPlayer(damageSource) && super.isInvulnerableTo(damageSource);
   }

   public boolean damage(DamageSource source, float amount) {
      if (isFireballFromPlayer(source)) {
         super.damage(source, 1000.0F);
         return true;
      } else {
         return this.isInvulnerableTo(source) ? false : super.damage(source, amount);
      }
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SHOOTING, false);
   }

   public static DefaultAttributeContainer.Builder createGhastAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_GHAST_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_GHAST_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_GHAST_DEATH;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0 && canMobSpawn(type, world, spawnReason, pos, random);
   }

   public int getLimitPerChunk() {
      return 1;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("ExplosionPower", (byte)this.fireballStrength);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("ExplosionPower", NbtElement.NUMBER_TYPE)) {
         this.fireballStrength = nbt.getByte("ExplosionPower");
      }

   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 2.6F;
   }

   static {
      SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   private static class GhastMoveControl extends MoveControl {
      private final GhastEntity ghast;
      private int collisionCheckCooldown;

      public GhastMoveControl(GhastEntity ghast) {
         super(ghast);
         this.ghast = ghast;
      }

      public void tick() {
         if (this.state == MoveControl.State.MOVE_TO) {
            if (this.collisionCheckCooldown-- <= 0) {
               this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
               Vec3d lv = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(), this.targetZ - this.ghast.getZ());
               double d = lv.length();
               lv = lv.normalize();
               if (this.willCollide(lv, MathHelper.ceil(d))) {
                  this.ghast.setVelocity(this.ghast.getVelocity().add(lv.multiply(0.1)));
               } else {
                  this.state = MoveControl.State.WAIT;
               }
            }

         }
      }

      private boolean willCollide(Vec3d direction, int steps) {
         Box lv = this.ghast.getBoundingBox();

         for(int j = 1; j < steps; ++j) {
            lv = lv.offset(direction);
            if (!this.ghast.world.isSpaceEmpty(this.ghast, lv)) {
               return false;
            }
         }

         return true;
      }
   }

   private static class FlyRandomlyGoal extends Goal {
      private final GhastEntity ghast;

      public FlyRandomlyGoal(GhastEntity ghast) {
         this.ghast = ghast;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         MoveControl lv = this.ghast.getMoveControl();
         if (!lv.isMoving()) {
            return true;
         } else {
            double d = lv.getTargetX() - this.ghast.getX();
            double e = lv.getTargetY() - this.ghast.getY();
            double f = lv.getTargetZ() - this.ghast.getZ();
            double g = d * d + e * e + f * f;
            return g < 1.0 || g > 3600.0;
         }
      }

      public boolean shouldContinue() {
         return false;
      }

      public void start() {
         Random lv = this.ghast.getRandom();
         double d = this.ghast.getX() + (double)((lv.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double e = this.ghast.getY() + (double)((lv.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double f = this.ghast.getZ() + (double)((lv.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.ghast.getMoveControl().moveTo(d, e, f, 1.0);
      }
   }

   private static class LookAtTargetGoal extends Goal {
      private final GhastEntity ghast;

      public LookAtTargetGoal(GhastEntity ghast) {
         this.ghast = ghast;
         this.setControls(EnumSet.of(Goal.Control.LOOK));
      }

      public boolean canStart() {
         return true;
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         if (this.ghast.getTarget() == null) {
            Vec3d lv = this.ghast.getVelocity();
            this.ghast.setYaw(-((float)MathHelper.atan2(lv.x, lv.z)) * 57.295776F);
            this.ghast.bodyYaw = this.ghast.getYaw();
         } else {
            LivingEntity lv2 = this.ghast.getTarget();
            double d = 64.0;
            if (lv2.squaredDistanceTo(this.ghast) < 4096.0) {
               double e = lv2.getX() - this.ghast.getX();
               double f = lv2.getZ() - this.ghast.getZ();
               this.ghast.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776F);
               this.ghast.bodyYaw = this.ghast.getYaw();
            }
         }

      }
   }

   private static class ShootFireballGoal extends Goal {
      private final GhastEntity ghast;
      public int cooldown;

      public ShootFireballGoal(GhastEntity ghast) {
         this.ghast = ghast;
      }

      public boolean canStart() {
         return this.ghast.getTarget() != null;
      }

      public void start() {
         this.cooldown = 0;
      }

      public void stop() {
         this.ghast.setShooting(false);
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         LivingEntity lv = this.ghast.getTarget();
         if (lv != null) {
            double d = 64.0;
            if (lv.squaredDistanceTo(this.ghast) < 4096.0 && this.ghast.canSee(lv)) {
               World lv2 = this.ghast.world;
               ++this.cooldown;
               if (this.cooldown == 10 && !this.ghast.isSilent()) {
                  lv2.syncWorldEvent((PlayerEntity)null, WorldEvents.GHAST_WARNS, this.ghast.getBlockPos(), 0);
               }

               if (this.cooldown == 20) {
                  double e = 4.0;
                  Vec3d lv3 = this.ghast.getRotationVec(1.0F);
                  double f = lv.getX() - (this.ghast.getX() + lv3.x * 4.0);
                  double g = lv.getBodyY(0.5) - (0.5 + this.ghast.getBodyY(0.5));
                  double h = lv.getZ() - (this.ghast.getZ() + lv3.z * 4.0);
                  if (!this.ghast.isSilent()) {
                     lv2.syncWorldEvent((PlayerEntity)null, WorldEvents.GHAST_SHOOTS, this.ghast.getBlockPos(), 0);
                  }

                  FireballEntity lv4 = new FireballEntity(lv2, this.ghast, f, g, h, this.ghast.getFireballStrength());
                  lv4.setPosition(this.ghast.getX() + lv3.x * 4.0, this.ghast.getBodyY(0.5) + 0.5, lv4.getZ() + lv3.z * 4.0);
                  lv2.spawnEntity(lv4);
                  this.cooldown = -40;
               }
            } else if (this.cooldown > 0) {
               --this.cooldown;
            }

            this.ghast.setShooting(this.cooldown > 10);
         }
      }
   }
}
