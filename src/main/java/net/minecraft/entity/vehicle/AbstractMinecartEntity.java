package net.minecraft.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecartEntity extends Entity {
   private static final TrackedData DAMAGE_WOBBLE_TICKS;
   private static final TrackedData DAMAGE_WOBBLE_SIDE;
   private static final TrackedData DAMAGE_WOBBLE_STRENGTH;
   private static final TrackedData CUSTOM_BLOCK_ID;
   private static final TrackedData CUSTOM_BLOCK_OFFSET;
   private static final TrackedData CUSTOM_BLOCK_PRESENT;
   private static final ImmutableMap DISMOUNT_FREE_Y_SPACES_NEEDED;
   protected static final float VELOCITY_SLOWDOWN_MULTIPLIER = 0.95F;
   private boolean yawFlipped;
   private static final Map ADJACENT_RAIL_POSITIONS_BY_SHAPE;
   private int clientInterpolationSteps;
   private double clientX;
   private double clientY;
   private double clientZ;
   private double clientYaw;
   private double clientPitch;
   private double clientXVelocity;
   private double clientYVelocity;
   private double clientZVelocity;

   protected AbstractMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.intersectionChecked = true;
   }

   protected AbstractMinecartEntity(EntityType type, World world, double x, double y, double z) {
      this(type, world);
      this.setPosition(x, y, z);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
   }

   public static AbstractMinecartEntity create(World world, double x, double y, double z, Type type) {
      if (type == AbstractMinecartEntity.Type.CHEST) {
         return new ChestMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.FURNACE) {
         return new FurnaceMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.TNT) {
         return new TntMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.SPAWNER) {
         return new SpawnerMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.HOPPER) {
         return new HopperMinecartEntity(world, x, y, z);
      } else {
         return (AbstractMinecartEntity)(type == AbstractMinecartEntity.Type.COMMAND_BLOCK ? new CommandBlockMinecartEntity(world, x, y, z) : new MinecartEntity(world, x, y, z));
      }
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.EVENTS;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);
      this.dataTracker.startTracking(CUSTOM_BLOCK_ID, Block.getRawIdFromState(Blocks.AIR.getDefaultState()));
      this.dataTracker.startTracking(CUSTOM_BLOCK_OFFSET, 6);
      this.dataTracker.startTracking(CUSTOM_BLOCK_PRESENT, false);
   }

   public boolean collidesWith(Entity other) {
      return BoatEntity.canCollide(this, other);
   }

   public boolean isPushable() {
      return true;
   }

   protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
      return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
   }

   public double getMountedHeightOffset() {
      return 0.0;
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Direction lv = this.getMovementDirection();
      if (lv.getAxis() == Direction.Axis.Y) {
         return super.updatePassengerForDismount(passenger);
      } else {
         int[][] is = Dismounting.getDismountOffsets(lv);
         BlockPos lv2 = this.getBlockPos();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();
         ImmutableList immutableList = passenger.getPoses();
         UnmodifiableIterator var7 = immutableList.iterator();

         while(var7.hasNext()) {
            EntityPose lv4 = (EntityPose)var7.next();
            EntityDimensions lv5 = passenger.getDimensions(lv4);
            float f = Math.min(lv5.width, 1.0F) / 2.0F;
            UnmodifiableIterator var11 = ((ImmutableList)DISMOUNT_FREE_Y_SPACES_NEEDED.get(lv4)).iterator();

            while(var11.hasNext()) {
               int i = (Integer)var11.next();
               int[][] var13 = is;
               int var14 = is.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  int[] js = var13[var15];
                  lv3.set(lv2.getX() + js[0], lv2.getY() + i, lv2.getZ() + js[1]);
                  double d = this.world.getDismountHeight(Dismounting.getCollisionShape(this.world, lv3), () -> {
                     return Dismounting.getCollisionShape(this.world, lv3.down());
                  });
                  if (Dismounting.canDismountInBlock(d)) {
                     Box lv6 = new Box((double)(-f), 0.0, (double)(-f), (double)f, (double)lv5.height, (double)f);
                     Vec3d lv7 = Vec3d.ofCenter(lv3, d);
                     if (Dismounting.canPlaceEntityAt(this.world, passenger, lv6.offset(lv7))) {
                        passenger.setPose(lv4);
                        return lv7;
                     }
                  }
               }
            }
         }

         double e = this.getBoundingBox().maxY;
         lv3.set((double)lv2.getX(), e, (double)lv2.getZ());
         UnmodifiableIterator var22 = immutableList.iterator();

         while(var22.hasNext()) {
            EntityPose lv8 = (EntityPose)var22.next();
            double g = (double)passenger.getDimensions(lv8).height;
            int j = MathHelper.ceil(e - (double)lv3.getY() + g);
            double h = Dismounting.getCeilingHeight(lv3, j, (pos) -> {
               return this.world.getBlockState(pos).getCollisionShape(this.world, pos);
            });
            if (e + g <= h) {
               passenger.setPose(lv8);
               break;
            }
         }

         return super.updatePassengerForDismount(passenger);
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.world.isClient && !this.isRemoved()) {
         if (this.isInvulnerableTo(source)) {
            return false;
         } else {
            this.setDamageWobbleSide(-this.getDamageWobbleSide());
            this.setDamageWobbleTicks(10);
            this.scheduleVelocityUpdate();
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            boolean bl = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode;
            if (bl || this.getDamageWobbleStrength() > 40.0F) {
               this.removeAllPassengers();
               if (bl && !this.hasCustomName()) {
                  this.discard();
               } else {
                  this.dropItems(source);
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   protected float getVelocityMultiplier() {
      BlockState lv = this.world.getBlockState(this.getBlockPos());
      return lv.isIn(BlockTags.RAILS) ? 1.0F : super.getVelocityMultiplier();
   }

   public void dropItems(DamageSource damageSource) {
      this.kill();
      if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
         ItemStack lv = new ItemStack(this.getItem());
         if (this.hasCustomName()) {
            lv.setCustomName(this.getCustomName());
         }

         this.dropStack(lv);
      }

   }

   abstract Item getItem();

   public void animateDamage(float yaw) {
      this.setDamageWobbleSide(-this.getDamageWobbleSide());
      this.setDamageWobbleTicks(10);
      this.setDamageWobbleStrength(this.getDamageWobbleStrength() + this.getDamageWobbleStrength() * 10.0F);
   }

   public boolean canHit() {
      return !this.isRemoved();
   }

   private static Pair getAdjacentRailPositionsByShape(RailShape shape) {
      return (Pair)ADJACENT_RAIL_POSITIONS_BY_SHAPE.get(shape);
   }

   public Direction getMovementDirection() {
      return this.yawFlipped ? this.getHorizontalFacing().getOpposite().rotateYClockwise() : this.getHorizontalFacing().rotateYClockwise();
   }

   public void tick() {
      if (this.getDamageWobbleTicks() > 0) {
         this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
      }

      if (this.getDamageWobbleStrength() > 0.0F) {
         this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
      }

      this.attemptTickInVoid();
      this.tickPortal();
      double d;
      if (this.world.isClient) {
         if (this.clientInterpolationSteps > 0) {
            d = this.getX() + (this.clientX - this.getX()) / (double)this.clientInterpolationSteps;
            double e = this.getY() + (this.clientY - this.getY()) / (double)this.clientInterpolationSteps;
            double f = this.getZ() + (this.clientZ - this.getZ()) / (double)this.clientInterpolationSteps;
            double g = MathHelper.wrapDegrees(this.clientYaw - (double)this.getYaw());
            this.setYaw(this.getYaw() + (float)g / (float)this.clientInterpolationSteps);
            this.setPitch(this.getPitch() + (float)(this.clientPitch - (double)this.getPitch()) / (float)this.clientInterpolationSteps);
            --this.clientInterpolationSteps;
            this.setPosition(d, e, f);
            this.setRotation(this.getYaw(), this.getPitch());
         } else {
            this.refreshPosition();
            this.setRotation(this.getYaw(), this.getPitch());
         }

      } else {
         if (!this.hasNoGravity()) {
            d = this.isTouchingWater() ? -0.005 : -0.04;
            this.setVelocity(this.getVelocity().add(0.0, d, 0.0));
         }

         int i = MathHelper.floor(this.getX());
         int j = MathHelper.floor(this.getY());
         int k = MathHelper.floor(this.getZ());
         if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
         }

         BlockPos lv = new BlockPos(i, j, k);
         BlockState lv2 = this.world.getBlockState(lv);
         if (AbstractRailBlock.isRail(lv2)) {
            this.moveOnRail(lv, lv2);
            if (lv2.isOf(Blocks.ACTIVATOR_RAIL)) {
               this.onActivatorRail(i, j, k, (Boolean)lv2.get(PoweredRailBlock.POWERED));
            }
         } else {
            this.moveOffRail();
         }

         this.checkBlockCollision();
         this.setPitch(0.0F);
         double h = this.prevX - this.getX();
         double l = this.prevZ - this.getZ();
         if (h * h + l * l > 0.001) {
            this.setYaw((float)(MathHelper.atan2(l, h) * 180.0 / Math.PI));
            if (this.yawFlipped) {
               this.setYaw(this.getYaw() + 180.0F);
            }
         }

         double m = (double)MathHelper.wrapDegrees(this.getYaw() - this.prevYaw);
         if (m < -170.0 || m >= 170.0) {
            this.setYaw(this.getYaw() + 180.0F);
            this.yawFlipped = !this.yawFlipped;
         }

         this.setRotation(this.getYaw(), this.getPitch());
         if (this.getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE && this.getVelocity().horizontalLengthSquared() > 0.01) {
            List list = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.20000000298023224, 0.0, 0.20000000298023224), EntityPredicates.canBePushedBy(this));
            if (!list.isEmpty()) {
               for(int n = 0; n < list.size(); ++n) {
                  Entity lv3 = (Entity)list.get(n);
                  if (!(lv3 instanceof PlayerEntity) && !(lv3 instanceof IronGolemEntity) && !(lv3 instanceof AbstractMinecartEntity) && !this.hasPassengers() && !lv3.hasVehicle()) {
                     lv3.startRiding(this);
                  } else {
                     lv3.pushAwayFrom(this);
                  }
               }
            }
         } else {
            Iterator var12 = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.20000000298023224, 0.0, 0.20000000298023224)).iterator();

            while(var12.hasNext()) {
               Entity lv4 = (Entity)var12.next();
               if (!this.hasPassenger(lv4) && lv4.isPushable() && lv4 instanceof AbstractMinecartEntity) {
                  lv4.pushAwayFrom(this);
               }
            }
         }

         this.updateWaterState();
         if (this.isInLava()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5F;
         }

         this.firstUpdate = false;
      }
   }

   protected double getMaxSpeed() {
      return (this.isTouchingWater() ? 4.0 : 8.0) / 20.0;
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
   }

   protected void moveOffRail() {
      double d = this.getMaxSpeed();
      Vec3d lv = this.getVelocity();
      this.setVelocity(MathHelper.clamp(lv.x, -d, d), lv.y, MathHelper.clamp(lv.z, -d, d));
      if (this.onGround) {
         this.setVelocity(this.getVelocity().multiply(0.5));
      }

      this.move(MovementType.SELF, this.getVelocity());
      if (!this.onGround) {
         this.setVelocity(this.getVelocity().multiply(0.95));
      }

   }

   protected void moveOnRail(BlockPos pos, BlockState state) {
      this.onLanding();
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      Vec3d lv = this.snapPositionToRail(d, e, f);
      e = (double)pos.getY();
      boolean bl = false;
      boolean bl2 = false;
      if (state.isOf(Blocks.POWERED_RAIL)) {
         bl = (Boolean)state.get(PoweredRailBlock.POWERED);
         bl2 = !bl;
      }

      double g = 0.0078125;
      if (this.isTouchingWater()) {
         g *= 0.2;
      }

      Vec3d lv2 = this.getVelocity();
      RailShape lv3 = (RailShape)state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty());
      switch (lv3) {
         case ASCENDING_EAST:
            this.setVelocity(lv2.add(-g, 0.0, 0.0));
            ++e;
            break;
         case ASCENDING_WEST:
            this.setVelocity(lv2.add(g, 0.0, 0.0));
            ++e;
            break;
         case ASCENDING_NORTH:
            this.setVelocity(lv2.add(0.0, 0.0, g));
            ++e;
            break;
         case ASCENDING_SOUTH:
            this.setVelocity(lv2.add(0.0, 0.0, -g));
            ++e;
      }

      lv2 = this.getVelocity();
      Pair pair = getAdjacentRailPositionsByShape(lv3);
      Vec3i lv4 = (Vec3i)pair.getFirst();
      Vec3i lv5 = (Vec3i)pair.getSecond();
      double h = (double)(lv5.getX() - lv4.getX());
      double i = (double)(lv5.getZ() - lv4.getZ());
      double j = Math.sqrt(h * h + i * i);
      double k = lv2.x * h + lv2.z * i;
      if (k < 0.0) {
         h = -h;
         i = -i;
      }

      double l = Math.min(2.0, lv2.horizontalLength());
      lv2 = new Vec3d(l * h / j, lv2.y, l * i / j);
      this.setVelocity(lv2);
      Entity lv6 = this.getFirstPassenger();
      if (lv6 instanceof PlayerEntity) {
         Vec3d lv7 = lv6.getVelocity();
         double m = lv7.horizontalLengthSquared();
         double n = this.getVelocity().horizontalLengthSquared();
         if (m > 1.0E-4 && n < 0.01) {
            this.setVelocity(this.getVelocity().add(lv7.x * 0.1, 0.0, lv7.z * 0.1));
            bl2 = false;
         }
      }

      double o;
      if (bl2) {
         o = this.getVelocity().horizontalLength();
         if (o < 0.03) {
            this.setVelocity(Vec3d.ZERO);
         } else {
            this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
         }
      }

      o = (double)pos.getX() + 0.5 + (double)lv4.getX() * 0.5;
      double p = (double)pos.getZ() + 0.5 + (double)lv4.getZ() * 0.5;
      double q = (double)pos.getX() + 0.5 + (double)lv5.getX() * 0.5;
      double r = (double)pos.getZ() + 0.5 + (double)lv5.getZ() * 0.5;
      h = q - o;
      i = r - p;
      double s;
      double t;
      double u;
      if (h == 0.0) {
         s = f - (double)pos.getZ();
      } else if (i == 0.0) {
         s = d - (double)pos.getX();
      } else {
         t = d - o;
         u = f - p;
         s = (t * h + u * i) * 2.0;
      }

      d = o + h * s;
      f = p + i * s;
      this.setPosition(d, e, f);
      t = this.hasPassengers() ? 0.75 : 1.0;
      u = this.getMaxSpeed();
      lv2 = this.getVelocity();
      this.move(MovementType.SELF, new Vec3d(MathHelper.clamp(t * lv2.x, -u, u), 0.0, MathHelper.clamp(t * lv2.z, -u, u)));
      if (lv4.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == lv4.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == lv4.getZ()) {
         this.setPosition(this.getX(), this.getY() + (double)lv4.getY(), this.getZ());
      } else if (lv5.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == lv5.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == lv5.getZ()) {
         this.setPosition(this.getX(), this.getY() + (double)lv5.getY(), this.getZ());
      }

      this.applySlowdown();
      Vec3d lv8 = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
      Vec3d lv9;
      double w;
      if (lv8 != null && lv != null) {
         double v = (lv.y - lv8.y) * 0.05;
         lv9 = this.getVelocity();
         w = lv9.horizontalLength();
         if (w > 0.0) {
            this.setVelocity(lv9.multiply((w + v) / w, 1.0, (w + v) / w));
         }

         this.setPosition(this.getX(), lv8.y, this.getZ());
      }

      int x = MathHelper.floor(this.getX());
      int y = MathHelper.floor(this.getZ());
      if (x != pos.getX() || y != pos.getZ()) {
         lv9 = this.getVelocity();
         w = lv9.horizontalLength();
         this.setVelocity(w * (double)(x - pos.getX()), lv9.y, w * (double)(y - pos.getZ()));
      }

      if (bl) {
         lv9 = this.getVelocity();
         w = lv9.horizontalLength();
         if (w > 0.01) {
            double z = 0.06;
            this.setVelocity(lv9.add(lv9.x / w * 0.06, 0.0, lv9.z / w * 0.06));
         } else {
            Vec3d lv10 = this.getVelocity();
            double aa = lv10.x;
            double ab = lv10.z;
            if (lv3 == RailShape.EAST_WEST) {
               if (this.willHitBlockAt(pos.west())) {
                  aa = 0.02;
               } else if (this.willHitBlockAt(pos.east())) {
                  aa = -0.02;
               }
            } else {
               if (lv3 != RailShape.NORTH_SOUTH) {
                  return;
               }

               if (this.willHitBlockAt(pos.north())) {
                  ab = 0.02;
               } else if (this.willHitBlockAt(pos.south())) {
                  ab = -0.02;
               }
            }

            this.setVelocity(aa, lv10.y, ab);
         }
      }

   }

   private boolean willHitBlockAt(BlockPos pos) {
      return this.world.getBlockState(pos).isSolidBlock(this.world, pos);
   }

   protected void applySlowdown() {
      double d = this.hasPassengers() ? 0.997 : 0.96;
      Vec3d lv = this.getVelocity();
      lv = lv.multiply(d, 0.0, d);
      if (this.isTouchingWater()) {
         lv = lv.multiply(0.949999988079071);
      }

      this.setVelocity(lv);
   }

   @Nullable
   public Vec3d snapPositionToRailWithOffset(double x, double y, double z, double offset) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y);
      int k = MathHelper.floor(z);
      if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
         --j;
      }

      BlockState lv = this.world.getBlockState(new BlockPos(i, j, k));
      if (AbstractRailBlock.isRail(lv)) {
         RailShape lv2 = (RailShape)lv.get(((AbstractRailBlock)lv.getBlock()).getShapeProperty());
         y = (double)j;
         if (lv2.isAscending()) {
            y = (double)(j + 1);
         }

         Pair pair = getAdjacentRailPositionsByShape(lv2);
         Vec3i lv3 = (Vec3i)pair.getFirst();
         Vec3i lv4 = (Vec3i)pair.getSecond();
         double h = (double)(lv4.getX() - lv3.getX());
         double l = (double)(lv4.getZ() - lv3.getZ());
         double m = Math.sqrt(h * h + l * l);
         h /= m;
         l /= m;
         x += h * offset;
         z += l * offset;
         if (lv3.getY() != 0 && MathHelper.floor(x) - i == lv3.getX() && MathHelper.floor(z) - k == lv3.getZ()) {
            y += (double)lv3.getY();
         } else if (lv4.getY() != 0 && MathHelper.floor(x) - i == lv4.getX() && MathHelper.floor(z) - k == lv4.getZ()) {
            y += (double)lv4.getY();
         }

         return this.snapPositionToRail(x, y, z);
      } else {
         return null;
      }
   }

   @Nullable
   public Vec3d snapPositionToRail(double x, double y, double z) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y);
      int k = MathHelper.floor(z);
      if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
         --j;
      }

      BlockState lv = this.world.getBlockState(new BlockPos(i, j, k));
      if (AbstractRailBlock.isRail(lv)) {
         RailShape lv2 = (RailShape)lv.get(((AbstractRailBlock)lv.getBlock()).getShapeProperty());
         Pair pair = getAdjacentRailPositionsByShape(lv2);
         Vec3i lv3 = (Vec3i)pair.getFirst();
         Vec3i lv4 = (Vec3i)pair.getSecond();
         double g = (double)i + 0.5 + (double)lv3.getX() * 0.5;
         double h = (double)j + 0.0625 + (double)lv3.getY() * 0.5;
         double l = (double)k + 0.5 + (double)lv3.getZ() * 0.5;
         double m = (double)i + 0.5 + (double)lv4.getX() * 0.5;
         double n = (double)j + 0.0625 + (double)lv4.getY() * 0.5;
         double o = (double)k + 0.5 + (double)lv4.getZ() * 0.5;
         double p = m - g;
         double q = (n - h) * 2.0;
         double r = o - l;
         double s;
         if (p == 0.0) {
            s = z - (double)k;
         } else if (r == 0.0) {
            s = x - (double)i;
         } else {
            double t = x - g;
            double u = z - l;
            s = (t * p + u * r) * 2.0;
         }

         x = g + p * s;
         y = h + q * s;
         z = l + r * s;
         if (q < 0.0) {
            ++y;
         } else if (q > 0.0) {
            y += 0.5;
         }

         return new Vec3d(x, y, z);
      } else {
         return null;
      }
   }

   public Box getVisibilityBoundingBox() {
      Box lv = this.getBoundingBox();
      return this.hasCustomBlock() ? lv.expand((double)Math.abs(this.getBlockOffset()) / 16.0) : lv;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.getBoolean("CustomDisplayTile")) {
         this.setCustomBlock(NbtHelper.toBlockState(this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("DisplayState")));
         this.setCustomBlockOffset(nbt.getInt("DisplayOffset"));
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      if (this.hasCustomBlock()) {
         nbt.putBoolean("CustomDisplayTile", true);
         nbt.put("DisplayState", NbtHelper.fromBlockState(this.getContainedBlock()));
         nbt.putInt("DisplayOffset", this.getBlockOffset());
      }

   }

   public void pushAwayFrom(Entity entity) {
      if (!this.world.isClient) {
         if (!entity.noClip && !this.noClip) {
            if (!this.hasPassenger(entity)) {
               double d = entity.getX() - this.getX();
               double e = entity.getZ() - this.getZ();
               double f = d * d + e * e;
               if (f >= 9.999999747378752E-5) {
                  f = Math.sqrt(f);
                  d /= f;
                  e /= f;
                  double g = 1.0 / f;
                  if (g > 1.0) {
                     g = 1.0;
                  }

                  d *= g;
                  e *= g;
                  d *= 0.10000000149011612;
                  e *= 0.10000000149011612;
                  d *= 0.5;
                  e *= 0.5;
                  if (entity instanceof AbstractMinecartEntity) {
                     double h = entity.getX() - this.getX();
                     double i = entity.getZ() - this.getZ();
                     Vec3d lv = (new Vec3d(h, 0.0, i)).normalize();
                     Vec3d lv2 = (new Vec3d((double)MathHelper.cos(this.getYaw() * 0.017453292F), 0.0, (double)MathHelper.sin(this.getYaw() * 0.017453292F))).normalize();
                     double j = Math.abs(lv.dotProduct(lv2));
                     if (j < 0.800000011920929) {
                        return;
                     }

                     Vec3d lv3 = this.getVelocity();
                     Vec3d lv4 = entity.getVelocity();
                     if (((AbstractMinecartEntity)entity).getMinecartType() == AbstractMinecartEntity.Type.FURNACE && this.getMinecartType() != AbstractMinecartEntity.Type.FURNACE) {
                        this.setVelocity(lv3.multiply(0.2, 1.0, 0.2));
                        this.addVelocity(lv4.x - d, 0.0, lv4.z - e);
                        entity.setVelocity(lv4.multiply(0.95, 1.0, 0.95));
                     } else if (((AbstractMinecartEntity)entity).getMinecartType() != AbstractMinecartEntity.Type.FURNACE && this.getMinecartType() == AbstractMinecartEntity.Type.FURNACE) {
                        entity.setVelocity(lv4.multiply(0.2, 1.0, 0.2));
                        entity.addVelocity(lv3.x + d, 0.0, lv3.z + e);
                        this.setVelocity(lv3.multiply(0.95, 1.0, 0.95));
                     } else {
                        double k = (lv4.x + lv3.x) / 2.0;
                        double l = (lv4.z + lv3.z) / 2.0;
                        this.setVelocity(lv3.multiply(0.2, 1.0, 0.2));
                        this.addVelocity(k - d, 0.0, l - e);
                        entity.setVelocity(lv4.multiply(0.2, 1.0, 0.2));
                        entity.addVelocity(k + d, 0.0, l + e);
                     }
                  } else {
                     this.addVelocity(-d, 0.0, -e);
                     entity.addVelocity(d / 4.0, 0.0, e / 4.0);
                  }
               }

            }
         }
      }
   }

   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.clientX = x;
      this.clientY = y;
      this.clientZ = z;
      this.clientYaw = (double)yaw;
      this.clientPitch = (double)pitch;
      this.clientInterpolationSteps = interpolationSteps + 2;
      this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
   }

   public void setVelocityClient(double x, double y, double z) {
      this.clientXVelocity = x;
      this.clientYVelocity = y;
      this.clientZVelocity = z;
      this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
   }

   public void setDamageWobbleStrength(float damageWobbleStrength) {
      this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, damageWobbleStrength);
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

   public void setDamageWobbleSide(int wobbleSide) {
      this.dataTracker.set(DAMAGE_WOBBLE_SIDE, wobbleSide);
   }

   public int getDamageWobbleSide() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
   }

   public abstract Type getMinecartType();

   public BlockState getContainedBlock() {
      return !this.hasCustomBlock() ? this.getDefaultContainedBlock() : Block.getStateFromRawId((Integer)this.getDataTracker().get(CUSTOM_BLOCK_ID));
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.AIR.getDefaultState();
   }

   public int getBlockOffset() {
      return !this.hasCustomBlock() ? this.getDefaultBlockOffset() : (Integer)this.getDataTracker().get(CUSTOM_BLOCK_OFFSET);
   }

   public int getDefaultBlockOffset() {
      return 6;
   }

   public void setCustomBlock(BlockState state) {
      this.getDataTracker().set(CUSTOM_BLOCK_ID, Block.getRawIdFromState(state));
      this.setCustomBlockPresent(true);
   }

   public void setCustomBlockOffset(int offset) {
      this.getDataTracker().set(CUSTOM_BLOCK_OFFSET, offset);
      this.setCustomBlockPresent(true);
   }

   public boolean hasCustomBlock() {
      return (Boolean)this.getDataTracker().get(CUSTOM_BLOCK_PRESENT);
   }

   public void setCustomBlockPresent(boolean present) {
      this.getDataTracker().set(CUSTOM_BLOCK_PRESENT, present);
   }

   public ItemStack getPickBlockStack() {
      Item lv;
      switch (this.getMinecartType()) {
         case FURNACE:
            lv = Items.FURNACE_MINECART;
            break;
         case CHEST:
            lv = Items.CHEST_MINECART;
            break;
         case TNT:
            lv = Items.TNT_MINECART;
            break;
         case HOPPER:
            lv = Items.HOPPER_MINECART;
            break;
         case COMMAND_BLOCK:
            lv = Items.COMMAND_BLOCK_MINECART;
            break;
         default:
            lv = Items.MINECART;
      }

      return new ItemStack(lv);
   }

   static {
      DAMAGE_WOBBLE_TICKS = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_SIDE = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.FLOAT);
      CUSTOM_BLOCK_ID = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_BLOCK_OFFSET = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_BLOCK_PRESENT = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      DISMOUNT_FREE_Y_SPACES_NEEDED = ImmutableMap.of(EntityPose.STANDING, ImmutableList.of(0, 1, -1), EntityPose.CROUCHING, ImmutableList.of(0, 1, -1), EntityPose.SWIMMING, ImmutableList.of(0, 1));
      ADJACENT_RAIL_POSITIONS_BY_SHAPE = (Map)Util.make(Maps.newEnumMap(RailShape.class), (map) -> {
         Vec3i lv = Direction.WEST.getVector();
         Vec3i lv2 = Direction.EAST.getVector();
         Vec3i lv3 = Direction.NORTH.getVector();
         Vec3i lv4 = Direction.SOUTH.getVector();
         Vec3i lv5 = lv.down();
         Vec3i lv6 = lv2.down();
         Vec3i lv7 = lv3.down();
         Vec3i lv8 = lv4.down();
         map.put(RailShape.NORTH_SOUTH, Pair.of(lv3, lv4));
         map.put(RailShape.EAST_WEST, Pair.of(lv, lv2));
         map.put(RailShape.ASCENDING_EAST, Pair.of(lv5, lv2));
         map.put(RailShape.ASCENDING_WEST, Pair.of(lv, lv6));
         map.put(RailShape.ASCENDING_NORTH, Pair.of(lv3, lv8));
         map.put(RailShape.ASCENDING_SOUTH, Pair.of(lv7, lv4));
         map.put(RailShape.SOUTH_EAST, Pair.of(lv4, lv2));
         map.put(RailShape.SOUTH_WEST, Pair.of(lv4, lv));
         map.put(RailShape.NORTH_WEST, Pair.of(lv3, lv));
         map.put(RailShape.NORTH_EAST, Pair.of(lv3, lv2));
      });
   }

   public static enum Type {
      RIDEABLE,
      CHEST,
      FURNACE,
      TNT,
      SPAWNER,
      HOPPER,
      COMMAND_BLOCK;

      // $FF: synthetic method
      private static Type[] method_36669() {
         return new Type[]{RIDEABLE, CHEST, FURNACE, TNT, SPAWNER, HOPPER, COMMAND_BLOCK};
      }
   }
}
