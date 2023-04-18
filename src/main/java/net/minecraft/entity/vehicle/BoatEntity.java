package net.minecraft.entity.vehicle;

import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BoatEntity extends Entity implements VariantHolder {
   private static final TrackedData DAMAGE_WOBBLE_TICKS;
   private static final TrackedData DAMAGE_WOBBLE_SIDE;
   private static final TrackedData DAMAGE_WOBBLE_STRENGTH;
   private static final TrackedData BOAT_TYPE;
   private static final TrackedData LEFT_PADDLE_MOVING;
   private static final TrackedData RIGHT_PADDLE_MOVING;
   private static final TrackedData BUBBLE_WOBBLE_TICKS;
   public static final int field_30697 = 0;
   public static final int field_30698 = 1;
   private static final int field_30695 = 60;
   private static final float NEXT_PADDLE_PHASE = 0.3926991F;
   public static final double EMIT_SOUND_EVENT_PADDLE_ROTATION = 0.7853981852531433;
   public static final int field_30700 = 60;
   private final float[] paddlePhases;
   private float velocityDecay;
   private float ticksUnderwater;
   private float yawVelocity;
   private int field_7708;
   private double x;
   private double y;
   private double z;
   private double boatYaw;
   private double boatPitch;
   private boolean pressingLeft;
   private boolean pressingRight;
   private boolean pressingForward;
   private boolean pressingBack;
   private double waterLevel;
   private float nearbySlipperiness;
   private Location location;
   private Location lastLocation;
   private double fallVelocity;
   private boolean onBubbleColumnSurface;
   private boolean bubbleColumnIsDrag;
   private float bubbleWobbleStrength;
   private float bubbleWobble;
   private float lastBubbleWobble;

   public BoatEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.paddlePhases = new float[2];
      this.intersectionChecked = true;
   }

   public BoatEntity(World world, double x, double y, double z) {
      this(EntityType.BOAT, world);
      this.setPosition(x, y, z);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height;
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.EVENTS;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);
      this.dataTracker.startTracking(BOAT_TYPE, BoatEntity.Type.OAK.ordinal());
      this.dataTracker.startTracking(LEFT_PADDLE_MOVING, false);
      this.dataTracker.startTracking(RIGHT_PADDLE_MOVING, false);
      this.dataTracker.startTracking(BUBBLE_WOBBLE_TICKS, 0);
   }

   public boolean collidesWith(Entity other) {
      return canCollide(this, other);
   }

   public static boolean canCollide(Entity entity, Entity other) {
      return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
   }

   public boolean isCollidable() {
      return true;
   }

   public boolean isPushable() {
      return true;
   }

   protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
      return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
   }

   public double getMountedHeightOffset() {
      return this.getVariant() == BoatEntity.Type.BAMBOO ? 0.25 : -0.1;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!this.world.isClient && !this.isRemoved()) {
         this.setDamageWobbleSide(-this.getDamageWobbleSide());
         this.setDamageWobbleTicks(10);
         this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
         this.scheduleVelocityUpdate();
         this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
         boolean bl = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode;
         if (bl || this.getDamageWobbleStrength() > 40.0F) {
            if (!bl && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
               this.dropItems(source);
            }

            this.discard();
         }

         return true;
      } else {
         return true;
      }
   }

   protected void dropItems(DamageSource source) {
      this.dropItem(this.asItem());
   }

   public void onBubbleColumnSurfaceCollision(boolean drag) {
      if (!this.world.isClient) {
         this.onBubbleColumnSurface = true;
         this.bubbleColumnIsDrag = drag;
         if (this.getBubbleWobbleTicks() == 0) {
            this.setBubbleWobbleTicks(60);
         }
      }

      this.world.addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
      if (this.random.nextInt(20) == 0) {
         this.world.playSound(this.getX(), this.getY(), this.getZ(), this.getSplashSound(), this.getSoundCategory(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
         this.emitGameEvent(GameEvent.SPLASH, this.getControllingPassenger());
      }

   }

   public void pushAwayFrom(Entity entity) {
      if (entity instanceof BoatEntity) {
         if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            super.pushAwayFrom(entity);
         }
      } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
         super.pushAwayFrom(entity);
      }

   }

   public Item asItem() {
      Item var10000;
      switch (this.getVariant()) {
         case SPRUCE:
            var10000 = Items.SPRUCE_BOAT;
            break;
         case BIRCH:
            var10000 = Items.BIRCH_BOAT;
            break;
         case JUNGLE:
            var10000 = Items.JUNGLE_BOAT;
            break;
         case ACACIA:
            var10000 = Items.ACACIA_BOAT;
            break;
         case CHERRY:
            var10000 = Items.CHERRY_BOAT;
            break;
         case DARK_OAK:
            var10000 = Items.DARK_OAK_BOAT;
            break;
         case MANGROVE:
            var10000 = Items.MANGROVE_BOAT;
            break;
         case BAMBOO:
            var10000 = Items.BAMBOO_RAFT;
            break;
         default:
            var10000 = Items.OAK_BOAT;
      }

      return var10000;
   }

   public void animateDamage(float yaw) {
      this.setDamageWobbleSide(-this.getDamageWobbleSide());
      this.setDamageWobbleTicks(10);
      this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0F);
   }

   public boolean canHit() {
      return !this.isRemoved();
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.boatYaw = (double)yaw;
      this.boatPitch = (double)pitch;
      this.field_7708 = 10;
   }

   public Direction getMovementDirection() {
      return this.getHorizontalFacing().rotateYClockwise();
   }

   public void tick() {
      this.lastLocation = this.location;
      this.location = this.checkLocation();
      if (this.location != BoatEntity.Location.UNDER_WATER && this.location != BoatEntity.Location.UNDER_FLOWING_WATER) {
         this.ticksUnderwater = 0.0F;
      } else {
         ++this.ticksUnderwater;
      }

      if (!this.world.isClient && this.ticksUnderwater >= 60.0F) {
         this.removeAllPassengers();
      }

      if (this.getDamageWobbleTicks() > 0) {
         this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
      }

      if (this.getDamageWobbleStrength() > 0.0F) {
         this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
      }

      super.tick();
      this.updatePositionAndRotation();
      if (this.isLogicalSideForUpdatingMovement()) {
         if (!(this.getFirstPassenger() instanceof PlayerEntity)) {
            this.setPaddleMovings(false, false);
         }

         this.updateVelocity();
         if (this.world.isClient) {
            this.updatePaddles();
            this.world.sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
         }

         this.move(MovementType.SELF, this.getVelocity());
      } else {
         this.setVelocity(Vec3d.ZERO);
      }

      this.handleBubbleColumn();

      for(int i = 0; i <= 1; ++i) {
         if (this.isPaddleMoving(i)) {
            if (!this.isSilent() && (double)(this.paddlePhases[i] % 6.2831855F) <= 0.7853981852531433 && (double)((this.paddlePhases[i] + 0.3926991F) % 6.2831855F) >= 0.7853981852531433) {
               SoundEvent lv = this.getPaddleSoundEvent();
               if (lv != null) {
                  Vec3d lv2 = this.getRotationVec(1.0F);
                  double d = i == 1 ? -lv2.z : lv2.z;
                  double e = i == 1 ? lv2.x : -lv2.x;
                  this.world.playSound((PlayerEntity)null, this.getX() + d, this.getY(), this.getZ() + e, lv, this.getSoundCategory(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
               }
            }

            float[] var10000 = this.paddlePhases;
            var10000[i] += 0.3926991F;
         } else {
            this.paddlePhases[i] = 0.0F;
         }
      }

      this.checkBlockCollision();
      List list = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.20000000298023224, -0.009999999776482582, 0.20000000298023224), EntityPredicates.canBePushedBy(this));
      if (!list.isEmpty()) {
         boolean bl = !this.world.isClient && !(this.getControllingPassenger() instanceof PlayerEntity);

         for(int j = 0; j < list.size(); ++j) {
            Entity lv3 = (Entity)list.get(j);
            if (!lv3.hasPassenger((Entity)this)) {
               if (bl && this.getPassengerList().size() < this.getMaxPassengers() && !lv3.hasVehicle() && this.isSmallerThanBoat(lv3) && lv3 instanceof LivingEntity && !(lv3 instanceof WaterCreatureEntity) && !(lv3 instanceof PlayerEntity)) {
                  lv3.startRiding(this);
               } else {
                  this.pushAwayFrom(lv3);
               }
            }
         }
      }

   }

   private void handleBubbleColumn() {
      int i;
      if (this.world.isClient) {
         i = this.getBubbleWobbleTicks();
         if (i > 0) {
            this.bubbleWobbleStrength += 0.05F;
         } else {
            this.bubbleWobbleStrength -= 0.1F;
         }

         this.bubbleWobbleStrength = MathHelper.clamp(this.bubbleWobbleStrength, 0.0F, 1.0F);
         this.lastBubbleWobble = this.bubbleWobble;
         this.bubbleWobble = 10.0F * (float)Math.sin((double)(0.5F * (float)this.world.getTime())) * this.bubbleWobbleStrength;
      } else {
         if (!this.onBubbleColumnSurface) {
            this.setBubbleWobbleTicks(0);
         }

         i = this.getBubbleWobbleTicks();
         if (i > 0) {
            --i;
            this.setBubbleWobbleTicks(i);
            int j = 60 - i - 1;
            if (j > 0 && i == 0) {
               this.setBubbleWobbleTicks(0);
               Vec3d lv = this.getVelocity();
               if (this.bubbleColumnIsDrag) {
                  this.setVelocity(lv.add(0.0, -0.7, 0.0));
                  this.removeAllPassengers();
               } else {
                  this.setVelocity(lv.x, this.hasPassenger((entity) -> {
                     return entity instanceof PlayerEntity;
                  }) ? 2.7 : 0.6, lv.z);
               }
            }

            this.onBubbleColumnSurface = false;
         }
      }

   }

   @Nullable
   protected SoundEvent getPaddleSoundEvent() {
      switch (this.checkLocation()) {
         case IN_WATER:
         case UNDER_WATER:
         case UNDER_FLOWING_WATER:
            return SoundEvents.ENTITY_BOAT_PADDLE_WATER;
         case ON_LAND:
            return SoundEvents.ENTITY_BOAT_PADDLE_LAND;
         case IN_AIR:
         default:
            return null;
      }
   }

   private void updatePositionAndRotation() {
      if (this.isLogicalSideForUpdatingMovement()) {
         this.field_7708 = 0;
         this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
      }

      if (this.field_7708 > 0) {
         double d = this.getX() + (this.x - this.getX()) / (double)this.field_7708;
         double e = this.getY() + (this.y - this.getY()) / (double)this.field_7708;
         double f = this.getZ() + (this.z - this.getZ()) / (double)this.field_7708;
         double g = MathHelper.wrapDegrees(this.boatYaw - (double)this.getYaw());
         this.setYaw(this.getYaw() + (float)g / (float)this.field_7708);
         this.setPitch(this.getPitch() + (float)(this.boatPitch - (double)this.getPitch()) / (float)this.field_7708);
         --this.field_7708;
         this.setPosition(d, e, f);
         this.setRotation(this.getYaw(), this.getPitch());
      }
   }

   public void setPaddleMovings(boolean leftMoving, boolean rightMoving) {
      this.dataTracker.set(LEFT_PADDLE_MOVING, leftMoving);
      this.dataTracker.set(RIGHT_PADDLE_MOVING, rightMoving);
   }

   public float interpolatePaddlePhase(int paddle, float tickDelta) {
      return this.isPaddleMoving(paddle) ? MathHelper.clampedLerp(this.paddlePhases[paddle] - 0.3926991F, this.paddlePhases[paddle], tickDelta) : 0.0F;
   }

   private Location checkLocation() {
      Location lv = this.getUnderWaterLocation();
      if (lv != null) {
         this.waterLevel = this.getBoundingBox().maxY;
         return lv;
      } else if (this.checkBoatInWater()) {
         return BoatEntity.Location.IN_WATER;
      } else {
         float f = this.getNearbySlipperiness();
         if (f > 0.0F) {
            this.nearbySlipperiness = f;
            return BoatEntity.Location.ON_LAND;
         } else {
            return BoatEntity.Location.IN_AIR;
         }
      }
   }

   public float getWaterHeightBelow() {
      Box lv = this.getBoundingBox();
      int i = MathHelper.floor(lv.minX);
      int j = MathHelper.ceil(lv.maxX);
      int k = MathHelper.floor(lv.maxY);
      int l = MathHelper.ceil(lv.maxY - this.fallVelocity);
      int m = MathHelper.floor(lv.minZ);
      int n = MathHelper.ceil(lv.maxZ);
      BlockPos.Mutable lv2 = new BlockPos.Mutable();

      label39:
      for(int o = k; o < l; ++o) {
         float f = 0.0F;

         for(int p = i; p < j; ++p) {
            for(int q = m; q < n; ++q) {
               lv2.set(p, o, q);
               FluidState lv3 = this.world.getFluidState(lv2);
               if (lv3.isIn(FluidTags.WATER)) {
                  f = Math.max(f, lv3.getHeight(this.world, lv2));
               }

               if (f >= 1.0F) {
                  continue label39;
               }
            }
         }

         if (f < 1.0F) {
            return (float)lv2.getY() + f;
         }
      }

      return (float)(l + 1);
   }

   public float getNearbySlipperiness() {
      Box lv = this.getBoundingBox();
      Box lv2 = new Box(lv.minX, lv.minY - 0.001, lv.minZ, lv.maxX, lv.minY, lv.maxZ);
      int i = MathHelper.floor(lv2.minX) - 1;
      int j = MathHelper.ceil(lv2.maxX) + 1;
      int k = MathHelper.floor(lv2.minY) - 1;
      int l = MathHelper.ceil(lv2.maxY) + 1;
      int m = MathHelper.floor(lv2.minZ) - 1;
      int n = MathHelper.ceil(lv2.maxZ) + 1;
      VoxelShape lv3 = VoxelShapes.cuboid(lv2);
      float f = 0.0F;
      int o = 0;
      BlockPos.Mutable lv4 = new BlockPos.Mutable();

      for(int p = i; p < j; ++p) {
         for(int q = m; q < n; ++q) {
            int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
            if (r != 2) {
               for(int s = k; s < l; ++s) {
                  if (r <= 0 || s != k && s != l - 1) {
                     lv4.set(p, s, q);
                     BlockState lv5 = this.world.getBlockState(lv4);
                     if (!(lv5.getBlock() instanceof LilyPadBlock) && VoxelShapes.matchesAnywhere(lv5.getCollisionShape(this.world, lv4).offset((double)p, (double)s, (double)q), lv3, BooleanBiFunction.AND)) {
                        f += lv5.getBlock().getSlipperiness();
                        ++o;
                     }
                  }
               }
            }
         }
      }

      return f / (float)o;
   }

   private boolean checkBoatInWater() {
      Box lv = this.getBoundingBox();
      int i = MathHelper.floor(lv.minX);
      int j = MathHelper.ceil(lv.maxX);
      int k = MathHelper.floor(lv.minY);
      int l = MathHelper.ceil(lv.minY + 0.001);
      int m = MathHelper.floor(lv.minZ);
      int n = MathHelper.ceil(lv.maxZ);
      boolean bl = false;
      this.waterLevel = -1.7976931348623157E308;
      BlockPos.Mutable lv2 = new BlockPos.Mutable();

      for(int o = i; o < j; ++o) {
         for(int p = k; p < l; ++p) {
            for(int q = m; q < n; ++q) {
               lv2.set(o, p, q);
               FluidState lv3 = this.world.getFluidState(lv2);
               if (lv3.isIn(FluidTags.WATER)) {
                  float f = (float)p + lv3.getHeight(this.world, lv2);
                  this.waterLevel = Math.max((double)f, this.waterLevel);
                  bl |= lv.minY < (double)f;
               }
            }
         }
      }

      return bl;
   }

   @Nullable
   private Location getUnderWaterLocation() {
      Box lv = this.getBoundingBox();
      double d = lv.maxY + 0.001;
      int i = MathHelper.floor(lv.minX);
      int j = MathHelper.ceil(lv.maxX);
      int k = MathHelper.floor(lv.maxY);
      int l = MathHelper.ceil(d);
      int m = MathHelper.floor(lv.minZ);
      int n = MathHelper.ceil(lv.maxZ);
      boolean bl = false;
      BlockPos.Mutable lv2 = new BlockPos.Mutable();

      for(int o = i; o < j; ++o) {
         for(int p = k; p < l; ++p) {
            for(int q = m; q < n; ++q) {
               lv2.set(o, p, q);
               FluidState lv3 = this.world.getFluidState(lv2);
               if (lv3.isIn(FluidTags.WATER) && d < (double)((float)lv2.getY() + lv3.getHeight(this.world, lv2))) {
                  if (!lv3.isStill()) {
                     return BoatEntity.Location.UNDER_FLOWING_WATER;
                  }

                  bl = true;
               }
            }
         }
      }

      return bl ? BoatEntity.Location.UNDER_WATER : null;
   }

   private void updateVelocity() {
      double d = -0.03999999910593033;
      double e = this.hasNoGravity() ? 0.0 : -0.03999999910593033;
      double f = 0.0;
      this.velocityDecay = 0.05F;
      if (this.lastLocation == BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.ON_LAND) {
         this.waterLevel = this.getBodyY(1.0);
         this.setPosition(this.getX(), (double)(this.getWaterHeightBelow() - this.getHeight()) + 0.101, this.getZ());
         this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
         this.fallVelocity = 0.0;
         this.location = BoatEntity.Location.IN_WATER;
      } else {
         if (this.location == BoatEntity.Location.IN_WATER) {
            f = (this.waterLevel - this.getY()) / (double)this.getHeight();
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.UNDER_FLOWING_WATER) {
            e = -7.0E-4;
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.UNDER_WATER) {
            f = 0.009999999776482582;
            this.velocityDecay = 0.45F;
         } else if (this.location == BoatEntity.Location.IN_AIR) {
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.ON_LAND) {
            this.velocityDecay = this.nearbySlipperiness;
            if (this.getControllingPassenger() instanceof PlayerEntity) {
               this.nearbySlipperiness /= 2.0F;
            }
         }

         Vec3d lv = this.getVelocity();
         this.setVelocity(lv.x * (double)this.velocityDecay, lv.y + e, lv.z * (double)this.velocityDecay);
         this.yawVelocity *= this.velocityDecay;
         if (f > 0.0) {
            Vec3d lv2 = this.getVelocity();
            this.setVelocity(lv2.x, (lv2.y + f * 0.06153846016296973) * 0.75, lv2.z);
         }
      }

   }

   private void updatePaddles() {
      if (this.hasPassengers()) {
         float f = 0.0F;
         if (this.pressingLeft) {
            --this.yawVelocity;
         }

         if (this.pressingRight) {
            ++this.yawVelocity;
         }

         if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
            f += 0.005F;
         }

         this.setYaw(this.getYaw() + this.yawVelocity);
         if (this.pressingForward) {
            f += 0.04F;
         }

         if (this.pressingBack) {
            f -= 0.005F;
         }

         this.setVelocity(this.getVelocity().add((double)(MathHelper.sin(-this.getYaw() * 0.017453292F) * f), 0.0, (double)(MathHelper.cos(this.getYaw() * 0.017453292F) * f)));
         this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
      }
   }

   protected float getPassengerHorizontalOffset() {
      return 0.0F;
   }

   public boolean isSmallerThanBoat(Entity entity) {
      return entity.getWidth() < this.getWidth();
   }

   public void updatePassengerPosition(Entity passenger) {
      if (this.hasPassenger(passenger)) {
         float f = this.getPassengerHorizontalOffset();
         float g = (float)((this.isRemoved() ? 0.009999999776482582 : this.getMountedHeightOffset()) + passenger.getHeightOffset());
         if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            if (i == 0) {
               f = 0.2F;
            } else {
               f = -0.6F;
            }

            if (passenger instanceof AnimalEntity) {
               f += 0.2F;
            }
         }

         Vec3d lv = (new Vec3d((double)f, 0.0, 0.0)).rotateY(-this.getYaw() * 0.017453292F - 1.5707964F);
         passenger.setPosition(this.getX() + lv.x, this.getY() + (double)g, this.getZ() + lv.z);
         passenger.setYaw(passenger.getYaw() + this.yawVelocity);
         passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
         this.copyEntityData(passenger);
         if (passenger instanceof AnimalEntity && this.getPassengerList().size() == this.getMaxPassengers()) {
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
         }

      }
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Vec3d lv = getPassengerDismountOffset((double)(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO), (double)passenger.getWidth(), passenger.getYaw());
      double d = this.getX() + lv.x;
      double e = this.getZ() + lv.z;
      BlockPos lv2 = BlockPos.ofFloored(d, this.getBoundingBox().maxY, e);
      BlockPos lv3 = lv2.down();
      if (!this.world.isWater(lv3)) {
         List list = Lists.newArrayList();
         double f = this.world.getDismountHeight(lv2);
         if (Dismounting.canDismountInBlock(f)) {
            list.add(new Vec3d(d, (double)lv2.getY() + f, e));
         }

         double g = this.world.getDismountHeight(lv3);
         if (Dismounting.canDismountInBlock(g)) {
            list.add(new Vec3d(d, (double)lv3.getY() + g, e));
         }

         UnmodifiableIterator var14 = passenger.getPoses().iterator();

         while(var14.hasNext()) {
            EntityPose lv4 = (EntityPose)var14.next();
            Iterator var16 = list.iterator();

            while(var16.hasNext()) {
               Vec3d lv5 = (Vec3d)var16.next();
               if (Dismounting.canPlaceEntityAt(this.world, lv5, passenger, lv4)) {
                  passenger.setPose(lv4);
                  return lv5;
               }
            }
         }
      }

      return super.updatePassengerForDismount(passenger);
   }

   protected void copyEntityData(Entity entity) {
      entity.setBodyYaw(this.getYaw());
      float f = MathHelper.wrapDegrees(entity.getYaw() - this.getYaw());
      float g = MathHelper.clamp(f, -105.0F, 105.0F);
      entity.prevYaw += g - f;
      entity.setYaw(entity.getYaw() + g - f);
      entity.setHeadYaw(entity.getYaw());
   }

   public void onPassengerLookAround(Entity passenger) {
      this.copyEntityData(passenger);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putString("Type", this.getVariant().asString());
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.contains("Type", NbtElement.STRING_TYPE)) {
         this.setVariant(BoatEntity.Type.getType(nbt.getString("Type")));
      }

   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (player.shouldCancelInteraction()) {
         return ActionResult.PASS;
      } else if (this.ticksUnderwater < 60.0F) {
         if (!this.world.isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
         } else {
            return ActionResult.SUCCESS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
      this.fallVelocity = this.getVelocity().y;
      if (!this.hasVehicle()) {
         if (onGround) {
            if (this.fallDistance > 3.0F) {
               if (this.location != BoatEntity.Location.ON_LAND) {
                  this.onLanding();
                  return;
               }

               this.handleFallDamage(this.fallDistance, 1.0F, this.getDamageSources().fall());
               if (!this.world.isClient && !this.isRemoved()) {
                  this.kill();
                  if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                     int i;
                     for(i = 0; i < 3; ++i) {
                        this.dropItem(this.getVariant().getBaseBlock());
                     }

                     for(i = 0; i < 2; ++i) {
                        this.dropItem(Items.STICK);
                     }
                  }
               }
            }

            this.onLanding();
         } else if (!this.world.getFluidState(this.getBlockPos().down()).isIn(FluidTags.WATER) && heightDifference < 0.0) {
            this.fallDistance -= (float)heightDifference;
         }

      }
   }

   public boolean isPaddleMoving(int paddle) {
      return (Boolean)this.dataTracker.get(paddle == 0 ? LEFT_PADDLE_MOVING : RIGHT_PADDLE_MOVING) && this.getControllingPassenger() != null;
   }

   public void setDamageWobbleStrength(float wobbleStrength) {
      this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
   }

   public float getDamageWobbleStrength() {
      return (Float)this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
   }

   public void setDamageWobbleTicks(int wobbleTicks) {
      this.dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
   }

   public int getDamageWobbleTicks() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
   }

   private void setBubbleWobbleTicks(int wobbleTicks) {
      this.dataTracker.set(BUBBLE_WOBBLE_TICKS, wobbleTicks);
   }

   private int getBubbleWobbleTicks() {
      return (Integer)this.dataTracker.get(BUBBLE_WOBBLE_TICKS);
   }

   public float interpolateBubbleWobble(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastBubbleWobble, this.bubbleWobble);
   }

   public void setDamageWobbleSide(int side) {
      this.dataTracker.set(DAMAGE_WOBBLE_SIDE, side);
   }

   public int getDamageWobbleSide() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
   }

   public void setVariant(Type arg) {
      this.dataTracker.set(BOAT_TYPE, arg.ordinal());
   }

   public Type getVariant() {
      return BoatEntity.Type.getType((Integer)this.dataTracker.get(BOAT_TYPE));
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengerList().size() < this.getMaxPassengers() && !this.isSubmergedIn(FluidTags.WATER);
   }

   protected int getMaxPassengers() {
      return 2;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity var2 = this.getFirstPassenger();
      LivingEntity var10000;
      if (var2 instanceof LivingEntity lv) {
         var10000 = lv;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack) {
      this.pressingLeft = pressingLeft;
      this.pressingRight = pressingRight;
      this.pressingForward = pressingForward;
      this.pressingBack = pressingBack;
   }

   public boolean isSubmergedInWater() {
      return this.location == BoatEntity.Location.UNDER_WATER || this.location == BoatEntity.Location.UNDER_FLOWING_WATER;
   }

   public ItemStack getPickBlockStack() {
      return new ItemStack(this.asItem());
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      DAMAGE_WOBBLE_TICKS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_SIDE = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.FLOAT);
      BOAT_TYPE = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      LEFT_PADDLE_MOVING = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      RIGHT_PADDLE_MOVING = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      BUBBLE_WOBBLE_TICKS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static enum Type implements StringIdentifiable {
      OAK(Blocks.OAK_PLANKS, "oak"),
      SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
      BIRCH(Blocks.BIRCH_PLANKS, "birch"),
      JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
      ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
      CHERRY(Blocks.CHERRY_PLANKS, "cherry"),
      DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak"),
      MANGROVE(Blocks.MANGROVE_PLANKS, "mangrove"),
      BAMBOO(Blocks.BAMBOO_PLANKS, "bamboo");

      private final String name;
      private final Block baseBlock;
      public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Type::values);
      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Enum::ordinal, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);

      private Type(Block baseBlock, String name) {
         this.name = name;
         this.baseBlock = baseBlock;
      }

      public String asString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }

      public Block getBaseBlock() {
         return this.baseBlock;
      }

      public String toString() {
         return this.name;
      }

      public static Type getType(int type) {
         return (Type)BY_ID.apply(type);
      }

      public static Type getType(String name) {
         return (Type)CODEC.byId(name, OAK);
      }

      // $FF: synthetic method
      private static Type[] method_36671() {
         return new Type[]{OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, CHERRY, DARK_OAK, MANGROVE, BAMBOO};
      }
   }

   public static enum Location {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;

      // $FF: synthetic method
      private static Location[] method_36670() {
         return new Location[]{IN_WATER, UNDER_WATER, UNDER_FLOWING_WATER, ON_LAND, IN_AIR};
      }
   }
}
