package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ShulkerEntity extends GolemEntity implements VariantHolder, Monster {
   private static final UUID COVERED_ARMOR_BONUS_ID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
   private static final EntityAttributeModifier COVERED_ARMOR_BONUS;
   protected static final TrackedData ATTACHED_FACE;
   protected static final TrackedData PEEK_AMOUNT;
   protected static final TrackedData COLOR;
   private static final int field_30487 = 6;
   private static final byte field_30488 = 16;
   private static final byte field_30489 = 16;
   private static final int field_30490 = 8;
   private static final int field_30491 = 8;
   private static final int field_30492 = 5;
   private static final float field_30493 = 0.05F;
   static final Vector3f SOUTH_VECTOR;
   private float prevOpenProgress;
   private float openProgress;
   @Nullable
   private BlockPos prevAttachedBlock;
   private int teleportLerpTimer;
   private static final float field_30494 = 1.0F;

   public ShulkerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.experiencePoints = 5;
      this.lookControl = new ShulkerLookControl(this);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F, 0.02F, true));
      this.goalSelector.add(4, new ShootBulletGoal());
      this.goalSelector.add(7, new PeekGoal());
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{this.getClass()})).setGroupRevenge());
      this.targetSelector.add(2, new TargetPlayerGoal(this));
      this.targetSelector.add(3, new TargetOtherTeamGoal(this));
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SHULKER_AMBIENT;
   }

   public void playAmbientSound() {
      if (!this.isClosed()) {
         super.playAmbientSound();
      }

   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SHULKER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isClosed() ? SoundEvents.ENTITY_SHULKER_HURT_CLOSED : SoundEvents.ENTITY_SHULKER_HURT;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(ATTACHED_FACE, Direction.DOWN);
      this.dataTracker.startTracking(PEEK_AMOUNT, (byte)0);
      this.dataTracker.startTracking(COLOR, (byte)16);
   }

   public static DefaultAttributeContainer.Builder createShulkerAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0);
   }

   protected BodyControl createBodyControl() {
      return new ShulkerBodyControl(this);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setAttachedFace(Direction.byId(nbt.getByte("AttachFace")));
      this.dataTracker.set(PEEK_AMOUNT, nbt.getByte("Peek"));
      if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
         this.dataTracker.set(COLOR, nbt.getByte("Color"));
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("AttachFace", (byte)this.getAttachedFace().getId());
      nbt.putByte("Peek", (Byte)this.dataTracker.get(PEEK_AMOUNT));
      nbt.putByte("Color", (Byte)this.dataTracker.get(COLOR));
   }

   public void tick() {
      super.tick();
      if (!this.world.isClient && !this.hasVehicle() && !this.canStay(this.getBlockPos(), this.getAttachedFace())) {
         this.tryAttachOrTeleport();
      }

      if (this.tickOpenProgress()) {
         this.moveEntities();
      }

      if (this.world.isClient) {
         if (this.teleportLerpTimer > 0) {
            --this.teleportLerpTimer;
         } else {
            this.prevAttachedBlock = null;
         }
      }

   }

   private void tryAttachOrTeleport() {
      Direction lv = this.findAttachSide(this.getBlockPos());
      if (lv != null) {
         this.setAttachedFace(lv);
      } else {
         this.tryTeleport();
      }

   }

   protected Box calculateBoundingBox() {
      float f = getExtraLength(this.openProgress);
      Direction lv = this.getAttachedFace().getOpposite();
      float g = this.getType().getWidth() / 2.0F;
      return calculateBoundingBox(lv, f).offset(this.getX() - (double)g, this.getY(), this.getZ() - (double)g);
   }

   private static float getExtraLength(float openProgress) {
      return 0.5F - MathHelper.sin((0.5F + openProgress) * 3.1415927F) * 0.5F;
   }

   private boolean tickOpenProgress() {
      this.prevOpenProgress = this.openProgress;
      float f = (float)this.getPeekAmount() * 0.01F;
      if (this.openProgress == f) {
         return false;
      } else {
         if (this.openProgress > f) {
            this.openProgress = MathHelper.clamp(this.openProgress - 0.05F, f, 1.0F);
         } else {
            this.openProgress = MathHelper.clamp(this.openProgress + 0.05F, 0.0F, f);
         }

         return true;
      }
   }

   private void moveEntities() {
      this.refreshPosition();
      float f = getExtraLength(this.openProgress);
      float g = getExtraLength(this.prevOpenProgress);
      Direction lv = this.getAttachedFace().getOpposite();
      float h = f - g;
      if (!(h <= 0.0F)) {
         List list = this.world.getOtherEntities(this, calculateBoundingBox(lv, g, f).offset(this.getX() - 0.5, this.getY(), this.getZ() - 0.5), EntityPredicates.EXCEPT_SPECTATOR.and((arg) -> {
            return !arg.isConnectedThroughVehicle(this);
         }));
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            Entity lv2 = (Entity)var6.next();
            if (!(lv2 instanceof ShulkerEntity) && !lv2.noClip) {
               lv2.move(MovementType.SHULKER, new Vec3d((double)(h * (float)lv.getOffsetX()), (double)(h * (float)lv.getOffsetY()), (double)(h * (float)lv.getOffsetZ())));
            }
         }

      }
   }

   public static Box calculateBoundingBox(Direction direction, float extraLength) {
      return calculateBoundingBox(direction, -1.0F, extraLength);
   }

   public static Box calculateBoundingBox(Direction direction, float prevExtraLength, float extraLength) {
      double d = (double)Math.max(prevExtraLength, extraLength);
      double e = (double)Math.min(prevExtraLength, extraLength);
      return (new Box(BlockPos.ORIGIN)).stretch((double)direction.getOffsetX() * d, (double)direction.getOffsetY() * d, (double)direction.getOffsetZ() * d).shrink((double)(-direction.getOffsetX()) * (1.0 + e), (double)(-direction.getOffsetY()) * (1.0 + e), (double)(-direction.getOffsetZ()) * (1.0 + e));
   }

   public double getHeightOffset() {
      EntityType lv = this.getVehicle().getType();
      return !(this.getVehicle() instanceof BoatEntity) && lv != EntityType.MINECART ? super.getHeightOffset() : 0.1875 - this.getVehicle().getMountedHeightOffset();
   }

   public boolean startRiding(Entity entity, boolean force) {
      if (this.world.isClient()) {
         this.prevAttachedBlock = null;
         this.teleportLerpTimer = 0;
      }

      this.setAttachedFace(Direction.DOWN);
      return super.startRiding(entity, force);
   }

   public void stopRiding() {
      super.stopRiding();
      if (this.world.isClient) {
         this.prevAttachedBlock = this.getBlockPos();
      }

      this.prevBodyYaw = 0.0F;
      this.bodyYaw = 0.0F;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setYaw(0.0F);
      this.headYaw = this.getYaw();
      this.resetPosition();
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (movementType == MovementType.SHULKER_BOX) {
         this.tryTeleport();
      } else {
         super.move(movementType, movement);
      }

   }

   public Vec3d getVelocity() {
      return Vec3d.ZERO;
   }

   public void setVelocity(Vec3d velocity) {
   }

   public void setPosition(double x, double y, double z) {
      BlockPos lv = this.getBlockPos();
      if (this.hasVehicle()) {
         super.setPosition(x, y, z);
      } else {
         super.setPosition((double)MathHelper.floor(x) + 0.5, (double)MathHelper.floor(y + 0.5), (double)MathHelper.floor(z) + 0.5);
      }

      if (this.age != 0) {
         BlockPos lv2 = this.getBlockPos();
         if (!lv2.equals(lv)) {
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.velocityDirty = true;
            if (this.world.isClient && !this.hasVehicle() && !lv2.equals(this.prevAttachedBlock)) {
               this.prevAttachedBlock = lv;
               this.teleportLerpTimer = 6;
               this.lastRenderX = this.getX();
               this.lastRenderY = this.getY();
               this.lastRenderZ = this.getZ();
            }
         }

      }
   }

   @Nullable
   protected Direction findAttachSide(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         if (this.canStay(pos, lv)) {
            return lv;
         }
      }

      return null;
   }

   boolean canStay(BlockPos pos, Direction direction) {
      if (this.isInvalidPosition(pos)) {
         return false;
      } else {
         Direction lv = direction.getOpposite();
         if (!this.world.isDirectionSolid(pos.offset(direction), this, lv)) {
            return false;
         } else {
            Box lv2 = calculateBoundingBox(lv, 1.0F).offset(pos).contract(1.0E-6);
            return this.world.isSpaceEmpty(this, lv2);
         }
      }
   }

   private boolean isInvalidPosition(BlockPos pos) {
      BlockState lv = this.world.getBlockState(pos);
      if (lv.isAir()) {
         return false;
      } else {
         boolean bl = lv.isOf(Blocks.MOVING_PISTON) && pos.equals(this.getBlockPos());
         return !bl;
      }
   }

   protected boolean tryTeleport() {
      if (!this.isAiDisabled() && this.isAlive()) {
         BlockPos lv = this.getBlockPos();

         for(int i = 0; i < 5; ++i) {
            BlockPos lv2 = lv.add(MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8));
            if (lv2.getY() > this.world.getBottomY() && this.world.isAir(lv2) && this.world.getWorldBorder().contains(lv2) && this.world.isSpaceEmpty(this, (new Box(lv2)).contract(1.0E-6))) {
               Direction lv3 = this.findAttachSide(lv2);
               if (lv3 != null) {
                  this.detach();
                  this.setAttachedFace(lv3);
                  this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0F, 1.0F);
                  this.setPosition((double)lv2.getX() + 0.5, (double)lv2.getY(), (double)lv2.getZ() + 0.5);
                  this.world.emitGameEvent(GameEvent.TELEPORT, lv, GameEvent.Emitter.of((Entity)this));
                  this.dataTracker.set(PEEK_AMOUNT, (byte)0);
                  this.setTarget((LivingEntity)null);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.bodyTrackingIncrements = 0;
      this.setPosition(x, y, z);
      this.setRotation(yaw, pitch);
   }

   public boolean damage(DamageSource source, float amount) {
      Entity lv;
      if (this.isClosed()) {
         lv = source.getSource();
         if (lv instanceof PersistentProjectileEntity) {
            return false;
         }
      }

      if (!super.damage(source, amount)) {
         return false;
      } else {
         if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
            this.tryTeleport();
         } else if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            lv = source.getSource();
            if (lv != null && lv.getType() == EntityType.SHULKER_BULLET) {
               this.spawnNewShulker();
            }
         }

         return true;
      }
   }

   private boolean isClosed() {
      return this.getPeekAmount() == 0;
   }

   private void spawnNewShulker() {
      Vec3d lv = this.getPos();
      Box lv2 = this.getBoundingBox();
      if (!this.isClosed() && this.tryTeleport()) {
         int i = this.world.getEntitiesByType(EntityType.SHULKER, lv2.expand(8.0), Entity::isAlive).size();
         float f = (float)(i - 1) / 5.0F;
         if (!(this.world.random.nextFloat() < f)) {
            ShulkerEntity lv3 = (ShulkerEntity)EntityType.SHULKER.create(this.world);
            if (lv3 != null) {
               lv3.setVariant(this.getVariant());
               lv3.refreshPositionAfterTeleport(lv);
               this.world.spawnEntity(lv3);
            }

         }
      }
   }

   public boolean isCollidable() {
      return this.isAlive();
   }

   public Direction getAttachedFace() {
      return (Direction)this.dataTracker.get(ATTACHED_FACE);
   }

   private void setAttachedFace(Direction face) {
      this.dataTracker.set(ATTACHED_FACE, face);
   }

   public void onTrackedDataSet(TrackedData data) {
      if (ATTACHED_FACE.equals(data)) {
         this.setBoundingBox(this.calculateBoundingBox());
      }

      super.onTrackedDataSet(data);
   }

   private int getPeekAmount() {
      return (Byte)this.dataTracker.get(PEEK_AMOUNT);
   }

   void setPeekAmount(int peekAmount) {
      if (!this.world.isClient) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(COVERED_ARMOR_BONUS);
         if (peekAmount == 0) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addPersistentModifier(COVERED_ARMOR_BONUS);
            this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0F, 1.0F);
            this.emitGameEvent(GameEvent.CONTAINER_CLOSE);
         } else {
            this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0F, 1.0F);
            this.emitGameEvent(GameEvent.CONTAINER_OPEN);
         }
      }

      this.dataTracker.set(PEEK_AMOUNT, (byte)peekAmount);
   }

   public float getOpenProgress(float delta) {
      return MathHelper.lerp(delta, this.prevOpenProgress, this.openProgress);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.5F;
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      this.bodyYaw = 0.0F;
      this.prevBodyYaw = 0.0F;
   }

   public int getMaxLookPitchChange() {
      return 180;
   }

   public int getMaxHeadRotation() {
      return 180;
   }

   public void pushAwayFrom(Entity entity) {
   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   public Optional getRenderPositionOffset(float tickDelta) {
      if (this.prevAttachedBlock != null && this.teleportLerpTimer > 0) {
         double d = (double)((float)this.teleportLerpTimer - tickDelta) / 6.0;
         d *= d;
         BlockPos lv = this.getBlockPos();
         double e = (double)(lv.getX() - this.prevAttachedBlock.getX()) * d;
         double g = (double)(lv.getY() - this.prevAttachedBlock.getY()) * d;
         double h = (double)(lv.getZ() - this.prevAttachedBlock.getZ()) * d;
         return Optional.of(new Vec3d(-e, -g, -h));
      } else {
         return Optional.empty();
      }
   }

   public void setVariant(Optional optional) {
      this.dataTracker.set(COLOR, (Byte)optional.map((color) -> {
         return (byte)color.getId();
      }).orElse((byte)16));
   }

   public Optional getVariant() {
      return Optional.ofNullable(this.getColor());
   }

   @Nullable
   public DyeColor getColor() {
      byte b = (Byte)this.dataTracker.get(COLOR);
      return b != 16 && b <= 15 ? DyeColor.byId(b) : null;
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      COVERED_ARMOR_BONUS = new EntityAttributeModifier(COVERED_ARMOR_BONUS_ID, "Covered armor bonus", 20.0, EntityAttributeModifier.Operation.ADDITION);
      ATTACHED_FACE = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.FACING);
      PEEK_AMOUNT = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
      COLOR = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
      SOUTH_VECTOR = (Vector3f)Util.make(() -> {
         Vec3i lv = Direction.SOUTH.getVector();
         return new Vector3f((float)lv.getX(), (float)lv.getY(), (float)lv.getZ());
      });
   }

   private class ShulkerLookControl extends LookControl {
      public ShulkerLookControl(MobEntity entity) {
         super(entity);
      }

      protected void clampHeadYaw() {
      }

      protected Optional getTargetYaw() {
         Direction lv = ShulkerEntity.this.getAttachedFace().getOpposite();
         Vector3f vector3f = lv.getRotationQuaternion().transform(new Vector3f(ShulkerEntity.SOUTH_VECTOR));
         Vec3i lv2 = lv.getVector();
         Vector3f vector3f2 = new Vector3f((float)lv2.getX(), (float)lv2.getY(), (float)lv2.getZ());
         vector3f2.cross(vector3f);
         double d = this.x - this.entity.getX();
         double e = this.y - this.entity.getEyeY();
         double f = this.z - this.entity.getZ();
         Vector3f vector3f3 = new Vector3f((float)d, (float)e, (float)f);
         float g = vector3f2.dot(vector3f3);
         float h = vector3f.dot(vector3f3);
         return !(Math.abs(g) > 1.0E-5F) && !(Math.abs(h) > 1.0E-5F) ? Optional.empty() : Optional.of((float)(MathHelper.atan2((double)(-g), (double)h) * 57.2957763671875));
      }

      protected Optional getTargetPitch() {
         return Optional.of(0.0F);
      }
   }

   private class ShootBulletGoal extends Goal {
      private int counter;

      public ShootBulletGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         LivingEntity lv = ShulkerEntity.this.getTarget();
         if (lv != null && lv.isAlive()) {
            return ShulkerEntity.this.world.getDifficulty() != Difficulty.PEACEFUL;
         } else {
            return false;
         }
      }

      public void start() {
         this.counter = 20;
         ShulkerEntity.this.setPeekAmount(100);
      }

      public void stop() {
         ShulkerEntity.this.setPeekAmount(0);
      }

      public boolean shouldRunEveryTick() {
         return true;
      }

      public void tick() {
         if (ShulkerEntity.this.world.getDifficulty() != Difficulty.PEACEFUL) {
            --this.counter;
            LivingEntity lv = ShulkerEntity.this.getTarget();
            if (lv != null) {
               ShulkerEntity.this.getLookControl().lookAt(lv, 180.0F, 180.0F);
               double d = ShulkerEntity.this.squaredDistanceTo(lv);
               if (d < 400.0) {
                  if (this.counter <= 0) {
                     this.counter = 20 + ShulkerEntity.this.random.nextInt(10) * 20 / 2;
                     ShulkerEntity.this.world.spawnEntity(new ShulkerBulletEntity(ShulkerEntity.this.world, ShulkerEntity.this, lv, ShulkerEntity.this.getAttachedFace().getAxis()));
                     ShulkerEntity.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (ShulkerEntity.this.random.nextFloat() - ShulkerEntity.this.random.nextFloat()) * 0.2F + 1.0F);
                  }
               } else {
                  ShulkerEntity.this.setTarget((LivingEntity)null);
               }

               super.tick();
            }
         }
      }
   }

   class PeekGoal extends Goal {
      private int counter;

      public boolean canStart() {
         return ShulkerEntity.this.getTarget() == null && ShulkerEntity.this.random.nextInt(toGoalTicks(40)) == 0 && ShulkerEntity.this.canStay(ShulkerEntity.this.getBlockPos(), ShulkerEntity.this.getAttachedFace());
      }

      public boolean shouldContinue() {
         return ShulkerEntity.this.getTarget() == null && this.counter > 0;
      }

      public void start() {
         this.counter = this.getTickCount(20 * (1 + ShulkerEntity.this.random.nextInt(3)));
         ShulkerEntity.this.setPeekAmount(30);
      }

      public void stop() {
         if (ShulkerEntity.this.getTarget() == null) {
            ShulkerEntity.this.setPeekAmount(0);
         }

      }

      public void tick() {
         --this.counter;
      }
   }

   class TargetPlayerGoal extends ActiveTargetGoal {
      public TargetPlayerGoal(ShulkerEntity shulker) {
         super(shulker, PlayerEntity.class, true);
      }

      public boolean canStart() {
         return ShulkerEntity.this.world.getDifficulty() == Difficulty.PEACEFUL ? false : super.canStart();
      }

      protected Box getSearchBox(double distance) {
         Direction lv = ((ShulkerEntity)this.mob).getAttachedFace();
         if (lv.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().expand(4.0, distance, distance);
         } else {
            return lv.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().expand(distance, distance, 4.0) : this.mob.getBoundingBox().expand(distance, 4.0, distance);
         }
      }
   }

   private static class TargetOtherTeamGoal extends ActiveTargetGoal {
      public TargetOtherTeamGoal(ShulkerEntity shulker) {
         super(shulker, LivingEntity.class, 10, true, false, (entity) -> {
            return entity instanceof Monster;
         });
      }

      public boolean canStart() {
         return this.mob.getScoreboardTeam() == null ? false : super.canStart();
      }

      protected Box getSearchBox(double distance) {
         Direction lv = ((ShulkerEntity)this.mob).getAttachedFace();
         if (lv.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().expand(4.0, distance, distance);
         } else {
            return lv.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().expand(distance, distance, 4.0) : this.mob.getBoundingBox().expand(distance, 4.0, distance);
         }
      }
   }

   private static class ShulkerBodyControl extends BodyControl {
      public ShulkerBodyControl(MobEntity arg) {
         super(arg);
      }

      public void tick() {
      }
   }
}
