package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SlimeEntity extends MobEntity implements Monster {
   private static final TrackedData SLIME_SIZE;
   public static final int MIN_SIZE = 1;
   public static final int MAX_SIZE = 127;
   public float targetStretch;
   public float stretch;
   public float lastStretch;
   private boolean onGroundLastTick;

   public SlimeEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.reinitDimensions();
      this.moveControl = new SlimeMoveControl(this);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimmingGoal(this));
      this.goalSelector.add(2, new FaceTowardTargetGoal(this));
      this.goalSelector.add(3, new RandomLookGoal(this));
      this.goalSelector.add(5, new MoveGoal(this));
      this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, (arg) -> {
         return Math.abs(arg.getY() - this.getY()) <= 4.0;
      }));
      this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SLIME_SIZE, 1);
   }

   @VisibleForTesting
   public void setSize(int size, boolean heal) {
      int j = MathHelper.clamp(size, 1, 127);
      this.dataTracker.set(SLIME_SIZE, j);
      this.refreshPosition();
      this.calculateDimensions();
      this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue((double)(j * j));
      this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)j));
      this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue((double)j);
      if (heal) {
         this.setHealth(this.getMaxHealth());
      }

      this.experiencePoints = j;
   }

   public int getSize() {
      return (Integer)this.dataTracker.get(SLIME_SIZE);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Size", this.getSize() - 1);
      nbt.putBoolean("wasOnGround", this.onGroundLastTick);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      this.setSize(nbt.getInt("Size") + 1, false);
      super.readCustomDataFromNbt(nbt);
      this.onGroundLastTick = nbt.getBoolean("wasOnGround");
   }

   public boolean isSmall() {
      return this.getSize() <= 1;
   }

   protected ParticleEffect getParticles() {
      return ParticleTypes.ITEM_SLIME;
   }

   protected boolean isDisallowedInPeaceful() {
      return this.getSize() > 0;
   }

   public void tick() {
      this.stretch += (this.targetStretch - this.stretch) * 0.5F;
      this.lastStretch = this.stretch;
      super.tick();
      if (this.onGround && !this.onGroundLastTick) {
         int i = this.getSize();

         for(int j = 0; j < i * 8; ++j) {
            float f = this.random.nextFloat() * 6.2831855F;
            float g = this.random.nextFloat() * 0.5F + 0.5F;
            float h = MathHelper.sin(f) * (float)i * 0.5F * g;
            float k = MathHelper.cos(f) * (float)i * 0.5F * g;
            this.world.addParticle(this.getParticles(), this.getX() + (double)h, this.getY(), this.getZ() + (double)k, 0.0, 0.0, 0.0);
         }

         this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.targetStretch = -0.5F;
      } else if (!this.onGround && this.onGroundLastTick) {
         this.targetStretch = 1.0F;
      }

      this.onGroundLastTick = this.onGround;
      this.updateStretch();
   }

   protected void updateStretch() {
      this.targetStretch *= 0.6F;
   }

   protected int getTicksUntilNextJump() {
      return this.random.nextInt(20) + 10;
   }

   public void calculateDimensions() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      super.calculateDimensions();
      this.setPosition(d, e, f);
   }

   public void onTrackedDataSet(TrackedData data) {
      if (SLIME_SIZE.equals(data)) {
         this.calculateDimensions();
         this.setYaw(this.headYaw);
         this.bodyYaw = this.headYaw;
         if (this.isTouchingWater() && this.random.nextInt(20) == 0) {
            this.onSwimmingStart();
         }
      }

      super.onTrackedDataSet(data);
   }

   public EntityType getType() {
      return super.getType();
   }

   public void remove(Entity.RemovalReason reason) {
      int i = this.getSize();
      if (!this.world.isClient && i > 1 && this.isDead()) {
         Text lv = this.getCustomName();
         boolean bl = this.isAiDisabled();
         float f = (float)i / 4.0F;
         int j = i / 2;
         int k = 2 + this.random.nextInt(3);

         for(int l = 0; l < k; ++l) {
            float g = ((float)(l % 2) - 0.5F) * f;
            float h = ((float)(l / 2) - 0.5F) * f;
            SlimeEntity lv2 = (SlimeEntity)this.getType().create(this.world);
            if (lv2 != null) {
               if (this.isPersistent()) {
                  lv2.setPersistent();
               }

               lv2.setCustomName(lv);
               lv2.setAiDisabled(bl);
               lv2.setInvulnerable(this.isInvulnerable());
               lv2.setSize(j, true);
               lv2.refreshPositionAndAngles(this.getX() + (double)g, this.getY() + 0.5, this.getZ() + (double)h, this.random.nextFloat() * 360.0F, 0.0F);
               this.world.spawnEntity(lv2);
            }
         }
      }

      super.remove(reason);
   }

   public void pushAwayFrom(Entity entity) {
      super.pushAwayFrom(entity);
      if (entity instanceof IronGolemEntity && this.canAttack()) {
         this.damage((LivingEntity)entity);
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      if (this.canAttack()) {
         this.damage(player);
      }

   }

   protected void damage(LivingEntity target) {
      if (this.isAlive()) {
         int i = this.getSize();
         if (this.squaredDistanceTo(target) < 0.6 * (double)i * 0.6 * (double)i && this.canSee(target) && target.damage(this.getDamageSources().mobAttack(this), this.getDamageAmount())) {
            this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.applyDamageEffects(this, target);
         }
      }

   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.625F * dimensions.height;
   }

   protected boolean canAttack() {
      return !this.isSmall() && this.canMoveVoluntarily();
   }

   protected float getDamageAmount() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isSmall() ? SoundEvents.ENTITY_SLIME_HURT_SMALL : SoundEvents.ENTITY_SLIME_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isSmall() ? SoundEvents.ENTITY_SLIME_DEATH_SMALL : SoundEvents.ENTITY_SLIME_DEATH;
   }

   protected SoundEvent getSquishSound() {
      return this.isSmall() ? SoundEvents.ENTITY_SLIME_SQUISH_SMALL : SoundEvents.ENTITY_SLIME_SQUISH;
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      if (world.getDifficulty() != Difficulty.PEACEFUL) {
         if (world.getBiome(pos).isIn(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS) && pos.getY() > 50 && pos.getY() < 70 && random.nextFloat() < 0.5F && random.nextFloat() < world.getMoonSize() && world.getLightLevel(pos) <= random.nextInt(8)) {
            return canMobSpawn(type, world, spawnReason, pos, random);
         }

         if (!(world instanceof StructureWorldAccess)) {
            return false;
         }

         ChunkPos lv = new ChunkPos(pos);
         boolean bl = ChunkRandom.getSlimeRandom(lv.x, lv.z, ((StructureWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;
         if (random.nextInt(10) == 0 && bl && pos.getY() < 40) {
            return canMobSpawn(type, world, spawnReason, pos, random);
         }
      }

      return false;
   }

   protected float getSoundVolume() {
      return 0.4F * (float)this.getSize();
   }

   public int getMaxLookPitchChange() {
      return 0;
   }

   protected boolean makesJumpSound() {
      return this.getSize() > 0;
   }

   protected void jump() {
      Vec3d lv = this.getVelocity();
      this.setVelocity(lv.x, (double)this.getJumpVelocity(), lv.z);
      this.velocityDirty = true;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      int i = lv.nextInt(3);
      if (i < 2 && lv.nextFloat() < 0.5F * difficulty.getClampedLocalDifficulty()) {
         ++i;
      }

      int j = 1 << i;
      this.setSize(j, true);
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   float getJumpSoundPitch() {
      float f = this.isSmall() ? 1.4F : 0.8F;
      return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * f;
   }

   protected SoundEvent getJumpSound() {
      return this.isSmall() ? SoundEvents.ENTITY_SLIME_JUMP_SMALL : SoundEvents.ENTITY_SLIME_JUMP;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return super.getDimensions(pose).scaled(0.255F * (float)this.getSize());
   }

   static {
      SLIME_SIZE = DataTracker.registerData(SlimeEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   private static class SlimeMoveControl extends MoveControl {
      private float targetYaw;
      private int ticksUntilJump;
      private final SlimeEntity slime;
      private boolean jumpOften;

      public SlimeMoveControl(SlimeEntity slime) {
         super(slime);
         this.slime = slime;
         this.targetYaw = 180.0F * slime.getYaw() / 3.1415927F;
      }

      public void look(float targetYaw, boolean jumpOften) {
         this.targetYaw = targetYaw;
         this.jumpOften = jumpOften;
      }

      public void move(double speed) {
         this.speed = speed;
         this.state = MoveControl.State.MOVE_TO;
      }

      public void tick() {
         this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), this.targetYaw, 90.0F));
         this.entity.headYaw = this.entity.getYaw();
         this.entity.bodyYaw = this.entity.getYaw();
         if (this.state != MoveControl.State.MOVE_TO) {
            this.entity.setForwardSpeed(0.0F);
         } else {
            this.state = MoveControl.State.WAIT;
            if (this.entity.isOnGround()) {
               this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
               if (this.ticksUntilJump-- <= 0) {
                  this.ticksUntilJump = this.slime.getTicksUntilNextJump();
                  if (this.jumpOften) {
                     this.ticksUntilJump /= 3;
                  }

                  this.slime.getJumpControl().setActive();
                  if (this.slime.makesJumpSound()) {
                     this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getJumpSoundPitch());
                  }
               } else {
                  this.slime.sidewaysSpeed = 0.0F;
                  this.slime.forwardSpeed = 0.0F;
                  this.entity.setMovementSpeed(0.0F);
               }
            } else {
               this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            }

         }
      }
   }

   private static class SwimmingGoal extends Goal {
      private final SlimeEntity slime;

      public SwimmingGoal(SlimeEntity slime) {
         this.slime = slime;
         this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
         slime.getNavigation().setCanSwim(true);
      }

      public boolean canStart() {
         return (this.slime.isTouchingWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         if (this.slime.getRandom().nextFloat() < 0.8F) {
            this.slime.getJumpControl().setActive();
         }

         MoveControl var2 = this.slime.getMoveControl();
         if (var2 instanceof SlimeMoveControl lv) {
            lv.move(1.2);
         }

      }
   }

   static class FaceTowardTargetGoal extends Goal {
      private final SlimeEntity slime;
      private int ticksLeft;

      public FaceTowardTargetGoal(SlimeEntity slime) {
         this.slime = slime;
         this.setControls(EnumSet.of(Goal.Control.LOOK));
      }

      public boolean canStart() {
         LivingEntity lv = this.slime.getTarget();
         if (lv == null) {
            return false;
         } else {
            return !this.slime.canTarget(lv) ? false : this.slime.getMoveControl() instanceof SlimeMoveControl;
         }
      }

      public void start() {
         this.ticksLeft = toGoalTicks(300);
         super.start();
      }

      public boolean shouldContinue() {
         LivingEntity lv = this.slime.getTarget();
         if (lv == null) {
            return false;
         } else if (!this.slime.canTarget(lv)) {
            return false;
         } else {
            return --this.ticksLeft > 0;
         }
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         LivingEntity lv = this.slime.getTarget();
         if (lv != null) {
            this.slime.lookAtEntity(lv, 10.0F, 10.0F);
         }

         MoveControl var3 = this.slime.getMoveControl();
         if (var3 instanceof SlimeMoveControl lv2) {
            lv2.look(this.slime.getYaw(), this.slime.canAttack());
         }

      }
   }

   private static class RandomLookGoal extends Goal {
      private final SlimeEntity slime;
      private float targetYaw;
      private int timer;

      public RandomLookGoal(SlimeEntity slime) {
         this.slime = slime;
         this.setControls(EnumSet.of(Goal.Control.LOOK));
      }

      public boolean canStart() {
         return this.slime.getTarget() == null && (this.slime.onGround || this.slime.isTouchingWater() || this.slime.isInLava() || this.slime.hasStatusEffect(StatusEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SlimeMoveControl;
      }

      public void tick() {
         if (--this.timer <= 0) {
            this.timer = this.getTickCount(40 + this.slime.getRandom().nextInt(60));
            this.targetYaw = (float)this.slime.getRandom().nextInt(360);
         }

         MoveControl var2 = this.slime.getMoveControl();
         if (var2 instanceof SlimeMoveControl lv) {
            lv.look(this.targetYaw, false);
         }

      }
   }

   private static class MoveGoal extends Goal {
      private final SlimeEntity slime;

      public MoveGoal(SlimeEntity slime) {
         this.slime = slime;
         this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
      }

      public boolean canStart() {
         return !this.slime.hasVehicle();
      }

      public void tick() {
         MoveControl var2 = this.slime.getMoveControl();
         if (var2 instanceof SlimeMoveControl lv) {
            lv.move(1.0);
         }

      }
   }
}
