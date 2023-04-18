package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SquidEntity extends WaterCreatureEntity {
   public float tiltAngle;
   public float prevTiltAngle;
   public float rollAngle;
   public float prevRollAngle;
   public float thrustTimer;
   public float prevThrustTimer;
   public float tentacleAngle;
   public float prevTentacleAngle;
   private float swimVelocityScale;
   private float thrustTimerSpeed;
   private float turningSpeed;
   private float swimX;
   private float swimY;
   private float swimZ;

   public SquidEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.random.setSeed((long)this.getId());
      this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EscapeAttackerGoal());
   }

   public static DefaultAttributeContainer.Builder createSquidAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.5F;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SQUID_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SQUID_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SQUID_DEATH;
   }

   protected SoundEvent getSquirtSound() {
      return SoundEvents.ENTITY_SQUID_SQUIRT;
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.EVENTS;
   }

   public void tickMovement() {
      super.tickMovement();
      this.prevTiltAngle = this.tiltAngle;
      this.prevRollAngle = this.rollAngle;
      this.prevThrustTimer = this.thrustTimer;
      this.prevTentacleAngle = this.tentacleAngle;
      this.thrustTimer += this.thrustTimerSpeed;
      if ((double)this.thrustTimer > 6.283185307179586) {
         if (this.world.isClient) {
            this.thrustTimer = 6.2831855F;
         } else {
            this.thrustTimer -= 6.2831855F;
            if (this.random.nextInt(10) == 0) {
               this.thrustTimerSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
            }

            this.world.sendEntityStatus(this, EntityStatuses.RESET_SQUID_THRUST_TIMER);
         }
      }

      if (this.isInsideWaterOrBubbleColumn()) {
         if (this.thrustTimer < 3.1415927F) {
            float f = this.thrustTimer / 3.1415927F;
            this.tentacleAngle = MathHelper.sin(f * f * 3.1415927F) * 3.1415927F * 0.25F;
            if ((double)f > 0.75) {
               this.swimVelocityScale = 1.0F;
               this.turningSpeed = 1.0F;
            } else {
               this.turningSpeed *= 0.8F;
            }
         } else {
            this.tentacleAngle = 0.0F;
            this.swimVelocityScale *= 0.9F;
            this.turningSpeed *= 0.99F;
         }

         if (!this.world.isClient) {
            this.setVelocity((double)(this.swimX * this.swimVelocityScale), (double)(this.swimY * this.swimVelocityScale), (double)(this.swimZ * this.swimVelocityScale));
         }

         Vec3d lv = this.getVelocity();
         double d = lv.horizontalLength();
         this.bodyYaw += (-((float)MathHelper.atan2(lv.x, lv.z)) * 57.295776F - this.bodyYaw) * 0.1F;
         this.setYaw(this.bodyYaw);
         this.rollAngle += 3.1415927F * this.turningSpeed * 1.5F;
         this.tiltAngle += (-((float)MathHelper.atan2(d, lv.y)) * 57.295776F - this.tiltAngle) * 0.1F;
      } else {
         this.tentacleAngle = MathHelper.abs(MathHelper.sin(this.thrustTimer)) * 3.1415927F * 0.25F;
         if (!this.world.isClient) {
            double e = this.getVelocity().y;
            if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
               e = 0.05 * (double)(this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1);
            } else if (!this.hasNoGravity()) {
               e -= 0.08;
            }

            this.setVelocity(0.0, e * 0.9800000190734863, 0.0);
         }

         this.tiltAngle += (-90.0F - this.tiltAngle) * 0.02F;
      }

   }

   public boolean damage(DamageSource source, float amount) {
      if (super.damage(source, amount) && this.getAttacker() != null) {
         if (!this.world.isClient) {
            this.squirt();
         }

         return true;
      } else {
         return false;
      }
   }

   private Vec3d applyBodyRotations(Vec3d shootVector) {
      Vec3d lv = shootVector.rotateX(this.prevTiltAngle * 0.017453292F);
      lv = lv.rotateY(-this.prevBodyYaw * 0.017453292F);
      return lv;
   }

   private void squirt() {
      this.playSound(this.getSquirtSound(), this.getSoundVolume(), this.getSoundPitch());
      Vec3d lv = this.applyBodyRotations(new Vec3d(0.0, -1.0, 0.0)).add(this.getX(), this.getY(), this.getZ());

      for(int i = 0; i < 30; ++i) {
         Vec3d lv2 = this.applyBodyRotations(new Vec3d((double)this.random.nextFloat() * 0.6 - 0.3, -1.0, (double)this.random.nextFloat() * 0.6 - 0.3));
         Vec3d lv3 = lv2.multiply(0.3 + (double)(this.random.nextFloat() * 2.0F));
         ((ServerWorld)this.world).spawnParticles(this.getInkParticle(), lv.x, lv.y + 0.5, lv.z, 0, lv3.x, lv3.y, lv3.z, 0.10000000149011612);
      }

   }

   protected ParticleEffect getInkParticle() {
      return ParticleTypes.SQUID_INK;
   }

   public void travel(Vec3d movementInput) {
      this.move(MovementType.SELF, this.getVelocity());
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.RESET_SQUID_THRUST_TIMER) {
         this.thrustTimer = 0.0F;
      } else {
         super.handleStatus(status);
      }

   }

   public void setSwimmingVector(float x, float y, float z) {
      this.swimX = x;
      this.swimY = y;
      this.swimZ = z;
   }

   public boolean hasSwimmingVector() {
      return this.swimX != 0.0F || this.swimY != 0.0F || this.swimZ != 0.0F;
   }

   class SwimGoal extends Goal {
      private final SquidEntity squid;

      public SwimGoal(SquidEntity squid) {
         this.squid = squid;
      }

      public boolean canStart() {
         return true;
      }

      public void tick() {
         int i = this.squid.getDespawnCounter();
         if (i > 100) {
            this.squid.setSwimmingVector(0.0F, 0.0F, 0.0F);
         } else if (this.squid.getRandom().nextInt(toGoalTicks(50)) == 0 || !this.squid.touchingWater || !this.squid.hasSwimmingVector()) {
            float f = this.squid.getRandom().nextFloat() * 6.2831855F;
            float g = MathHelper.cos(f) * 0.2F;
            float h = -0.1F + this.squid.getRandom().nextFloat() * 0.2F;
            float j = MathHelper.sin(f) * 0.2F;
            this.squid.setSwimmingVector(g, h, j);
         }

      }
   }

   class EscapeAttackerGoal extends Goal {
      private static final float field_30375 = 3.0F;
      private static final float field_30376 = 5.0F;
      private static final float field_30377 = 10.0F;
      private int timer;

      public boolean canStart() {
         LivingEntity lv = SquidEntity.this.getAttacker();
         if (SquidEntity.this.isTouchingWater() && lv != null) {
            return SquidEntity.this.squaredDistanceTo(lv) < 100.0;
         } else {
            return false;
         }
      }

      public void start() {
         this.timer = 0;
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         ++this.timer;
         LivingEntity lv = SquidEntity.this.getAttacker();
         if (lv != null) {
            Vec3d lv2 = new Vec3d(SquidEntity.this.getX() - lv.getX(), SquidEntity.this.getY() - lv.getY(), SquidEntity.this.getZ() - lv.getZ());
            BlockState lv3 = SquidEntity.this.world.getBlockState(BlockPos.ofFloored(SquidEntity.this.getX() + lv2.x, SquidEntity.this.getY() + lv2.y, SquidEntity.this.getZ() + lv2.z));
            FluidState lv4 = SquidEntity.this.world.getFluidState(BlockPos.ofFloored(SquidEntity.this.getX() + lv2.x, SquidEntity.this.getY() + lv2.y, SquidEntity.this.getZ() + lv2.z));
            if (lv4.isIn(FluidTags.WATER) || lv3.isAir()) {
               double d = lv2.length();
               if (d > 0.0) {
                  lv2.normalize();
                  double e = 3.0;
                  if (d > 5.0) {
                     e -= (d - 5.0) / 5.0;
                  }

                  if (e > 0.0) {
                     lv2 = lv2.multiply(e);
                  }
               }

               if (lv3.isAir()) {
                  lv2 = lv2.subtract(0.0, lv2.y, 0.0);
               }

               SquidEntity.this.setSwimmingVector((float)lv2.x / 20.0F, (float)lv2.y / 20.0F, (float)lv2.z / 20.0F);
            }

            if (this.timer % 10 == 5) {
               SquidEntity.this.world.addParticle(ParticleTypes.BUBBLE, SquidEntity.this.getX(), SquidEntity.this.getY(), SquidEntity.this.getZ(), 0.0, 0.0, 0.0);
            }

         }
      }
   }
}
