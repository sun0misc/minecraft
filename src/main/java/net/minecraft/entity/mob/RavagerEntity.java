package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class RavagerEntity extends RaiderEntity {
   private static final Predicate IS_NOT_RAVAGER = (entity) -> {
      return entity.isAlive() && !(entity instanceof RavagerEntity);
   };
   private static final double field_30480 = 0.3;
   private static final double field_30481 = 0.35;
   private static final int field_30482 = 8356754;
   private static final double field_30483 = 0.5725490196078431;
   private static final double field_30484 = 0.5137254901960784;
   private static final double field_30485 = 0.4980392156862745;
   private static final int field_30486 = 10;
   public static final int field_30479 = 40;
   private int attackTick;
   private int stunTick;
   private int roarTick;

   public RavagerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setStepHeight(1.0F);
      this.experiencePoints = 20;
      this.setPathfindingPenalty(PathNodeType.LEAVES, 0.0F);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(4, new AttackGoal());
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.4));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
      this.targetSelector.add(2, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(3, new ActiveTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(4, new ActiveTargetGoal(this, MerchantEntity.class, true, (entity) -> {
         return !entity.isBaby();
      }));
      this.targetSelector.add(4, new ActiveTargetGoal(this, IronGolemEntity.class, true));
   }

   protected void updateGoalControls() {
      boolean bl = !(this.getControllingPassenger() instanceof MobEntity) || this.getControllingPassenger().getType().isIn(EntityTypeTags.RAIDERS);
      boolean bl2 = !(this.getVehicle() instanceof BoatEntity);
      this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
      this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
      this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
      this.goalSelector.setControlEnabled(Goal.Control.TARGET, bl);
   }

   public static DefaultAttributeContainer.Builder createRavagerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.75).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("AttackTick", this.attackTick);
      nbt.putInt("StunTick", this.stunTick);
      nbt.putInt("RoarTick", this.roarTick);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.attackTick = nbt.getInt("AttackTick");
      this.stunTick = nbt.getInt("StunTick");
      this.roarTick = nbt.getInt("RoarTick");
   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_RAVAGER_CELEBRATE;
   }

   public int getMaxHeadRotation() {
      return 45;
   }

   public double getMountedHeightOffset() {
      return 2.1;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      LivingEntity var10000;
      if (!this.isAiDisabled()) {
         Entity var2 = this.getFirstPassenger();
         if (var2 instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)var2;
            var10000 = lv;
            return var10000;
         }
      }

      var10000 = null;
      return var10000;
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isAlive()) {
         if (this.isImmobile()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
         } else {
            double d = this.getTarget() != null ? 0.35 : 0.3;
            double e = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(MathHelper.lerp(0.1, e, d));
         }

         if (this.horizontalCollision && this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            boolean bl = false;
            Box lv = this.getBoundingBox().expand(0.2);
            Iterator var8 = BlockPos.iterate(MathHelper.floor(lv.minX), MathHelper.floor(lv.minY), MathHelper.floor(lv.minZ), MathHelper.floor(lv.maxX), MathHelper.floor(lv.maxY), MathHelper.floor(lv.maxZ)).iterator();

            label60:
            while(true) {
               BlockPos lv2;
               Block lv4;
               do {
                  if (!var8.hasNext()) {
                     if (!bl && this.onGround) {
                        this.jump();
                     }
                     break label60;
                  }

                  lv2 = (BlockPos)var8.next();
                  BlockState lv3 = this.world.getBlockState(lv2);
                  lv4 = lv3.getBlock();
               } while(!(lv4 instanceof LeavesBlock));

               bl = this.world.breakBlock(lv2, true, this) || bl;
            }
         }

         if (this.roarTick > 0) {
            --this.roarTick;
            if (this.roarTick == 10) {
               this.roar();
            }
         }

         if (this.attackTick > 0) {
            --this.attackTick;
         }

         if (this.stunTick > 0) {
            --this.stunTick;
            this.spawnStunnedParticles();
            if (this.stunTick == 0) {
               this.playSound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
            }
         }

      }
   }

   private void spawnStunnedParticles() {
      if (this.random.nextInt(6) == 0) {
         double d = this.getX() - (double)this.getWidth() * Math.sin((double)(this.bodyYaw * 0.017453292F)) + (this.random.nextDouble() * 0.6 - 0.3);
         double e = this.getY() + (double)this.getHeight() - 0.3;
         double f = this.getZ() + (double)this.getWidth() * Math.cos((double)(this.bodyYaw * 0.017453292F)) + (this.random.nextDouble() * 0.6 - 0.3);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, d, e, f, 0.4980392156862745, 0.5137254901960784, 0.5725490196078431);
      }

   }

   protected boolean isImmobile() {
      return super.isImmobile() || this.attackTick > 0 || this.stunTick > 0 || this.roarTick > 0;
   }

   public boolean canSee(Entity entity) {
      return this.stunTick <= 0 && this.roarTick <= 0 ? super.canSee(entity) : false;
   }

   protected void knockback(LivingEntity target) {
      if (this.roarTick == 0) {
         if (this.random.nextDouble() < 0.5) {
            this.stunTick = 40;
            this.playSound(SoundEvents.ENTITY_RAVAGER_STUNNED, 1.0F, 1.0F);
            this.world.sendEntityStatus(this, EntityStatuses.STUN_RAVAGER);
            target.pushAwayFrom(this);
         } else {
            this.knockBack(target);
         }

         target.velocityModified = true;
      }

   }

   private void roar() {
      if (this.isAlive()) {
         List list = this.world.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(4.0), IS_NOT_RAVAGER);

         LivingEntity lv;
         for(Iterator var2 = list.iterator(); var2.hasNext(); this.knockBack(lv)) {
            lv = (LivingEntity)var2.next();
            if (!(lv instanceof IllagerEntity)) {
               lv.damage(this.getDamageSources().mobAttack(this), 6.0F);
            }
         }

         Vec3d lv2 = this.getBoundingBox().getCenter();

         for(int i = 0; i < 40; ++i) {
            double d = this.random.nextGaussian() * 0.2;
            double e = this.random.nextGaussian() * 0.2;
            double f = this.random.nextGaussian() * 0.2;
            this.world.addParticle(ParticleTypes.POOF, lv2.x, lv2.y, lv2.z, d, e, f);
         }

         this.emitGameEvent(GameEvent.ENTITY_ROAR);
      }

   }

   private void knockBack(Entity entity) {
      double d = entity.getX() - this.getX();
      double e = entity.getZ() - this.getZ();
      double f = Math.max(d * d + e * e, 0.001);
      entity.addVelocity(d / f * 4.0, 0.2, e / f * 4.0);
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
         this.attackTick = 10;
         this.playSound(SoundEvents.ENTITY_RAVAGER_ATTACK, 1.0F, 1.0F);
      } else if (status == EntityStatuses.STUN_RAVAGER) {
         this.stunTick = 40;
      }

      super.handleStatus(status);
   }

   public int getAttackTick() {
      return this.attackTick;
   }

   public int getStunTick() {
      return this.stunTick;
   }

   public int getRoarTick() {
      return this.roarTick;
   }

   public boolean tryAttack(Entity target) {
      this.attackTick = 10;
      this.world.sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
      this.playSound(SoundEvents.ENTITY_RAVAGER_ATTACK, 1.0F, 1.0F);
      return super.tryAttack(target);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_RAVAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_RAVAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_RAVAGER_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_RAVAGER_STEP, 0.15F, 1.0F);
   }

   public boolean canSpawn(WorldView world) {
      return !world.containsFluid(this.getBoundingBox());
   }

   public void addBonusForWave(int wave, boolean unused) {
   }

   public boolean canLead() {
      return false;
   }

   private class AttackGoal extends MeleeAttackGoal {
      public AttackGoal() {
         super(RavagerEntity.this, 1.0, true);
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         float f = RavagerEntity.this.getWidth() - 0.1F;
         return (double)(f * 2.0F * f * 2.0F + entity.getWidth());
      }
   }
}
